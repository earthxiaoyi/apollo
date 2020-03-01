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