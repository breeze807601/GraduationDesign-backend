package com.example.backend.service;

import com.example.backend.common.Result;
import com.example.backend.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.query.UserQuery;
import com.example.backend.pojo.vo.UserVo;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * 住户表 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface IUserService extends IService<User> {
    Result<String> login(User u);
    PageDTO<UserVo> getUserPage(UserQuery userQuery);
    UserVo getUserVo(User user);
    void export(HttpServletResponse response) throws Exception;
    void saveWithMeter(User user);
    void updateWithBuilding(User user);
}
