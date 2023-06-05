package com.ice.hxy.annotation;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ice.hxy.config.SensitiveConfig.SensitiveInfoSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 脱敏注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveInfoSerialize.class)
public @interface Sensitive {
    DesensitizedUtil.DesensitizedType value();
}
