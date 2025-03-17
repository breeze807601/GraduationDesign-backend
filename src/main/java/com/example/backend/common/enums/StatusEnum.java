package com.example.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum StatusEnum {
    PAYMENT_IN_PROGRESS(0,"待支付"),
    PAID_IN(1,"已支付"),
    REFUND(2,"余额不足");
    // EnumValue标记数据库存的值是code
    @EnumValue
    private final Integer code;
    // JsonValue标记前端展示值
    @JsonValue
    private final String status;

    StatusEnum(Integer code, String status) {
        this.code = code;
        this.status = status;
    }
}
