package com.wy.spring;

import com.wy.config.AppConfig;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;

public class ApplicationContext {
    private Class configClass;

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
                                 
                             }
                         } catch (ClassNotFoundException e) {
                             throw new RuntimeException(e);
                         }
                     }
                     
                 }
             }

         }
    }

    public Object getBean(String beanName) {
        return null;
    }
}
