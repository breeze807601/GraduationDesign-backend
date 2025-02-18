package com.example.backend.pojo.dto;

import com.example.backend.pojo.entity.User;
import lombok.Data;

@Data
public class UserDTO extends User {
    private String buildingNum;
    private String floor;
    private String doorplate;
}
