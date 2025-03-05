package com.example.backend.service;

import com.example.backend.common.Result;
import com.example.backend.pojo.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.dto.ChangePwDTO;

/**
 * <p>
 * 管理员表 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface IAdminService extends IService<Admin> {
    Result<String> login(Admin a);
    Result<String> updatePw(ChangePwDTO dto);
}
