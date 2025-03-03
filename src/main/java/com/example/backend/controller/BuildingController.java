package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.Result;
import com.example.backend.pojo.entity.Building;
import com.example.backend.service.IBuildingService;
import com.example.backend.pojo.vo.BuildingOptionsVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
    @Operation(summary = "导出")
    @SaCheckRole("admin")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        buildingService.export(response);
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
    @Operation(summary = "楼房列表")
    @SaCheckRole("admin")
    @GetMapping("/list")
    public Result<List<Building>> list(Building building) {
        LambdaQueryWrapper<Building> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(building.getId() != null, Building::getId, building.getId())
                .eq(building.getBuildingNum() != null, Building::getBuildingNum, building.getBuildingNum())
                .eq(building.getFloor() != null, Building::getFloor, building.getFloor())
                .eq(building.getDoorplate() != null, Building::getDoorplate, building.getDoorplate())
                .orderByAsc(Building::getId);
        return Result.success(buildingService.list(queryWrapper));
    }
}
