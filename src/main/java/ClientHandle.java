import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ClientHandle {
    private final Socket clientSocket;

    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void handleClient() {
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream writer = clientSocket.getOutputStream()) {

            writer.write("+PONG\r\n".getBytes());
            writer.flush();

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
}
