package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.backend.common.Result;
import com.example.backend.service.IBuildingService;
import com.example.backend.pojo.vo.BuildingOptionsVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 楼房表 前端控制器
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Tag(name = "楼房相关")
@RequiredArgsConstructor
@RestController
@RequestMapping("/building")
public class BuildingController {
    private final IBuildingService buildingService;
    @Operation(summary = "导入")
    @SaCheckRole("admin")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile multipartFile) {
        return buildingService.upload(multipartFile);
    }
    @Operation(summary = "获取楼房选项")
    @SaCheckRole("admin")
    @GetMapping("/getBuildingOptions")
    public Result<List<BuildingOptionsVo>> getBuildingOptions(Long id) {
        return buildingService.getBuildingOptions(id);
    }
    @Operation(summary = "返回楼房id")
    @SaCheckRole("admin")
    @GetMapping("/getBuildingId")
    public Result<Long> getBuildingId(String buildingNum, String floor, String doorplate) {
        return buildingService.getBuildingId(buildingNum, floor, doorplate);
    }
}
