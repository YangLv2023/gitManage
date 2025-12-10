package com.neil.demo.message;

import com.alibaba.fastjson.JSONObject;
import com.neil.demo.converter.ExecGitConverter;
import com.neil.demo.dao.GitAuditRecordMapper;
import com.neil.demo.dto.ShellDto;
import com.neil.demo.dto.ShellVo;
import com.neil.demo.model.GitAuditRecord;
import com.neil.demo.util.HttpClient;
import lombok.Getter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Component
public class MessageSend {

    @Value("${wxBotUrl}")
    private String url;
    /**
     * @author ly
     * @description: 合并消息发送
     * @date 17:18 2024/3/7
     * @param []
     * @return
     **/
    public void sendMargeInfo(GitAuditRecord gitAuditRecord) {
        /*GitAuditRecord gitAuditRecord = new GitAuditRecord();
        gitAuditRecord.setSerialNumber(1123123L).setServiceName("brach1test").setFormBranch("brach1test").setTargetBranch("brach1test").setRemark("备注test");*/
        StringBuilder sb = new StringBuilder();
        sb.append("### 【").append(" ").append("代码合并请求").append("】").append("\n");
        sb.append("> 流水号：").append("<font color=\"comment\">").append(gitAuditRecord.getSerialNumber()).append("</font>\n");
        sb.append("> 合并服务：").append("<font color=\"comment\">").append(gitAuditRecord.getServiceName()).append("</font>\n");
        sb.append("> 合并分支：").append("<font color=\"comment\">").append(gitAuditRecord.getFormBranch()).append("</font>\n");
        sb.append("> 目标分支：").append("<font color=\"comment\">").append(gitAuditRecord.getTargetBranch()).append("</font>\n");
        sb.append("> 备注信息：").append("<font color=\"comment\">").append(gitAuditRecord.getRemark()).append("</font>\n");
        sb.append("> [☞  审核执行](http://192.168.11.70:5555/git_audit.html)").append("\n");


        HttpClient client = new HttpClient();
        Markdonw markdonw = new Markdonw();
        markdonw.setContent(sb.toString());
        System.out.println("参数:"+markdonw.jsonString());
        System.out.println("结果："+client.sendPostByJson(url,markdonw));
    }



    @Test
    public void sendMargeAudit(GitAuditRecord gitAuditRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 【").append(" ").append("合并执行通知").append("】").append("\n");
        sb.append("> 流水号：").append("<font color=\"comment\">").append(gitAuditRecord.getSerialNumber()).append("</font>\n");
        sb.append("> 合并服务：").append("<font color=\"comment\">")
                .append(gitAuditRecord.getServiceName())
                .append("分支")
                .append(gitAuditRecord.getTargetBranch())
                .append("已合并")
                .append(gitAuditRecord.getFormBranch()).append("</font>\n");
        sb.append("> [☞  执行结果："+new ShellVo.MargeVo().setResult(gitAuditRecord.getResult())+"](http://192.168.11.70:5555/git_audit.html)").append("\n");

        HttpClient client = new HttpClient();
        Markdonw markdonw = new Markdonw();
        markdonw.setContent(sb.toString());
        System.out.println("参数:"+markdonw.jsonString());
        System.out.println("结果："+client.sendPostByJson(url,markdonw));
    }


    /**
     * @author ly
     * @description: 合并消息发送
     * @date 17:18 2024/3/7
     * @param []
     * @return
     **/
    public void sendCheckOutInfo(GitAuditRecord gitAuditRecord) {
        /*GitAuditRecord gitAuditRecord = new GitAuditRecord();
        gitAuditRecord.setSerialNumber(1123123L).setServiceName("brach1test").setFormBranch("brach1test").setTargetBranch("brach1test").setRemark("备注test");*/
        StringBuilder sb = new StringBuilder();
        sb.append("### 【").append(" ").append("检出执行通知").append("】").append("\n");
        sb.append("> 流水号：").append("<font color=\"comment\">").append(gitAuditRecord.getSerialNumber()).append("</font>\n");
        sb.append("> 检出服务：").append("<font color=\"comment\">").append(gitAuditRecord.getServiceName()).append("</font>\n");
        sb.append("> 新开分支：").append("<font color=\"comment\">").append(gitAuditRecord.getFormBranch()).append("</font>\n");
        sb.append("> 来源分支：").append("<font color=\"comment\">").append(gitAuditRecord.getTargetBranch()).append("</font>\n");
        sb.append("> 备注信息：").append("<font color=\"comment\">").append(gitAuditRecord.getRemark()).append("</font>\n");
        sb.append("> [☞  审核执行](http://192.168.11.70:5555/git_audit.html)").append("\n");


        HttpClient client = new HttpClient();
        Markdonw markdonw = new Markdonw();
        markdonw.setContent(sb.toString());
        System.out.println("参数:"+markdonw.jsonString());
        System.out.println("结果："+client.sendPostByJson(url,markdonw));
    }


    public void sendCheckAudit(GitAuditRecord gitAuditRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 【").append(" ").append("检出执行通知").append("】").append("\n");
        sb.append("> 流水号：").append("<font color=\"comment\">").append(gitAuditRecord.getSerialNumber()).append("</font>\n");
        sb.append("> 检出服务：").append("<font color=\"comment\">")
                .append(gitAuditRecord.getServiceName())
                .append("分支")
                .append(gitAuditRecord.getFormBranch())
                .append("已从")
                .append(gitAuditRecord.getTargetBranch())
                .append("拉取").append("</font>\n");
        sb.append("> [☞  执行结果："+new ShellVo.MargeVo().setResult(gitAuditRecord.getResult())+"](http://192.168.11.70:5555/git_audit.html)").append("\n");

        HttpClient client = new HttpClient();
        Markdonw markdonw = new Markdonw();
        markdonw.setContent(sb.toString());
        System.out.println("参数:"+markdonw.jsonString());
        System.out.println("结果："+client.sendPostByJson(url,markdonw));
    }

    public static class Markdonw {
        @Getter
        private JSONObject markdown;

        @Getter
        private final String msgtype = "markdown";

        public void setContent(String content) {
            JSONObject markdown = new JSONObject();
            markdown.put("content", content);
            this.markdown = markdown;
        }
        public String jsonString() {
            return JSONObject.toJSONString(this);
        }
    }


}
