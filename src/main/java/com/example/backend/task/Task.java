package com.example.backend.task;

import com.example.backend.common.enums.SMSCodeEnum;
import com.example.backend.common.enums.StatusEnum;
import com.example.backend.pojo.entity.User;
import com.example.backend.service.IElectricityBillService;
import com.example.backend.service.IWaterBillService;
import com.example.backend.utils.SendSMSUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Task {
    private final IElectricityBillService electricityBillService;
    private final IWaterBillService waterBillService;

    /**
     * 两个automaticPayment执行完毕
     * 住户订单分为三种：已支付，待支付，余额不足
     */
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
    @Scheduled(cron = "0 30 2 1 * ?")  //每月一号凌晨2点30执行一次
    public void SMSNotification() {
        try {
            // 发送水电账单已支付通知已支付用户
            electricityBillService.paidSMSNotification();
            waterBillService.paidSMSNotification();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Scheduled(cron = "0 0 3 1 * ?")    //每月一号凌晨3点执行一次
    public void paymentNotice() {
        try {
            // 缴费通知，即大额，且余额充足的住户
            List<User> list1 = electricityBillService.getUserPhoneWithName(StatusEnum.PAYMENT_IN_PROGRESS.getCode());
            list1.addAll(waterBillService.getUserPhoneWithName(StatusEnum.PAYMENT_IN_PROGRESS.getCode()));
            Set<User> set = new HashSet<>(list1);
            List<User> paymentnoticeList = new ArrayList<>(set);
            for (User user : paymentnoticeList) {
                SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), SMSCodeEnum.PAYMENT_NOTICE.getCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Scheduled(cron = "0 30 3 1 * ?")    //每月一号凌晨3点30执行一次
    public void insufficientBalance() {
        try {
            // 通知余额不足用户
            List<User> list1 = electricityBillService.getUserPhoneWithName(StatusEnum.INSUFFICIENT_BALANCE.getCode());
            list1.addAll(waterBillService.getUserPhoneWithName(StatusEnum.INSUFFICIENT_BALANCE.getCode()));
            Set<User> set = new HashSet<>(list1);
            List<User> paymentnoticeList = new ArrayList<>(set);
            for (User user : paymentnoticeList) {
                SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), SMSCodeEnum.INSUFFICIENT_BALANCE.getCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
