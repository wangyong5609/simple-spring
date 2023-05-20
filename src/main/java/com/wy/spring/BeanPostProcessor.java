package com.wy.spring;

/**
 * Bean后置处理程序
 */
public interface BeanPostProcessor {
    void postProcessBeforeInitialization(String beanName, Object Bean);
    void postProcessAfterInitialization(String beanName, Object Bean);
}
