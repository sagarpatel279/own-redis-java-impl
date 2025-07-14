package com.codecrafters.ownredis.sockets.server;


import com.codecrafters.ownredis.components.handlers.ClientHandler;
import com.codecrafters.ownredis.components.handlers.CommandHandler;
import com.codecrafters.ownredis.configurations.RDBConfig;
import com.codecrafters.ownredis.sockets.client.Client;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
@RequiredArgsConstructor
public class RedisServer implements CommandLineRunner {
    private final ApplicationContext context;
    private static int clientId;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
//    @Value("${ownredis.host:0.0.0.0}")
//    String host="0.0.0.0";
//    @Value("${ownredis.port:6379}")
    private int port=6379;
    private String dir=null;
    private String dbFileName=null;
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Redis Server started on port: "+port);
            while (true) {
                System.out.println("==================================Waiting for client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client: "+clientSocket.getRemoteSocketAddress()+" has connected...");
                Client client = new Client(clientSocket, ++clientId);
                CommandHandler handler = context.getBean(CommandHandler.class);
                RDBConfig rdbConfig=context.getBean(RDBConfig.class);
                rdbConfig.setDir(dir);
                rdbConfig.setDbFileName(dbFileName);
                executor.submit(new ClientHandler(client, handler));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if ("--dir".equals(args[i])) {
                dir = args[i + 1];
                i++;
            } else if ("--dbfilename".equals(args[i])) {
                dbFileName = args[i + 1];
                i++;
            }
        }
        Thread redisThread = new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                System.err.println("Redis server error: " + e.getMessage());
            }
        });
        redisThread.setName("Redis-Socket-Server");
        redisThread.start();
    }
}
