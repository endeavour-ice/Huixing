package com.ice.hxy.config.mybatis;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.ice.hxy.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author ice
 * @Date 2023/5/26 20:31
 * @Description: 自定义id生成器
 */
@Component
@Slf4j
public class IdGenerator implements IdentifierGenerator {
    @Override
    public Number nextId(Object entity) {
        return SnowFlake.getSnowLong();
    }


}
