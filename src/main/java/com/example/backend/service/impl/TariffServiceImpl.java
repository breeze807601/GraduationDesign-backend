package com.example.backend.service.impl;

import com.example.backend.pojo.entity.Tariff;
import com.example.backend.mapper.TariffMapper;
import com.example.backend.service.ITariffService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 水电价格表 服务实现类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Service
public class TariffServiceImpl extends ServiceImpl<TariffMapper, Tariff> implements ITariffService {

}
