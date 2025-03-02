package com.example.backend.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 水电价格表
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Getter
@Setter
@TableName("tariff")
@Schema(description = "费用表")
@Accessors(chain = true)
public class Tariff implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId("id")
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;
    @Schema(description = "0：电，1：水")
    @TableField("name")
    private Integer name;
    @Schema(description = "价格,x.xx度每元或者x.xx方每元")
    @TableField("price")
    private BigDecimal price;
    @Schema(description = "自动扣费额度")
    @TableField("quota")
    private BigDecimal quota;
}
