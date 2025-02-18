package com.example.backend.pojo.excelVo;

import com.example.backend.common.enums.SexEnum;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserExcel {
    private Long id;
    private String phone;
    private String name;
    private SexEnum sex;
    private LocalDate time;
    private Long buildingId;
    private String buildingNum;
    private String floor;
    private String doorplate;

    // 添加两个临时字段，用于存储性别，入住时间描述，用于导出excel
    private String sexDesc;
    private String excelTime;
    public String getExcelTime() {
        return time != null ? time.toString() : null;
    }
    public String getSexDesc() {
        return sex != null ? sex.getSex() : null;
    }
}
