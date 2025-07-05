package sockets.server;


import components.handlers.ClientHandler;
import components.handlers.CommandHandler;
import components.repos.ExpiringMap;
import configurations.ApplicationConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import sockets.client.Client;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
@RequiredArgsConstructor
public class RedisServer {
//    private final ApplicationContext context;
//    private static int clientId;
//    private final ExecutorService executor = Executors.newCachedThreadPool();
//
//    public void startServer() throws UnknownHostException {
//        InetAddress localAddress = InetAddress.getByName("0.0.0.0"); // or any other local IP
//        try (ServerSocket serverSocket = new ServerSocket(6389,50,localAddress)) {
//            serverSocket.setReuseAddress(true);
//            System.out.println("Server started on: " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
//            while (true) {
//                System.out.println("======Waiting for client=======");
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("Client connected....");
////                clientSocket.setSoTimeout(20000);//20 Seconds timeout for each user
//                Client client=new Client(clientSocket,context.getBean(CommandHandler.class),++clientId);
//                executor.submit(new ClientHandler(client));
//            }
//        } catch (Exception e) {
//            System.out.println("Server exception: " + e.getMessage());
//        }
//    }
    private final ApplicationContext context;
    private static int clientId;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostConstruct
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(6389, 50, InetAddress.getByName("0.0.0.0"))) {
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
