package com.example.backend.service;

import com.example.backend.common.Result;
import com.example.backend.pojo.entity.User;
import com.example.backend.pojo.entity.WaterBill;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.query.BillQuery;
import com.example.backend.pojo.vo.BillVo;
import com.example.backend.pojo.vo.PieChartVo;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用水账单 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface IWaterBillService extends IService<WaterBill> {
    void mySave(LocalDate now);
    PageDTO<BillVo> getPage(BillQuery billQuery);
    void automaticPayment();
    void paidSMSNotification() throws Exception;
    List<User> getUserPhoneWithName(Integer status);
    void export(HttpServletResponse response) throws Exception;
    Result<String> noticeOfInsufficientBalance() throws Exception;
    Result<String> notifyPayment() throws Exception;
    Map<String, Object> getMonthlyUsage(LocalDate start, LocalDate end);
    Map<String, Object> getCostStatistics(LocalDate start, LocalDate end);
    List<PieChartVo> getBillStatusPieChart();
    BigDecimal myCount();
}
