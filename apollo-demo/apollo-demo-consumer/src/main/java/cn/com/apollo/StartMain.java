package cn.com.apollo;

import cn.com.apollo.demo.HelloService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@EnableAutoConfiguration
public class StartMain {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("apollo-client.xml");
        HelloService helloService = context.getBean("helloService", HelloService.class);
        long start = System.currentTimeMillis();
        String requestStr = "1111111111 2222222222 2222222222 2222222222 2222222222 1111111111 2222222222 2222222222 2222222222 2222222222";
        for (int i = 0; i < 10000; i++) {
            try {
                String result = helloService.sayHello(requestStr);
                if (i % 1000 == 0) {
                    System.out.println(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("毫秒：" + (end - start));
        /*CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();*/
    }

}
