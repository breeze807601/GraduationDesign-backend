package com.example.backend.pojo.query;

import lombok.Data;

@Data
public class UserQuery extends PageQuery{
    private String name;
    private String phone;
    private String buildingNum;
    private String floor;
    private String doorplate;
}
