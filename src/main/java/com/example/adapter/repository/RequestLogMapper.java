package com.example.adapter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.adapter.entity.RequestLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLog> {
}
