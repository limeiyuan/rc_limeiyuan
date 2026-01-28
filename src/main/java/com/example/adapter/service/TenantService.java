package com.example.adapter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adapter.common.constant.StatusConstant;
import com.example.adapter.common.exception.BusinessException;
import com.example.adapter.common.exception.ErrorCode;
import com.example.adapter.entity.Tenant;
import com.example.adapter.repository.TenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;

    public Tenant getByTenantCode(String tenantCode) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getTenantCode, tenantCode);
        return tenantMapper.selectOne(wrapper);
    }

    public Tenant getActiveTenant(String tenantCode) {
        Tenant tenant = getByTenantCode(tenantCode);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        if (tenant.getStatus() != StatusConstant.ENABLED) {
            throw new BusinessException(ErrorCode.TENANT_DISABLED);
        }
        return tenant;
    }

    public Tenant create(Tenant tenant) {
        Tenant existing = getByTenantCode(tenant.getTenantCode());
        if (existing != null) {
            throw new BusinessException(ErrorCode.TENANT_CODE_EXISTS);
        }
        tenant.setStatus(StatusConstant.ENABLED);
        tenantMapper.insert(tenant);
        return tenant;
    }

    public Tenant update(Long id, Tenant tenant) {
        Tenant existing = tenantMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        if (StringUtils.hasText(tenant.getTenantCode())
                && !tenant.getTenantCode().equals(existing.getTenantCode())) {
            Tenant codeExist = getByTenantCode(tenant.getTenantCode());
            if (codeExist != null) {
                throw new BusinessException(ErrorCode.TENANT_CODE_EXISTS);
            }
            existing.setTenantCode(tenant.getTenantCode());
        }
        if (StringUtils.hasText(tenant.getTenantName())) {
            existing.setTenantName(tenant.getTenantName());
        }
        if (tenant.getDescription() != null) {
            existing.setDescription(tenant.getDescription());
        }
        if (tenant.getStatus() != null) {
            existing.setStatus(tenant.getStatus());
        }
        tenantMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        Tenant existing = tenantMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        tenantMapper.deleteById(id);
    }

    public Page<Tenant> list(int pageNum, int pageSize, String tenantCode, String tenantName) {
        Page<Tenant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tenantCode)) {
            wrapper.like(Tenant::getTenantCode, tenantCode);
        }
        if (StringUtils.hasText(tenantName)) {
            wrapper.like(Tenant::getTenantName, tenantName);
        }
        wrapper.orderByDesc(Tenant::getCreateTime);
        return tenantMapper.selectPage(page, wrapper);
    }

    public Tenant getById(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        }
        return tenant;
    }
}
