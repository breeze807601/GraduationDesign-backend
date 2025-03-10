package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.Result;
import com.example.backend.mapper.BuildingMapper;
import com.example.backend.mapper.ElectricityMeterMapper;
import com.example.backend.mapper.WaterMeterMapper;
import com.example.backend.pojo.entity.Building;
import com.example.backend.pojo.entity.ElectricityMeter;
import com.example.backend.pojo.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.pojo.entity.WaterMeter;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.excelVo.UserExcel;
import com.example.backend.pojo.query.UserQuery;
import com.example.backend.pojo.vo.UserVo;
import com.example.backend.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.utils.EncryptionUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 住户表 服务实现类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final BuildingMapper buildingMapper;
    private final ElectricityMeterMapper electricityMeterMapper;
    private final WaterMeterMapper waterMeterMapper;
    @Override
    public Result<String> login(User u) {
        User user = super.lambdaQuery()
                .eq(User::getPhone, u.getPhone()).one();
        if(user == null) {
            return Result.error("用户不存在！");
        }
        if (EncryptionUtil.checkPassword(u.getPassword(),user.getPassword())) {
            StpUtil.login(user.getId());
            return Result.success("登录成功！");
        }
        return Result.error("密码错误！");
    }
    @Override
    @Transactional
    public PageDTO<UserVo> getUserPage(UserQuery userQuery) {
        // 如果存在building元素则先查询出id
        List<Long> buildingIds = buildingMapper.getIdList(userQuery.getBuildingNum(), userQuery.getFloor(), userQuery.getDoorplate());
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.like(StrUtil.isNotEmpty(userQuery.getName()),User::getName,userQuery.getName())
                .like(StrUtil.isNotEmpty(userQuery.getPhone()),User::getPhone,userQuery.getPhone())
                .eq(User::getDeleted, 0)  // 只查询未删除的
                .in(buildingIds!=null,User::getBuildingId,buildingIds)
                .orderByAsc(User::getBuildingId)
                // 排除密码和余额
                .select(User.class, item -> !(item.getColumn().equals("password") || item.getColumn().equals("balance")));
        Page<User> page = super.page(userQuery.toMpPage(), userWrapper);
        List<UserVo> userVos = page.getRecords().stream().map(this::getUserVo).toList();
        return new PageDTO<>(page.getTotal(), page.getPages(), userVos);
    }
    public UserVo getUserVo(User user) {
        Building building = buildingMapper.selectById(user.getBuildingId());
        // 转换vo
        UserVo userVo = BeanUtil.copyProperties(user, UserVo.class);
        userVo.setBuildingNum(building.getBuildingNum())
                .setFloor(building.getFloor())
                .setDoorplate(building.getDoorplate());
        return userVo;
    }
    @Override
    public void export(HttpServletResponse response) throws Exception {
        // 查询所有
        List<UserExcel> list = super.getBaseMapper().selectAll();
        BigExcelWriter writer = ExcelUtil.getBigWriter();
        // 设置表头
        writer.addHeaderAlias("name", "姓名");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("sexDesc", "性别");
        writer.addHeaderAlias("excelTime", "入住时间");
        writer.addHeaderAlias("buildingNum", "楼号");
        writer.addHeaderAlias("floor", "楼层");
        writer.addHeaderAlias("doorplate", "门牌");
        // 导出设置了别名的字段
        writer.setOnlyAlias(true);

        CellStyle textCellStyle = writer.getWorkbook().createCellStyle();
        textCellStyle.setDataFormat(writer.getWorkbook().createDataFormat().getFormat("@"));
        writer.getSheet().setDefaultColumnStyle(1, textCellStyle); // 电话在第二列

        CellStyle dateCellStyle = writer.getWorkbook().createCellStyle();
        dateCellStyle.setDataFormat(writer.getWorkbook().createDataFormat().getFormat("@"));
        writer.getSheet().setDefaultColumnStyle(3, dateCellStyle); // 入住时间在第四列
        // 设置统一的列宽
        Sheet sheet = writer.getSheet();
        int uniformWidth = 15 * 256; // 统一宽度为 15 个字符
        for (int i = 0; i < 7; i++) { // 有 7 列
            sheet.setColumnWidth(i, uniformWidth);
        }
        // 写入数据
        writer.write(list, true);
        // 设置响应头并写出到浏览器
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("住户信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }
    @Transactional
    @Override
    public Result<String> saveWithMeter(User user) {
        if (super.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()))) {
            return Result.error("住户已存在！手机号重复");
        } else {
            user.setPassword(EncryptionUtil.encrypt("111111"))
                    .setTime(LocalDate.now());
            super.save(user);
            // 初始化记录表
            saveMeter(user);
            return Result.success("添加成功！");
        }
    }
    @Override
    @Transactional
    public void updateWithBuilding(User user) {
        User u = super.getById(user.getId());
        // 如果楼号改变，则更新记录表
        if (!u.getBuildingId().equals(user.getBuildingId())) {
            saveMeter(user);
        }
        super.updateById(user);
    }
    @Transactional
    public void saveMeter(User user) {
        boolean flag = waterMeterMapper.exists(
                new LambdaQueryWrapper<WaterMeter>().eq(WaterMeter::getBuildingId, user.getBuildingId()));
        if (flag){
            LambdaQueryWrapper<WaterMeter> waterMeterWrapper = new LambdaQueryWrapper<>();
            waterMeterWrapper.eq(WaterMeter::getBuildingId, user.getBuildingId())
                    .orderByDesc(WaterMeter::getTime)
                    .last("limit 1");
            LambdaQueryWrapper<ElectricityMeter> electricityMeterWrapper = new LambdaQueryWrapper<>();
            electricityMeterWrapper.eq(ElectricityMeter::getBuildingId, user.getBuildingId())
                    .orderByDesc(ElectricityMeter::getTime)
                    .last("limit 1");
            WaterMeter waterMeter = waterMeterMapper.selectOne(waterMeterWrapper);
            ElectricityMeter electricityMeter = electricityMeterMapper.selectOne(electricityMeterWrapper);
            waterMeterMapper.insert(new WaterMeter()
                    .setUserId(user.getId())
                    .setPreviousReading(waterMeter.getReading())
                    .setReading(BigDecimal.valueOf(0.00))
                    .setTime(LocalDate.now())
                    .setBuildingId(user.getBuildingId()));
            electricityMeterMapper.insert(new ElectricityMeter()
                    .setUserId(user.getId())
                    .setPreviousReading(electricityMeter.getReading())
                    .setReading(BigDecimal.valueOf(0.00))
                    .setTime(LocalDate.now())
                    .setBuildingId(user.getBuildingId()));
        }else {
            waterMeterMapper.insert(new WaterMeter()
                    .setUserId(user.getId())
                    .setTime(LocalDate.now())
                    .setBuildingId(user.getBuildingId()));
            electricityMeterMapper.insert(new ElectricityMeter()
                    .setUserId(user.getId())
                    .setTime(LocalDate.now())
                    .setBuildingId(user.getBuildingId()));
        }
    }
}
