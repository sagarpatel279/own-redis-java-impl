package resp.handlers;

import static resp.constants.RESPParserConstants.*;
import static resp.constants.RESPCommandsConstants.*;
import static resp.constants.RESPEncodingConstants.*;

import resp.parser.RESPArrayParser;
import socket.client.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClientHandler implements Runnable {
    private final Client client;
    private CommandHandler commandHandler;
    public ClientHandler(Client client) {
        this.client = client;
    }
    @Override
    public void run() {
        handleClient();
    }
    private void handleClient() {
        System.out.println("Client connected: " + client.getSocket().getRemoteSocketAddress());

        try(OutputStream writer = client.getOutputStream()) {
            while(true) {
                InputStream stream=client.getInputStream();
                if(stream.available()<=0)continue;
                RESPArrayParser parser = RESPArrayParser.getBuilder().setInputStream(stream).build();
                commandHandler= new CommandHandler(parser.getCommandList());

                String commandName = commandHandler.pollCommand().toString();
                String response;
                if(commandName.equalsIgnoreCase(C_PING)) {
                    response =  commandHandler.handlePingCommand();
                }else if(commandName.equalsIgnoreCase(C_ECHO)){
                    response = commandHandler.handleEchoCommand();
                }else if (commandName.equalsIgnoreCase(C_SET)) {
                    response = commandHandler.handleSetCommand();
                }else if (commandName.equalsIgnoreCase(C_GET)) {
                    response = commandHandler.handleGetCommand();
                }else{
                    response= BULK_STRING+NULL_VALUE;
                }
                writer.write(response.getBytes());
                writer.flush();
            }

        }catch (Exception e) {
            System.out.println("Client handler error: " + e.getMessage());
        }finally {
            try {
                client.getSocket().close();
            } catch (IOException e) {
                System.out.println("Close error: " + e.getMessage());
            }
        }
    }
}
