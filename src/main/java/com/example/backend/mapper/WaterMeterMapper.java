package com.example.backend.mapper;

import com.example.backend.pojo.entity.WaterMeter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.pojo.excelVo.MeterExcel;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 用水记录表 Mapper 接口
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface WaterMeterMapper extends BaseMapper<WaterMeter> {

    List<MeterExcel> selectExcel(LocalDate now, LocalDate lastMonth);
}
