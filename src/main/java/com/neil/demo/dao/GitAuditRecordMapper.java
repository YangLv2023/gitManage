package com.neil.demo.dao;

import com.neil.demo.model.GitAuditRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GitAuditRecordMapper {

  @Insert("INSERT INTO GitAuditRecord (serialNumber,serviceName,formBranch, createTime, targetBranch, remark,submitTime,auditTime,result,dataStatus,execLog,gitType) " +
          "VALUES (#{serialNumber},#{serviceName},#{formBranch},#{createTime}, #{targetBranch}, #{remark},#{submitTime}, #{auditTime},#{result},#{dataStatus},#{execLog},#{gitType})")
  void saveRecord(GitAuditRecord conversation);


  @Select("select * from GitAuditRecord where dataStatus = 1 and gitType = #{gitType} order by submitTime desc limit 100")
  List<GitAuditRecord> selectAll(Integer gitType);

  @Select("select * from GitAuditRecord where dataStatus = 1 and gitType = #{gitType} order by submitTime desc limit #{size} offset #{offset}")
  List<GitAuditRecord> selectPage(@Param("gitType") Integer gitType, @Param("offset") int offset, @Param("size") int size);

  @Select("select count(*) from GitAuditRecord where dataStatus = 1 and gitType = #{gitType}")
  int countAll(Integer gitType);

  @Select("select * from GitAuditRecord where id= #{id} and dataStatus = 1")
  GitAuditRecord getRecordById(Long id);


  @Update("UPDATE GitAuditRecord set result = #{result},execLog=#{execLog},auditTime=#{auditTime} WHERE id = #{id}")
  void auditGit(Long id,Long auditTime,Integer result,String execLog);


}
