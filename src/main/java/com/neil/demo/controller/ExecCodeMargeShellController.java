package com.neil.demo.controller;

import com.neil.demo.converter.ExecGitConverter;
import com.neil.demo.dao.GitAuditRecordMapper;
import com.neil.demo.dto.ShellDto;
import com.neil.demo.dto.ShellVo;
import com.neil.demo.message.MessageSend;
import com.neil.demo.model.GitAuditRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shell")
public class ExecCodeMargeShellController {

    @Autowired
    private GitAuditRecordMapper gitAuditRecordMapper;
    @Autowired
    private MessageSend messageSend;

    SseEmitter emitter;

    @Value("${fileUrl}")
    private String url;
    //String url = "C:\\Program Files\\Git\\bin\\bash.exe";
    //String url = ""D:\\Program Files\\Git\\bin\\bash.exe"";


    @PostMapping(value="marge")
    @Async
    public void marge(@RequestBody @Valid ShellDto.MargeAuditDto margeAuditDto, HttpServletResponse response) {
        emitter = new SseEmitter();
        try {
            GitAuditRecord gitAuditRecord = gitAuditRecordMapper.getRecordById(margeAuditDto.getId());

            StringBuilder command = new StringBuilder(url);
            command.append(" -c 'E:/workspace/handday/checkOriginMergeCode.sh ");
            command.append(" "+ gitAuditRecord.getServiceName() +" ");
            command.append(" "+ gitAuditRecord.getFormBranch() +" ");
            command.append(" false ");
            StringBuffer remark = new StringBuffer(gitAuditRecord.getRemark().replaceAll(" ","—")).append(";")
                    .append(gitAuditRecord.getFormBranch()).append("-》").append(gitAuditRecord.getTargetBranch());
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
        emitter = new SseEmitter();
        try {
            GitAuditRecord gitAuditRecord = gitAuditRecordMapper.getRecordById(margeAuditDto.getId());

            StringBuilder command = new StringBuilder(url);
            command.append(" -c 'E:/workspace/handday/checkOutNewCode.sh ");
            command.append(" "+ gitAuditRecord.getServiceName() +" ");
            command.append(" "+ gitAuditRecord.getTargetBranch() +" ");
            command.append(" "+ gitAuditRecord.getFormBranch() +" ");
            System.out.println(command.toString());
            Process process = Runtime.getRuntime().exec(command.toString());
            // 读取Shell脚本的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder output = new StringBuilder();
            String line;
            Boolean issuccess = Boolean.FALSE;
            while ((line = reader.readLine()) != null) {
                byte[] gbkBytes = line.getBytes("UTF-8");
                String gbkString = new String(gbkBytes, "UTF-8");
                System.out.println(gbkString);
                emitter.send(gbkString+"<br/>");
                output.append(gbkString).append("<br/>");
                if(gbkString.contains("操作成功完成")){
                    issuccess = true;
                }
            }

            // 等待Shell脚本执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0 && issuccess) {
                emitter.send("Shell脚本执行成功");
                gitAuditRecordMapper.auditGit(gitAuditRecord.getId(),System.currentTimeMillis(),1,output.toString());
                gitAuditRecord.setResult(1);
                messageSend.sendCheckAudit(gitAuditRecord);
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

    /**
     * 分页查询。老的 select 接口保持不动（仍返回最近 100 条数组），
     * 新页面使用本接口获取总数与分页数据。
     */
    @GetMapping("selectPage")
    public Map<String, Object> selectPage(@RequestParam(value = "gitType") Integer gitType,
                                          @RequestParam(value = "page", defaultValue = "1") int page,
                                          @RequestParam(value = "size", defaultValue = "20") int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 200) size = 200;
        int offset = (page - 1) * size;
        int total = gitAuditRecordMapper.countAll(gitType);
        List<GitAuditRecord> gitAuditRecordList = gitAuditRecordMapper.selectPage(gitType, offset, size);
        Map<String, Object> result = new HashMap<>(4);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("records", ExecGitConverter.INSTANCE.gitAuditRecordToVo(gitAuditRecordList));
        return result;
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
        messageSend.sendMargeInfo(gitAuditRecord);
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
        messageSend.sendCheckOutInfo(gitAuditRecord);
        return true;
    }

}
