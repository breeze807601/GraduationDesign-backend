package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.backend.common.Result;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.ElectricityBill;
import com.example.backend.pojo.query.BillQuery;
import com.example.backend.pojo.vo.BillVo;
import com.example.backend.pojo.vo.PieChartVo;
import com.example.backend.service.IElectricityBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用电账单 前端控制器
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Tag(name = "用电账单相关")
@RequiredArgsConstructor
@RestController
@RequestMapping("/electricityBill")
public class ElectricityBillController {
    private final IElectricityBillService electricityBillService;
    @Operation(summary = "自动生成用电账单")
    @SaCheckRole("admin")
    @PostMapping("save")
    public Result<String> save(@RequestParam("now")LocalDate now) {
        electricityBillService.mySave(now);
        return Result.success("保存成功!");
    }
    @Operation(summary = "获取账单")
    @SaCheckRole("admin")
    @GetMapping("list")
    public Result<PageDTO<BillVo>> list(BillQuery billQuery) {
        return Result.success(electricityBillService.getPage(billQuery));
    }
    @Operation(summary = "导出本月账单")
    @SaCheckRole("admin")
    @GetMapping("export")
    public void export(HttpServletResponse response) throws Exception {
        electricityBillService.export(response);
    }
    @Operation(summary = "通知余额不足住户")
    @SaCheckRole("admin")
    @PostMapping("noticeOfInsufficientBalance")
    public Result<String> noticeOfInsufficientBalance() throws Exception {
        return electricityBillService.noticeOfInsufficientBalance();
    }
    @Operation(summary = "通知住户缴费")
    @SaCheckRole("admin")
    @PostMapping("notifyPayment")
    public Result<String> notifyPayment() throws Exception {
        return electricityBillService.notifyPayment();
    }
    @Operation(summary = "统计时间段内每月用电量")
    @SaCheckRole("admin")
    @GetMapping("getMonthlyUsage")
    public Result<Map<String, Object>> getMonthlyUsage(LocalDate start, LocalDate end) {
        return Result.success(electricityBillService.getMonthlyUsage(start, end));
    }
    @Operation(summary = "每月用电费用统计和平均值")
    @SaCheckRole("admin")
    @GetMapping("getCostStatistics")
    public Result<Map<String, Object>> getCostStatistics(LocalDate start, LocalDate end) {
        return Result.success(electricityBillService.getCostStatistics(start,end));
    }
    @Operation(summary = "统计上月账单状态饼图")
    @SaCheckRole("admin")
    @GetMapping("getBillStatusPieChart")
    public Result<List<PieChartVo>> getBillStatusPieChart() {
        return Result.success(electricityBillService.getBillStatusPieChart());
    }
    @Operation(summary = "上月用电量统计")
    @SaCheckRole("admin")
    @GetMapping("count")
    public Result<BigDecimal> count() {
        return Result.success(electricityBillService.myCount());
    }

}
