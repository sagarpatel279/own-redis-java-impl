package com.codecrafts.ownredis;

import com.codecrafts.ownredis.configurations.ApplicationConfiguration;
import com.codecrafts.ownredis.sockets.server.RedisServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.UnknownHostException;

//@SpringBootApplication
public class Main {
    public static void main(String[] args) throws UnknownHostException {
//        SpringApplication.run(Main.class, args);
        ApplicationContext context=new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        RedisServer redisServer=new RedisServer(context);
        redisServer.startServer();
    }
}
