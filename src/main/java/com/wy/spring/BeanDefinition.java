package com.wy.spring;

/**
 * Bean定义信息
 */
public class BeanDefinition {
    /**
     * bean的类型
     */
    private Class type;
    /**
     * bean的作用域
     */
    private String scope;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
