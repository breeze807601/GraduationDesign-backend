package com.example.backend.service;

import com.example.backend.common.Result;
import com.example.backend.pojo.entity.WaterMeter;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.query.MeterQuery;
import com.example.backend.pojo.vo.MeterVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * <p>
 * 用水记录表 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface IWaterMeterService extends IService<WaterMeter> {
    PageDTO<MeterVo> getPage(MeterQuery query);
    void export(HttpServletResponse response) throws Exception;
    Result<LocalDate> upload(MultipartFile multipartFile);
    void updateWithReading(Long id, BigDecimal reading);
}
