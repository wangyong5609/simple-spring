package com.wy.service;

import com.wy.spring.BeanPostProcessor;
import com.wy.spring.Component;

import java.util.Objects;

@Component
public class WyPostProcessor implements BeanPostProcessor {
    @Override
    public void postProcessBeforeInitialization(String beanName, Object Bean) {
        if (Objects.equals(beanName, "userService")){
            System.out.println("userService postProcessBeforeInitialization"); 
        }
    }

    @Override
    public void postProcessAfterInitialization(String beanName, Object Bean) {
        if (Objects.equals(beanName, "userService")){
            System.out.println("userService postProcessAfterInitialization");
        }
    }
}
