package com.example.adapter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adapter.common.constant.StatusConstant;
import com.example.adapter.common.exception.BusinessException;
import com.example.adapter.common.exception.ErrorCode;
import com.example.adapter.entity.ApiParamConfig;
import com.example.adapter.entity.ExternalApiConfig;
import com.example.adapter.entity.Tenant;
import com.example.adapter.repository.ApiParamConfigMapper;
import com.example.adapter.repository.ExternalApiConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiConfigService {

    private final ExternalApiConfigMapper apiConfigMapper;
    private final ApiParamConfigMapper paramConfigMapper;
    private final TenantService tenantService;

    public ExternalApiConfig getByTenantAndApiCode(Long tenantId, String apiCode) {
        LambdaQueryWrapper<ExternalApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExternalApiConfig::getTenantId, tenantId)
                .eq(ExternalApiConfig::getApiCode, apiCode);
        return apiConfigMapper.selectOne(wrapper);
    }

    public ExternalApiConfig getActiveConfig(String tenantCode, String apiCode) {
        Tenant tenant = tenantService.getActiveTenant(tenantCode);
        ExternalApiConfig config = getByTenantAndApiCode(tenant.getId(), apiCode);
        if (config == null) {
            throw new BusinessException(ErrorCode.API_CONFIG_NOT_FOUND);
        }
        if (config.getStatus() != StatusConstant.ENABLED) {
            throw new BusinessException(ErrorCode.API_CONFIG_DISABLED);
        }
        return config;
    }

    public List<ApiParamConfig> getParamConfigs(Long configId) {
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getConfigId, configId)
                .orderByAsc(ApiParamConfig::getSortOrder);
        return paramConfigMapper.selectList(wrapper);
    }

    @Transactional
    public ExternalApiConfig create(ExternalApiConfig config) {
        tenantService.getById(config.getTenantId());
        ExternalApiConfig existing = getByTenantAndApiCode(config.getTenantId(), config.getApiCode());
        if (existing != null) {
            throw new BusinessException(ErrorCode.API_CODE_EXISTS);
        }
        config.setStatus(StatusConstant.ENABLED);
        apiConfigMapper.insert(config);
        return config;
    }

    @Transactional
    public ExternalApiConfig update(Long id, ExternalApiConfig config) {
        ExternalApiConfig existing = apiConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.API_CONFIG_NOT_FOUND);
        }
        if (StringUtils.hasText(config.getApiCode())
                && !config.getApiCode().equals(existing.getApiCode())) {
            ExternalApiConfig codeExist = getByTenantAndApiCode(existing.getTenantId(), config.getApiCode());
            if (codeExist != null) {
                throw new BusinessException(ErrorCode.API_CODE_EXISTS);
            }
            existing.setApiCode(config.getApiCode());
        }
        if (StringUtils.hasText(config.getApiName())) {
            existing.setApiName(config.getApiName());
        }
        if (StringUtils.hasText(config.getApiUrl())) {
            existing.setApiUrl(config.getApiUrl());
        }
        if (StringUtils.hasText(config.getHttpMethod())) {
            existing.setHttpMethod(config.getHttpMethod());
        }
        if (StringUtils.hasText(config.getContentType())) {
            existing.setContentType(config.getContentType());
        }
        if (config.getTimeout() != null) {
            existing.setTimeout(config.getTimeout());
        }
        if (config.getRetryCount() != null) {
            existing.setRetryCount(config.getRetryCount());
        }
        if (config.getDescription() != null) {
            existing.setDescription(config.getDescription());
        }
        if (config.getStatus() != null) {
            existing.setStatus(config.getStatus());
        }
        apiConfigMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        ExternalApiConfig existing = apiConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.API_CONFIG_NOT_FOUND);
        }
        apiConfigMapper.deleteById(id);
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getConfigId, id);
        paramConfigMapper.delete(wrapper);
    }

    public Page<ExternalApiConfig> list(int pageNum, int pageSize, Long tenantId, String apiCode, String apiName) {
        Page<ExternalApiConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExternalApiConfig> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(ExternalApiConfig::getTenantId, tenantId);
        }
        if (StringUtils.hasText(apiCode)) {
            wrapper.like(ExternalApiConfig::getApiCode, apiCode);
        }
        if (StringUtils.hasText(apiName)) {
            wrapper.like(ExternalApiConfig::getApiName, apiName);
        }
        wrapper.orderByDesc(ExternalApiConfig::getCreateTime);
        return apiConfigMapper.selectPage(page, wrapper);
    }

    public ExternalApiConfig getById(Long id) {
        ExternalApiConfig config = apiConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ErrorCode.API_CONFIG_NOT_FOUND);
        }
        return config;
    }

    public ApiParamConfig createParam(ApiParamConfig param) {
        getById(param.getConfigId());
        paramConfigMapper.insert(param);
        return param;
    }

    public ApiParamConfig updateParam(Long id, ApiParamConfig param) {
        ApiParamConfig existing = paramConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PARAM_CONFIG_NOT_FOUND);
        }
        if (StringUtils.hasText(param.getParamType())) {
            existing.setParamType(param.getParamType());
        }
        if (StringUtils.hasText(param.getParamKey())) {
            existing.setParamKey(param.getParamKey());
        }
        if (param.getParamValue() != null) {
            existing.setParamValue(param.getParamValue());
        }
        if (StringUtils.hasText(param.getValueSource())) {
            existing.setValueSource(param.getValueSource());
        }
        if (param.getValueExpression() != null) {
            existing.setValueExpression(param.getValueExpression());
        }
        if (param.getRequired() != null) {
            existing.setRequired(param.getRequired());
        }
        if (param.getDescription() != null) {
            existing.setDescription(param.getDescription());
        }
        if (param.getSortOrder() != null) {
            existing.setSortOrder(param.getSortOrder());
        }
        paramConfigMapper.updateById(existing);
        return existing;
    }

    public void deleteParam(Long id) {
        ApiParamConfig existing = paramConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PARAM_CONFIG_NOT_FOUND);
        }
        paramConfigMapper.deleteById(id);
    }
}
