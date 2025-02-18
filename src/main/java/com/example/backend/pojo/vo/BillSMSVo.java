package com.example.backend.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class BillSMSVo {
    private String name;
    private String time;
    private BigDecimal price;
    private BigDecimal summation;
    private BigDecimal cost;
    @Override
    public String toString() {
        return String.format("{\"name\":\"%s\",\"time\":\"%s\",\"price\":\"%s\",\"summation\":\"%s\",\"cost\":\"%s\"}",
                Objects.toString(name, ""),
                Objects.toString(time, ""),
                Objects.toString(price, ""),
                Objects.toString(summation, ""),
                Objects.toString(cost, ""));
    }
}
