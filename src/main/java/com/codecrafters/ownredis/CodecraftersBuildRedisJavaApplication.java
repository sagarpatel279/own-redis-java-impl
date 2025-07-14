package com.codecrafters.ownredis;

import com.codecrafters.ownredis.configurations.ApplicationConfiguration;
import com.codecrafters.ownredis.components.config.RDBConfig;
import com.codecrafters.ownredis.sockets.server.RedisServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CodecraftersBuildRedisJavaApplication {

    public static void main(String[] args) {
        String dir=null;
        String dbFileName=null;
        for (int i = 0; i < args.length; i++) {
            if ("--dir".equals(args[i])) {
                dir = args[i + 1];
                i++;
            } else if ("--dbfilename".equals(args[i])) {
                dbFileName = args[i + 1];
                i++;
            }
        }
        ApplicationContext context =
                new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        RDBConfig rdbConfig=context.getBean(RDBConfig.class);
        rdbConfig.setDir(dir);
        rdbConfig.setDbFileName(dbFileName);
        RedisServer redisServer=new RedisServer(context);
        redisServer.startServer();
    }
}
