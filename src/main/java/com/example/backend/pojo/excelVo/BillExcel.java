package com.example.backend.pojo.excelVo;

import com.example.backend.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillExcel {
    private String name;
    private LocalDate time;
    private BigDecimal previousReading;
    private BigDecimal reading;
    private BigDecimal price;
    private BigDecimal summation;
    private BigDecimal cost;
    private StatusEnum status;

    private String buildingNum;
    private String floor;
    private String doorplate;

    // 添加临时字段，用于导出excel列
    private String timeExcel;
    private String statusExcel;
    public String getTimeExcel() {
        return time != null ? time.toString() : null;
    }
    public String getStatusExcel() {
        return status != null ? status.getStatus() : null;
    }
}
