package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.Result;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.Building;
import com.example.backend.pojo.query.BuildingQuery;
import com.example.backend.pojo.vo.UserVo;
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
    @GetMapping("/page")
    public Result<PageDTO<Building>> getPage(BuildingQuery query) {
        return Result.success(buildingService.getPage(query));
    }
    @Operation(summary = "添加楼房")
    @SaCheckRole("admin")
    @PostMapping("/add")
    public Result<String> add(@RequestBody Building building) {
        boolean exists = buildingService.lambdaQuery().eq(Building::getBuildingNum, building.getBuildingNum())
                .eq(Building::getFloor, building.getFloor())
                .eq(Building::getDoorplate, building.getDoorplate()).exists();
        return exists ? Result.error(building.getBuildingNum()+"-"+building.getFloor()+"-"+building.getDoorplate()+"已存在") :
                buildingService.save(building) ? Result.success("添加成功") : Result.error("添加失败");
    }
    @Operation(summary = "修改楼房")
    @SaCheckRole("admin")
    @PutMapping("update")
    public Result<String> update(@RequestBody Building building) {
        boolean exists = buildingService.lambdaQuery().eq(Building::getBuildingNum, building.getBuildingNum())
                .eq(Building::getFloor, building.getFloor())
                .eq(Building::getDoorplate, building.getDoorplate()).exists();
        return exists ? Result.error(building.getBuildingNum()+"-"+building.getFloor()+"-"+building.getDoorplate()+"已存在") :
                buildingService.updateById(building) ? Result.success("添加成功") : Result.error("添加失败");
    }
    @Operation(summary = "删除楼房")
    @SaCheckRole("admin")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return buildingService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
    @Operation(summary = "楼房统计")
    @SaCheckRole("admin")
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(buildingService.count());
    }
}
