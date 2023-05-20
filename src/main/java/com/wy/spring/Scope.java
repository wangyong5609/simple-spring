package com.wy.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * bean的类型，单例或者多例
 */
@Retention(RetentionPolicy.RUNTIME)
// 在类上面使用
@Target(ElementType.TYPE)
public @interface Scope {
    String value() default "";
}
