package com.example.backend.pojo.query;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MeterQuery extends PageQuery{
    private LocalDate time;
    private String buildingNum;
    private String floor;
    private String doorplate;
}
