package com.ice.hxy.extend.wx;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author ice
 * @Date 2023/3/24 14:01
 * @Description: TODO
 */
@Slf4j
public class WxUtils {

    public static String wxOfficialTokenCheck(String signature, String timestamp, String nonce, String echostr) {
        log.info("开始校验此次消息是否来自微信服务器，param->signature:{},\ntimestamp:{},\nnonce:{},\nechostr:{}",
                signature, timestamp, nonce, echostr);
        if (checkSignature(signature, timestamp, nonce)) {
            return echostr;
        }
        return "";
    }

    private static final String TOKEN = "TestTokens";

    /**
     * 校验微信服务器Token签名
     *
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @return boolean
     */
    public static boolean checkSignature(String signature, String timestamp, String nonce) {
        String[] arr = {TOKEN, timestamp, nonce};
        Arrays.sort(arr);
        StringBuilder stringBuilder = new StringBuilder();
        for (String param : arr) {
            stringBuilder.append(param);
        }

        String hexString = SHA1(stringBuilder.toString());
        return signature.equals(hexString);
    }

    private static String SHA1(String str) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(str.getBytes());
            return toHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            log.info("校验令牌Token出现错误：{}", e.getMessage());
        }
        return "";
    }

    /**
     * 字节数组转化为十六进制
     *
     * @param digest 字节数组
     * @return String
     */
    private static String toHexString(byte[] digest) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String shaHex = Integer.toHexString(b & 0xff);
            if (shaHex.length() < 2) {
                hexString.append(0);
            }
            hexString.append(shaHex);
        }
        return hexString.toString();
    }

    public static Map<String, String> xmlToMap(InputStream inputStream) {
        Map<String, String> map = new HashMap<>();
        try {
            SAXReader reader = new SAXReader();
            org.dom4j.Document document = reader.read(inputStream);
            Element root = document.getRootElement();
            List<Element> elementList = root.elements();
            // 遍历所有子节点
            for (Element e : elementList) {
                map.put(e.getName(), e.getText());
            }
            // 释放资源
            inputStream.close();
        } catch (IOException | DocumentException e) {
            log.info("xml转化为map出现异常：{}", e.getMessage());
        }
        return map;
    }

}
