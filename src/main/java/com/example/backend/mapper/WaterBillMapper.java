package com.example.backend.mapper;

import com.example.backend.pojo.entity.User;
import com.example.backend.pojo.entity.WaterBill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.pojo.excelVo.BillExcel;
import com.example.backend.pojo.vo.DataItem;
import com.example.backend.pojo.vo.PieChartVo;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 用水账单 Mapper 接口
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface WaterBillMapper extends BaseMapper<WaterBill> {

    List<User> getUserPhoneWithName(Integer code);
    List<BillExcel> selectExcel(LocalDate startOfMonth, LocalDate endOfMonth);
    List<DataItem> getSummation(LocalDate start, LocalDate end, Long userId);
    List<DataItem> getCostStatistics(LocalDate start, LocalDate end,Long userId);
}
