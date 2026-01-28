package com.example.adapter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adapter.common.response.Result;
import com.example.adapter.entity.ApiParamConfig;
import com.example.adapter.entity.ExternalApiConfig;
import com.example.adapter.service.ExternalApiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "接口配置管理")
@RestController
@RequestMapping("/api/admin/configs")
@RequiredArgsConstructor
public class ExternalApiConfigController {

    private final ExternalApiConfigService apiConfigService;

    @Operation(summary = "创建接口配置")
    @PostMapping
    public Result<ExternalApiConfig> create(@Valid @RequestBody ExternalApiConfig config) {
        return Result.success(apiConfigService.create(config));
    }

    @Operation(summary = "更新接口配置")
    @PutMapping("/{id}")
    public Result<ExternalApiConfig> update(@PathVariable Long id, @RequestBody ExternalApiConfig config) {
        return Result.success(apiConfigService.update(id, config));
    }

    @Operation(summary = "删除接口配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        apiConfigService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取接口配置详情")
    @GetMapping("/{id}")
    public Result<ExternalApiConfig> getById(@PathVariable Long id) {
        return Result.success(apiConfigService.getById(id));
    }

    @Operation(summary = "接口配置列表")
    @GetMapping
    public Result<Page<ExternalApiConfig>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String apiCode,
            @RequestParam(required = false) String apiName) {
        return Result.success(apiConfigService.list(pageNum, pageSize, tenantId, apiCode, apiName));
    }

    @Operation(summary = "获取接口参数配置列表")
    @GetMapping("/{id}/params")
    public Result<List<ApiParamConfig>> getParams(@PathVariable Long id) {
        return Result.success(apiConfigService.getParamConfigs(id));
    }

    @Operation(summary = "创建参数配置")
    @PostMapping("/{configId}/params")
    public Result<ApiParamConfig> createParam(@PathVariable Long configId, @Valid @RequestBody ApiParamConfig param) {
        param.setConfigId(configId);
        return Result.success(apiConfigService.createParam(param));
    }

    @Operation(summary = "更新参数配置")
    @PutMapping("/params/{id}")
    public Result<ApiParamConfig> updateParam(@PathVariable Long id, @RequestBody ApiParamConfig param) {
        return Result.success(apiConfigService.updateParam(id, param));
    }

    @Operation(summary = "删除参数配置")
    @DeleteMapping("/params/{id}")
    public Result<Void> deleteParam(@PathVariable Long id) {
        apiConfigService.deleteParam(id);
        return Result.success();
    }
}
