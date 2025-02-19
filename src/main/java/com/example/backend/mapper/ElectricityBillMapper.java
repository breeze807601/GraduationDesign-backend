package com.example.backend.mapper;

import com.example.backend.pojo.entity.ElectricityBill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.pojo.entity.User;

import java.util.List;

/**
 * <p>
 * 用电账单 Mapper 接口
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface ElectricityBillMapper extends BaseMapper<ElectricityBill> {

    List<User> getUserPhoneWithName(Integer code);
}
