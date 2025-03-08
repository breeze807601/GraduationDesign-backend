package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.common.Result;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.Admin;
import com.example.backend.pojo.entity.Notice;
import com.example.backend.pojo.query.NoticeQuery;
import com.example.backend.pojo.query.PageQuery;
import com.example.backend.service.IAdminService;
import com.example.backend.service.INoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * <p>
 * 公告相关 前端控制器
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Tag(name = "公告相关")
@RequiredArgsConstructor
@RestController
@RequestMapping("/notice")
public class NoticeController {
    private final INoticeService noticeService;
    private final IAdminService adminService;
    @Operation(summary = "获取列表")
    @SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)
    @GetMapping("list")
    public Result<PageDTO<Notice>> list(NoticeQuery query) {
        return Result.success(noticeService.getPage(query));
    }
    @Operation(summary = "获取详情")
    @SaCheckRole(value = {"admin", "user"}, mode = SaMode.OR)
    @GetMapping("info")
    public Result<Notice> info(Long id) {
        Notice notice = noticeService.getById(id);
        return Result.success(notice);
    }
    @Operation(summary = "新增")
    @SaCheckRole("admin")
    @PostMapping("add")
    @Transactional
    public Result<String> add(@RequestBody Notice notice) {
        // sa-token获取当前登录id
        Long id = Long.parseLong(StpUtil.getLoginId().toString());
        Admin admin  = adminService.getById(id);
        notice.setCreator(admin.getUsername())
                .setTime(LocalDate.now())
                .setContent(
                        notice.getContent()
                        .replace("\r", ""));
        noticeService.save(notice);
        return Result.success("新增成功");
    }
    @Operation(summary = "删除")
    @SaCheckRole("admin")
    @DeleteMapping("delete")
    public Result<String> delete(Long id) {
        noticeService.removeById(id);
        return Result.success("删除成功");
    }
    @Operation(summary = "修改")
    @SaCheckRole("admin")
    @PutMapping("update")
    public Result<String> update(@RequestBody Notice notice) {
        noticeService.updateById(notice);
        return Result.success("修改成功");
    }
}
