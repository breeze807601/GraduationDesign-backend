package com.example.backend.pojo.vo;

import com.example.backend.common.enums.StatusEnum;
import lombok.Data;

@Data
public class PieChartVo {
    private StatusEnum name;  // 状态
    private Integer value;   // 数量
}
