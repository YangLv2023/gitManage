package com.neil.demo.dao;

import com.neil.demo.model.GitAuditRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GitAuditRecordMapper {

  @Insert("INSERT INTO GitAuditRecord (serialNumber,serviceName,formBranch, createTime, targetBranch, remark,submitTime,auditTime,result,dataStatus,execLog) " +
          "VALUES (#{serialNumber},#{serviceName},#{formBranch},#{createTime}, #{targetBranch}, #{remark},#{submitTime}, #{auditTime},#{result},#{dataStatus},#{execLog})")
  void saveRecord(GitAuditRecord conversation);


  @Select("select * from GitAuditRecord where dataStatus = 1 order by submitTime desc")
  List<GitAuditRecord> selectAll();

  @Select("select * from GitAuditRecord where id= #{id} and dataStatus = 1")
  GitAuditRecord getRecordById(Long id);


  @Update("UPDATE GitAuditRecord set result = #{result},execLog=#{execLog},auditTime=#{auditTime} WHERE id = #{id}")
  void auditGit(Long id,Long auditTime,Integer result,String execLog);


}
