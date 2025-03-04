package com.example.backend.pojo.query;

import lombok.Data;

@Data
public class BuildingQuery extends PageQuery{
    private String buildingNum;
    private String floor;
    private String doorplate;
}
