package com.example.adapter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.adapter.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
