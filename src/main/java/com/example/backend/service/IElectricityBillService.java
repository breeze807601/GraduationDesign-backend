package com.example.backend.service;

import com.example.backend.common.Result;
import com.example.backend.pojo.entity.ElectricityBill;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.User;
import com.example.backend.pojo.query.BillQuery;
import com.example.backend.pojo.vo.BillVo;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用电账单 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface IElectricityBillService extends IService<ElectricityBill> {
    void mySave(LocalDate now);
    PageDTO<BillVo> getPage(BillQuery billQuery);
    List<User> getUserPhoneWithName(Integer status);
    void export(HttpServletResponse response,LocalDate startTime, LocalDate endTime) throws Exception;
    Map<String, Object> electricityStatistics(LocalDate start, LocalDate end, Boolean isUser);
    Map<String, Object> getCostStatistics(LocalDate start, LocalDate end, Boolean isUser);
    BigDecimal myCount();
    Result<String> notifyRecharge() throws Exception;
}