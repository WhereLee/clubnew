# AI Agent 模块说明（三端赋能架构）

> 面向面试与接手的架构文档。设计目标一句话：**AI 赋能是业务流的嵌入，不是端点的装饰——一个底座，三端工具。**

## 一、总体架构

```
三端（用户端/业务管理端/技术管理端）
      │  各自挂载不同权限档的工具
      ▼
AgentService（Spring AI ChatClient 编排）
      ├─ ChatModel（mimo-v2.5，OpenAI 协议；无 key 自动 Mock）
      ├─ AgentToolRegistry（工具注册表，按权限过滤注入 system prompt）
      └─ SSE 流式输出（text/done 事件协议）
      ▼
工具层（AbstractAgentTool 执行链：权限二道防线 → 业务 → 轨迹落库 → 异常兜底）
      ▼
数据层（agent_session / agent_message / agent_tool_log）+ 业务表（受限 NL2SQL）
```

核心决策：
1. **一个 ChatClient 底座 + 三端各挂工具**，而非三个聊天机器人——工具即能力，权限即边界。
2. **工具权限三档**：ALL（学生可见，如知识库）/ CLUB_ADMIN（社长，如 NL2SQL）/ ADMIN（管理员，如运维分析）。编排层过滤 + 工具内二道校验，两道防线。
3. **一切写操作只出建议**（人审闭环）：NL2SQL 只读、审批只摘要、文案标「草稿」。
4. **可解释性**：每次工具调用落 `agent_tool_log`（谁/何时/调什么/耗时/成败），assistant 消息存 `tool_calls` JSON 轨迹。
5. **模型可切换**：`MIMO_API_KEY` 环境变量——无 key 走 MockChatModel（CI 全链路可跑），有 key 走 mimo-v2.5，切换零代码。

## 二、三端工具清单

| 端 | 工具 | 权限 | 说明 |
|---|---|---|---|
| 用户端 | `search_platform_knowledge` | ALL | RAG 语义检索平台规则（入社/报名/签到/经费/创建/行为规范） |
| 业务端 | `query_business_data` | CLUB_ADMIN | 受限 NL2SQL（单表 SELECT） |
| 业务端 | `pending_approval_summary` | CLUB_ADMIN | 待审批摘要（管理员全平台/社长本社团） |
| 技术端 | `analyze_today_errors` | ADMIN | 今日操作失败/登录失败 TOP 聚合 |
| 技术端 | `data_health_check` | ADMIN | 数据一致性体检（计数对账/孤儿引用/社长完整性） |
| 技术端 | `system_health` | ADMIN | Redis 连通/登录趋势/限流拦截统计 |

## 三、NL2SQL 安全模型（六轮安全审查收敛）

正则对 SQL 词法必然存在绕过——最终采用 **jsqlparser AST 级防护**（项目已有 mybatis-plus-jsqlparser 依赖，零新增）：

1. **预筛**：仅拦多语句/注释（jsqlparser 对分号后内容静默截断，必须预筛）；
2. **解析层**：语法错误/非 PlainSelect（UNION 等）直接拒绝，注释被解析器丢弃；
3. **结构层**：单表无别名、无 JOIN/逗号多表/CTE/FOR UPDATE/INTO/跨库表；子查询全树检测（visit(Select) 拦截——5.1 的 ParenthesedSelect.accept 分发特性）；WHERE/HAVING/ORDER BY/GROUP BY/SELECT 全子句**递归白名单默认拒绝**（只放行白名单列/字面量/白名单函数 COUNT/SUM/AVG/MAX/MIN（参数递归）/算术比较逻辑操作符）；
4. **数据权限层**：非管理员注入 scope 过滤——受控字符串重建（jsqlparser 5.1 Parenthesis AST 有 toString Index bug）+ **注入后语义自校验**（顶层 AND 结构、右支 scope 谓词逐字段比对、左支等于原 WHERE 规范化文本）+ validateStructure 重跑，三重校验封死字面量欺骗构造攻击；
5. **资源层**：LIMIT AST 强制 ≤100、PreparedStatement 3 秒超时、执行错误只回传通用文案（细节入日志）。

六轮安全审查发现的真实攻击路径（面试素材）：
- TOCTOU、# 注释吞过滤、ORDER BY 后注入失效、WHERE 子查询布尔盲注侧信道
- 函数参数列逃逸（MAX(password)）、CASE WHEN 布尔 oracle、-SLEEP(2) 正负号绕过函数白名单
- CTE 内任意子查询、SELECT 字面量伪造 WHERE 前缀欺骗字符串注入定位
- DataScope 注入 AND 优先级翻转（OR 恒真越权）

## 四、RAG 设计

- **Embedding**：bge-base-zh-v1.5 本地 ONNX（`BGE_MODEL_URI`/`BGE_TOKENIZER_URI` 注入，Spring AI TransformersEmbeddingModel + last_hidden_state 输出）——数据不出内网、零 token 成本；未配置时 DisabledVectorStore 降级「未启用」。
- **存储**：SimpleVectorStore JSON 文件持久化（知识库 <1000 条）；VectorStore 抽象可切 pgvector（生产多实例），业务零改动。
- **切块**：段落聚合 500 字 + 100 重叠（复用旧 RAG 项目验证参数）；6 篇平台规则文档随 Flyway 外的 classpath 资源发布，启动时幂等入库。
- **检索**：topK=4 + 相似度阈值 0.35，回答强制基于片段并标注出处。
- **优化点（已知取舍）**：BGE 官方建议 CLS 池化（当前 mean）+ query 侧指令前缀，召回质量仍有提升空间；bge-reranker-v2-m3（2.3GB）二期接入。

## 五、SSE 协议

```
POST /agent/chat          text/event-stream
  event:text  data=增量文本
  event:done  data={"sessionId":..,"messageId":..}
GET  /agent/sessions      会话列表（当前用户）
GET  /agent/sessions/{id}/messages
DELETE /agent/sessions/{id}
```

前端 AiDrawer（admin 侧边栏入口 + user 悬浮球）：fetch + ReadableStream 手写 SSE 解析，流式光标/会话侧轨/深夜星空视觉。

## 六、面试话术

1. **架构**：「AI 能力不做一个孤立的聊天机器人，而是一个 ChatClient 编排底座 + 按 RBAC 过滤的工具注册表，三端各挂一组工具——工具即能力、权限即边界。」
2. **NL2SQL**：「大模型生成 SQL 最危险的是注入与越权。我做了六层防护：语句预筛、AST 结构校验、递归白名单默认拒绝、数据权限注入 + 语义自校验、LIMIT 与超时、错误不回显。而且这个工具经历过六轮安全审查，每轮都发现了真实的攻击路径——比如 WHERE 子查询布尔盲注、MAX(password) 函数参数逃逸、CTE 绕过白名单，全部修复并有回归测试。」
3. **RAG**：「embedding 本地化（数据不出内网 + 零 token 成本），知识库切块参数来自我之前项目验证过的段落聚合策略；向量库用接口抽象，现在文件存储，未来切 pgvector 一行配置。」
4. **工程化**：「无 key 环境 Mock 模型全链路可跑（CI 不依赖外网）；模型切换只改环境变量；每次工具调用有审计轨迹；流式输出 SSE 手写解析。」
