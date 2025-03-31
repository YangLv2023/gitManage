package com.neil.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ShellDto {


    @Data
    public static class MargeAuditDto {
        @NotNull(message ="id 不能空" )
        private Long id;

    }


    @Data
    public static class MargeDto {
        @NotNull(message ="服务 不能空" )
        private String serviceName;
        @NotNull(message ="分支 不能空" )
        private String formBranch;
        @NotNull(message ="备注 不能空" )
        private String remark;
        @NotNull(message ="目标分支 不能空" )
        private String targetBranch;
        @NotNull(message ="提交类型 不能空" )
        private Integer gitType;

    }

    /*@Data
    public static class MargeDto {
        //流水号
        private Integer serialNumber;
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
    }*/


}
