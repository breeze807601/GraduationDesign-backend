package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.Notice;
import com.example.backend.pojo.query.NoticeQuery;

/**
 * <p>
 * 公告 服务类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
public interface INoticeService extends IService<Notice> {
    PageDTO<Notice> getPage(NoticeQuery query);
}
