package com.example.backend.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.backend.common.enums.StatusEnum;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用水账单
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Getter
@Setter
@TableName("water_bill")
@Schema(description = "用水账单")
@Accessors(chain = true)
public class WaterBill implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId("id")
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;

    @Schema(description = "时间")
    @TableField("time")
    private LocalDate time;

    @Schema(description = "0：待支付，1：支付成功")
    @TableField("status")
    private StatusEnum status;

    @Schema(description = "用水量")
    @TableField("summation")
    private BigDecimal summation;

    @Schema(description = "当时价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "总费用")
    @TableField("cost")
    private BigDecimal cost;

    @Schema(description = "用水信息")
    @TableField("water_meter_id")
    private Long waterMeterId;
    // 用户id和住宅id，避免连接太多表查询数据
    @Schema(description = "用户id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "住宅id")
    @TableField("building_id")
    private Long buildingId;
}
