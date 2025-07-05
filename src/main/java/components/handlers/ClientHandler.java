package components.handlers;

import static resp.constants.RESPParserConstants.*;
import static resp.constants.RESPCommandsConstants.*;
import static resp.constants.RESPEncodingConstants.*;

import resp.parser.RESPArrayParser;
import sockets.client.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

public class ClientHandler implements Runnable {
    private final Client client;
    public ClientHandler(Client client) {
        this.client = client;
    }
    @Override
    public void run() {
        handleClient();
    }
    private void handleClient() {
        System.out.println("Client connected: " + client.getSocket().getRemoteSocketAddress());

        try(OutputStream writer = client.getOutputStream();
                        InputStream stream=client.getInputStream()) {
            while(client.getSocket().isConnected()) {
                if(stream.available()<=0)continue;
                System.out.println("Handled By Thread: "+Thread.currentThread().getName());
                RESPArrayParser parser = RESPArrayParser.getBuilder().setInputStream(stream).build();
                client.getCommandHandler().setCommandQueue(parser.getCommandList());
                String commandName = client.getCommandHandler().pullCommand().toString();
                String response;
                if(commandName.equalsIgnoreCase(C_PING)) {
                    response =  client.getCommandHandler().handlePingCommand();
                }else if(commandName.equalsIgnoreCase(C_ECHO)){
                    response = client.getCommandHandler().handleEchoCommand();
                }else if (commandName.equalsIgnoreCase(C_SET)) {
                    response = client.getCommandHandler().handleSetCommand();
                }else if (commandName.equalsIgnoreCase(C_GET)) {
                    response = client.getCommandHandler().handleGetCommand();
                }else{
                    response= BULK_STRING+NULL_VALUE;
                }
                writer.write(response.getBytes());
                writer.flush();
            }

        }catch (SocketTimeoutException ste){
            System.out.println("Time out for Client: "+client.getSocket().getRemoteSocketAddress());
            try {
                client.getSocket().getOutputStream().write("Timeout..please reconnect".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }catch (Exception e) {
            System.out.println("Client handler error: " + e.getMessage());
        }finally {
            try {
                if(!client.getSocket().isClosed()) {
                    System.out.println("Client: "+client.getSocket().getRemoteSocketAddress()+" has been closed...");
                    client.getSocket().close();
                }
            } catch (IOException e) {
                System.out.println("Close error: " + e.getMessage());
            }
        }
    }
}
