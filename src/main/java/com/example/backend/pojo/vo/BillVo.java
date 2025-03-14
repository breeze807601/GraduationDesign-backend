package com.example.backend.pojo.vo;

import com.example.backend.common.enums.StatusEnum;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class BillVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private LocalDate time;
    private StatusEnum status;
    private BigDecimal summation;
    private BigDecimal price;
    private BigDecimal cost;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long meterId;
    private BigDecimal previousReading;
    private BigDecimal reading;
    private BigDecimal availableLimit;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String name;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long buildingId;
    private String buildingNum;
    private String floor;
    private String doorplate;
}
