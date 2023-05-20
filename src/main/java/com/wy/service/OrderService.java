package com.wy.service;

import com.wy.spring.Component;

@Component
public class OrderService implements OrderInterface{
    @Override
    public void test() {
        System.out.println("OrderService test");
    }
}
