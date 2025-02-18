package com.example.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SexEnum {
    MAN(1,"男"),
    GIRL(0,"女");
    // EnumValue标记数据库存的值是code
    @EnumValue
    private final Integer code;
    // JsonValue标记前端展示值
    @JsonValue
    private final String sex;

    SexEnum(Integer code, String sex) {
        this.code = code;
        this.sex = sex;
    }
}