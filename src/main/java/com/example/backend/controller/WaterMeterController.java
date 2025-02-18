package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.backend.common.Result;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.query.MeterQuery;
import com.example.backend.pojo.vo.MeterVo;
import com.example.backend.service.IWaterMeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 用水记录表 前端控制器
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Tag(name = "用水记录相关")
@RequiredArgsConstructor
@RestController
@RequestMapping("/waterMeter")
public class WaterMeterController {
    private final IWaterMeterService waterMeterService;
    @Operation(summary = "获取用水记录列表")
    @SaCheckRole("admin")
    @GetMapping("list")
    public Result<PageDTO<MeterVo>> list(MeterQuery query) {
        return Result.success(waterMeterService.getPage(query));
    }

    @Operation(summary = "导出抄水表")
    @SaCheckRole("admin")
    @GetMapping("export")
    public void export(HttpServletResponse response) throws Exception {
        waterMeterService.export(response);
    }
    @Operation(summary = "导入")
    @SaCheckRole("admin")
    @PostMapping("upload")
    public Result<LocalDate> upload(@RequestParam("file") MultipartFile multipartFile) {
        return waterMeterService.upload(multipartFile);
    }
    @Operation(summary = "修改")
    @SaCheckRole("admin")
    @PutMapping("update")
    public Result<String> update(@RequestParam("id") Long id, @RequestParam("reading") BigDecimal reading) {
        waterMeterService.updateWithReading(id, reading);
        return Result.success("修改成功");
    }
}
