package com.wy.spring;


import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring容器
 * 1. 通过注解扫描搜有的bean
 * 2. 实例化单例bean
 */
public class ApplicationContext {
    private Class configClass;
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> beanObjects = new ConcurrentHashMap<>();

    public ApplicationContext(Class appConfigClass) {
        this.configClass = appConfigClass;

        // 类上是否有Component注解
        if (appConfigClass.isAnnotationPresent(ComponentScan.class)) {

            ComponentScan annotation = (ComponentScan) appConfigClass.getAnnotation(ComponentScan.class);
            // 拿到注解的包路径
            String packagePath = annotation.value();

            //拿到这个包的绝对路径
            String path = packagePath.replace(".", "/");
            ClassLoader classLoader = ApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            // 扫描绝对路径下的文件
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                for (File listFile : file.listFiles()) {
                    System.out.println("listFile.getPath() = " + listFile.getAbsolutePath());

                    // 是否为class文件
                    if (listFile.getName().endsWith(".class")) {
                        // 获取类路径
                        String absolutePath = listFile.getAbsolutePath();
                        String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                        className = className.replace("\\", ".");

                        System.out.println(className);
                        try {
                            Class<?> aClass = classLoader.loadClass(className);
                            if (aClass.isAnnotationPresent(Component.class)) {
                                // 不直接生成Bean对象，因为Bean有多种形态，单例，多例，按需加载等等
                                // 所以生成BeanDefinition对象
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(aClass);
                                // 判断class上有没有scope注解
                                if (aClass.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = aClass.getAnnotation(Scope.class);
                                    String scope = scopeAnnotation.value();
                                    beanDefinition.setScope(scope);
                                } else {
                                    beanDefinition.setScope("singleton");
                                }

                                Component componentAnnotation = aClass.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();
                                if ("".equals(beanName) || beanName == null) {
                                    beanName = Introspector.decapitalize(aClass.getSimpleName());
                                }
                                
                                // 将beanDefinition存入map
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }

        }

        // 创建单例bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            Object bean = createBean(beanName, beanDefinition);
            if (bean != null) {
                beanObjects.put(beanName, bean);
            }
        }

    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();
        Object instance;
        try {
            // 使用无参构造方法创建实例
            instance = clazz.getConstructor().newInstance();
            // 依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(instance, getBean(field.getName()));
                }
            }
            // beanName回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware)instance).setBeanName(beanName);
            }
            // 初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean)instance).afterPropertiesSet();
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    public Object getBean(String beanName) {
        // 单例bean可以从bean池子里面取， 多例bean需要新建
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        }
        if (beanDefinition.getScope().equals("singleton")) {
            Object bean = beanObjects.get(beanName);
            if (bean == null) {
                bean = createBean(beanName, beanDefinition);
                assert bean != null;
                beanObjects.put(beanName, bean);
            }
            return bean;
        } else {
            return createBean(beanName, beanDefinition);
        }
    }
}
