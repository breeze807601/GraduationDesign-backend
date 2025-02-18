package com.example.backend.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class BuildingOptionsVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String value;
    private String label;
    private boolean disabled;
    private List<BuildingOptionsVo> children;
    public BuildingOptionsVo(String value, String label, boolean disabled) {
        this.value = value;
        this.label = label;
        this.disabled = disabled;
        this.children = new ArrayList<>();
    }
}