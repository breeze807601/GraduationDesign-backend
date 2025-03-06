package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.backend.common.Result;
import com.example.backend.common.enums.SMSCodeEnum;
import com.example.backend.pojo.dto.ForgotPwDTO;
import com.example.backend.pojo.dto.UpdatePhoneDTO;
import com.example.backend.pojo.entity.Admin;
import com.example.backend.pojo.dto.ChangePwDTO;
import com.example.backend.pojo.entity.User;
import com.example.backend.service.IAdminService;
import com.example.backend.utils.EncryptionUtil;
import com.example.backend.utils.SendSMSUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private final StringRedisTemplate redisTemplate;
    @SaIgnore
    @Operation(summary = "管理员登录")
    @PostMapping("login")
    public Result<String> login(@RequestBody Admin admin) {
        return adminService.login(admin);
    }
    @Operation(summary = "管理员登出")
    @GetMapping("logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("登出成功！");
    }
    @Operation(summary = "获取当前信息")
    @SaCheckRole("admin")
    @GetMapping("info")
    public Result<Admin> info() {
        Long id =  Long.parseLong(StpUtil.getLoginId().toString());
        Admin admin = adminService.getById(id);
        admin.setPassword("");
        return Result.success(admin);
    }
    @Operation(summary = "获取管理员信息")
    @SaCheckRole("admin")
    @GetMapping("getAdminInfo")
    public Result<Admin> getAdminInfo(Long id) {
        Admin admin = adminService.getById(id);
        admin.setPassword("");
        return Result.success(admin);
    }
    @Operation(summary = "修改密码")
    @SaCheckRole("admin")
    @PutMapping("updatePassword")
    public Result<String> updatePw(@RequestBody ChangePwDTO dto) {
        return adminService.updatePw(dto);
    }
    @Operation(summary = "获取验证码")
    @SaIgnore
    @GetMapping("getCode")
    public Result<String> getCode(String phone) throws Exception {
        String code = RandomUtil.randomNumbers(6);
        Admin admin = adminService.lambdaQuery().eq(Admin::getPhone, phone).one();
        if (admin == null) {
            return Result.error("住户不存在！");
        }
        // 发送短信验证码
        SendSMSUtil.sendPaymentNotice(phone,code, SMSCodeEnum.VERIFICATION_CODE.getCode());
        // 将验证码存储到Redis中，设置过期时间为5分钟（300秒）
        redisTemplate.opsForValue().set(admin.getId() + "_code", code, 600, TimeUnit.SECONDS);
        return Result.success("验证码已发送");
    }
    @Operation(summary = "忘记密码")
    @SaIgnore
    @PutMapping("forgetPassword")
    public Result<String> forgetPassword(@RequestBody ForgotPwDTO forgotPwDTO) {
        Admin admin = adminService.lambdaQuery().eq(Admin::getPhone, forgotPwDTO.getPhone()).one();
        if (admin == null) {
            return Result.error("住户不存在！");
        }
        String code = redisTemplate.opsForValue().get(admin.getId() + "_code");
        if (code == null) {
            return Result.error("验证码已过期！");
        }
        if (!code.equals(forgotPwDTO.getCode())) {
            return Result.error("验证码错误！");
        }
        if (!forgotPwDTO.getNewPw().equals(forgotPwDTO.getConfirmPw())) {
            return Result.error("两次密码不一致！");
        }
        // 加密
        admin.setPassword(EncryptionUtil.encrypt(forgotPwDTO.getNewPw()));
        adminService.updateById(admin);
        return Result.success("修改成功！");
    }
    @Operation(summary = "修改电话")
    @SaCheckRole("admin")
    @PutMapping("updatePhone")
    public Result<String> updatePhone(@RequestBody UpdatePhoneDTO dto) {
        String code = redisTemplate.opsForValue().get(dto.getId() + "_code");
        if (code == null) {
            return Result.error("验证码已过期！");
        }
        if (!code.equals(dto.getCode())) {
            return Result.error("验证码错误！");
        }
        if (adminService.lambdaQuery().eq(Admin::getPhone, dto.getPhone()).exists()) {
            return Result.error("该手机号已存在，不可重复！");
        }
        Admin admin = adminService.getById(dto.getId())
                .setPhone(dto.getPhone());
        adminService.updateById(admin);
        return Result.success("修改成功！");
    }
    @Operation(summary = "获取管理员列表")
    @SaCheckRole("admin")
    @GetMapping("list")
    public Result<List<Admin>> list(Admin admin) {
        List<Admin> list = adminService.lambdaQuery().like(admin.getPhone() != null, Admin::getPhone, admin.getPhone())
                .like(admin.getUsername() != null, Admin::getUsername, admin.getUsername())
                .eq(admin.getPower() != null, Admin::getPower, admin.getPower())
                .orderByDesc(Admin::getPower).list();
        return Result.success(list);
    }
    @Operation(summary = "添加管理员")
    @SaCheckRole("superAdmin")
    @PostMapping("add")
    public Result<String> add(@RequestBody Admin admin) {
        if (adminService.lambdaQuery().eq(Admin::getPhone, admin.getPhone()).exists()) {
            return Result.error("该手机号已存在，不可重复！");
        }
        if (adminService.lambdaQuery().eq(Admin::getUsername, admin.getUsername()).exists()) {
            return Result.error("该用户名已存在，不可重复！");
        }
        admin.setPassword(EncryptionUtil.encrypt("123456"));
        adminService.save(admin);
        return Result.success("添加成功！");
    }
    @Operation(summary = "删除管理员")
    @SaCheckRole("superAdmin")
    @DeleteMapping("delete")
    public Result<String> delete(Long id) {
        adminService.removeById(id);
        return Result.success("删除成功！");
    }
    @Operation(summary = "修改管理员")
    @SaCheckRole("superAdmin")
    @PutMapping("update")
    public Result<String> update(@RequestBody Admin admin) {
        if (adminService.lambdaQuery().eq(Admin::getPhone, admin.getPhone()).ne(Admin::getId, admin.getId()).exists()) {
            return Result.error("该手机号已存在，不可重复！");
        }
        if (adminService.lambdaQuery().eq(Admin::getUsername, admin.getUsername()).ne(Admin::getId, admin.getId()).exists()) {
            return Result.error("该用户名已存在，不可重复！");
        }
        adminService.updateById(admin);
        return Result.success("修改成功！");
    }
    @Operation(summary = "重置密码")
    @SaCheckRole("admin")
    @PutMapping("resetPassword")
    public Result<String> resetPassword(Long id) {
        Admin admin = adminService.getById(id);
        admin.setPassword(EncryptionUtil.encrypt("123456"));
        adminService.updateById(admin);
        return Result.success("重置密码成功！");
    }
}
