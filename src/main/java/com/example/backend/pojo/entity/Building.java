package com.example.backend.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 楼房表
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Getter
@Setter
@TableName("building")
@Schema(description = "门牌表")
@Accessors(chain = true)
public class Building implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId("id")
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;

    @Schema(description = "楼号")
    @TableField("building_num")
    private String buildingNum;

    @Schema(description = "楼层，如：5楼")
    @TableField("floor")
    private String floor;

    @Schema(description = "门牌号")
    @TableField("doorplate")
    private String doorplate;
}
