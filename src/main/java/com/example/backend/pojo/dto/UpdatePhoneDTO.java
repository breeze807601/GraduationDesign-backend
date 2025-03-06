package com.example.backend.pojo.dto;

import lombok.Data;

@Data
public class UpdatePhoneDTO {
    private Long id;
    private String phone;
    private String code;
}
