package com.example.backend.service;

import com.example.backend.common.Result;
import com.example.backend.pojo.entity.Building;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.vo.BuildingOptionsVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 楼房表 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface IBuildingService extends IService<Building> {
    Result<String> upload(MultipartFile multipartFile);
    Result<List<BuildingOptionsVo>> getBuildingOptions(Long id);
    Result<Long> getBuildingId(String buildingNum, String floor, String doorplate);
    void export(HttpServletResponse response) throws Exception;
}
