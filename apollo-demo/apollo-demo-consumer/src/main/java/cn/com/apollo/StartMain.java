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
