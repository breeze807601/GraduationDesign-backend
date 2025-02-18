package com.example.backend.pojo.query;

import com.example.backend.common.enums.StatusEnum;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BillQuery extends PageQuery{
    private LocalDate time;
    private Integer status;
    private String buildingNum;
    private String floor;
    private String doorplate;
    private String name;
}
