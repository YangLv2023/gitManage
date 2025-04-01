package com.neil.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class ShellVo {


    @Data
    public static class MargeVo {
        //id
        private Long id;
        //流水号
        private Long serialNumber;
        //服务
        private String serviceName;
        //合并分支
        private String formBranch;
        //目标分支
        private String targetBranch;
        //备注
        private String remark;
        //提交时间
        private String submitTime;
        //审核时间
        private String auditTime;
        //结果
        private String result;
        //执行日志
        private String execLog;

        public void setSubmitTime(Long submitTime) {
            if(submitTime == null || submitTime == 0){
                this.submitTime = "";
                return;
            }
            // 创建 SimpleDateFormat 实例，指定日期格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.submitTime = sdf.format(new Date(submitTime));
        }

        public void setAuditTime(Long auditTime) {
            if(auditTime == null || auditTime == 0){
                this.auditTime = "";
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.auditTime = sdf.format(new Date(auditTime));
        }
        //结果 0 待审 1 成功通过 2=驳回 3=执行失败
        public String setResult(Integer result) {
            switch (result){
                case 0:
                    this.result = "待审";
                    break;
                case 1:
                    this.result = "成功通过";
                    break;
                case 2:
                    this.result = "驳回";
                    break;
                case 3:
                    this.result = "执行失败";
                    break;
            }
            return this.result;
        }

    }




}
