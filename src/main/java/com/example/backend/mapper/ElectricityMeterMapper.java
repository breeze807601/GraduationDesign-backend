package com.example.backend.mapper;

import com.example.backend.pojo.entity.ElectricityMeter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.pojo.excelVo.MeterExcel;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 用电记录表 Mapper 接口
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface ElectricityMeterMapper extends BaseMapper<ElectricityMeter> {
    // 查询需要导出的数据，在时间区间内，该套房最新一条用电记录
    List<MeterExcel> selectExcel(LocalDate now, LocalDate lastMonth);
    List<Long> checkTheCreditLimit();
}
