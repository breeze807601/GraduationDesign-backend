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
 * 管理员表
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Getter
@Setter
@TableName("admin")
@Schema(description = "管理员表")
@Accessors(chain = true)
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId("id")
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;
}
