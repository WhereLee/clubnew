# 社团全流程管理系统（Club Flow）

> 面向高校的社团全流程管理系统，覆盖社团从申请创建到注销的完整生命周期。
> 技术栈：Java 17 + Spring Boot 3.5 + MyBatis-Plus + MySQL 8 + Redis 7（Stream 事件管道）+ Vue 3 + Vite。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 + Spring Boot 3.5.16 + Spring Security + JWT（jjwt 0.13） |
| ORM / 迁移 | MyBatis-Plus 3.5.12 + Flyway 11（测试与生产同源迁移） |
| 缓存 / 队列 | Redis 7：缓存三件套、分布式锁（Redisson 3.52）、限流 Lua、防重切面、**Stream 事件管道** |
| 数据库 | MySQL 8.0（生产）/ H2（测试，MODE=MySQL） |
| 可观测性 | Spring Boot Actuator（health/info/metrics） |
| 前端 | Vue 3 + TypeScript + Element Plus + Vite + Pinia + Vue Router |
| 测试 | JUnit 5 + Mockito + H2（56 个测试全绿） |
| 部署 | Docker Compose（MySQL/Redis/backend/双前端 + nginx 反代）+ GitHub Actions CI |

## 目录结构

```
club-flow/
├── backend/                # Spring Boot 后端（8081，context-path /api）
│   ├── src/main/java/com/club/
│   │   ├── annotation/     # @RepeatSubmit @RateLimiter @DataScope @Log
│   │   ├── aspect/         # 四个切面（限流 Lua 原子、防重 fail-open、数据权限、操作日志）
│   │   ├── controller/     # 18 个 REST Controller
│   │   ├── domain/ dto/ vo/ enums/ mapper/ service/
│   │   ├── event/          # Redis Stream 事件管道（ClubEventPublisher + RankEventConsumer）
│   │   ├── security/       # JWT 认证（Redis 会话 + 故障降级）
│   │   └── config/ common/ handler/
│   ├── src/main/resources/
│   │   ├── db/migration/   # Flyway：V1 基线 + V2 索引/CHECK
│   │   ├── application.yml / application-dev.yml / application-prod.yml
│   │   └── sql/club_flow.sql  # ⚠ 已废弃存档（schema 由 Flyway 管理）
│   ├── src/test/           # 56 个测试（含并发防超卖、幂等、数据权限、事件降级）
│   └── Dockerfile
├── admin-web/              # 管理端（5173）
├── user-web/               # 用户端（5174）
├── docker-compose.yml      # 云服务器一键部署
└── .github/workflows/ci.yml
```

## 快速启动（本机开发）

前置：JDK 17+、Maven 3.8+、Node 18+、MySQL 8、Redis 7（本机运行中）。

```bash
# 1) 初始化数据库（只需建空库，Flyway 启动时自动建表 + 种子数据）
mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS club_flow DEFAULT CHARACTER SET utf8mb4;"

# 2) 后端（8081）
cd backend && mvn spring-boot:run
# 验证: curl http://localhost:8081/api/health → {"code":200}

# 3) 管理端（5173）与用户端（5174）
cd admin-web && npm install && npm run dev
cd user-web && npm install && npm run dev
```

默认账号：**admin / admin123**（BCrypt 种子数据内置）。

## 云服务器部署（Docker Compose）

```bash
# 1) 设置强随机密钥（生产禁止默认值）
export JWT_SECRET="$(openssl rand -base64 48)"
export MYSQL_PASSWORD="$(openssl rand -base64 24)"

# 2) 一键拉起（backend 自动执行 Flyway 迁移）
docker compose up -d --build

# 3) 验证
curl http://<服务器IP>:8081/api/actuator/health   # {"status":"UP"}
# admin-web: http://<IP>:5173   user-web: http://<IP>:5174
```

生产 profile 说明：`SPRING_PROFILES_ACTIVE=prod` 下所有敏感配置必须由环境变量注入（见 `application-prod.yml`），SQL 日志关闭、Swagger 关闭、Hikari 调优、日志落盘。

### 安全部署须知（终审遗留项）

1. **后端不可绕过 nginx 直接对公网暴露**：限流按 IP 分片依赖 nginx 覆盖写入的 `X-Real-IP`（compose 已绑回环 + nginx 反代，请勿私自改回 0.0.0.0 直连）。
2. **refresh token 跨设备共用**：同一 refresh token 在 5 秒宽限期后由第二个设备使用，会被判定为疑似泄露并吊销该用户全部会话——这是设计行为（轮换检测），属于正常安全语义。
3. **refresh token 存储于 localStorage**：XSS 风险由 CSP（`script-src 'self'`）缓解；如需更强防护可改造为 HttpOnly cookie（需配套 CSRF 防护），属可选增强。

## 运行测试

```bash
cd backend && mvn test
# 56 个测试：并发防超卖（100 并发抢 10 名额恰好 10 人成功）、重复签到幂等、
# 状态机非法流转、数据权限（data_scope 1~5）、换届分布式锁、事件发布器降级等。
# 测试库 H2 与生产 MySQL 共用 Flyway 迁移脚本，schema 完全同源。
```

## 架构亮点（面试可深挖）

1. **防超卖三层防线**：Redis Lua 预扣库存（EXISTS→DECR→超限回滚）→ DB 原子 UPDATE（`WHERE applied_count < quota`）→ 唯一约束兜底；Redis 故障自动降级 DB 兜底。
2. **Redis Stream 事件管道**：六类互动事件（发动态/评论/点赞/报名/签到/纳新报名）经 Stream 异步消费加分；消费者组 + ACK + PENDING 重投 + eventId 幂等去重；发布失败降级为同步加分，排行榜数据不丢。
3. **有状态 JWT + 故障降级**：token 内嵌身份与权限快照，Redis 会话支持主动踢人；Redis 不可用时自动降级为无状态认证，登录与鉴权均不中断。
4. **数据权限**：`@DataScope` 切面按角色 `data_scope`（1 全部/2 本社团及以下/3 本社团/4 仅本人/5 自定义）生成过滤片段，多角色取最严，ThreadLocal 用后即清。
5. **缓存三件套**：穿透（空值缓存短 TTL）、击穿（SETNX 互斥）、雪崩（随机 TTL）；写操作在 Service 层自动失效，Controller 不感知缓存。
6. **换届分布式锁**：Redisson `RLock` tryLock + 锁内事务 + finally 释放。
7. **工程化底座**：Flyway 同源迁移（测试/生产 schema 一致）、dev/prod 环境分离、actuator 可观测性、Docker Compose 一键部署、GitHub Actions CI。

## 默认账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员（全部菜单权限） |
