package com.wy.spring;

/**
 * Bean 名称回调
 * 在创建Bean时，判断Bean实现了该接口，然后调用bean的setBeanName方法，将容器中的beanName传给Bean
 */
public interface BeanNameAware {
    void setBeanName(String beanName);
}
