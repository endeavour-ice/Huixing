package com.ice.hxy.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.ice.hxy.service.commService.HttpService;
import com.ice.hxy.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author ice
 * @Date 2022/9/27 22:17
 * @ClassName: IpUtils
 * @Description: ip 工具类
 * @Version 1.0
 */
@Slf4j
public class IpUtils {
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";
    private static final HttpService httpService = SpringUtil.getBean(HttpService.class);
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    // 客户端与服务器同为一台机器，获取的 ip 有时候是 ipv6 格式
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String SEPARATOR = ",";
    public static String getIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return LOCALHOST_IPV6.equals(ip) ? LOCALHOST_IP : getMultistageReverseProxyIp(ip);
    }
    public static boolean isUnknown(String checkString) {
        return !StringUtils.hasText(checkString) || UNKNOWN.equalsIgnoreCase(checkString);
    }
    public static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(SEPARATOR) > 0) {
            final String[] ips = ip.trim().split(SEPARATOR);
            for (String subIp : ips) {
                if (!isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }
    // 根据 HttpServletRequest 获取 IP
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST_IP.equalsIgnoreCase(ip) || LOCALHOST_IPV6.equalsIgnoreCase(ip)) {
                // 根据网卡取本机配置的 IP
                InetAddress iNet;
                try {
                    iNet = InetAddress.getLocalHost();
                    if (iNet != null) {
                        ip = iNet.getHostAddress();
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

            }
        }
        // 对于通过多个代理的情况，分割出第一个 IP
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(SEPARATOR) > 0) {
                ip = ip.substring(0, ip.indexOf(SEPARATOR));
            }
        }
        return LOCALHOST_IPV6.equals(ip) ? LOCALHOST_IP : ip;
    }
    // 根据ip 获取 地理位置
    public static String getRealAddressByIP(String ip) {
            try {
                if (ip.equals(LOCALHOST_IP)) {
                    return LOCALHOST_IP;
                }
                String rspStr = sendAddress(IP_URL+ "ip=" + ip + "&json=true");
                if (!StringUtils.hasText(rspStr)) {
                    log.error("获取地理位置异常 {}", ip);
                    return UNKNOWN;
                }
                JSONObject obj = JSON.parseObject(rspStr);
                String region = obj.getString("pro");
                String city = obj.getString("city");
                return String.format("%s %s", region, city);
            } catch (Exception e) {
                log.error("获取地理位置异常 {}", ip);
            }

        return UNKNOWN;
    }
    private static String sendAddress(String url) {
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("accept", "*/*");
        httpHeaders.add("connection", "Keep-Alive");
        httpHeaders.add("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        ResponseEntity<String> response = httpService.get(url, httpHeaders, String.class);
        try {
            if (response.getStatusCodeValue()==200) {
                // 判断返回状态是否为200
                return response.getBody();
            }
            return UNKNOWN;
        } catch (Exception e) {

            log.error("调用HttpUtils.sendGet ConnectException, url=" + url);
            return UNKNOWN;
        }
    }
}
