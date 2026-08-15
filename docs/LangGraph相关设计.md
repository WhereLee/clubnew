# LangGraph 相关设计（Agent 编排与人机边界）

> 状态：**设计讨论记录（未实现）** —— 记录 2026-08 讨论结论与演进方向
> 关联：《技术管理员设计.md》（L4 prompt 闭环与此文档的"内循环人审"衔接）

---

## 1. 背景与结论摘要

### 1.1 讨论起因

在 agent 开发过程中确认一个核心痛点：**人机边界**。agent 无法设计得完美，数据处理经常需要人工审查——项目里管理员层 agent 的产出用词一直是"建议"（AI 只出建议、人审生效），这正是人机边界直觉的体现。LangGraph 的 interrupt + checkpoint 机制恰好是这个命题的标杆解法。

### 1.2 用户决策方向（记录）

- **不排斥混合编程**：此前所有 agent 相关项目均非纯 Java（有 Python 版、有 Python+Java 混合版）
- **倾向"自研 + 成熟产品"双轨**：在自研轻量状态机的同时，也愿意尝试成熟的 LangGraph（含 Python 侧）
- 本项目的 Java 主体地位不变，但**不排除 agent 编排层引入 Python/LangGraph 的混合形态**（具体分配后续再议）

---

## 2. 调研事实（2026-08 实测）

| 项 | 结论 |
|---|---|
| LangGraph 官方 | **仅 Python / JS，无官方 Java 版** |
| langgraph4j（社区移植） | 最新 `1.9.0-beta2`（2026-08-09 仍在发版，活跃），但版本带 beta、个人维护（bsorrentino），无官方背书 |
| Spring AI | 最新 `2.0.0`（2026-06-12），模块清单无图编排/StateGraph/多代理模块；编排能力 = ChatClient + tool calling + advisors + chat-memory + MCP |
| 结论 | Java 生态图编排**未补齐**，只有社区替代品 |

---

## 3. LangGraph vs LangChain（本质区别）

**不是竞品，是"积木"和"施工图"的关系**：LangChain 是组件库（LLM 封装、Prompt、RAG、Memory、Tool），LangGraph 是把组件组织成可执行图的编排引擎。LangChain 官方已把 LangGraph 作为构建 agent 的默认选择。

| 维度 | LangChain | LangGraph |
|---|---|---|
| 数据流模型 | 管道 Pipeline/DAG，固定单向 | 图 StateGraph：节点+边+条件边+**循环** |
| 状态 | 无全局状态 | 共享 State 对象，节点间读写 |
| 循环 | ❌ 不支持 | ✅ 原生（agent 循环 = 标准模式） |
| 人机交互 | ❌ 无概念 | ✅ interrupt 暂停 + checkpoint 恢复 |
| 可恢复性 | ❌ 中断重来 | ✅ 断点续跑、时间旅行 |

### 3.1 LangGraph 四大核心能力

1. **StateGraph**：节点 = 函数/agent；边 = 流转；条件边 = 按状态分支
2. **循环**：agent 本质（思考→调工具→看结果→再思考）是循环，图原生支持
3. **Checkpoint**：每节点保存完整状态 → 中断恢复、回放、插入人工审批
4. **多代理模式**：supervisor / 层级 / 并行 fan-out/fan-in

---

## 4. 使用判断标准

### 建议用（满足其一）

1. 流程有**分支和回退**（异常重试、阈值升级）
2. 需要**人插一脚**（审批/确认/修正 —— interrupt + checkpoint）
3. **多代理分工**（主管派活给子代理）
4. 流程**频繁演化**（改图比改 if/else 安全）
5. **长时间任务可恢复**（中断不从头来）

### 不要用

1. 单轮问答 + 简单工具调用（本项目三端 agent 现状）
2. 固定线性流水线（纯 RAG 问答链）
3. 无状态流转需求的一次性任务

### 具体例子

- **售后客服**：查单 → 条件边（金额>阈值 → 人工审批 checkpoint 暂停）→ 恢复执行
- **小说内容审核**（旧项目场景）：并行违禁检测 + 质量评估（fan-out）→ 汇聚（fan-in）→ 裁决条件边（双过自动发布 / 严重违规下架 / 边界走人工）
- **技术管理员 L4 闭环**（未来）：统计 → LLM 起草 → checkpoint（技术管理员审批）→ 生效 → **观察一周后评估，效果差自动回滚**（延迟回访 = 图 + checkpoint 的看家本领）

---

## 5. 人机边界（Human-in-the-Loop）——核心设计命题

### 5.1 两种形态

**外循环人审（项目现状）**

```
用户提问 → agent 循环 → 输出"建议" →【循环结束】
                                        ↓
              人在系统里自行操作（审批/改数据）→ 系统完成
```

- 现状：`ApprovalAssistTool` 即外循环——AI 生成建议文本，人读完建议后自己到系统点审批
- 优点：简单、安全（写操作永不经过 AI）
- 代价：人要做"翻译"——把 AI 建议翻译成系统操作

**内循环人审（LangGraph interrupt 形态）**

```
用户提问 → agent 循环：LLM → 生成待审事项（暂停！）
                                    ↓ checkpoint 挂起（可挂几天）
                    人工审查：同意/修改/驳回
                                    ↓ 恢复
                agent 拿人工裁决继续执行 → 完成
```

- 人直接在 agent 流程内部参与：流程知道自己"在等谁点头"，状态不丢，审完自动往下走
- **"AI 出建议、人审生效"从口头铁律变成流程机制**

### 5.2 落点判断

- **只读查询**永远保持外循环（不需要人介入）
- **写操作**（审批、发布、状态变更）是内循环的候选：AI 起草 → 人拍板 → 机器执行
- 项目内的候选场景：
  - 业务端：审批辅助升级（社长说"处理李四的入社申请" → agent 生成待审事项 → 社长在 AI 面板批准/驳回 → agent 调用真实审批接口）
  - 技术端：prompt 版本生效（LLM 起草 → 技术管理员审 → 激活 → 观察回滚）

---

## 6. Java 侧落地映射（自研轻量版）

### 6.1 图模式四要素（Java 自研本质）

```
节点    = 函数/方法
边      = 下一个节点的指针
条件边  = 根据 State 决定下一节点的函数
Checkpoint = 每节点执行后把 State 快照存 DB
```

### 6.2 内循环人审的最小落地（agent_approval 表）

```sql
CREATE TABLE agent_approval (
    id BIGINT PRIMARY KEY,
    trace_id VARCHAR(64),              -- 链路追踪
    agent_key VARCHAR(64),             -- 哪个 agent 发起的
    approval_type VARCHAR(32),         -- 事项类型（入社审批/经费审批/prompt 版本…）
    payload_json TEXT,                 -- 待审数据
    ai_suggestion TEXT,                -- AI 起草的建议（含理由/风险提示）
    status VARCHAR(16),                -- PENDING / APPROVED / REJECTED
    approver_id BIGINT,                -- 审批人
    approval_comment VARCHAR(512),
    create_time DATETIME, update_time DATETIME, deleted TINYINT
);
```

agent 循环改造：

```
LLM 想执行写操作 → 不直接执行 → 生成待审事项落库 → 循环暂停（返回"等待审批"）
人审批 → 状态更新 → 轮询/推送恢复 → agent 继续循环
```

工作量评估：300~500 行 Java，且面试叙事更硬——
**"我理解 human-in-the-loop 的本质是 interrupt + checkpoint + 状态恢复，我在项目里实现了它的简化形态"**。

### 6.3 双轨决策路径（自研 vs langgraph4j vs Python LangGraph）

```
出现多代理/复杂人机流程真实需求？
├── 否 → 维持现状（单层 agent + 工具调用）
└── 是 → 流程复杂度评估
         ├── 简单内循环人审 → 自研 agent_approval（Java，300~500 行）
         ├── 复杂图编排（多步串联审批/条件分支/超时升级）→ 二选一：
         │     ├── langgraph4j（Java 侧，beta 评估维护投入）
         │     └── Python LangGraph 微服务（混合开发，用语言优势）
         └── 混合形态 → 按"各语言优势"分配：
               Java 保持业务主体/事务/安全边界；Python 承担编排/图执行/评估闭环
```

---

## 7. 演进路线（全部待定，按真实需求触发）

| 阶段 | 内容 | 触发条件 |
|---|---|---|
| A | 外循环人审维持现状（建议文本） | 现状 |
| B | 内循环最小落地：agent_approval 表 + 审批辅助升级 | 用户拍板第一个升级场景 |
| C | L4 prompt 闭环内循环化（衔接《技术管理员设计.md》） | 技术管理员开发期 |
| D | 图编排评估（langgraph4j vs Python LangGraph） | 出现多代理/复杂流程需求 |

**原则**：需求先于技术。任何图编排引入都必须在"人机边界"或"多代理分工"上产生真实收益，而不是为了用 LangGraph 而用。
