import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        int port = 6379;
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

