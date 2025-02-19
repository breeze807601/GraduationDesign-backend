package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.enums.SMSCodeEnum;
import com.example.backend.common.enums.StatusEnum;
import com.example.backend.mapper.*;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.*;
import com.example.backend.pojo.query.BillQuery;
import com.example.backend.pojo.vo.BillSMSVo;
import com.example.backend.pojo.vo.BillVo;
import com.example.backend.service.IWaterBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.utils.SendSMSUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用水账单 服务实现类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Service
@RequiredArgsConstructor
public class WaterBillServiceImpl extends ServiceImpl<WaterBillMapper, WaterBill> implements IWaterBillService {

    private final WaterMeterMapper waterMeterMapper;
    private final TariffMapper tariffMapper;
    private final BuildingMapper buildingMapper;
    private final UserMapper userMapper;
    @Override
    public void mySave(LocalDate now) {
        Tariff tariff = tariffMapper.selectOne(new LambdaQueryWrapper<Tariff>().eq(Tariff::getName, 0));
        LambdaQueryWrapper<WaterMeter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterMeter::getTime, now);
        List<WaterMeter> waterMeters =  waterMeterMapper.selectList(wrapper);

        List<WaterBill> list = new ArrayList<>();
        for (WaterMeter waterMeter : waterMeters) {
            BigDecimal summation = waterMeter.getReading().subtract(waterMeter.getPreviousReading());
            WaterBill waterBill = new WaterBill()
                    .setTime(now)
                    .setWaterMeterId(waterMeter.getId())
                    .setSummation(summation)
                    .setPrice(tariff.getPrice())
                    .setCost(summation.multiply(tariff.getPrice()))
                    .setUserId(waterMeter.getUserId())
                    .setBuildingId(waterMeter.getBuildingId());
            list.add(waterBill);
        }
        super.saveBatch(list);
    }

    @Transactional
    @Override
    public PageDTO<BillVo> getPage(BillQuery q) {
        List<Long> buildingIds = buildingMapper.getIdList(q.getBuildingNum(), q.getFloor(), q.getDoorplate());
        List<Long> userIds = userMapper.getIds(q.getName());
        LambdaQueryWrapper<WaterBill> wrapper = new LambdaQueryWrapper<>();
        if (q.getTime() != null) {
            wrapper.like(WaterBill::getTime, YearMonth.from(q.getTime()));
        }
        wrapper.in(buildingIds!=null, WaterBill::getBuildingId,buildingIds)
                .in(userIds!=null, WaterBill::getUserId,userIds)
                .eq(q.getStatus()!=null,WaterBill::getStatus,q.getStatus())
                .orderByDesc(WaterBill::getTime)
                .orderByAsc(WaterBill::getBuildingId);
        Page<WaterBill> page = super.page(q.toMpPage(), wrapper);
        List<BillVo> vos = page.getRecords().stream().map(this::getBillVo).toList();
        return new PageDTO<>(page.getTotal(), page.getPages(), vos);
    }
    @Override
    @Transactional
    public void automaticPayment() {
        try {
            // 待支付的账单
            List<WaterBill> bills = super.list(new LambdaQueryWrapper<WaterBill>()
                    .eq(WaterBill::getStatus, StatusEnum.PAYMENT_IN_PROGRESS.getCode()));
            // 用户
            Map<Long, Map<String, Object>> userMap = userMapper.getUserMap(false);
            List<WaterBill> billList = new ArrayList<>();
            List<User> userList = new ArrayList<>();
            for (WaterBill bill : bills) {
                Long userId = bill.getUserId();
                BigDecimal balance = new BigDecimal(userMap.get(userId).get("balance").toString());
                if (balance.compareTo(bill.getCost()) < 0) {  // 余额不足
                    bill.setStatus(StatusEnum.INSUFFICIENT_BALANCE);
                    billList.add(bill);
                    continue;
                }
                if (bill.getCost().compareTo(new BigDecimal("70")) < 0) {
                    // 账单金额小于200元，则为小额，自动扣款，并将要修改的值封装到list中
                    userList.add(new User()
                            .setId(userId).setBalance(balance.subtract(bill.getCost())));
                    bill.setStatus(StatusEnum.PAID_IN);
                    billList.add(bill);
                }
            }
            if (!userList.isEmpty()) {
                userMapper.updateBatch(userList);
            }
            super.updateBatchById(billList);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void paidSMSNotification() throws Exception {
        List<WaterBill> list = super.list(new LambdaQueryWrapper<WaterBill>()
                .eq(WaterBill::getStatus, StatusEnum.PAID_IN.getCode()));
        Map<Long, Map<String, Object>> userMap = userMapper.getUserMap(true);
        for (WaterBill bill : list) {
            String phone = userMap.get(bill.getUserId()).get("phone").toString();
            BillSMSVo billSMSVo = new BillSMSVo()
                    .setName(userMap.get(bill.getUserId()).get("name").toString())
                    .setTime(bill.getTime().toString())
                    .setPrice(bill.getPrice())
                    .setSummation(bill.getSummation())
                    .setCost(bill.getCost());
            SendSMSUtil.paidReminder(phone, billSMSVo, SMSCodeEnum.REMINDER_OF_PAID_ELECTRICITY.getCode());
        }
    }

    @Override
    public List<User> getUserPhoneWithName(Integer code) {  // 根据code获取不同状态的住户列表
        return super.getBaseMapper().getUserPhoneWithName(code);
    }
    @Transactional
    public BillVo getBillVo(WaterBill w) {
        BillVo billVo = BeanUtil.copyProperties(w, BillVo.class);
        User user = userMapper.selectById(w.getUserId());
        Building building = buildingMapper.selectById(w.getBuildingId());
        WaterMeter waterMeter = waterMeterMapper.selectById(w.getWaterMeterId());
        billVo.setBuildingNum(building.getBuildingNum())
                .setFloor(building.getFloor())
                .setDoorplate(building.getDoorplate())
                .setName(user.getName())
                .setMeterId(waterMeter.getId())
                .setPreviousReading(waterMeter.getPreviousReading())
                .setReading(waterMeter.getReading());
        return billVo;
    }
}
