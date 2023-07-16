package com.ice.hxy.config.SensitiveConfig;

import com.github.houbb.sensitive.word.api.IWordDeny;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/27 23:44
 * @Description: 自定义敏感词
 */
@Slf4j
public class MyWordDeny implements IWordDeny {
    @Override
    public List<String> deny() {
        List<String> list = new ArrayList<>();
        try {
            Resource myAllowWords = new ClassPathResource("/static/mg.txt");
            Path myAllowWordsPath = Paths.get(myAllowWords.getFile().getPath());
            list = Files.readAllLines(myAllowWordsPath, StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            log.warn("读取非敏感词文件错误！" + ioException.getMessage());
        }
        return list;
    }

}
