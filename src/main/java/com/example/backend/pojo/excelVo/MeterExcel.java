package com.example.backend.pojo.excelVo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeterExcel {
    private Long id;
    private String excelId; // 避免id精度丢失
    private String buildingNum;
    private String floor;
    private String doorplate;
    private String name;
    private BigDecimal previousReading;
    private BigDecimal reading;
    private BigDecimal availableLimit;
}
