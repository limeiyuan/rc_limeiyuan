package com.example.adapter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adapter.common.response.Result;
import com.example.adapter.entity.Tenant;
import com.example.adapter.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "租户管理")
@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "创建租户")
    @PostMapping
    public Result<Tenant> create(@Valid @RequestBody Tenant tenant) {
        return Result.success(tenantService.create(tenant));
    }

    @Operation(summary = "更新租户")
    @PutMapping("/{id}")
    public Result<Tenant> update(@PathVariable Long id, @RequestBody Tenant tenant) {
        return Result.success(tenantService.update(id, tenant));
    }

    @Operation(summary = "删除租户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取租户详情")
    @GetMapping("/{id}")
    public Result<Tenant> getById(@PathVariable Long id) {
        return Result.success(tenantService.getById(id));
    }

    @Operation(summary = "租户列表")
    @GetMapping
    public Result<Page<Tenant>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String tenantCode,
            @RequestParam(required = false) String tenantName) {
        return Result.success(tenantService.list(pageNum, pageSize, tenantCode, tenantName));
    }
}
