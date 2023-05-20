package com.wy.service;

import com.wy.config.AppConfig;
import com.wy.spring.ApplicationContext;

public class Test {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
        // 这里必须使用接口接收返回值，因为AOP代理的是OrderInterface
        OrderInterface orderService = (OrderInterface) applicationContext.getBean("orderService");
        orderService.test();
    }
}
