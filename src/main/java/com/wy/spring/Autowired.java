package com.wy.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 生效时间
@Retention(RetentionPolicy.RUNTIME)
// 在字段上面使用
@Target(ElementType.FIELD)
public @interface Autowired {
}
