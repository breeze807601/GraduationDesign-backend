package com.example.backend.mapper;

import com.example.backend.pojo.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.pojo.excelVo.UserExcel;
import org.apache.ibatis.annotations.MapKey;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 住户表 Mapper 接口
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface UserMapper extends BaseMapper<User> {
    List<UserExcel> selectAll();
    void refund(Long id, BigDecimal refund);
    @MapKey("building_id")
    Map<Long,Map<String,Object>> getIdMap();
    List<Long> getIds(String name);
    @MapKey("id")
    Map<Long,Map<String,Object>> getUserMap(boolean flag);
    void updateBatch(List<User> list);
}
