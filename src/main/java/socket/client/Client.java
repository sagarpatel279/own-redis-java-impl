package socket.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final int clientId;
    public Client(Socket socket, InputStream inputStream, OutputStream outputStream,int clientId) {
        this.socket=socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.clientId=clientId;
    }

    public Socket getSocket() {
        return socket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public int getClientId() {
        return clientId;
    }
}
