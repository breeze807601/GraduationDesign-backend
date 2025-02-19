package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.Result;
import com.example.backend.common.enums.SMSCodeEnum;
import com.example.backend.common.enums.StatusEnum;
import com.example.backend.mapper.*;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.*;
import com.example.backend.pojo.excelVo.BillExcel;
import com.example.backend.pojo.query.BillQuery;
import com.example.backend.pojo.vo.BillSMSVo;
import com.example.backend.pojo.vo.BillVo;
import com.example.backend.service.IElectricityBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.utils.SMSUtil;
import com.example.backend.utils.SendSMSUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用电账单 服务实现类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Service
@RequiredArgsConstructor
public class ElectricityBillServiceImpl extends ServiceImpl<ElectricityBillMapper, ElectricityBill> implements IElectricityBillService {

    private final ElectricityMeterMapper electricityMeterMapper;
    private final TariffMapper tariffMapper;
    private final BuildingMapper buildingMapper;
    private final UserMapper userMapper;
    @Transactional
    @Override
    public void mySave(LocalDate now) {
        Tariff tariff = tariffMapper.selectOne(new LambdaQueryWrapper<Tariff>().eq(Tariff::getName, 0));
        // 查询当天的电表数据
        LambdaQueryWrapper<ElectricityMeter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElectricityMeter::getTime, now);
        List<ElectricityMeter> electricityMeters = electricityMeterMapper.selectList(wrapper);

        List<ElectricityBill> list = new ArrayList<>();
        for (ElectricityMeter electricityMeter : electricityMeters) {
            // 电量
            BigDecimal summation = electricityMeter.getReading().subtract(electricityMeter.getPreviousReading());
            ElectricityBill electricityBill = new ElectricityBill()
                    .setTime(now)
                    .setElectricityMeterId(electricityMeter.getId())
                    .setSummation(summation)
                    .setPrice(tariff.getPrice())
                    .setCost(summation.multiply(tariff.getPrice()))
                    .setUserId(electricityMeter.getUserId())
                    .setBuildingId(electricityMeter.getBuildingId());
            list.add(electricityBill);
        }
        super.saveBatch(list);
    }
    @Transactional
    @Override
    public PageDTO<BillVo> getPage(BillQuery q) {
        List<Long> buildingIds = buildingMapper.getIdList(q.getBuildingNum(), q.getFloor(), q.getDoorplate());
        List<Long> userIds = userMapper.getIds(q.getName());
        LambdaQueryWrapper<ElectricityBill> wrapper = new LambdaQueryWrapper<>();
        if (q.getTime() != null) {
            wrapper.like(ElectricityBill::getTime, YearMonth.from(q.getTime()));
        }
        wrapper.in(buildingIds!=null, ElectricityBill::getBuildingId,buildingIds)
                .in(userIds!=null, ElectricityBill::getUserId,userIds)
                .eq(q.getStatus()!=null,ElectricityBill::getStatus,q.getStatus())
                .orderByDesc(ElectricityBill::getTime)
                .orderByAsc(ElectricityBill::getBuildingId);
        Page<ElectricityBill> page = super.page(q.toMpPage(), wrapper);
        List<BillVo> vos = page.getRecords().stream().map(this::getBillVo).toList();
        return new PageDTO<>(page.getTotal(), page.getPages(), vos);
    }

    @Override
    @Transactional
    public void automaticPayment() {
        try {
            // 待支付的账单
            List<ElectricityBill> bills = super.list(new LambdaQueryWrapper<ElectricityBill>()
                    .eq(ElectricityBill::getStatus, StatusEnum.PAYMENT_IN_PROGRESS.getCode()));
            // 用户
            Map<Long, Map<String, Object>> userMap = userMapper.getUserMap(false);
            List<ElectricityBill> billList = new ArrayList<>();
            List<User> userList = new ArrayList<>();
            for (ElectricityBill bill : bills) {
                Long userId = bill.getUserId();
                BigDecimal balance = new BigDecimal(userMap.get(userId).get("balance").toString());
                if (balance.compareTo(bill.getCost()) < 0) {  // 余额不足
                    bill.setStatus(StatusEnum.INSUFFICIENT_BALANCE);
                    billList.add(bill);
                    continue;
                }
                if (bill.getCost().compareTo(new BigDecimal("200")) < 0) {
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
        List<ElectricityBill> list = super.list(new LambdaQueryWrapper<ElectricityBill>()
                .eq(ElectricityBill::getStatus, StatusEnum.PAID_IN.getCode()));
        Map<Long, Map<String, Object>> userMap = userMapper.getUserMap(true);
        for (ElectricityBill bill : list) {
            String phone = userMap.get(bill.getUserId()).get("phone").toString();
            BillSMSVo billSMSVo = new BillSMSVo()
                    .setName(userMap.get(bill.getUserId()).get("name").toString())
                    .setTime(bill.getTime().toString())
                    .setPrice(bill.getPrice())
                    .setSummation(bill.getSummation())
                    .setCost(bill.getCost());
            SendSMSUtil.paidReminder(phone, billSMSVo,SMSCodeEnum.REMINDER_OF_PAID_ELECTRICITY.getCode());
        }
    }
    @Override
    public List<User> getUserPhoneWithName(Integer code) {
        return super.getBaseMapper().getUserPhoneWithName(code);
    }
    @Override
    public void export(HttpServletResponse response) throws Exception  {
        LocalDate date = LocalDate.now();
        LocalDate startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());

        List<BillExcel> list = super.getBaseMapper().selectExcel(startOfMonth,endOfMonth);
        BigExcelWriter writer = ExcelUtil.getBigWriter();
        // 导出设置了别名的字段
        writer.addHeaderAlias("buildingNum", "楼号");
        writer.addHeaderAlias("floor", "楼层");
        writer.addHeaderAlias("doorplate", "门牌");
        writer.addHeaderAlias("timeExcel", "账单时间");
        writer.addHeaderAlias("name", "住户姓名");
        writer.addHeaderAlias("previousReading", "上次读数(度)");
        writer.addHeaderAlias("reading", "本次读数(度)");
        writer.addHeaderAlias("summation", "总用电量(度)");
        writer.addHeaderAlias("price", "当前价格(度/元)");
        writer.addHeaderAlias("cost", "总费用(元)");
        writer.addHeaderAlias("statusExcel", "状态");
        writer.setOnlyAlias(true);
        writer.write(list, true);

        Sheet sheet = writer.getSheet();
        for (int i = 3; i < 11; i++) {
            sheet.setColumnWidth(i, 15 * 256);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("本月账单", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }
    @Override
    public Result<String> noticeOfInsufficientBalance() throws Exception {
        List<User> list = getUserPhoneWithName(StatusEnum.INSUFFICIENT_BALANCE.getCode());
        if (list.isEmpty()) {   // 余额不足名单为空
            return Result.success("暂无住户是余额不足状态");
        } else {
            for (User user : list) {
                SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), SMSCodeEnum.INSUFFICIENT_BALANCE.getCode());
            }
            return Result.success("短信发送成功");
        }
    }
    @Override
    public Result<String> notifyPayment() throws Exception {
        List<User> list = getUserPhoneWithName(StatusEnum.PAYMENT_IN_PROGRESS.getCode());
        if (list.isEmpty()) {   // 待支付名单为空
            return Result.success("暂无住户是待支付状态");
        } else {
            for (User user : list) {
                SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), SMSCodeEnum.PAYMENT_NOTICE.getCode());
            }
            return Result.success("短信发送成功");
        }
    }
    @Transactional
    public BillVo getBillVo(ElectricityBill e) {
        BillVo billVo = BeanUtil.copyProperties(e, BillVo.class);
        User user = userMapper.selectById(e.getUserId());
        Building building = buildingMapper.selectById(e.getBuildingId());
        ElectricityMeter electricityMeter = electricityMeterMapper.selectById(e.getElectricityMeterId());
        billVo.setBuildingNum(building.getBuildingNum())
                .setFloor(building.getFloor())
                .setDoorplate(building.getDoorplate())
                .setName(user.getName())
                .setMeterId(electricityMeter.getId())
                .setPreviousReading(electricityMeter.getPreviousReading())
                .setReading(electricityMeter.getReading());
        return billVo;
    }
}