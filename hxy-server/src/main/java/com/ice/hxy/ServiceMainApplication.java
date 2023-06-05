package com.ice.hxy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author ice
 * @Date 2022/10/9 16:17
 * @PackageName:com.ice.hxy
 * @ClassName: ServiceMain
 * @Description: 主启动类
 * @Version 1.0
 */
@SpringBootApplication
@EnableScheduling // 开启定时任务
@MapperScan("com.ice.hxy.mapper")
@ComponentScan("com.ice")
@ServletComponentScan("com.ice.hxy.config.filter")
public class ServiceMainApplication {
    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(ServiceMainApplication.class, args);
    }
}
