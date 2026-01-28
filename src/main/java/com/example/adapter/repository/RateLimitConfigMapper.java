package com.example.adapter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.adapter.entity.RateLimitConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RateLimitConfigMapper extends BaseMapper<RateLimitConfig> {
}
