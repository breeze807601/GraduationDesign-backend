package com.example.backend.task;

import com.example.backend.service.IElectricityBillService;
import com.example.backend.service.IWaterBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Task {
    private final IElectricityBillService electricityBillService;
    private final IWaterBillService waterBillService;
    @Scheduled(cron = "0 0 1 1 * ?")  //每月一号凌晨1点执行一次
    public void automaticDeductionOfElectricityBills() {
        try {
            // 自动在用户余额中扣除电费，仅限金额小于200元的账单
            electricityBillService.automaticPayment();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    @Scheduled(cron = "0 0 2 1 * ?")  //每月一号凌晨2点执行一次
    public void automaticDeductionOfWaterBills() {
        try {
            // 自动在用户余额中扣除水费，仅限金额小于200元的账单
            waterBillService.automaticPayment();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    @Scheduled(cron = "0 0 3 1 * ?")  //每月一号凌晨3点执行一次
    public void SMSNotification() {
        // 发起短信通知，通知已支付的住户
        try {
            // 发送电费通知
            electricityBillService.SMSNotification();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
