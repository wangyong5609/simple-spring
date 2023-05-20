package com.wy.spring;

/**
 * Bean后置处理程序
 */
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(String beanName, Object bean);
    Object postProcessAfterInitialization(String beanName, Object bean);
}
