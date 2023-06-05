package com.ice.hxy.util;

import com.google.gson.Gson;
import com.ice.hxy.mode.comm.ChinesePoetry;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @Author ice
 * @Date 2023/5/27 16:52
 * @Description: TODO
 */
public class ChinesePoetryUtil {
    private static List<ChinesePoetry> chinesePoetries = null;

    static {
        try {
            Resource resource = new ClassPathResource("/cs/宋词三百首.json");
            InputStream inputStream = resource.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            String string = FileCopyUtils.copyToString(reader);
            Gson gson = GsonUtils.getGson();
            ChinesePoetry[] c = gson.fromJson(string, ChinesePoetry[].class);
            chinesePoetries = Arrays.asList(c);
        } catch (IOException ignored) {

        }

    }

    public static ChinesePoetry getRandomPoetry() {
        if (chinesePoetries.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int index = random.nextInt(chinesePoetries.size());
        return chinesePoetries.get(index);
    }
}
