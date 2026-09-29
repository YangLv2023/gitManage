---
name: handday-git-branch
description: 通过 gitManage 审计系统发起 Git 分支合并申请与新开分支申请，并跟踪审核状态。当用户要求"发起合并/合并申请/合并到 pre/把某分支合过去/发起代码合并"，"新开分支/拉个分支/从 uat 或 master 检出新分支"，"查合并状态/查分支申请状态/看待审列表/查审计记录"，或要求"初始化 handday-git-branch / 安装 git 审计 MCP / 检查 gitManage MCP 环境"时使用。自带 MCP server（本 skill 的 mcp/ 子目录），真实合并与检出由人工在审核页执行，本 skill 只发起申请不执行。
---

# Handday Git Branch（gitManage 合并与新开分支申请）

## 概述

本 skill 自带一个 Node 版 MCP server（位于本 skill 目录下 `mcp/`），对接 gitManage Spring Boot 后端（默认 `http://192.168.11.70:5555`），暴露 3 个工具：

| 工具 | 用途 |
|---|---|
| `submit_merge_request` | 发起合并申请（sourceBranch → targetBranch） |
| `submit_new_branch_request` | 新开分支申请（基于 sourceBranch 检出 newBranch） |
| `list_audit_records` | 分页查询审计记录、跟踪审核状态 |

安全模型：申请提交后由企业微信机器人通知审核人，**真实合并/检出由人工在审核页输入服务密码执行**。本 skill 只发起与查询，无任何执行能力。

## 初始化向导（首次使用必须执行）

判断是否已初始化：当前 agent 的 MCP 配置中已存在 `command: node` 且 `args` 指向本 skill `mcp/index.js` 的 `gitmanage` 条目，且 `node --version` 正常输出 → 已初始化，直接进入日常使用。否则按以下步骤执行：

### 第 1 步：检查并安装环境

1. 运行 `node --version`。需要 Node >= 18。
   - 命令不存在或版本过低：向用户说明并经确认后安装（Windows：`winget install OpenJS.NodeJS.LTS`，或从 nodejs.org 下载 LTS 安装包；安装后重开终端再验证）。
2. 检查本 skill 的 `mcp/node_modules/` 目录是否存在。
   - 不存在：在 `mcp/` 目录下执行 `npm install --no-fund --no-audit`（有 package-lock.json，可离线走缓存）。

### 第 2 步：收集后端地址

询问用户 gitManage 后端地址（用于 env `GITMANAGE_BASE_URL`）：
- 默认值 `http://192.168.11.70:5555`（内网常规部署，不确定时直接用默认）。
- 本机开发部署时为 `http://localhost:5555`。
- 用户表示"不确定/就是那个"时，可用默认地址做连通性探测（GET `<地址>/shell/select?gitType=0`，HTTP 200 即可用）后确认。

### 第 3 步：注册 MCP 到当前 agent

读取 [references/mcp-install.md](references/mcp-install.md)，按当前 agent 类型将 `gitmanage` 配置写入对应的 MCP 配置文件：
- `command: node`，`args: [<本 skill 绝对路径>/mcp/index.js]`（Windows 路径用正斜杠）。
- env 至少写 `GITMANAGE_BASE_URL`。
- **合并写入，不要覆盖用户已有配置**；Windows 下写 JSON 必须无 BOM（见 references/mcp-install.md 的坑）。

### 第 4 步：验证

1. 提示用户重启 agent 或重连 MCP server，确认 3 个工具（submit_merge_request / submit_new_branch_request / list_audit_records）可见。
2. 用 `list_audit_records`（`gitType: "merge"`）做一次只读调用验证连通性。
   - 报"无法连接 gitManage 后端"：后端未启动，向用户说明需要先启动 gitManage 服务，初始化到此暂停（配置已就绪，后端起来即可用）。

## 日常使用：发起合并申请

从用户话语识别意图后，对照下表收集信息。**用户已说清的字段不重复问**，缺什么问什么，可一次问齐：

| 字段 | 必填 | 收集要点 |
|---|---|---|
| serviceName | 是 | 枚举 6 选 1：billservice / goodsservice / StatisticsService / imexportservice / saassystemsetting / customerservice；用户说"bill 服务"即 billservice |
| sourceBranch | 是 | 被合并的源分支，如 feature/dh/202403；可从用户当前工作分支推断，但需口头确认 |
| targetBranch | 是 | 仅允许 pre / pre_temp（用户说"合到预发"= pre） |
| remark | 是 | 建议格式 `marge:用户名+功能`；空格会被自动替换为 —，禁引号等元字符 |

**触发前必须向用户确认**，明示副作用：提交后会通过企微机器人通知审核人，审核通过前代码不会被合并。用户确认后才调用 `submit_merge_request`，然后向用户报告返回的记录 ID、流水号与当前状态（待审）。

## 日常使用：新开分支申请

| 字段 | 必填 | 收集要点 |
|---|---|---|
| serviceName | 是 | 同上枚举 |
| newBranch | 是 | 要新开的分支名，如 hotfix/dh/202609 |
| sourceBranch | 是 | 基于哪个分支拉取，常用 uat / master；用户说"从 uat 拉"即 uat |
| remark | 是 | 建议格式 `用户名+新开分支说明` |

**参数语义警示**：newBranch 是新分支名、sourceBranch 是来源分支，绝不能填反（工具入参已是业务语义命名，直接对应填写即可）。同样先确认副作用（企微通知审核人，审核通过前分支不会被创建），确认后调用 `submit_new_branch_request`。

## 跟踪状态

- 用户问"查下状态/合了没有/待审有哪些"→ 调 `list_audit_records`。
- `gitType`：merge=合并申请，new_branch=新开分支申请；可加 `status` 过滤：pending=待审、success=成功通过、rejected=驳回、failed=执行失败。
- 发起申请时返回的记录 ID 可在查询后向用户对号汇报。
- 状态为"待审"时提醒用户：需审核人在审核页（输入服务密码）执行；"驳回/执行失败"时建议用户联系审核人处理。

## 安全边界（必须遵守）

- 只发起申请与查询，绝不尝试触发真实执行（执行类接口 /shell/marge、/shell/checkOut 未暴露在 MCP 中，也不要引导用户让 agent 去调）。
- serviceName 与合并目标分支在 MCP schema 层已锁死：收到枚举校验错误时，引导用户从合法值中选择，不要想办法绕过。
- env `GITMANAGE_SERVICES` 只能在内置 6 服务之上增补服务，不能移除（MCP server 代码内锁定的语义）。
