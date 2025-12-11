package com.qdq.config;

import cn.dev33.satoken.stp.StpInterface;
import com.qdq.mapper.SysUserMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;

    public StpInterfaceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return sysUserMapper.selectPermissionCodesByUserId(userId);
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return sysUserMapper.selectRoleCodesByUserId(userId);
    }
}
