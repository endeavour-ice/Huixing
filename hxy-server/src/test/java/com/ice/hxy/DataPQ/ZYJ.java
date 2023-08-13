package com.ice.hxy.DataPQ;

import cn.hutool.http.HttpRequest;

/**
 * @Author ice
 * @Date 2023/8/12 22:46
 * @Description: TODO
 */
public class ZYJ {
    public static void main(String[] args) {
        System.out.println(get("https://www.jobui.com/trends/quanguo/", ""));
    }

    public static String get(String url, String cookie) {
       return HttpRequest.get(url)
                .header("content-type", "application/json; charset=UTF-8")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36").execute().body();
    }
}
