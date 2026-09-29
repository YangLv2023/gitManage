# mcp-server-gitmanage

gitManage 审计系统的 MCP Server（stdio transport），将「发起合并申请 / 新开分支申请 / 审计记录查询」暴露为 MCP 工具供 agent 调用。

本目录位于 handday-git-branch skill 内（`<skill目录>/mcp/`），安装 skill 后 MCP 注册地址即本目录的 index.js。**Java 后端零改动**：本进程仅通过 HTTP 调用 Spring Boot 现有接口（默认 `http://192.168.11.70:5555`）。

## 工具清单

| 工具 | 对应后端接口 | 说明 |
|---|---|---|
| `submit_merge_request` | `POST /shell/submit`（gitType=0） | 发起合并申请，触发企微机器人通知 |
| `submit_new_branch_request` | `POST /shell/checkOutNew`（gitType=1） | 新开分支申请，触发企微机器人通知 |
| `list_audit_records` | `GET /shell/selectPage`（降级 `/shell/select`） | 分页查询审计记录，跟踪审核状态 |

有意 **不暴露** 执行类接口（`/shell/marge`、`/shell/checkOut`）：真实执行合并/检出保留在人工审核页面完成，这是系统的安全边界。

## 前置条件

- Node.js >= 18
- gitManage 后端已启动（默认 `http://192.168.11.70:5555`，本机开发部署时为 `http://localhost:5555`）

## 安装与启动

```bash
cd <skill目录>/mcp
npm install
node index.js   # 常规情况下由 MCP 客户端拉起，无需手动启动
```

## 环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| `GITMANAGE_BASE_URL` | `http://192.168.11.70:5555` | 后端地址（本机开发部署时改 `http://localhost:5555`） |
| `GITMANAGE_SERVICES` | （内置前端 6 服务） | 在内置白名单之上增补服务，逗号分隔（**只能加不能减**）。内置清单已锁死为前端 git_audit.html 服务下拉框选项：billservice, goodsservice, StatisticsService, imexportservice, saassystemsetting, customerservice |

## 在 agent IDE 中注册（mcpServers 配置示例）

```json
{
  "mcpServers": {
    "gitmanage": {
      "command": "node",
      "args": ["e:/workspace/mytest/gitManage/src/main/resources/static/handday-git-branch/mcp/index.js"],
      "env": {
        "GITMANAGE_BASE_URL": "http://192.168.11.70:5555"
      }
    }
  }
}
```

在 Qoder 中：设置 → MCP → 添加自定义 MCP Server，粘贴上述 JSON（路径按实际位置调整）。若前端将来新增了服务，可在 env 中加 `GITMANAGE_SERVICES`（逗号分隔，只能增补不能移除内置 6 个）临时放开，无需改代码。

## 设计要点（为什么这么写）

- **字段语义翻转**：新开分支时后端 `formBranch`=新分支名、`targetBranch`=来源分支。工具参数改用 `newBranch` / `sourceBranch` 业务语义命名，在适配层完成映射，避免 agent 填反。
- **提交后反查**：后端 submit 类接口只返回 `true`，本服务提交后按「服务+双分支+时间窗」反查 `select` 接口拿回记录 ID 与流水号，让 agent 可闭环跟踪（内网低并发场景下可靠）。
- **服务名与目标分支锁死**：serviceName 在工具 schema 层即为 zod 枚举，内置取自前端下拉框的 6 个服务（env `GITMANAGE_SERVICES` 只能增补、不能移除）；合并目标分支锁死为 `pre` / `pre_temp`（与前端封闭下拉框一致）。注：老页面 git_audit_old.html 曾有第 7 个服务 erpadapter，现行页面已移除，如仍需要可在 env 中增补。
- **输入校验挡注入**：serviceName / 分支名 / 备注均做正则白名单校验（禁引号、`$`、`;`、`|` 等 shell 元字符），备注空格自动替换为 `—` 与后端行为对齐。
- **SSE 不适配 MCP**：执行类接口是「POST 启动 → GET 接流」的 SSE 异步流，与 MCP 工具一问一答模型不匹配，故不暴露；跟踪执行结果改用 `list_audit_records` 轮询状态。
