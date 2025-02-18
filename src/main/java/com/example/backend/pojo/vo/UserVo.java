package com.example.backend.pojo.vo;

import com.example.backend.common.enums.SexEnum;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class UserVo {
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;
    private String phone;
    private String name;
    private SexEnum sex;
    private LocalDate time;
    private Long buildingId;
    private String buildingNum;
    private String floor;
    private String doorplate;
}
