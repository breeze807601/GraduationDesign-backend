package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.Result;
import com.example.backend.common.enums.StatusEnum;
import com.example.backend.mapper.*;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.*;
import com.example.backend.pojo.excelVo.MeterExcel;
import com.example.backend.pojo.query.MeterQuery;
import com.example.backend.pojo.vo.MeterVo;
import com.example.backend.service.IWaterMeterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 用水记录表 服务实现类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Service
@RequiredArgsConstructor
public class WaterMeterServiceImpl extends ServiceImpl<WaterMeterMapper, WaterMeter> implements IWaterMeterService {
    private final BuildingMapper buildingMapper;
    private final UserMapper userMapper;
    private final WaterBillMapper waterBillMapper;
    private final TariffMapper tariffMapper;
    @Transactional
    @Override
    public PageDTO<MeterVo> getPage(MeterQuery query) {
        List<Long> buildingIds = buildingMapper.getIdList(query.getBuildingNum(), query.getFloor(), query.getDoorplate());
        LambdaQueryWrapper<WaterMeter> wrapper = new LambdaQueryWrapper<>();
        if (query.getTime() != null) {
            wrapper.like(WaterMeter::getTime, YearMonth.from(query.getTime()));
        }
        wrapper.in(buildingIds!=null, WaterMeter::getBuildingId,buildingIds)
                .orderByDesc(WaterMeter::getTime)
                .orderByAsc(WaterMeter::getBuildingId);
        Page<WaterMeter> page = super.page(query.toMpPage(), wrapper);
        List<MeterVo> Vos = page.getRecords().stream().map(this::getMeterVo).toList();
        return new PageDTO<>(page.getTotal(), page.getPages(), Vos);
    }

    @Override
    public void export(HttpServletResponse response) throws Exception {
        // 获取昨天记录，一天更新一次
        LocalDate now = LocalDate.now().minusDays(1);
        List<MeterExcel> list = super.getBaseMapper().selectExcel(now, now);
        BigExcelWriter writer = ExcelUtil.getBigWriter();
        // 导出设置了别名的字段
        writer.addHeaderAlias("excelId", "编号");
        writer.addHeaderAlias("buildingNum", "楼号");
        writer.addHeaderAlias("floor", "楼层");
        writer.addHeaderAlias("doorplate", "门牌");
        writer.addHeaderAlias("name", "住户姓名");
        writer.addHeaderAlias("availableLimit", "可用额度");
        writer.addHeaderAlias("previousReading", "上次读数");
        writer.addHeaderAlias("reading", "本次读数");
        writer.setOnlyAlias(true);
        for (MeterExcel w : list) {
            w.setExcelId(w.getId().toString());
        }
        // 设置列宽
        Sheet sheet = writer.getSheet();
        sheet.setColumnWidth(0, 25 * 256);

        writer.write(list, true);
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用水记录抄表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        // 写出到浏览器
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }

    @Override
    public Result<LocalDate> upload(MultipartFile multipartFile) {
        InputStream inputStream;
        LocalDate time = LocalDate.now();      // 录入时间
        try {
            inputStream = multipartFile.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(inputStream);
            List<WaterMeter> list = new ArrayList<>();
            Map<Long,Map<String,Object>> userMap = userMapper.getIdMap();
            List<List<Object>> read = reader.read(1, reader.getRowCount());
            for (List<Object> objects : read) {
                WaterMeter w = new WaterMeter();
                Long buildingId = Long.valueOf(objects.get(0).toString());
                w.setBuildingId(buildingId)
                        .setUserId(Long.valueOf(userMap.get(buildingId).get("id").toString()))
                        .setAvailableLimit(new BigDecimal(objects.get(5).toString()))   // 可用额度
                        .setPreviousReading(new BigDecimal(objects.get(6).toString()))
                        .setReading(new BigDecimal(objects.get(7).toString()))
                        .setTime(time);
                list.add(w);
            }
            super.saveBatch(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.success(time);
    }
    @Override
    public List<User> smallAutomaticRecharge() {
        Tariff tariff = tariffMapper.selectOne(new LambdaQueryWrapper<Tariff>().eq(Tariff::getName, 0));
        // 小额自动充值，先获取可用余额不足的订单
        List<WaterBill> waterBills = waterBillMapper.selectList(
                new LambdaQueryWrapper<WaterBill>()
                        .eq(WaterBill::getStatus, 4));
        if (waterBills == null || waterBills.isEmpty()) {
            return null;
        }
        Map<Long, Map<String, Object>> userMap = userMapper.getUserMap(true);

        List<WaterMeter> waterMeters = super.lambdaQuery()
                .in(WaterMeter::getId, waterBills.stream().map(WaterBill::getWaterMeterId).toList()).list();
        Map<Long, WaterMeter> meterMap = waterMeters.stream().collect(Collectors.toMap(WaterMeter::getId, Function.identity()));
        waterMeters.clear();  // 清空列表，用来存储需要修改的记录

        List<User> modUsers = new ArrayList<>();      // 需要修改的住户
        List<User> insufficientList = new ArrayList<>();  // 可用额度仍然不足的住户
        BigDecimal price = tariff.getPrice().multiply(new BigDecimal("15")); // 充值所需价格，15为充值进入记录表的可用额度
        // 遍历账单，进行处理
        for (WaterBill bill : waterBills) {
            // 获取用户
            User user = new User()
                    .setId(bill.getUserId())
                    .setBalance(new BigDecimal(userMap.get(bill.getUserId()).get("balance").toString()))
                    .setName(userMap.get(bill.getUserId()).get("name").toString())
                    .setPhone(userMap.get(bill.getUserId()).get("phone").toString());
            // 余额不足，跳过
            if (new BigDecimal(userMap.get(bill.getUserId()).get("balance").toString()).compareTo(price) < 0) {
                continue;
            }
            // 充值
            user.setBalance(user.getBalance().subtract(price));
            modUsers.add(user);
            // 更新用电记录的可用额度
            WaterMeter waterMeter = meterMap.get(bill.getWaterMeterId());
            waterMeter.setAvailableLimit(waterMeter.getAvailableLimit().add(new BigDecimal("15")));  // 加15方
            // 处理支付账单，判断是否够减去账单的电量
            if (bill.getSummation().compareTo(waterMeter.getAvailableLimit()) < 0 ) {
                // 减去额度
                waterMeter.setAvailableLimit(waterMeter.getAvailableLimit().subtract(bill.getSummation()));
                bill.setStatus(StatusEnum.PAID_IN);
            } else {
                // 可用额度不够，发送通知
                insufficientList.add(user);
            }
            waterMeters.add(waterMeter);
        }
        // 修改用户信息和电表记录
        if (!modUsers.isEmpty()) {
            userMapper.updateById(modUsers);
        }
        if (!waterBills.isEmpty()) {
            waterBillMapper.updateById(waterBills);
        }
        if (!waterMeters.isEmpty()) {
            super.updateBatchById(waterMeters);
        }
        return insufficientList;
    }
    @Override
    public void updateWithReading(Long id, BigDecimal reading) {
        LocalDate now = LocalDate.now();
        // 修改后的电表记录
        WaterMeter waterMeter = super.getById(id);
        // 判断该电表是否有新记录
        if (judge(waterMeter.getBuildingId(),waterMeter.getTime())) {
            throw new RuntimeException("水表已有新记录，不可修改！");
        }
        waterMeter.setReading(reading).setTime(now);
        // 账单
        WaterBill waterBill = waterBillMapper.selectOne(new LambdaQueryWrapper<WaterBill>()
                .eq(WaterBill::getWaterMeterId, id));
        // 修改前的总费用
        BigDecimal c = waterBill.getCost();
        // 修改后的参数
        BigDecimal summation = reading.subtract(waterMeter.getPreviousReading());
        BigDecimal cost = summation.multiply(waterBill.getPrice());
        if (waterBill.getStatus().getCode() == 0) {           // 如果是待支付的账单，则直接修改
            waterBill.setSummation(summation)
                    .setCost(cost)
                    .setTime(LocalDate.now());
            waterBillMapper.updateById(waterBill);
        } else if (waterBill.getStatus().getCode() == 1) {    // 账单状态已支付，修改原账单状态，退款创建新账单
            userMapper.refund(waterBill.getUserId(), c);
            waterBill.setStatus(StatusEnum.REFUND);  // 修改状态为退款
            waterBillMapper.updateById(waterBill);
            WaterBill wb = new WaterBill()
                    .setTime(now)
                    .setSummation(summation)
                    .setPrice(waterBill.getPrice())
                    .setCost(cost)
                    .setBuildingId(waterMeter.getBuildingId())
                    .setUserId(waterMeter.getUserId())
                    .setWaterMeterId(waterMeter.getId());
            waterBillMapper.insert(wb);
        } else if (waterBill.getStatus().getCode() == 2) {
            throw new RuntimeException("该账单已退款");
        }
        super.updateById(waterMeter);
    }
    public boolean judge(Long buildingId,LocalDate time) {
        LambdaQueryWrapper<WaterMeter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterMeter::getBuildingId, buildingId)
                .orderByDesc(WaterMeter::getTime)
                .last("limit 1");
        WaterMeter one = super.getOne(wrapper);    // 最新的记录
        // 如果最新记录的时间在time后，则说明有新记录
        return one.getTime().isAfter(time);
    }
    @Transactional
    public MeterVo getMeterVo(WaterMeter w) {
        MeterVo vo = BeanUtil.copyProperties(w, MeterVo.class);
        Building building = buildingMapper.selectById(w.getBuildingId());
        User user = userMapper.selectById(w.getUserId());
        vo.setBuildingNum(building.getBuildingNum())
                .setFloor(building.getFloor())
                .setDoorplate(building.getDoorplate())
                .setName(user.getName());
        return vo;
    }
}
