package com.ice.hxy.config.SensitiveConfig;

import com.github.houbb.sensitive.word.api.IWordAllow;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/27 23:44
 * @Description: 非敏感词
 */
public class MyWordAllow implements IWordAllow {
    @Override
    public List<String> allow() {
        return new ArrayList<>();
    }
}
