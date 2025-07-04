package socket.client;

import repos.ExpiringMap;
import static resp.constants.RESPParserConstants.*;
import static resp.constants.RESPConstantCommands.*;
import static resp.constants.RESPEncodingConstants.*;
import resp.parser.RESPArrayParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.*;

public class ClientHandle implements Runnable {
    private final Socket clientSocket;
    private final ExpiringMap<Object,Object> expiringMap=new ExpiringMap<>();
    private ConcurrentLinkedQueue<Object> commandQueue;
    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        handleClient();
    }
    private void handleClient() {
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

        try(OutputStream writer = clientSocket.getOutputStream()) {
            while(true) {
                InputStream stream=clientSocket.getInputStream();
                if(stream.available()<=0)continue;
                RESPArrayParser parser = RESPArrayParser.getBuilder().setInputStream(stream).build();
                Object commands = parser.parse();

                if(!(commands instanceof List))continue;

                commandQueue= new ConcurrentLinkedQueue<>((List<Object>) commands);

                System.out.println("Size of Commands: "+commandQueue.size()+" & List: "+commands);

                String commandName = pollCommand().toString();
                String response;
                if(commandName.equalsIgnoreCase(C_PING)) {
                    response =  handlePingCommand();
                }else if(commandName.equalsIgnoreCase(C_ECHO)){
                    response = handleEchoCommand();
                }else if (commandName.equalsIgnoreCase(C_SET)) {
                    response = handleSetCommand();
                }else if (commandName.equalsIgnoreCase(C_GET)) {
                    response = handleGetCommand();
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
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Close error: " + e.getMessage());
            }
        }
    }
    private Object pollCommand(){
        if(commandQueue==null){
            throw new NullPointerException("Command Queue is null");
        }else if(commandQueue.isEmpty())
            throw new NoSuchElementException("Queue is Empty");
        return commandQueue.poll();
    }
    private String handleEchoCommand(){
        String returnValue= pollCommand().toString();
        return BULK_STRING+returnValue.length()+C_CRLF+returnValue+C_CRLF;
    }

    private String handlePingCommand(){
        return SIMPLE_STRING+C_PONG+C_CRLF;
    }

    private String handleSetCommand(){
        Object key = pollCommand();
        Object value = pollCommand();
        if(!commandQueue.isEmpty()) {
            Object expiryType= pollCommand();
            TimeUnit timeUnit;
            if (expiryType.equals(C_PX)) {
                timeUnit = TimeUnit.MILLISECONDS;
            } else if (expiryType.equals(C_EX)) {
                timeUnit = TimeUnit.SECONDS;
            } else {
                throw new IllegalArgumentException("Expiry type not supported: " + expiryType);
            }
            long delay;
            try {
                delay = Long.parseLong(pollCommand().toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid expiry time" , e);
            }
            expiringMap.put(key, value, delay, timeUnit);
        } else {
            expiringMap.put(key, value);
        }
        return SIMPLE_STRING + C_OK + C_CRLF;
    }
    private String handleGetCommand(){
        String response = BULK_STRING;
        Object key = pollCommand();
        if (expiringMap.containsKey(key)) {
            String value = expiringMap.get(key).toString();
            response += value.length() + C_CRLF + value + C_CRLF;
        } else {
            response += NULL_VALUE;
        }
        return response;
    }
}
