package com.example.backend.common.enums;

import lombok.Getter;

@Getter
public enum SMSCodeEnum {
    REMINDER_OF_PAID_ELECTRICITY("SMS_478965172"),
    REMINDER_OF_PAID_WATER("SMS_479070173"),
    PAYMENT_NOTICE("SMS_478995179"),
    INSUFFICIENT_BALANCE("SMS_479005183"),
    VERIFICATION_CODE("SMS_479865109");
    private final String code;

    SMSCodeEnum(String code) {
        this.code = code;
    }
}
