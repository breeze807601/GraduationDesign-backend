package com.example.backend.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.backend.common.enums.SexEnum;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 住户表
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Getter
@Setter
@TableName("user")
@Schema(description = "住户表")
@Accessors(chain = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId("id")
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "姓名")
    @TableField("name")
    private String name;

    @Schema(description = "性别")
    @TableField("sex")
    private SexEnum sex;

    @Schema(description = "余额")
    @TableField("balance")
    private BigDecimal balance;

    @Schema(description = "入住时间")
    @TableField("time")
    private LocalDate time;

    @Schema(description = "住宅")
    @TableField("building_id")
    private Long buildingId;

    @Schema(description = "逻辑删除")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
