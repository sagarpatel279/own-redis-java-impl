package com.codecrafts.ownredis.sockets.client;

import lombok.Getter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Getter
public class Client {
    private final Socket socket;
    private final int clientId;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public Client(Socket socket, int clientId) throws IOException {
        this.socket = socket;
        this.clientId = clientId;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }
}
