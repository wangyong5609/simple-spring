课程地址：https://www.bilibili.com/video/BV1Tz4y1a7FM?p=3&vd_source=9673597c3ab5b03cf9994776ea4b5fc2

## 笔记

### P17 推断构造方法底层原理

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

### P18 AOP底层实现原理

假如现在有下面一个Bean，对它做了AOP代理

~~~java
public class UserService() {
    @Autowire
    OrderService orderService;
    
    public void test() {
        orderService.test();
    }
}
~~~

Spring会使用CGLib为它生成这样一个代理类

~~~java
class UserServiceProxy extends UserService {
    UserService target;
    public void test() {
    	// 切面逻辑 @Before
        target.test(); // 普通对象.test();
    }
}
~~~

再看一下上面的Bean的生命周期

1. 代理对象在创建以后，没有进行依赖注入就放入了单例池，所以UserServiceProxy中的orderService是null。

2. 为了解决UserService中的依赖是null的情况，所以在代理类中加入了一个属性target，它的值就是普通对象，而普通对象是做过依赖注入的，所以用target调用UserService的test方法是可以调通的。

3. 代理对象会通过判断程序是否对类中的方法做了切面处理，在方法中加入@Before和@After的切面逻辑处理。

4. 代理类为什么要继承UserService呢，看起来好像不继承也能用，这是因为getBean() 从单例池中拿到的是代理对象，就像这样

   ~~~java
   UserService userService = (UserService)ApplicationContext.getBean("userService");
   ~~~

   如果不继承的话，这里是无法进行强制转换的。

### P19 Spring事务底层原理

首先开启事务管理，使用注解EnableTransactionManagement

Spring在生成Bean的时候会扫描@Transaction注解，并对使用了该注解的方法进行AOP处理

~~~java
@Transaction
public void test() {
    // 执行sql1 sql2
}
~~~

~~~java
class UserServiceProxy extends UserService {
    UserService target;
    public void test() {
    	// Spring事务切面逻辑
        // 开启事务
        // 事务管理器创建一个数据库连接，将autoCommit设为false
        target.test(); // 普通对象.test() ql1 sql2;
        // 如果test方法没有抛异常，则commit，否则rollback
    }
}
~~~

以上就是事务的一个简单原理

### P20 Spring事务失效原理

简单理解就是：执行方法的对象是普通对象，没有被事务机制管理。

代码场景：

~~~java
@Component
public class UserService() {
    @Transaction
    public void test1() {
        // 执行sql1
        test2();
    }
    @Transaction(propagation=Propagation.NEVER) // 如果在此方法外部发现事务支持就抛出异常
    public void test2() {
        // 执行sql2
	}
}
~~~

~~~java
UserService userService = (UserService)ApplicationContext.getBean("userService");
userService.test1();
~~~

在上面的场景中，理想情况是执行test2()抛出异常，但结果却是sql1和sql2都执行成功了，为什么呢

因为test1方法是被UserService的代理对象调用，可以参考P19的底层原理，代理对象会对test1做事务处理，对test1中的sql执行结果进行commit或者rollback。

实际执行test1方法的是一个普通对象，等同于 new UserService(), 普通对象里面调用test2方法，是不会有事务处理的，因此test2方法上的注解会失效。

1. userService.test1()
2. proxy.test1()
3. 事务切面处理
4. 普通对象.test1()
5. 普通对象.test2()

那么这里@Transaction注解怎么解决呢

本质上是因为执行方法的对象是普通对象，那么我们拿到代理对象去执行test2就可以了，下面有几种方式

1. 将test2方法另外写在其它BeanA里面，然后将BeanA注入到UserService中, 由BeanA来执行test2方法

2. 将UserService代理对象注入到UserService中（推荐）

   ~~~java
   @Component
   public class UserService() {
       @Autowired
       UserService userService;
       
       @Transaction
       public void test1() {
           // 执行sql1
           userService.test2();
       }
       @Transaction(propagation=Propagation.NEVER) // 如果在此方法外部发现事务支持就抛出异常
       public void test2() {
           // 执行sql2
   	}
   }
   ~~~


3. 使用AopContext.currentProxy()拿到当前代理对象

### P25 第二级缓存earlySingletonObjects的作用

earlySingletonObjects的作用是为了保证bean单例。

![image-20230521195858057](https://gitee.com/hammer-w/images/raw/master/image-20230521195858057.png)

