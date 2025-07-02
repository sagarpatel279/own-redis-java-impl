package socket.server;


import socket.client.ClientHandle;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisServer {
    private final int port;
    public RedisServer(int port){
        this.port=port;
    }
    public void startServer(){
        ExecutorService executor= Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Server running on port " + port);
            while (true) {
                System.out.println("======Waiting for client=======");
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandle(clientSocket));
            }
        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }
}
