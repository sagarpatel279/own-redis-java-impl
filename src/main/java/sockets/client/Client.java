package sockets.client;

import components.handlers.CommandHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final int clientId;
    private final CommandHandler commandHandler;
    public Client(Socket socket,CommandHandler commandHandler,int clientId) throws IOException {
        this.socket=socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.clientId=clientId;
        this.commandHandler=commandHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
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
