package com.wy.service;

import com.wy.spring.BeanPostProcessor;
import com.wy.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

@Component
public class WyPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        if (Objects.equals(beanName, "userService")){
            System.out.println("userService postProcessBeforeInitialization"); 
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        if (Objects.equals(beanName, "userService")){
            System.out.println("userService postProcessAfterInitialization");
        }

        if (Objects.equals(beanName, "orderService")){
            System.out.println("orderService postProcessAfterInitialization");
            // 生成代理对象
            Object proxyInstance = Proxy.newProxyInstance(OrderService.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
