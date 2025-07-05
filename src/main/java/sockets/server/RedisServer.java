package sockets.server;


import components.handlers.ClientHandler;
import components.handlers.CommandHandler;
import components.repos.ExpiringMap;
import configurations.ApplicationConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sockets.client.Client;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisServer {
    private final int port;
    private static int clientId;
    public RedisServer(int port){
        this.port=port;
    }
    public void startServer() throws UnknownHostException {
        ExecutorService executor= Executors.newCachedThreadPool();
        InetAddress localAddress = InetAddress.getByName("0.0.0.0"); // or any other local IP
        int backlog = 50;
        try (ServerSocket serverSocket = new ServerSocket(port,backlog,localAddress)) {
            serverSocket.setReuseAddress(true);
            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
            System.out.println("Server started on: " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
            while (true) {
                System.out.println("======Waiting for client=======");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected....");
//                clientSocket.setSoTimeout(20000);//20 Seconds timeout for each user
                Client client=new Client(clientSocket,clientSocket.getInputStream(),clientSocket.getOutputStream(),new CommandHandler(context.getBean(ExpiringMap.class)),++clientId);
                executor.submit(new ClientHandler(client));
            }
        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }
}
