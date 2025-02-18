package com.example.backend.mapper;

import com.example.backend.pojo.entity.Building;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 楼房表 Mapper 接口
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface BuildingMapper extends BaseMapper<Building> {
    List<Long> getIdList(String buildingNum, String floor, String doorplate);
}
