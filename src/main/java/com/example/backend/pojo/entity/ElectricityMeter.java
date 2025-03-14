package com.example.backend.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用电记录表
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Getter
@Setter
@TableName("electricity_meter")
@Schema(description = "用电记录表")
@Accessors(chain = true)
public class ElectricityMeter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId("id")
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;

    @Schema(description = "上次读数")
    @TableField("previous_reading")
    private BigDecimal previousReading;

    @Schema(description = "本次读数")
    @TableField("reading")
    private BigDecimal reading;

    @Schema(description = "录入时间")
    @TableField("time")
    private LocalDate time;

    @Schema(description = "所属住宅")
    @TableField("building_id")
    private Long buildingId;

    @Schema(description = "住户")
    @TableField("user_id")
    private Long userId;
    @Schema(description = "可用额度")
    @TableField("available_limit")
    private BigDecimal availableLimit;
}
