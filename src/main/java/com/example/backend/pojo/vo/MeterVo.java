package com.example.backend.pojo.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class MeterVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;
    private String name;
    private String buildingNum;
    private String floor;
    private String doorplate;
    private LocalDate time;
    private BigDecimal previousReading;
    private BigDecimal reading;
}
