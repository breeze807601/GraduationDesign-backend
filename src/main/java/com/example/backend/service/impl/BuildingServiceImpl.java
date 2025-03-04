package com.example.backend.service.impl;

import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.Result;
import com.example.backend.pojo.dto.PageDTO;
import com.example.backend.pojo.entity.Building;
import com.example.backend.mapper.BuildingMapper;
import com.example.backend.pojo.entity.User;
import com.example.backend.pojo.query.BuildingQuery;
import com.example.backend.service.IBuildingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.pojo.vo.BuildingOptionsVo;
import com.example.backend.service.IUserService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * <p>
 * 楼房表 服务实现类
 * </p>
 *
 * @author lwl
 * @since 2025-01-11
 */
@Service
@RequiredArgsConstructor
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements IBuildingService {
    private final IUserService userService;
    @Transactional
    @Override
    public Result<String> upload(MultipartFile multipartFile) {
        InputStream inputStream;
        try {
            inputStream = multipartFile.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(inputStream);
            List<Building> list = new ArrayList<>();
            // 读取第二行到最后一行数据
            List<List<Object>> read = reader.read(1);
            for (List<Object> objects : read) {
                Building b = new Building();
                b.setBuildingNum(objects.get(0).toString())
                        .setFloor(objects.get(1).toString())
                        .setDoorplate(objects.get(2).toString());
                list.add(b);
            }
//            super.saveBatch(list);
            // 尝试批量插入数据
            try {
                super.saveBatch(list);
            } catch (DataIntegrityViolationException e) {    // 捕获唯一性冲突异常
                throw new RuntimeException("部分数据已存在，重复的数据如下：" + extractDuplicatedBuildings(e.getMessage()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.success("导入成功！");
    }
    private String extractDuplicatedBuildings(String message) {
        List<String> duplicatedBuildings = new ArrayList<>();
        // 正则表达式：提取单引号包裹的内容
        Pattern pattern = Pattern.compile("'([^']*)'");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            if (matcher.group(1).equals("building.unique_building")) {
                continue;
            }
            duplicatedBuildings.add(matcher.group(1));
        }
        return String.join("; ", duplicatedBuildings);  // 将重复的数据以分号分隔并返回
    }
    public Result<List<BuildingOptionsVo>> getBuildingOptions(Long id) {
        // 查询所有建筑信息
        List<Building> buildings = this.list();
        List<Long> ids = null;
        if (id != null) {
            ids = userService.getBaseMapper().selectObjs(Wrappers.lambdaQuery(User.class).select(User::getBuildingId));
        }
        // 使用 Map 来存储楼号和其对应的 BuildingOption
        Map<String, BuildingOptionsVo> buildingMap = new HashMap<>();
        // 遍历 buildings，构建树形结构
        for (Building building : buildings) {
            // 判断该门牌是否禁用
            boolean disabled = ids != null && ids.contains(building.getId()) && !building.getId().equals(id);
            String buildingNum = building.getBuildingNum();
            String floor = building.getFloor();
            String doorplate = building.getDoorplate();
            // 构建楼号节点，若当前key不存在，就创建一个放入map
            BuildingOptionsVo buildingOption = buildingMap.computeIfAbsent(buildingNum, k -> new BuildingOptionsVo(buildingNum, buildingNum,false));
            // 构建楼层节点，并且将其添加到对应楼号下
            BuildingOptionsVo floorOption = buildingOption.getChildren().stream()
                    .filter(child -> child.getValue().equals(floor))
                    .findFirst()
                    .orElseGet(() -> {
                        BuildingOptionsVo bo = new BuildingOptionsVo(floor, floor,false);
                        buildingOption.getChildren().add(bo);
                        return bo;
                    });
            // 构建门牌号节点
            BuildingOptionsVo doorplateOption = new BuildingOptionsVo(doorplate, doorplate, disabled);

            // 将门牌号节点添加到楼层节点
            floorOption.getChildren().add(doorplateOption);
        }
        // 返回最终的树形结构
        return Result.success(new ArrayList<>(buildingMap.values()));
    }

    @Override
    public Result<Long> getBuildingId(String buildingNum, String floor, String doorplate) {
        Building building = super.lambdaQuery().eq(Building::getBuildingNum, buildingNum)
                .eq(Building::getFloor, floor)
                .eq(Building::getDoorplate, doorplate)
                .one();
        return Result.success(building.getId());
    }

    @Override
    public void export(HttpServletResponse response) throws Exception  {
        List<Building> list = super.lambdaQuery().orderByAsc(Building::getId).list();
        BigExcelWriter writer = ExcelUtil.getBigWriter();
        // 设置表头
        writer.addHeaderAlias("buildingNum", "楼号");
        writer.addHeaderAlias("floor", "楼层");
        writer.addHeaderAlias("doorplate", "门牌");
        // 导出设置了别名的字段
        writer.setOnlyAlias(true);
        writer.write(list, true);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("楼房表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }
    @Override
    public PageDTO<Building> getPage(BuildingQuery query) {
        LambdaQueryWrapper<Building> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(query.getBuildingNum() != null, Building::getBuildingNum, query.getBuildingNum())
                .eq(query.getFloor() != null, Building::getFloor, query.getFloor())
                .eq(query.getDoorplate() != null, Building::getDoorplate, query.getDoorplate())
                .orderByAsc(Building::getId);
        Page<Building> page = super.page(query.toMpPage(), queryWrapper);
        return new PageDTO<>(page.getTotal(), page.getPages(),page.getRecords());
    }
}
