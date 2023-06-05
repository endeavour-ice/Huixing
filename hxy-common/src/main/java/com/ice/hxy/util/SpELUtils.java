package com.ice.hxy.util;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @Author ice
 * @Date 2023/5/24 15:11
 * @Description: spring el表达式解析
 */
public class SpELUtils {
    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    /**
     * 解析 EL 表达式
     * @param expression 表达式字符串
     * @param clazz 返回值类型
     * @return 返回表达式解析后的值
     */
    public static <T> T parseExpression(String expression, Class<T> clazz,EvaluationContext context) {
        Expression expr = parser.parseExpression(expression);
        return expr.getValue(context,clazz);
    }
    public static String getMethodKey(Method method){
        return method.getDeclaringClass()+"#"+method.getName();
    }

    public static String parseSpEl(Method method, Object[] args, String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        String[] params = parameterNameDiscoverer.getParameterNames(method);//解析参数名
        if (params == null) {
            return key;
        }
        EvaluationContext context = new StandardEvaluationContext();//el解析需要的上下文对象
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);//所有参数都作为原材料扔进去
        }
        return parseExpression(key, String.class, context);
    }

}
