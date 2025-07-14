package com.codecrafters.ownredis.sockets.server;


import com.codecrafters.ownredis.components.handlers.ClientHandler;
import com.codecrafters.ownredis.components.handlers.CommandHandler;
import com.codecrafters.ownredis.sockets.client.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
@RequiredArgsConstructor
public class RedisServer {
    private final ApplicationContext context;
    private static int clientId;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private int port=6379;

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
                executor.submit(new ClientHandler(client, handler));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void run(String... args) throws Exception {
//
//        Thread redisThread = new Thread(() -> {
//            try {
//                startServer();
//            } catch (Exception e) {
//                System.err.println("Redis server error: " + e.getMessage());
//            }
//        });
//        redisThread.setName("Redis-Socket-Server");
//        redisThread.start();
//    }
}
