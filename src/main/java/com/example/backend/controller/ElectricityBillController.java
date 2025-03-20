package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.backend.common.Result;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.query.BillQuery;
import com.example.backend.pojo.vo.BillVo;
import com.example.backend.service.IElectricityBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Operation(summary = "导出账单")
    @SaCheckRole("admin")
    @GetMapping("export")
    public void export(HttpServletResponse response,LocalDate startTime, LocalDate endTime) throws Exception {
        electricityBillService.export(response,startTime,endTime);
    }
    @Operation(summary = "通知住户充值")
    @SaCheckRole("admin")
    @PostMapping("notifyRecharge")
    public Result<String> notifyRecharge() throws Exception {
        return electricityBillService.notifyRecharge();
    }
    @Operation(summary = "统计时间段内用电量")
    @SaCheckRole("admin")
    @GetMapping("electricityStatistics")
    public Result<Map<String, Object>> electricityStatistics(LocalDate start, LocalDate end) {
        return Result.success(electricityBillService.electricityStatistics(start, end));
    }
    @Operation(summary = "每月用电费用统计和平均值")
    @SaCheckRole("admin")
    @GetMapping("getCostStatistics")
    public Result<Map<String, Object>> getCostStatistics(LocalDate start, LocalDate end) {
        return Result.success(electricityBillService.getCostStatistics(start,end));
    }
    @Operation(summary = "本月用电量统计")
    @SaCheckRole("admin")
    @GetMapping("count")
    public Result<BigDecimal> count() {
        return Result.success(electricityBillService.myCount());
    }
}