package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.common.Result;
import com.example.backend.pojo.entity.Admin;
import com.example.backend.pojo.dto.AdminPwDTO;
import com.example.backend.service.IAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 管理员
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Tag(name = "管理员管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final IAdminService adminService;
    @SaIgnore  // 忽略此接口的认证
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody Admin admin) {
        return adminService.login(admin);
    }
    @Operation(summary = "管理员登出")
    @GetMapping("/logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("登出成功！");
    }
    @Operation(summary = "获取管理员信息")
    @GetMapping("/info")
    public Result<Admin> info() {
        Long id =  Long.parseLong(StpUtil.getLoginId().toString());
        Admin admin = adminService.getById(id);
        admin.setPassword("");
        return Result.success(admin);
    }
    @Operation(summary = "修改密码")
    @SaCheckRole("admin")
    @PutMapping("/updatePassword")
    public Result<String> updatePw(@RequestBody AdminPwDTO dto) {
        return adminService.updatePw(dto);
    }
}
