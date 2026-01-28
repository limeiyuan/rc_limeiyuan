package com.example.adapter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adapter.common.response.Result;
import com.example.adapter.entity.RateLimitConfig;
import com.example.adapter.repository.RateLimitConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "限流配置管理")
@RestController
@RequestMapping("/api/admin/rate-limits")
@RequiredArgsConstructor
public class RateLimitConfigController {

    private final RateLimitConfigMapper rateLimitConfigMapper;

    @Operation(summary = "创建限流配置")
    @PostMapping
    public Result<RateLimitConfig> create(@Valid @RequestBody RateLimitConfig config) {
        rateLimitConfigMapper.insert(config);
        return Result.success(config);
    }

    @Operation(summary = "更新限流配置")
    @PutMapping("/{id}")
    public Result<RateLimitConfig> update(@PathVariable Long id, @RequestBody RateLimitConfig config) {
        config.setId(id);
        rateLimitConfigMapper.updateById(config);
        return Result.success(config);
    }

    @Operation(summary = "删除限流配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        rateLimitConfigMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "限流配置列表")
    @GetMapping
    public Result<Page<RateLimitConfig>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long tenantId) {
        Page<RateLimitConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RateLimitConfig> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(RateLimitConfig::getTenantId, tenantId);
        }
        wrapper.orderByDesc(RateLimitConfig::getCreateTime);
        return Result.success(rateLimitConfigMapper.selectPage(page, wrapper));
    }
}
