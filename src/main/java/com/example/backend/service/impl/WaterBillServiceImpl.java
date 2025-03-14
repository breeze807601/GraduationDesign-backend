package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
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
import com.example.backend.pojo.vo.DataItem;
import com.example.backend.pojo.vo.PieChartVo;
import com.example.backend.service.IWaterBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.utils.SendSMSUtil;
import com.example.backend.utils.StatisticsUtil;
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
import java.util.*;

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
    @Transactional
    public void mySave(LocalDate now) {
        Tariff tariff = tariffMapper.selectOne(new LambdaQueryWrapper<Tariff>().eq(Tariff::getName, 1));
        LambdaQueryWrapper<WaterMeter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterMeter::getTime, now);
        List<WaterMeter> waterMeters =  waterMeterMapper.selectList(wrapper);
        List<WaterBill> list = new ArrayList<>();
        for (WaterMeter waterMeter : waterMeters) {
            // 判断本次读数和上次读数是否是0，是0则是今天添加的住户，不需要保存
            if (waterMeter.getReading().compareTo(BigDecimal.ZERO) == 0 && waterMeter.getPreviousReading().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal summation = waterMeter.getReading().subtract(waterMeter.getPreviousReading());
            WaterBill waterBill = new WaterBill()
                    .setTime(now)
                    .setWaterMeterId(waterMeter.getId())
                    .setSummation(summation)
                    .setPrice(tariff.getPrice())
                    .setCost(summation.multiply(tariff.getPrice()))
                    .setUserId(waterMeter.getUserId())
                    .setBuildingId(waterMeter.getBuildingId());
            if (summation.compareTo(waterMeter.getAvailableLimit()) <= 0) {
                // 减去额度
                waterMeter.setAvailableLimit(waterMeter.getAvailableLimit().subtract(summation));
                waterBill.setStatus(StatusEnum.PAID_IN);
            } else {
                // 可用额度不够，状态为可用额度不足
                waterBill.setStatus(StatusEnum.INSUFFICIENT_AVAILABLE_CREDIT_LIMIT);
           }
            list.add(waterBill);
        }
        // 更新水表数据，保存账单
        waterMeterMapper.updateById(waterMeters);
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
        Tariff tariff = tariffMapper.selectOne(new LambdaQueryWrapper<Tariff>().eq(Tariff::getName, 1));
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
                if (bill.getCost().compareTo(tariff.getQuota()) < 0) {
                    // 账单金额小于自动扣费额度，则为小额，自动扣款，并将要修改的值封装到list中
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
    @Override
    public void export(HttpServletResponse response) throws Exception {
        LocalDate date = LocalDate.now();
        // 获取上个月最后一天
        LocalDate lastDayOfLastMonth = date.with(TemporalAdjusters.firstDayOfMonth()).minusDays(1);
        List<BillExcel> list = super.getBaseMapper().selectExcel(lastDayOfLastMonth,date);
        BigExcelWriter writer = ExcelUtil.getBigWriter();
        // 导出设置了别名的字段
        writer.addHeaderAlias("buildingNum", "楼号");
        writer.addHeaderAlias("floor", "楼层");
        writer.addHeaderAlias("doorplate", "门牌");
        writer.addHeaderAlias("timeExcel", "账单时间");
        writer.addHeaderAlias("name", "住户姓名");
        writer.addHeaderAlias("previousReading", "上次读数(方)");
        writer.addHeaderAlias("reading", "本次读数(方)");
        writer.addHeaderAlias("summation", "总用电量(方)");
        writer.addHeaderAlias("price", "当前价格(方/元)");
        writer.addHeaderAlias("cost", "总费用(元)");
        writer.addHeaderAlias("statusExcel", "状态");
        writer.setOnlyAlias(true);

        Sheet sheet = writer.getSheet();
        for (int i = 3; i < 11; i++) {
            sheet.setColumnWidth(i, 15 * 256);
        }
        writer.write(list, true);  // 写入数据
        // 设置响应头并写出到浏览器
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("上月用电账单", "UTF-8");
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
    @Override
    public Map<String, Object> getMonthlyUsage(LocalDate start, LocalDate end) {
        if (start == null && end == null) {  // 默认为半年内
            start = LocalDate.now().minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
            end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        } else {  // 设置为当月最后一天
            end = end.with(TemporalAdjusters.lastDayOfMonth());
        }
        return StatisticsUtil.getMap(super.getBaseMapper().getMonthlySummation(start,end));
    }
    @Override
    public Map<String, Object> getCostStatistics(LocalDate start, LocalDate end) {
        if (start == null && end == null) {  // 默认为半年内
            start = LocalDate.now().minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
            end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        } else {  // 设置为当月最后一天
            end = end.with(TemporalAdjusters.lastDayOfMonth());
        }
        return StatisticsUtil.getMap(super.getBaseMapper().getCostStatistics(start,end));
    }
    @Override
    public List<PieChartVo> getBillStatusPieChart() {
        LocalDate now = LocalDate.now();
        // 获取上个月第一天
        LocalDate firstDayOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        // 获取上个月最后一天
        LocalDate lastDayOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return super.getBaseMapper().getBillStatusPieChart(firstDayOfLastMonth,lastDayOfLastMonth);
    }
    @Override
    public BigDecimal myCount() {
        // 获取上个月的第一天和最后一天
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastMonth = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfLastMonth = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        List<DataItem> monthlySummation = super.getBaseMapper().getMonthlySummation(firstDayOfLastMonth, lastDayOfLastMonth);
        if (!monthlySummation.isEmpty()) {
            return monthlySummation.get(0).getNum();
        }
        return new BigDecimal("0");
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
