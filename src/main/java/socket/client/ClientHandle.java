package socket.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientHandle implements Runnable {
    private final Socket clientSocket;
    private final ConcurrentMap<Object,Object> concurrentMap=new ConcurrentHashMap<>();
    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private void handleClient() {
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

        try(Scanner sc = new Scanner(clientSocket.getInputStream());
            OutputStream writer = clientSocket.getOutputStream()) {
            while(sc.hasNextLine()){
                String message=sc.nextLine();

                writer.flush();
            }
        }catch (Exception e) {
            System.out.println("Client handler error: " + e.getMessage());
        }finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Close error: " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        handleClient();
    }
}
