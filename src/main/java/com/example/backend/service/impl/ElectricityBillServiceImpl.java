package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
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
import com.example.backend.service.IElectricityBillService;
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
        LambdaQueryWrapper<ElectricityMeter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElectricityMeter::getTime, now);
        List<ElectricityMeter> electricityMeters = electricityMeterMapper.selectList(wrapper);
        List<ElectricityBill> list = new ArrayList<>();
        for (ElectricityMeter electricityMeter : electricityMeters) {
            // 判断本次读数和上次读数是否是0，是0则是今天添加的住户，不需要保存
            if (electricityMeter.getReading().compareTo(BigDecimal.ZERO) == 0 && electricityMeter.getPreviousReading().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal summation = electricityMeter.getReading().subtract(electricityMeter.getPreviousReading());
            ElectricityBill electricityBill = new ElectricityBill()
                    .setTime(now)
                    .setElectricityMeterId(electricityMeter.getId())
                    .setSummation(summation)
                    .setPrice(tariff.getPrice())
                    .setCost(summation.multiply(tariff.getPrice()))
                    .setUserId(electricityMeter.getUserId())
                    .setBuildingId(electricityMeter.getBuildingId());
            if (summation.compareTo(electricityMeter.getAvailableLimit()) <= 0) {
                // 减去额度
                electricityMeter.setAvailableLimit(electricityMeter.getAvailableLimit().subtract(summation));
                electricityBill.setStatus(StatusEnum.PAID_IN);
            } else {
                // 可用额度不够，状态为余额不足
                electricityBill.setStatus(StatusEnum.REFUND);
            }
            list.add(electricityBill);
        }
        // 更新电表数据，保存账单
        electricityMeterMapper.updateById(electricityMeters);
        super.saveBatch(list);
    }
    @Transactional
    @Override
    public PageDTO<BillVo> getPage(BillQuery q) {
        List<Long> buildingIds = buildingMapper.getIdList(q.getBuildingNum(), q.getFloor(), q.getDoorplate());
        List<Long> userIds = userMapper.getIds(q.getName());
        LambdaQueryWrapper<ElectricityBill> wrapper = new LambdaQueryWrapper<>();
        if (q.getTime() != null) {
            wrapper.eq(ElectricityBill::getTime, q.getTime());
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
    public List<User> getUserPhoneWithName(Integer code) {
        return super.getBaseMapper().getUserPhoneWithName(code);
    }
    @Override
    public void export(HttpServletResponse response,LocalDate startTime, LocalDate endTime) throws Exception  {
        List<BillExcel> list = super.getBaseMapper().selectExcel(startTime,endTime);
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
        Sheet sheet = writer.getSheet();
        for (int i = 3; i < 11; i++) {
            sheet.setColumnWidth(i, 15 * 256);
        }
        writer.write(list, true); // 写入数据
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("上月用电账单", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }
    @Override
    public Map<String, Object> electricityStatistics(LocalDate start, LocalDate end, Boolean isUser) {
        if (start == null && end == null) {  // 默认为半年内
            start = LocalDate.now().minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
            end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        } else {  // 设置为当月最后一天
            end = end.with(TemporalAdjusters.lastDayOfMonth());
        }
        Long id = null;
        if (isUser) {
            id = Long.parseLong(StpUtil.getLoginId().toString());
        }
        return StatisticsUtil.getMap(super.getBaseMapper().getSummation(start,end,id));
    }
    @Override
    public Map<String, Object> getCostStatistics(LocalDate start, LocalDate end, Boolean isUser) {
        if (start == null && end == null) {  // 默认为半年内
            start = LocalDate.now().minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
            end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        } else {  // 设置为当月最后一天
            end = end.with(TemporalAdjusters.lastDayOfMonth());
        }
        Long id = null;
        if (isUser) {
            id = Long.parseLong(StpUtil.getLoginId().toString());
        }
        return StatisticsUtil.getMap(super.getBaseMapper().getCostStatistics(start,end,id));
    }
    @Override
    public BigDecimal myCount() {
        // 获取本月的第一天和最后一天
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfLastMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        List<DataItem> monthlySummation = super.getBaseMapper().getSummation(firstDayOfLastMonth, lastDayOfLastMonth,null);
        if (!monthlySummation.isEmpty()) {
            return monthlySummation.get(0).getNum();
        }
        return new BigDecimal("0");
    }
    @Override
    public Result<String> notifyRecharge() throws Exception {
        List<User> list = getUserPhoneWithName(StatusEnum.REFUND.getCode());
        if (list.isEmpty()) {   // 余额不足名单为空
            return Result.success("暂无住户有未缴账单");
        }
        for (User user : list) {
            SendSMSUtil.sendPaymentNotice(user.getPhone(),user.getName(), "SMS_480570194");
        }
        return Result.success("短信提醒成功");
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
                .setReading(electricityMeter.getReading())
                .setAvailableLimit(electricityMeter.getAvailableLimit());
        return billVo;
    }
}