package com.example.backend.pojo.dto;

import lombok.Data;

@Data
public class ChangePwDTO {
    private Long id;
    private String oldPw;
    private String newPw;
    private String confirmPw;
}
