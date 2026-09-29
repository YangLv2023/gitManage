package com.neil.demo.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * handday-git-branch skill 目录下载：请求时实时把 classpath 下的
 * static/handday-git-branch/ 打包为 zip 返回，改动 skill 文件后无需任何打包步骤。
 * mcp/node_modules 不进包（体积大），由 skill 初始化向导执行 npm install 补齐。
 */
@RestController
public class SkillDownloadController {

    private static final String SKILL_DIR = "handday-git-branch";

    @GetMapping("/skill/download")
    public ResponseEntity<StreamingResponseBody> download() throws IOException {
        StreamingResponseBody body = out -> zipSkillDir(out);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + SKILL_DIR + ".zip")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }

    private void zipSkillDir(OutputStream out) throws IOException {
        // classpath*: 同时兼容 IDE exploded（target/classes）与 jar 内资源
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/static/" + SKILL_DIR + "/**");
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Resource res : resources) {
                String url = res.getURL().toString();
                int idx = url.indexOf("/static/" + SKILL_DIR + "/");
                if (idx < 0) {
                    continue;
                }
                String rel = url.substring(idx + ("/static/" + SKILL_DIR + "/").length());
                // IDE exploded 模式会匹配到目录项（无扩展名）；node_modules 体积大不进包
                if (rel.isEmpty() || rel.endsWith("/") || rel.contains("node_modules")
                        || rel.lastIndexOf('.') < rel.lastIndexOf('/')) {
                    continue;
                }
                zip.putNextEntry(new ZipEntry(SKILL_DIR + "/" + rel));
                try (InputStream in = res.getInputStream()) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        zip.write(buf, 0, len);
                    }
                }
                zip.closeEntry();
            }
        }
    }
}
