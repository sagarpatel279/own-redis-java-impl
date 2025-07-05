package com.codecrafts.ownredis.components.handlers;

import lombok.RequiredArgsConstructor;
import com.codecrafts.ownredis.resp.parser.RESPArrayParser;
import com.codecrafts.ownredis.sockets.client.Client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
@RequiredArgsConstructor
public class ClientHandler implements Runnable {
    private final Client client;
    private final CommandHandler commandHandler;

    @Override
    public void run() {
        try (InputStream stream = client.getInputStream(); OutputStream writer = client.getOutputStream()) {
            client.getSocket().setSoTimeout(20000);
            while (client.getSocket().isConnected()) {
                if (stream.available() <= 0) continue;
                RESPArrayParser parser = RESPArrayParser.getBuilder().setInputStream(stream).build();
                commandHandler.setCommandQueue(parser.getCommandList());
                String commandName = commandHandler.pullCommand().toString();
                String response = commandHandler.handleCommand(commandName);
                writer.write(response.getBytes());
                writer.flush();
            }
        } catch (SocketTimeoutException ste) {
            System.out.println("Client Timeout: " + client.getClientId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
