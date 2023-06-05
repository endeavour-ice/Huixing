package com.ice.hxy.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.Collections;

/**
 * @Author ice
 * @Date 2023/4/22 16:20
 * @Description: mysql表格自动生成 会直接输出到该目录下
 */
public class AutoTable {
    public static void main(String[] args) {
        table();
    }
    public static void table() {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/user_center?serverTimezone=GMT%2B8", "root", "pwb2001")
                .globalConfig(builder -> {
                    builder.author("ice") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .outputDir("D:\\JavaUser\\Huixing\\hxy-common\\src\\main\\java"); // 指定输出目录
                }).dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                    int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                    if (typeCode == Types.SMALLINT) {
                        // 自定义类型转换
                        return DbColumnType.INTEGER;
                    }
                    return typeRegistry.getColumnType(metaInfo);

                }))
                .packageConfig(builder -> {
                    builder.parent("com.ice.hxy") // 设置父包名
                            .moduleName("generator") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "D:\\JavaUser\\Huixing\\hxy-server\\src\\main\\resources\\mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("tags") // 设置需要生成的表名
                            .addTablePrefix(); // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}
