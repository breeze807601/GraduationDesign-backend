package com.example.backend.service.impl;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.backend.common.Result;
import com.example.backend.pojo.entity.Building;
import com.example.backend.mapper.BuildingMapper;
import com.example.backend.pojo.entity.User;
import com.example.backend.service.IBuildingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.pojo.vo.BuildingOptionsVo;
import com.example.backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            super.saveBatch(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.success("导入成功！");
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
}
