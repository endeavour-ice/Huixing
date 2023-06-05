package com.ice.hxy.util;


import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import java.util.Map;

/**
 * 动态加载方法
 */
public class DyMethodUtil {

    public static Object invokeMethod(String jexlExp, Map<String, Object> map) {
        JexlEngine jexl = new JexlEngine();
        Expression e = jexl.createExpression(jexlExp);
        JexlContext jc = new MapContext();
        for (String key : map.keySet()) {
            jc.set(key, map.get(key));
        }
        if (null == e.evaluate(jc)) {
            return "";
        }
        return e.evaluate(jc);
    }

    public void A(String a) {
        System.out.println(a);
    }
}