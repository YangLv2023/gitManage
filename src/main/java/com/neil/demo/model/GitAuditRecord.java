package com.neil.demo.model;


import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author ly
 * @description: 审核记录
 * @date 10:28 2024/3/6
 * @param
 * @return
 **/
@Data
@Accessors(chain = true)
public class GitAuditRecord {
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
    private Long submitTime;
    //审核时间
    private Long auditTime;
    private Long createTime;
    //结果 0 待审 1 成功通过 2=驳回 3=执行失败
    private Integer result = 0;
    //执行日志
    private String execLog;

    private Integer dataStatus = 1;

    //操作类型
    private Integer gitType;



}
