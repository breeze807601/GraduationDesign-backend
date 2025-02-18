package com.example.backend;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class MybatisPlusTest {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/graduation_design_db", "root", "123456")
                .globalConfig(builder -> {
                    builder.author("lwl") // 设置作者
                            .outputDir("src/main/java"); // 指定输出目录
                })
                .packageConfig(builder ->
                        builder.parent("com.example.backend") // 设置父包名
                                .entity("pojo")       // 设置实体类包名
                                .mapper("mapper")     // 设置Mapper包名
                                .service("service")   // 设置Service包名
                                .serviceImpl("service.impl")  // 设置Service实现类包名
                                .xml("mappers")       // 设置Mapper的xml映射
                                .pathInfo(Collections.singletonMap(OutputFile.xml, "src/main/resources/mappers")) // 设置mapperXml生成路径
                )
                .strategyConfig(builder -> {
                    builder.addInclude("admin", "user", "building", "electricity_meter",
                                    "water_meter", "electricity_bill", "water_bill", "tariff") // 设置需要生成的表名
                            .entityBuilder()
                            .enableLombok() // 启用 Lombok
                            .enableTableFieldAnnotation() // 启用字段注解
                            .controllerBuilder()
                            .enableRestStyle(); // 启用 REST 风格
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}
