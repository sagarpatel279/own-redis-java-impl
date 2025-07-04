package socket.server;


import resp.handlers.ClientHandler;
import socket.client.Client;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisServer {
    private final int port;
    private static int clientId;
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
                Client client=new Client(clientSocket,clientSocket.getInputStream(),clientSocket.getOutputStream(),++clientId);
                executor.submit(new ClientHandler(client));
            }
        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }
}
