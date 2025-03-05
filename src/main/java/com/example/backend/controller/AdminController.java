package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.backend.common.Result;
import com.example.backend.common.enums.SMSCodeEnum;
import com.example.backend.pojo.dto.ForgotPwDTO;
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
    @SaIgnore  // 忽略此接口的认证
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
    @Operation(summary = "获取管理员信息")
    @GetMapping("info")
    public Result<Admin> info() {
        Long id =  Long.parseLong(StpUtil.getLoginId().toString());
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
}
