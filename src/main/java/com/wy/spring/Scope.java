package com.wy.spring;

/**
 * bean的类型，单例或者多例
 */
public @interface Scope {
    String value() default "";
}
