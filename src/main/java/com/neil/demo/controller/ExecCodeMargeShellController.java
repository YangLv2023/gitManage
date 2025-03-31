package com.neil.demo.controller;

import com.neil.demo.converter.ExecGitConverter;
import com.neil.demo.dao.GitAuditRecordMapper;
import com.neil.demo.dto.ShellDto;
import com.neil.demo.dto.ShellVo;
import com.neil.demo.message.MessageSend;
import com.neil.demo.model.GitAuditRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/shell")
public class ExecCodeMargeShellController {

    @Autowired
    private GitAuditRecordMapper gitAuditRecordMapper;
    @Autowired
    private MessageSend messageSend;

    SseEmitter emitter;

    @PostMapping(value="marge")
    @Async
    public void marge(@RequestBody @Valid ShellDto.MargeAuditDto margeAuditDto, HttpServletResponse response) {
        emitter = new SseEmitter();
        try {
            GitAuditRecord gitAuditRecord = gitAuditRecordMapper.getRecordById(margeAuditDto.getId());

            StringBuilder command = new StringBuilder("D:\\Program Files\\Git\\bin\\bash.exe");
            command.append(" -c 'E:/workspace/handday/checkOriginMergeCode.sh ");
            command.append(" "+ gitAuditRecord.getServiceName() +" ");
            command.append(" "+ gitAuditRecord.getFormBranch() +" ");
            command.append(" false ");
            /*StringBuffer remark = new StringBuffer(gitAuditRecord.getRemark().replaceAll(" ","—")).append(";")
                    .append(gitAuditRecord.getFormBranch()).append("-》").append(gitAuditRecord.getTargetBranch());*/
            command.append(" "+ gitAuditRecord.getRemark() +" ");
            command.append(" "+ gitAuditRecord.getTargetBranch() +" ");
            System.out.println(command.toString());
            Process process = Runtime.getRuntime().exec(command.toString());
            // 读取Shell脚本的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder output = new StringBuilder();
            String line;
            Boolean issuccess = Boolean.TRUE;
            while ((line = reader.readLine()) != null) {
                byte[] gbkBytes = line.getBytes("UTF-8");
                String gbkString = new String(gbkBytes, "UTF-8");
                System.out.println(gbkString);
                emitter.send(gbkString+"<br/>");
                output.append(gbkString).append("<br/>");
                if(gbkString.indexOf("回滚") > 0){
                    issuccess = false;
                }
            }

            // 等待Shell脚本执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0 && issuccess) {
                emitter.send("Shell脚本执行成功");
                gitAuditRecordMapper.auditGit(gitAuditRecord.getId(),System.currentTimeMillis(),1,output.toString());
                gitAuditRecord.setResult(1);
                //messageSend.sendMargeAudit(gitAuditRecord);
            } else {
                emitter.send("Shell脚本执行失败");
                gitAuditRecordMapper.auditGit(gitAuditRecord.getId(),System.currentTimeMillis(),3,output.toString());
                gitAuditRecord.setResult(3);
            }
            emitter.complete();
            process.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
     /*   try {
            emitter.send("test");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


    @GetMapping(value = "/marge", produces = "text/event-stream")
    public SseEmitter streamOutput() {
        return emitter;
    }




    @PostMapping(value="checkOut")
    @Async
    public void checkOut(@RequestBody @Valid ShellDto.MargeAuditDto margeAuditDto, HttpServletResponse response) {
       /* emitter = new SseEmitter();
        try {
            GitAuditRecord gitAuditRecord = gitAuditRecordMapper.getRecordById(margeAuditDto.getId());

            StringBuilder command = new StringBuilder("D:\\Program Files\\Git\\bin\\bash.exe");
            command.append(" -c 'E:/workspace/handday/checkOriginMergeCode.sh ");
            command.append(" "+ gitAuditRecord.getServiceName() +" ");
            command.append(" "+ gitAuditRecord.getFormBranch() +" ");
            command.append(" false ");
            *//*StringBuffer remark = new StringBuffer(gitAuditRecord.getRemark().replaceAll(" ","—")).append(";")
                    .append(gitAuditRecord.getFormBranch()).append("-》").append(gitAuditRecord.getTargetBranch());*//*
            command.append(" "+ gitAuditRecord.getRemark() +" ");
            command.append(" "+ gitAuditRecord.getTargetBranch() +" ");
            System.out.println(command.toString());
            Process process = Runtime.getRuntime().exec(command.toString());
            // 读取Shell脚本的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder output = new StringBuilder();
            String line;
            Boolean issuccess = Boolean.TRUE;
            while ((line = reader.readLine()) != null) {
                byte[] gbkBytes = line.getBytes("UTF-8");
                String gbkString = new String(gbkBytes, "UTF-8");
                System.out.println(gbkString);
                emitter.send(gbkString+"<br/>");
                output.append(gbkString).append("<br/>");
                if(gbkString.indexOf("回滚") > 0){
                    issuccess = false;
                }
            }

            // 等待Shell脚本执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0 && issuccess) {
                emitter.send("Shell脚本执行成功");
                gitAuditRecordMapper.auditGit(gitAuditRecord.getId(),System.currentTimeMillis(),1,output.toString());
                gitAuditRecord.setResult(1);
                messageSend.sendMargeAudit(gitAuditRecord);
            } else {
                emitter.send("Shell脚本执行失败");
                gitAuditRecordMapper.auditGit(gitAuditRecord.getId(),System.currentTimeMillis(),3,output.toString());
                gitAuditRecord.setResult(3);
            }
            emitter.complete();
            process.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

    }
    @GetMapping(value = "/checkOut", produces = "text/event-stream")
    public SseEmitter chekOutStreamOutput() {
        return emitter;
    }


    @GetMapping("select")
    public List<ShellVo.MargeVo> select(@RequestParam(value = "gitType", required = false) Integer gitType) {
        List<GitAuditRecord> gitAuditRecordList = gitAuditRecordMapper.selectAll(gitType);
        return ExecGitConverter.INSTANCE.gitAuditRecordToVo(gitAuditRecordList);
    }

    @PostMapping("submit")
    public boolean submit(@RequestBody @Valid ShellDto.MargeDto margeDto) {
        margeDto.setRemark(margeDto.getRemark().replaceAll(" ","—"));
        GitAuditRecord gitAuditRecord = new GitAuditRecord();
        gitAuditRecord.setSerialNumber(System.currentTimeMillis()).setServiceName(margeDto.getServiceName().trim())
                .setFormBranch(margeDto.getFormBranch().trim())
                .setTargetBranch(margeDto.getTargetBranch().trim()).setRemark(margeDto.getRemark().trim())
                .setSubmitTime(System.currentTimeMillis()).setAuditTime(0L).setCreateTime(System.currentTimeMillis()).setExecLog("").setGitType(margeDto.getGitType());
        gitAuditRecordMapper.saveRecord(gitAuditRecord);
        //messageSend.sendMargeInfo(gitAuditRecord);
       return true;
    }

    @PostMapping("checkOutNew")
    public boolean checkOutNew(@RequestBody @Valid ShellDto.MargeDto margeDto) {
        margeDto.setRemark(margeDto.getRemark().replaceAll(" ","—"));
        GitAuditRecord gitAuditRecord = new GitAuditRecord();
        gitAuditRecord.setSerialNumber(System.currentTimeMillis()).setServiceName(margeDto.getServiceName().trim())
                .setFormBranch(margeDto.getFormBranch().trim())
                .setTargetBranch(margeDto.getTargetBranch().trim()).setRemark(margeDto.getRemark().trim())
                .setSubmitTime(System.currentTimeMillis()).setAuditTime(0L).setCreateTime(System.currentTimeMillis()).setExecLog("").setGitType(margeDto.getGitType());
        gitAuditRecordMapper.saveRecord(gitAuditRecord);
        //messageSend.sendMargeInfo(gitAuditRecord);
        return true;
    }

}
