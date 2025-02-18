package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.example.backend.common.Result;
import com.example.backend.pojo.entity.Tariff;
import com.example.backend.service.ITariffService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 水电价格表 前端控制器
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Tag(name = "水电价格相关")
@RequiredArgsConstructor
@RestController
@RequestMapping("/tariff")
public class TariffController {
    private final ITariffService tariffService;

    @Schema(description = "获取水电价格")
    @SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)
    @GetMapping
    public Result<Tariff> getTariff(Integer name) {
        return Result.success(tariffService.lambdaQuery().eq(Tariff::getName, name).one());
    }
    @Schema(description = "修改水电价格")
    @SaCheckRole("admin")
    @PutMapping
    public Result<String> updateTariff(@RequestBody Tariff tariff) {
        tariffService.updateById(tariff);
        return Result.success("修改成功");
    }
}
