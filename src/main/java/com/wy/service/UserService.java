package com.wy.service;

import com.wy.spring.Autowired;
import com.wy.spring.BeanNameAware;
import com.wy.spring.Component;
import com.wy.spring.Scope;

@Component("userService")
@Scope("prototype")
public class UserService implements BeanNameAware {
    @Autowired
    private OrderService orderService;
    private String beanName;
    
    public void test(){
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
