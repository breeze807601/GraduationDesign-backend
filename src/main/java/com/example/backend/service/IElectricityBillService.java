package com.example.backend.service;

import com.example.backend.pojo.entity.ElectricityBill;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.query.BillQuery;
import com.example.backend.pojo.vo.BillSMSVo;
import com.example.backend.pojo.vo.BillVo;

import java.time.LocalDate;

/**
 * <p>
 * 用电账单 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface IElectricityBillService extends IService<ElectricityBill> {
    void mySave(LocalDate now);
    PageDTO<BillVo> getPage(BillQuery billQuery);
    void automaticPayment();
    void SMSNotification() throws Exception;
    void sendSms(String phone, BillSMSVo billSMSVo) throws Exception;
}
