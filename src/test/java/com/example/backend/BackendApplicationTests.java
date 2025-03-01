package com.example.backend;

import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.ElectricityMeter;
import com.example.backend.pojo.entity.Tariff;
import com.example.backend.pojo.entity.User;
import com.example.backend.pojo.entity.WaterMeter;
import com.example.backend.pojo.query.UserQuery;
import com.example.backend.pojo.vo.BillSMSVo;
import com.example.backend.pojo.vo.PieChartVo;
import com.example.backend.pojo.vo.UserVo;
import com.example.backend.service.*;
import com.example.backend.utils.EncryptionUtil;
import com.example.backend.utils.SMSUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class BackendApplicationTests {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    IUserService userService;
    @Autowired
    IElectricityMeterService electricityMeterService;
    @Autowired
    IWaterMeterService waterMeterService;
    @Autowired
    ITariffService tariffService;
    @Autowired
    IElectricityBillService electricityBillService;
    @Test
    void contextLoads() {
        String password = EncryptionUtil.encrypt("admin");
        String encrypt = EncryptionUtil.decrypts(password);
        System.out.println("加密后："+password);
        System.out.println("解密password:"+encrypt);
        System.out.println("校验密码："+EncryptionUtil.checkPassword("admin",password));
    }

    @Test
    void test(){
        UserQuery userQuery = new UserQuery();
//        userQuery.setName("张三");
//        userQuery.setBuildingNum("1号楼");
//        userQuery.setFloor("2楼");
//        userQuery.setDoorplate("0202");
        PageDTO<UserVo> pageDTO = userService.getUserPage(userQuery);
        System.out.println(pageDTO.getList().size());
    }
    @Test
    void testOne() {
        Tariff tariff = new Tariff()
                .setName(0)
                .setPrice(BigDecimal.valueOf(1.60));
        Tariff t = new Tariff()
                .setName(1)
                .setPrice(BigDecimal.valueOf(0.36));
        tariffService.save(tariff);
        tariffService.save(t);
    }

    @Test
    void t(){
        List<User> list = userService.list();
        List<ElectricityMeter> l = new ArrayList<>();
        for (User user : list) {
            ElectricityMeter electricityMeter = new ElectricityMeter();
            electricityMeter.setBuildingId(user.getBuildingId())
                    .setUserId(user.getId())
                    .setTime(LocalDate.now());
            l.add(electricityMeter);
        }
        boolean saveBatch = electricityMeterService.saveBatch(l);
        System.out.println(saveBatch);
    }
    @Test
    void a(){
        List<User> list = userService.list();
        List<WaterMeter> l = new ArrayList<>();
        for (User user : list) {
            WaterMeter waterMeter = new WaterMeter();
            waterMeter.setBuildingId(user.getBuildingId())
                    .setUserId(user.getId())
                    .setTime(user.getTime());
            l.add(waterMeter);
        }
        boolean saveBatch = waterMeterService.saveBatch(l);
        System.out.println(saveBatch);
    }

    @Test
    void mapper() {
        List<PieChartVo> billStatusPieChart = electricityBillService.getBillStatusPieChart();
        for (PieChartVo pieChartVo : billStatusPieChart) {
            System.out.println(pieChartVo.getName());
        }
    }
}
