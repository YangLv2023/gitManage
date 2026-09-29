#!/usr/bin/env node
/**
 * gitManage MCP Server（stdio transport）
 *
 * 将 gitManage 审计系统的三类能力暴露为 MCP 工具：
 *   1. submit_merge_request      发起合并申请（gitType=0，POST /shell/submit）
 *   2. submit_new_branch_request 新开分支申请（gitType=1，POST /shell/checkOutNew）
 *   3. list_audit_records        审计记录分页查询（GET /shell/selectPage，降级 /shell/select）
 *
 * Java 后端零改动：本进程仅作为 HTTP 适配层调用 Spring Boot 接口。
 * 注意边界：执行类接口（/shell/marge、/shell/checkOut）有意不暴露，
 * 真实执行合并/检出必须保留在人工审核页面完成。
 *
 * 环境变量：
 *   GITMANAGE_BASE_URL   后端地址，默认 http://192.168.11.70:5555（本机开发可改 localhost:5555）
 *   GITMANAGE_SERVICES   增补 serviceName 白名单，逗号分隔（只能加不能减，
 *                       内置清单已锁死为前端 git_audit.html 服务下拉框的 6 个服务）
 */

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";

const BASE_URL = (process.env.GITMANAGE_BASE_URL || "http://192.168.11.70:5555").replace(/\/+$/, "");
const HTTP_TIMEOUT_MS = 30000;

// serviceName 白名单：内置锁死为前端页面（git_audit.html 合并/新开分支表单共用的服务下拉框）全部选项，
// env GITMANAGE_SERVICES 只能在内置清单之上增补（逗号分隔），无法移除或绕过内置服务——锁死语义。
const DEFAULT_SERVICE_WHITELIST = [
  "billservice",
  "goodsservice",
  "StatisticsService",
  "imexportservice",
  "saassystemsetting",
  "customerservice",
];
const SERVICE_WHITELIST = (() => {
  const fromEnv = (process.env.GITMANAGE_SERVICES || "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);
  return [...new Set([...DEFAULT_SERVICE_WHITELIST, ...fromEnv])];
})();

// 输入校验：只放行无 shell 元字符的子集，从源头挡住命令注入
// （后端是字符串拼接后 Runtime.exec，serviceName/分支/备注都会进入命令串）
const SERVICE_RE = /^[A-Za-z0-9._-]+$/;
const BRANCH_RE = /^[A-Za-z0-9._/-]+$/;
const REMARK_DANGER_RE = /['`$;&|<>(){}"\\\n\r\t]/;

// 后端 VO 返回的中文状态文本
const GIT_TYPE = { merge: 0, new_branch: 1 };
const STATUS_CN = { pending: "待审", success: "成功通过", rejected: "驳回", failed: "执行失败" };

/** HTTP 封装：网络失败与业务失败统一抛 Error，由调用方转成 isError 响应 */
async function api(path, options = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), HTTP_TIMEOUT_MS);
  try {
    const res = await fetch(`${BASE_URL}${path}`, {
      method: options.method || "GET",
      headers: options.body ? { "Content-Type": "application/json" } : undefined,
      body: options.body ? JSON.stringify(options.body) : undefined,
      signal: controller.signal,
    });
    if (!res.ok) {
      throw new Error(`后端返回 HTTP ${res.status}（${options.method || "GET"} ${path}）`);
    }
    const text = await res.text();
    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  } catch (e) {
    if (e.name === "AbortError") {
      throw new Error(`请求超时（>${HTTP_TIMEOUT_MS}ms）：${options.method || "GET"} ${path}`);
    }
    if (e.cause && e.cause.code === "ECONNREFUSED") {
      throw new Error(`无法连接 gitManage 后端 ${BASE_URL}，请确认服务已启动`);
    }
    throw e;
  } finally {
    clearTimeout(timer);
  }
}

/** 校验 serviceName 与 remark，返回错误消息（null 表示通过） */
function validateServiceAndRemark(serviceName, remark) {
  if (!SERVICE_RE.test(serviceName)) {
    return `serviceName "${serviceName}" 含非法字符，仅允许字母/数字/点/下划线/连字符`;
  }
  if (!SERVICE_WHITELIST.includes(serviceName)) {
    return `serviceName "${serviceName}" 不在白名单内，允许的服务：${SERVICE_WHITELIST.join(", ")}`;
  }
  // 与后端行为对齐：空格会被替换为 "—"，这里提前替换，保证 agent 看到的与落库一致
  const sanitized = remark.replace(/ /g, "—");
  if (REMARK_DANGER_RE.test(sanitized)) {
    return "remark 含危险字符（引号、$、;、| 等 shell 元字符），请改写备注后重试";
  }
  return null;
}

function validateBranch(value, label) {
  if (!BRANCH_RE.test(value)) {
    return `${label} "${value}" 含非法字符，仅允许字母/数字/点/下划线/连字符/斜杠（如 feature/dh/202403）`;
  }
  return null;
}

/**
 * 提交后反查记录：后端 submit 只返回 true，这里通过 select 接口
 * （按 submitTime 倒序）匹配服务+双分支+时间窗，取回 id 与流水号。
 */
async function findSubmittedRecord(gitType, fields, sinceMs) {
  const list = await api(`/shell/select?gitType=${gitType}`);
  if (!Array.isArray(list)) return null;
  for (const rec of list) {
    const submitMs = Date.parse(rec.submitTime);
    if (
      rec.serviceName === fields.serviceName &&
      rec.formBranch === fields.formBranch &&
      rec.targetBranch === fields.targetBranch &&
      Number.isFinite(submitMs) &&
      submitMs >= sinceMs - 60000
    ) {
      return rec;
    }
  }
  return null;
}

/** 工具 handler 的统一文本响应 */
function textResponse(text, isError = false) {
  return { content: [{ type: "text", text }], isError };
}

function errResponse(e) {
  return textResponse(`调用失败：${e.message}`, true);
}

// ============================ MCP Server ============================

const server = new McpServer({ name: "gitmanage-audit", version: "1.0.0" });

// 服务名用枚举锁死：非法值在 MCP schema 校验层直接被拒，agent 在工具 schema 里就能看到全部合法值
const serviceNameSchema = z
  .enum(SERVICE_WHITELIST)
  .describe("服务名（与前端服务下拉框完全一致，仅允许枚举列出的值）");
const remarkSchema = z
  .string()
  .min(1)
  .describe("申请备注。注意：空格会被自动替换为 —，且不能包含引号等 shell 元字符");

// ---------- 工具 1：发起合并申请 ----------
server.registerTool(
  "submit_merge_request",
  {
    title: "发起合并申请",
    description:
      "向 gitManage 审计系统提交一次代码合并申请（将 sourceBranch 合并到 targetBranch）。" +
      "提交成功后会通过企业微信机器人通知审核人，由人工在审核页面执行真实合并——本工具只发起申请，不执行合并。" +
      "提交后会反查并返回记录 ID 与流水号，可用 list_audit_records 跟踪审核结果。",
    inputSchema: {
      serviceName: serviceNameSchema,
      sourceBranch: z.string().min(1).describe("被合并的源分支，如 feature/dh/202403"),
      // 前端该字段为封闭下拉框（select），同样锁死为枚举
      targetBranch: z
        .enum(["pre", "pre_temp"])
        .describe("合并目标分支（与前端下拉框一致，仅允许 pre / pre_temp）"),
      remark: remarkSchema,
    },
  },
  async ({ serviceName, sourceBranch, targetBranch, remark }) => {
    try {
      const invalid =
        validateServiceAndRemark(serviceName, remark) ||
        validateBranch(sourceBranch, "sourceBranch") ||
        validateBranch(targetBranch, "targetBranch");
      if (invalid) return textResponse(invalid, true);

      const fields = { serviceName, formBranch: sourceBranch, targetBranch };
      const sinceMs = Date.now();
      await api("/shell/submit", {
        method: "POST",
        body: { ...fields, remark: remark.replace(/ /g, "—"), gitType: GIT_TYPE.merge },
      });

      // 反查记录 ID（失败不影响提交结果本身，做降级提示）
      let rec = null;
      try {
        rec = await findSubmittedRecord(GIT_TYPE.merge, fields, sinceMs);
      } catch {
        /* 反查失败时降级为无 ID 返回 */
      }

      const lines = ["合并申请已提交，等待人工审核：", `- 服务：${serviceName}`];
      lines.push(`- 合并分支：${sourceBranch} → 目标分支：${targetBranch}`);
      if (rec) {
        lines.push(`- 记录ID：${rec.id}（后续查询用）`);
        lines.push(`- 流水号：${rec.serialNumber}`);
        lines.push(`- 当前状态：${rec.result}`);
      } else {
        lines.push("- 记录ID：未能反查到（提交本身已成功，可用 list_audit_records 查询确认）");
      }
      lines.push("- 副作用：企业微信机器人已向审核人推送通知");
      lines.push("提示：审核通过前代码不会被合并，可用 list_audit_records 跟踪状态。");
      return textResponse(lines.join("\n"));
    } catch (e) {
      return errResponse(e);
    }
  }
);

// ---------- 工具 2：新开分支申请 ----------
server.registerTool(
  "submit_new_branch_request",
  {
    title: "新开分支申请",
    description:
      "向 gitManage 审计系统提交一次新开分支申请（基于 sourceBranch 检出新分支 newBranch）。" +
      "注意参数语义：newBranch 是要新开的分支名，sourceBranch 是被拉取的来源分支，不要填反。" +
      "提交成功后会通过企业微信机器人通知审核人，由人工在审核页面执行真实检出——本工具只发起申请，不执行检出。",
    inputSchema: {
      serviceName: serviceNameSchema,
      newBranch: z.string().min(1).describe("要新开的分支名，如 feature/ly/202410"),
      sourceBranch: z
        .string()
        .min(1)
        .describe("基于哪个分支拉取，前端常用值为 uat / master（datalist 允许自由输入）"),
      remark: remarkSchema,
    },
  },
  async ({ serviceName, newBranch, sourceBranch, remark }) => {
    try {
      const invalid =
        validateServiceAndRemark(serviceName, remark) ||
        validateBranch(newBranch, "newBranch") ||
        validateBranch(sourceBranch, "sourceBranch");
      if (invalid) return textResponse(invalid, true);

      // 后端字段语义翻转：formBranch=新分支名，targetBranch=来源分支
      const fields = { serviceName, formBranch: newBranch, targetBranch: sourceBranch };
      const sinceMs = Date.now();
      await api("/shell/checkOutNew", {
        method: "POST",
        body: { ...fields, remark: remark.replace(/ /g, "—"), gitType: GIT_TYPE.new_branch },
      });

      let rec = null;
      try {
        rec = await findSubmittedRecord(GIT_TYPE.new_branch, fields, sinceMs);
      } catch {
        /* 反查失败时降级为无 ID 返回 */
      }

      const lines = ["新开分支申请已提交，等待人工审核：", `- 服务：${serviceName}`];
      lines.push(`- 新开分支：${newBranch}（基于 ${sourceBranch} 检出）`);
      if (rec) {
        lines.push(`- 记录ID：${rec.id}（后续查询用）`);
        lines.push(`- 流水号：${rec.serialNumber}`);
        lines.push(`- 当前状态：${rec.result}`);
      } else {
        lines.push("- 记录ID：未能反查到（提交本身已成功，可用 list_audit_records 查询确认）");
      }
      lines.push("- 副作用：企业微信机器人已向审核人推送通知");
      lines.push("提示：审核通过前分支不会被创建，可用 list_audit_records 跟踪状态。");
      return textResponse(lines.join("\n"));
    } catch (e) {
      return errResponse(e);
    }
  }
);

// ---------- 工具 3：审计记录查询 ----------
server.registerTool(
  "list_audit_records",
  {
    title: "查询审计记录",
    description:
      "分页查询 gitManage 审计记录（合并申请与新开分支申请共用一张表）。" +
      "可用于：查询待审清单、跟踪自己刚发起的申请是否已审核执行。" +
      "状态说明：待审=尚未执行；成功通过=已执行成功；驳回=被审核人驳回；执行失败=脚本执行失败。",
    inputSchema: {
      gitType: z
        .enum(["merge", "new_branch"])
        .describe("记录类型：merge=合并申请，new_branch=新开分支申请"),
      page: z.number().int().min(1).default(1).describe("页码，默认 1"),
      size: z.number().int().min(1).max(100).default(20).describe("每页条数，默认 20"),
      status: z
        .enum(["pending", "success", "rejected", "failed"])
        .optional()
        .describe("可选状态过滤：pending=待审，success=成功通过，rejected=驳回，failed=执行失败"),
    },
  },
  async ({ gitType, page, size, status }) => {
    try {
      const type = GIT_TYPE[gitType];
      const isMerge = type === GIT_TYPE.merge;

      // 优先新分页接口，后端为旧版本时降级到 /shell/select（最近 100 条）
      let records;
      let source;
      try {
        const data = await api(`/shell/selectPage?gitType=${type}&page=${page}&size=${size}`);
        records = Array.isArray(data) ? data : data.records || [];
        source = `selectPage 第 ${page} 页`;
      } catch {
        const data = await api(`/shell/select?gitType=${type}`);
        records = Array.isArray(data) ? data : data.records || [];
        source = "select（降级，最近 100 条）";
      }

      if (status) {
        records = records.filter((r) => r.result === STATUS_CN[status]);
      }
      if (!records.length) {
        return textResponse(
          status
            ? `没有匹配状态（${STATUS_CN[status]}）的${isMerge ? "合并" : "新开分支"}记录（数据源：${source}）`
            : `当前没有${isMerge ? "合并" : "新开分支"}记录（数据源：${source}）`
        );
      }

      // 紧凑表格输出：合并场景列语义为 源→目标；新开分支场景为 新分支←来源
      const trunc = (s, n) => (String(s || "").length > n ? String(s).slice(0, n - 1) + "…" : String(s || ""));
      const header = isMerge
        ? ["ID", "流水号", "服务", "合并分支", "目标分支", "状态", "提交时间", "备注"]
        : ["ID", "流水号", "服务", "新开分支", "来源分支", "状态", "提交时间", "备注"];
      const rows = records.map((r) => [
        String(r.id),
        trunc(r.serialNumber, 16),
        trunc(r.serviceName, 16),
        trunc(r.formBranch, 24),
        trunc(r.targetBranch, 16),
        r.result,
        r.submitTime,
        trunc(r.remark, 20),
      ]);
      const widths = header.map((h, i) => Math.max(h.length * 2, ...rows.map((row) => String(row[i]).length)));
      // 中文字符宽度按 2 计，简单对齐
      const pad = (s, w) => {
        const len = [...String(s)].reduce((n, ch) => n + (ch.charCodeAt(0) > 255 ? 2 : 1), 0);
        return String(s) + " ".repeat(Math.max(0, w - len));
      };
      const table = [
        header.map((h, i) => pad(h, widths[i])).join(" | "),
        widths.map((w) => "-".repeat(w)).join("-+-"),
        ...rows.map((row) => row.map((c, i) => pad(c, widths[i])).join(" | ")),
      ].join("\n");

      return textResponse(
        `${isMerge ? "合并" : "新开分支"}审计记录（数据源：${source}，共 ${records.length} 条）：\n\n${table}` +
          `\n\n字段说明：ID 用于人工审核页定位；状态取值：待审/成功通过/驳回/执行失败。`
      );
    } catch (e) {
      return errResponse(e);
    }
  }
);

// ============================ 启动 ============================

await server.connect(new StdioServerTransport());
// stdio 模式下 stdout 被协议占用，日志只能走 stderr
console.error(`[gitmanage-audit] MCP server started (stdio), backend = ${BASE_URL}`);
console.error(
  `[gitmanage-audit] serviceName whitelist（内置前端清单${
    SERVICE_WHITELIST.length > DEFAULT_SERVICE_WHITELIST.length ? " + env 增补" : ""
  }）: ${SERVICE_WHITELIST.join(", ")}`
);
