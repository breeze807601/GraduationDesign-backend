package com.example.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.enums.SMSCodeEnum;
import com.example.backend.common.enums.StatusEnum;
import com.example.backend.pojo.entity.ElectricityMeter;
import com.example.backend.pojo.entity.User;
import com.example.backend.service.*;
import com.example.backend.utils.SendSMSUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class Task {
    private final IUserService userService;
    private final IElectricityMeterService electricityMeterService;
    private final IWaterMeterService waterMeterService;

    /**
     * smallAutomaticRecharge()方法后再运行checkBalance()
     * smallAutomaticRecharge()小额充值后可用额度不足的，则发送短信,足够的则扣除额度
     * 然后，checkBalance()给余额不足的发短信
     */
    @Transactional
    @Scheduled(cron = "0 0 8 * * ?")
    public void smallAutomaticRecharge() {
        try {
            List<User> electricityList = electricityMeterService.smallAutomaticRecharge();
            List<User> waterList = waterMeterService.smallAutomaticRecharge();
            // 检查是否为空
            if (electricityList == null) {
                electricityList = Collections.emptyList();
            }
            if (waterList == null) {
                waterList = Collections.emptyList();
            }
            electricityList.addAll(waterList);
            Set<User> set = new HashSet<>(electricityList);
            List<User> list = new ArrayList<>(set);
            // 去重,减少短信次数
            for (User user : list) {
                SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), "SMS_480625070");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Scheduled(cron = "0 10 8 * * ?")  //每天早上8点10分执行
    public void checkBalance() {
        try {
            List<User> list = userService.list(new LambdaQueryWrapper<User>()
                    .le(User::getBalance,50)
                    .eq(User::getDeleted,0));
            // 余额小于50的短信通知住户
            for (User user : list) {
                SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), "SMS_480530041");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Scheduled(cron = "0 0 9 * * ?")  //每天早上9点执行
    @Transactional
    public void noticeOfInsufficientCreditLimit() {
        try {
            List<Long> electricityMeters = electricityMeterService.checkTheCreditLimit();
            List<Long> waterMeters = waterMeterService.checkTheCreditLimit();
            if (electricityMeters == null) {
                electricityMeters = Collections.emptyList();
            }
            if (waterMeters == null) {
                waterMeters = Collections.emptyList();
            }
            electricityMeters.addAll(waterMeters);
            Set<Long> set = new HashSet<>(electricityMeters);
            List<Long> ids = new ArrayList<>(set);
            List<User> list = userService.lambdaQuery().in(User::getId, ids).list();
            // 可用额度小于临界值的,提前通知
            for (User user : list) {
                SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), "SMS_480400158");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /*@Scheduled(cron = "0 0 2 1 * ?")  //每月一号凌晨2点执行一次
    public void automaticDeductionOfWaterBills() {
        try {
            waterBillService.automaticPayment();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
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
            // 去重,减少短信次数
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
            // 去重,减少短信次数
            Set<User> set = new HashSet<>(list1);
            List<User> paymentnoticeList = new ArrayList<>(set);
            for (User user : paymentnoticeList) {
                SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), SMSCodeEnum.INSUFFICIENT_BALANCE.getCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/
}
