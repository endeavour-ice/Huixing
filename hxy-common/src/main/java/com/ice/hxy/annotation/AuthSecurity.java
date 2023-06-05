package com.ice.hxy.annotation;

import com.ice.hxy.mode.enums.UserRole;

import java.lang.annotation.*;

/**
 * 权限注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthSecurity {
    UserRole[] isRole() default {};
    UserRole[] isNoRole() default {};

}
