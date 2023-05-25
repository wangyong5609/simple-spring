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

earlySingletonObjects中放的是没有经过Bean创建完整生命周期的对象

earlySingletonObjects的作用是为了保证bean单例。

![image-20230521195858057](https://gitee.com/hammer-w/images/raw/master/image-20230521195858057.png)

### P26 第三级缓存singletonFactories的作用

第三级缓存里存了一个lambda表达式，这个表达式的作用是拿到普通对象然后生成代理对象， 然后把代理对象放入earlySingletonObjects

![image-20230521201707789](https://gitee.com/hammer-w/images/raw/master/image-20230521201707789.png)

### P30 ImportBeanDefinitionRegistrar的作用

通过FactoryBean接口生成Mybatis Mapper Bean，核心代码：

~~~java
AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
applicationContext.register(AppConfig.class);
// 创建BeanDefinition，指定bean类型为FactoryBean.class
AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
beanDefinition.setBeanClass(ZhouyuFactoryBean.class);
// 然后通过执行构造函数的参数值
beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(UserMapper.class);
// 将BeanDefinition注册到applicationContext中
applicationContext.registerBeanDefinition("userMapper", beanDefinition);

applicationContext.refresh();
~~~

Spring在创建Bean ZhouyuFactoryBean 的时候，发现它实现了FactoryBean接口，就会调用实现类的getObject方法，而返回的对象类型已经通过构造函数定义好了

另一种创建方式：基于@Import 和ImportBeanDefinitionRegistrar接口

~~~java
public class WyBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
        beanDefinition.setBeanClass(ZhouyuFactoryBean.class);
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(UserMapper.class);
        registry.registerBeanDefinition("userMapper", beanDefinition);
    }
}
~~~

在启动类上导入

~~~
@Import(WyBeanDefinitionRegistrar.class)
~~~

### P31 Mapper扫描的底层原理

1. 定义一个@MapperScan注解,在启动类使用该注解，值为mapper包路径

2. 在@MapperScan注解上添加 @Import(WyBeanDefinitionRegistrar.class)

   ~~~java
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   @Import(WyBeanDefinitionRegistrar.class)
   public @interface MapperScan {
       String value() default "";
   }                                            
   ~~~

3. 修改WyBeanDefinitionRegistrar.registerBeanDefinitions()

   1. 从AnnotationMetadata中拿到我们@MapperScan注解中定义的包扫描路径

   2. 自定义一个Scanner扫描器，去扫描包下面的mapper

   3. Spring的扫描器是不处理接口类的，所以需要重写扫描器的isCandidateComponent方法，让扫描器只处理接口

   4. 然后重写doScan方法，doScan方法默认返回扫描到的类的Definition，我们在上面的章节中提到，我们需要的是ZhouyuFactoryBean的BeanDefinition,

      所以在方法中将UserMapper转换为ZhouyuFactoryBean，并将UserMapper.class通过构造函数传给ZhouyuFactoryBean。

   5. 将doScan方法返回的BeanDefinition注册到Spring容器中

### P34 Spring中的Bean name生成机制

bean name默认由Spring的BeanNameGenerator生成，我们也可以定义自己的BeanNameGenerator，只要继承它即可。

bean name默认首字母小写，如果Bean类名前两个字母都是大写，那么将用类名作为Bean name。

### P35 ScopedProxyMode的作用

ScopedProxyMode 属性可用在@Component和@ComponentScan中，它的作用是，当Bean的scope是request或者session时，需要为它创建代理对象注入到其他单例Bean中，ScopedProxyMode指定生成代理对象的方式：CGLib，JDK等。

resourcePattern属性：指定@ComponentScan扫描的路径规则，默认"* */ *.class", 扫描指定包名下所有class文件

### P35 ExcludeFilter机制

ExcludeFilter的目的是为了将扫描到的一些类排除掉，不实例化为Bean。

### P38 扫描中的ASM技术

ASM技术是通过字节码的规则来获取类信息和注解信息，从而判定类是否需要加载为Bean。

假如一个包下有上千个类，但是只有少部分是Bean，那么ASM技术比将类加载到JVM中利用反射去扫描Bean的方式要节省资源

### P40 扫描中的独立类，抽象类，接口

Spring中类实例化Bean的几个条件：

- 独立类：类中不能有普通内部类（静态内部类可以）
- 不能是接口或者抽象类
- 虽然是抽象类，但是有方法使用了@LookUp注解

### P41 @LookUp注解与ComponentsIndex

LookUp注解用来查找Bean，可以用在抽象类和普通类的方法上，如果是多例Bean，则方法每执行一次就会生成一个新的实例，方法体不会执行。

ComponentsIndex是一种包扫描优化机制，将要扫描的Bean定义在spring.components文件中，Spring就会直接扫描文件中定义的类，而不用对包进行遍历。