package com.example.backend.service;

import com.example.backend.pojo.entity.WaterBill;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.query.BillQuery;
import com.example.backend.pojo.vo.BillVo;

import java.time.LocalDate;

/**
 * <p>
 * 用水账单 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface IWaterBillService extends IService<WaterBill> {

    void mySave(LocalDate now);
    PageDTO<BillVo> getPage(BillQuery billQuery);
    void automaticPayment();
}
