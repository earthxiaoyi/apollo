# apolllo
 
##Overview
apollo是一个高性能、轻量级的开源java RPC框架，它提供了服务自动注册发现、负载均衡和自动容错、面向接口的远程方法调用等核心功能。

##Features

* 创建分布式服务不需要额外编写代码。
* 提供集群支持，并支持zookeeper做服务发现
* 支持负载均衡和自动容错
* 支持高可用

##Quick Start

要求：

1.JDK 1.8 or above

2.基于Java的项目管理软件maven

###同步调用
1.将依赖加入到pom中：

    <dependency>
        <groupId>cn.com</groupId>
        <artifactId>apollo-config-spring</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>


2.为provider和consumer创建一个接口：
    
    src/main/java/cn/com/apollo/demo/HelloService.java 
   <br>
    
    package cn.com.apollo.demo;
    
    public interface HelloService {
    
        String sayHello(String name);
    
    }

3.为这个接口编写一个实现：

    src/main/java/cn/com/apollo/demo/HelloServiceImpl.java 
<br>
   
    package cn.com.apollo.demo;
    
    import org.springframework.stereotype.Service;
    
    @Service
    public class HelloServiceImpl implements HelloService {
        
        @Override
        public String sayHello(String name) {
            return "hello," + name;
        }
    
    }

<br>

   1）将我们创建好的接口实现暴露出去：

    src/main/resources/apollo-server.xml
<br>
    
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:apollo="http://www.apollo.com/schema/apollo"
           xmlns="http://www.springframework.org/schema/beans"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
           http://www.apollo.com/schema/apollo
           http://www.apollo.com/schema/apollo.xsd">
    
        <apollo:application name="server"/>
        
        <apollo:nameservice id="nameService" protocol="zookeeper" address="127.0.0.1:2181"/>
        
        <bean id="helloServiceImpl" class="cn.com.apollo.demo.HelloServiceImpl"/>
        
        <apollo:service interface="cn.com.apollo.demo.HelloService" ref="helloServiceImpl" port="20030"/>
    
    </beans>
<br>

2）运行provider server：

    src/main/java/cn/com/apollo/demo/StartMain.java
    
    package cn.com.apollo.demo;
    
    import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
    import org.springframework.context.annotation.ComponentScan;
    import org.springframework.context.support.ClassPathXmlApplicationContext;
    
    import java.util.concurrent.CountDownLatch;
    
    @ComponentScan("cn.com.apollo.demo.**")
    @EnableAutoConfiguration
    public class StartMain {
    
        public static void main(String[] args) throws InterruptedException {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("apollo-server.xml");
            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatch.await();
        }
    
    }
    
4.创建一个consumer并启动服务：
    
    src/main/resources/apollo-client.xml
<br>
    
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:apollo="http://www.apollo.com/schema/apollo"
           xmlns="http://www.springframework.org/schema/beans"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
           http://www.apollo.com/schema/apollo
           http://www.apollo.com/schema/apollo.xsd">
    
        <apollo:application name="client"/>
    
        <apollo:nameservice id="nameService" protocol="zookeeper" address="127.0.0.1:2181"/>
    
        <apollo:reference id="helloService" interface="cn.com.apollo.demo.HelloService" timeout="5000"
                          group="catgroup" uri="localhost:8890"/>
    
    </beans>
<br>

运行consumer server并调用服务：
    
    package cn.com.apollo;
    
    import cn.com.apollo.demo.HelloService;
    import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
    import org.springframework.context.support.ClassPathXmlApplicationContext;
    
    import java.util.concurrent.CountDownLatch;
    
    @EnableAutoConfiguration
    public class StartMain {
    
        public static void main(String[] args) throws InterruptedException {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("apollo-client.xml");
            HelloService helloService = context.getBean("helloService", HelloService.class);
            int n = 3;
            for (int i = 0; i < n; i++) {
                new Thread(new WorkThread(helloService)).start();
            }
            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatch.await();
        }
    
        static class WorkThread implements Runnable {
    
            private HelloService service;
    
            public WorkThread(HelloService service) {
                this.service = service;
            }
    
            @Override
            public void run() {
                String requestStr = "111111";
                for (int i = 0; i < 10000; i++) {
                    try {
                        String result = service.sayHello(requestStr);
                        if (i % 1000 == 0) {
                            System.out.println(result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(Thread.currentThread().getName()+" finish!");
            }
        }
    
    }

##License
  Apollo is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).