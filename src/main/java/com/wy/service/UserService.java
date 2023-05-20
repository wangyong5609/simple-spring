package com.wy.service;

import com.wy.spring.Autowired;
import com.wy.spring.Component;
import com.wy.spring.Scope;

@Component("userService")
@Scope("prototype")
public class UserService {
    @Autowired
    private OrderService orderService;
    
    public void test(){
        System.out.println(orderService);
    }
}
