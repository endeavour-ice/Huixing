package com.ice.hxy.util;

import cn.hutool.core.util.RandomUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/3/21 18:16
 * @Description: TODO
 */
public class AvatarUrlUtils {
    private static final List<String> url = Arrays.asList("https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211345104010.webp",
            "https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211345104014.webp",
            "https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211345515494.webp",
            "https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211345515497.webp",
            "https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/VCG211348014461.webp");
    private static final List<String> team_url = Collections.singletonList("https://bing-edu.oss-cn-hangzhou.aliyuncs.com/user/2023/03/20/uTools_1679882007428.png");
    public static String getRandomUrl() {
        int anInt = RandomUtil.randomInt(0, url.size());
        return url.get(anInt);
    }

    public static List<String> getUrl() {
        return url;
    }

    public static List<String> getTeam_url() {
        return team_url;
    }
    public static String getRandomUrl(List<String> list) {
        if (list.size() <= 0) {
            return "";
        } else if (list.size() == 1) {
            return list.get(0);
        }else {
            int anInt = RandomUtil.randomInt(0, list.size());
            return url.get(anInt);
        }


    }
}
