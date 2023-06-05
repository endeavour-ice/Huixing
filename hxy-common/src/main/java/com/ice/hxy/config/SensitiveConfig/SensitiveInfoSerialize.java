package com.ice.hxy.config.SensitiveConfig;


import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.ice.hxy.annotation.Sensitive;

import java.io.IOException;

/**
 * @Author ice
 * @Date 2023/5/4 9:12
 * @Description: 脱敏序列化器
 */
public class SensitiveInfoSerialize extends JsonSerializer<String> implements ContextualSerializer {

    private DesensitizedUtil.DesensitizedType desensitizedType;

    @Override

    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(DesensitizedUtil.desensitized(value, desensitizedType));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Sensitive sensitive = property.getAnnotation(Sensitive.class);
        if (sensitive!=null) {
            this.desensitizedType = sensitive.value();
            return this;
        }
        return prov.findValueSerializer(property.getType());
    }
}
