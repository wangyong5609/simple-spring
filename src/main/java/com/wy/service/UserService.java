package com.wy.service;

import com.wy.spring.*;

@Component("userService")
@Scope("prototype")
public class UserService implements BeanNameAware, InitializingBean {
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

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化Bean
    }
}
