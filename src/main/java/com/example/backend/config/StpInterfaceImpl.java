package com.example.backend.config;

import cn.dev33.satoken.stp.StpInterface;
import com.example.backend.pojo.entity.Admin;
import com.example.backend.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    private final IAdminService adminService;
    private final StringRedisTemplate redisTemplate;
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return null;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String role = redisTemplate.opsForValue().get(loginId+"_role");
        if (role != null) {
            return List.of(role);
        }
        Admin admin = adminService.getById(Long.parseLong(loginId.toString()));
        if (admin != null) {
            redisTemplate.opsForValue().set(loginId+"_role", "admin");
            return List.of("admin");
        }
        // 否则为住户
        redisTemplate.opsForValue().set(loginId+"_role", "user");
        return List.of("user");
    }
}
