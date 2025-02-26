package com.example.backend.utils;

import com.example.backend.pojo.vo.DataItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsUtil {
    public static Map<String, Object> getMap(List<DataItem> list) {
        List<String> dates = new ArrayList<>(list.size());
        List<BigDecimal> usages = new ArrayList<>(list.size());
        List<BigDecimal> avgUsages = new ArrayList<>(list.size());
        for (DataItem item : list) {
            dates.add(item.getTime());
            usages.add(item.getNum());
            avgUsages.add(item.getAvgNum());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("date", dates);
        map.put("num", usages);
        map.put("avgNum", avgUsages);
        return map;
    }
}
