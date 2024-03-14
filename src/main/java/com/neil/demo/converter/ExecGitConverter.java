package com.neil.demo.converter;

import com.neil.demo.dto.ShellVo;
import com.neil.demo.model.GitAuditRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExecGitConverter {

    ExecGitConverter INSTANCE = Mappers.getMapper(ExecGitConverter.class);


    List<ShellVo.MargeVo> gitAuditRecordToVo(List<GitAuditRecord> gitAuditRecordList);

}

