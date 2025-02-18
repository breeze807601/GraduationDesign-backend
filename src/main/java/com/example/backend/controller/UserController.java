package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.common.Result;
import com.example.backend.pojo.entity.User;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.query.UserQuery;
import com.example.backend.pojo.vo.UserVo;
import com.example.backend.service.IUserService;
import com.example.backend.utils.EncryptionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 住户表 前端控制器
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Tag(name = "住户管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private final IUserService userService;
    @SaIgnore
    @Operation(summary = "住户登录")
    @PostMapping("login")
    public Result<String> login(@RequestBody User user) {
        return userService.login(user);
    }
    @Operation(summary = "住户登出")
    @PostMapping("logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("登出成功！");
    }

    @Operation(summary = "住户保存")
    @SaCheckRole("admin")
    @PostMapping("save")
    public Result<String> register(@RequestBody User user) {
        userService.saveWithMeter(user);
        return Result.success("保存成功！");
    }

    @Operation(summary = "获取用户信息")
    @SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)
    @GetMapping("getUserInfo")
    public Result<UserVo> getUserInfo(Long id) {
        return Result.success(userService.getUserVo(userService.getById(id)));
    }

    @Operation(summary = "修改用户信息")
    @SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)
    @PutMapping("update")
    public Result<String> update(@RequestBody User user) {
        userService.updateWithBuilding(user);
        return Result.success("修改成功");
    }

    @Operation(summary = "用户列表")
    @SaCheckRole("admin")
    @GetMapping("list")
    public Result<PageDTO<UserVo>> list(UserQuery userQuery) {
        return Result.success(userService.getUserPage(userQuery));
    }

    @Operation(summary = "删除用户")
    @SaCheckRole("admin")
    @DeleteMapping("delete")
    public Result<String> delete(Long id) {
        userService.removeById(id);
        return Result.success("删除成功！");
    }

    @Operation(summary = "重置密码")
    @SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)
    @PutMapping("resetPassword")
    public Result<String> resetPassword(Long id) {
        User user = userService.getById(id);
        user.setPassword(EncryptionUtil.encrypt("111111"));
        userService.updateById(user);
        return Result.success("重置密码成功！");
    }

    @Operation(summary = "住户导出")
    @SaCheckRole("admin")
    @GetMapping("export")
    public void export(HttpServletResponse response) throws Exception {
        userService.export(response);
    }
}
