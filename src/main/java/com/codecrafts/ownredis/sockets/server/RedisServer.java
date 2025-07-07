package com.codecrafts.ownredis.sockets.server;


import com.codecrafts.ownredis.components.handlers.ClientHandler;
import com.codecrafts.ownredis.components.handlers.CommandHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.codecrafts.ownredis.sockets.client.Client;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
@RequiredArgsConstructor
public class RedisServer {
    private final ApplicationContext context;
    private static int clientId;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    @Value("${ownredis.host}")
    String host;
    @Value("${ownredis.port}")
    String port;

    @PostConstruct
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port), 50, InetAddress.getByName(host))) {
            serverSocket.setReuseAddress(true);
            System.out.println("Redis Server started on port 6389");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Client client = new Client(clientSocket, ++clientId);
                CommandHandler handler = context.getBean(CommandHandler.class);
                executor.submit(new ClientHandler(client, handler));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
