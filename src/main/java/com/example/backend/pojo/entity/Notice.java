package com.example.backend.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Getter
@Setter
@TableName("notice")
@Schema(description = "公告表")
@Accessors(chain = true)
public class Notice {
    @Schema(description = "主键")
    @TableId("id")
    @JsonSerialize(using = ToStringSerializer.class)  // 解决Long精度丢失问题
    private Long id;
    @Schema(description = "标题")
    @TableField("title")
    private String title;
    @Schema(description = "内容")
    @TableField("content")
    private String content;
    @Schema(description = "发布时间")
    @TableField("time")
    private LocalDate time;
    @Schema(description = "发布者")
    @TableField("creator")
    private String creator;
}
