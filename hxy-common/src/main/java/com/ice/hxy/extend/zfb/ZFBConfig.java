package com.ice.hxy.extend.zfb;

import com.alipay.api.DefaultAlipayClient;
import com.ice.hxy.config.initConfig.ConstantProperties;


/**
 * @Author ice
 * @Date 2023/5/14 16:31
 * @Description: TODO
 */
public class ZFBConfig {
    public static DefaultAlipayClient getClient() {
        return new DefaultAlipayClient("https://openapi-sandbox.dl.alipaydev.com/gateway.do",
                ConstantProperties.ZFBAPPID, ConstantProperties.MERCHANTPRIVATEKEY, "json",
                "UTF-8", ConstantProperties.ALIPAYPUBLICKEY, "RSA2");

    }

}
