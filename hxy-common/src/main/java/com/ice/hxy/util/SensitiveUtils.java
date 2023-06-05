package com.ice.hxy.util;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import sun.misc.Resource;
import toolgood.words.StringSearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/2/24 12:15
 * @Description: 过滤工具类
 */
@Slf4j
public class SensitiveUtils implements ApplicationListener<ContextRefreshedEvent> {
    private SensitiveUtils() {
    }

    private static final List<String> list = new ArrayList<>();
    private static final SensitiveWordBs sensitiveWordBs = SpringUtil.getBean(SensitiveWordBs.class);

    /**
     * 过滤字符
     * 默认*
     * @param text
     * @return
     */
    public static String sensitive(String text) {
        return sensitiveWordBs.replace(text);
    }

    /**
     * 过滤字符
     * @param text 文本
     * @param replaceChar 要替换的字符
     * @return 过滤后的文本
     */
    public static String sensitive(String text,char replaceChar) {
        return sensitiveWordBs.replace(text,replaceChar);
    }

    // 刷新敏感词库与非敏感词库缓存
    public static void refresh() {
        sensitiveWordBs.init();
    }
    // 判断是否含有敏感词
    public static boolean contains(String text) {
        return sensitiveWordBs.contains(text);
    }
    // 返回所有敏感词
    public static List<String> findAll(String text) {
        return sensitiveWordBs.findAll(text);
    }
    public static String getTxt(String text) throws Exception {
        URL url = Resource.class.getResource("static/mg.txt");
        if (url == null) {
            return text;
        }
        String path = url.getPath();
        if (list.isEmpty()) {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        StringSearch search = new StringSearch();
        search.SetKeywords(list);
        return search.Replace(text);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            try {
                URL url = Thread.currentThread().getContextClassLoader().getResource("static/mg.txt");
                if (url != null) {
                    String path = url.getPath();
                    if (list.isEmpty()) {
                        BufferedReader br = new BufferedReader(new FileReader(path));
                        String line;
                        while ((line = br.readLine()) != null) {
                            list.add(line);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("文件不存在");
            }
        }
    }
}
