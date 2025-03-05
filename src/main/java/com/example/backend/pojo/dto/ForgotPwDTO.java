package com.example.backend.pojo.dto;

import lombok.Data;

@Data
public class ForgotPwDTO {
    private String phone;
    private String code;
    private String newPw;
    private String confirmPw;
}
