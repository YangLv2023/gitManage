# MCP 注册指南（将本 skill 的 MCP server 写入 agent 配置）

通用 JSON 片段（`<SKILL_DIR>` 替换为本 skill 目录的**绝对路径**，Windows 路径用正斜杠 `/`）：

```json
{
  "mcpServers": {
    "gitmanage": {
      "command": "node",
      "args": ["<SKILL_DIR>/mcp/index.js"],
      "env": {
        "GITMANAGE_BASE_URL": "http://192.168.11.70:5555"
      }
    }
  }
}
```

## 各 agent 的配置文件位置

| Agent | 配置文件 / 方式 | 说明 |
|---|---|---|
| Qoder | Windows：`%USERPROFILE%\.qoder\mcp.json`（全局） | `mcpServers` 对象内合并写入 `gitmanage` 条目；写完后需在 Qoder 中重启/重连该 MCP server |
| Claude Code | 命令行优先：`claude mcp add gitmanage -s user -- node <SKILL_DIR>/mcp/index.js`；或编辑 `~/.claude.json` 顶层 `mcpServers` | `-s user` 全局生效；也可 `-s project` 写入项目 `.mcp.json` |
| Cursor | `%USERPROFILE%\.cursor\mcp.json` | 同通用结构 |
| Codex CLI | `%USERPROFILE%\.codex\config.toml` 的 `[mcp_servers.gitmanage]` 段 | TOML 格式：`command = "node"`、`args = ["<SKILL_DIR>/mcp/index.js"]`、`env` 用键值表 |
| 其他 | 查找该 agent 文档中 mcpServers / MCP servers 配置项 | 结构基本同上 |

## env 可选项

| 变量 | 默认 | 说明 |
|---|---|---|
| `GITMANAGE_BASE_URL` | `http://192.168.11.70:5555` | gitManage 后端地址（本机开发部署时改 `http://localhost:5555`） |
| `GITMANAGE_SERVICES` | （不设） | 在内置 6 服务白名单之上**增补**服务（逗号分隔，只能加不能减）：billservice, goodsservice, StatisticsService, imexportservice, saassystemsetting, customerservice |

## Windows 写入 JSON 的关键坑

1. **BOM 陷阱**：Windows PowerShell 5.1 的 `Set-Content -Encoding UTF8` / `Out-File` 会写入 UTF-8 BOM，导致部分 agent 解析 JSON 失败。写入必须用无 BOM 方式：
   - PowerShell：`[System.IO.File]::WriteAllText($path, $json, (New-Object System.Text.UTF8Encoding($false)))`
   - 或直接使用 agent 自带的文件写入工具（通常默认无 BOM）。
2. **合并而非覆盖**：读出原文件 → 在 `mcpServers` 对象中新增/更新 `gitmanage` 键 → 写回。保留用户已有的其他 MCP server 条目。写前先备份原文件内容。
3. **路径**：`args` 中用正斜杠（`e:/workspace/.../mcp/index.js`），避免反斜杠转义问题。
4. 写入后告知用户：需重启 agent 或重连 MCP server 才生效。

## 验证

1. `tools/list` 应返回 3 个工具：`submit_merge_request`、`submit_new_branch_request`、`list_audit_records`。
2. 只读连通性验证：调用 `list_audit_records`，参数 `{"gitType": "merge"}`。后端未启动时会返回明确的"无法连接 gitManage 后端"错误——此时配置本身已正确，只需启动后端。
