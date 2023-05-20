课程地址：https://www.bilibili.com/video/BV1Tz4y1a7FM?p=3&vd_source=9673597c3ab5b03cf9994776ea4b5fc2

## 笔记

### P17

**Bean创建的生命周期**

*.class->推断构造方法->对象->依赖注入->初始化前（PostConstruct）->初始化（InitializingBean）->初始化后（AOP）->代理对象->放入单例池->Bean

**推断构造函数**

bean在初始化时，默认调用无参构造方法。如果类中有且只有一个构造函数，那么会使用这个构造函数初始化Bean，
如果有两个构造函数，Spring也不知道该调用哪个构造函数了就会抛异常。
这时可以通过在构造函数上添加Autowired注解告诉Spring使用哪个构造函数， 如果同时在多个构造函数上加了Autowired注解，spring还是不知道该使用哪个构造函数。

**构造函数依赖注入**
假如有这样一个构造函数：

~~~java
@Component
public class OrderService implements OrderInterface{
    public OrderService(UserService userService) {
    }
}
~~~

通过构造函数进行依赖注入时，Spring会判定是否有UserService类型的Bean。

如果找到一个UserService Bean对象，则直接注入。如果找到多个UserService Bean对象，则通过参数名称去单例池中匹配名为userService的Bean对象。

如果找到则直接注入，找不到则抛出异常信息“ no qualifying bean of type ...UserService”.

这里有个口诀：先By type，再By name。