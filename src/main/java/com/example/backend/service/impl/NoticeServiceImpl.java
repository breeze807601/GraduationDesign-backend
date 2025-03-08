package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.mapper.NoticeMapper;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.Notice;
import com.example.backend.pojo.query.NoticeQuery;
import com.example.backend.pojo.query.PageQuery;
import com.example.backend.service.INoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 公告 服务实现类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
    @Override
    public PageDTO<Notice> getPage(NoticeQuery query) {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(query.getTitle() != null, Notice::getTitle, query.getTitle())
                .like(query.getTime() != null, Notice::getTime, query.getTime())
                .orderByDesc(Notice::getTime)
                .orderByAsc(Notice::getId);
        Page<Notice> page = super.page(query.toMpPage(), queryWrapper);
        return new PageDTO<>(page.getTotal(), page.getPages(), page.getRecords());
    }
}
