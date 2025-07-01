import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        int port = 6379;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Server running on port " + port);
//            while (true) {
                System.out.println("======Waiting for client=======");
                Socket clientSocket = serverSocket.accept();
                ClientHandle clientHandle= new ClientHandle(clientSocket);
                clientHandle.handleClient();
//            }

        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }
}

