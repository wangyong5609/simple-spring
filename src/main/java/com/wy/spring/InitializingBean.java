package com.wy.spring;

/**
 * Bean初始化接口
 * 当Bean实例化时，就会调用afterPropertiesSet方法
 */
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
