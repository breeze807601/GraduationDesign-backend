package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.common.Result;
import com.example.backend.pojo.entity.Admin;
import com.example.backend.mapper.AdminMapper;
import com.example.backend.pojo.dto.AdminPwDTO;
import com.example.backend.service.IAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.utils.EncryptionUtil;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 管理员表 服务实现类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {
    @Override
    public Result<String> login(Admin a) {
        Admin admin = super.lambdaQuery()
                .eq(Admin::getUsername, a.getUsername()).one();
        if(admin == null) {
            return Result.error("用户名不存在！");
        }
        if (EncryptionUtil.checkPassword(a.getPassword(),admin.getPassword())) {
            StpUtil.login(admin.getId());
            return Result.success("登陆成功！");
        }
        return Result.error("密码错误！");
    }
    @Override
    public Result<String> updatePw(AdminPwDTO dto) {
        Admin admin = super.getById(dto.getId());
        if (!dto.getNewPw().equals(dto.getConfirmPw())) {
            return Result.error("两次密码不一致！");
        }
        if (!EncryptionUtil.checkPassword(dto.getOldPw(),admin.getPassword())) {
            return Result.error("原密码错误！");
        }
        if (EncryptionUtil.checkPassword(dto.getNewPw(),admin.getPassword())) {
            return Result.error("新密码不能与原密码相同！");
        }
        admin.setPassword(EncryptionUtil.encrypt(dto.getNewPw()));
        super.updateById(admin);
        return Result.success("修改成功！");
    }
}
