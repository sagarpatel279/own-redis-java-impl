package socket.client;

import repos.ExpiringMap;
import static resp.constants.RESPParserConstants.*;
import static resp.constants.RESPConstantCommands.*;
import static resp.constants.RESPEncodingConstants.*;

import resp.parser.RESPArrayParser;
import resp.parser.RESPJSONParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandle implements Runnable {
    private final Socket clientSocket;
    private final ExpiringMap<Object,Object> expiringMap=new ExpiringMap<>();
    private final ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);
    private RESPJSONParser RESPJSONParser;
    private final AtomicInteger currentCommandIndex;
    private int currentIndx;
    private Queue<Object> commandQueue;
    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
        currentCommandIndex=new AtomicInteger(0);
        this.currentIndx=0;
    }

    @Override
    public void run() {
        handleClient();
    }
    private void handleClient() {
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

        try(OutputStream writer = clientSocket.getOutputStream()) {
            while(true) {
                RESPArrayParser parser = RESPArrayParser.getBuilder().setInputStream(clientSocket.getInputStream()).build();
                Object commands = parser.parse();

                if(!(commands instanceof List))continue;

                commandQueue= (Queue<Object>) commands;

                System.out.println("Size of Commands: "+commandQueue.size());
                System.out.println("Commands List: "+commands);

                String commandName = getFirstCommand().toString();
                if(commandName.equalsIgnoreCase(C_PING)) {
                    handlePingCommand(writer);
                }else if(commandName.equalsIgnoreCase(C_ECHO)){

                    String returnValue=getFirstCommand().toString();
                    handleEchoCommand(writer,returnValue);
                }else if (commandName.equalsIgnoreCase(C_SET)) {
                    Object key = getFirstCommand();


                    Object value = getFirstCommand();
                    if(currentIndx<commandQueue.size()) {
                        Object expiryType=getFirstCommand();
                        Object delay=getFirstCommand();
                        handleSetCommand(writer, key, value,expiryType,delay);
                    }else{
                        handleSetCommand(writer, key, value);
                    }
                }else if (commandName.equalsIgnoreCase(C_GET)) {

                    Object key = getFirstCommand();
                    handleGetCommand(writer,key);
                }
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
    private Object getFirstCommand(){
        if(commandQueue==null){
            throw new NullPointerException("Command Queue is null");
        }else if(commandQueue.isEmpty())
            throw new NoSuchElementException("Queue is Empty");
        return commandQueue.poll();
    }
    private void handleEchoCommand(OutputStream writer,String returnValue) throws IOException {
        String response=BULK_STRING+returnValue.length()+C_CRLF+returnValue+C_CRLF;
        writer.write(response.getBytes());
    }

    private void handlePingCommand(OutputStream writer) throws IOException {
        String response= SIMPLE_STRING+C_PONG+C_CRLF;
        writer.write(response.getBytes());
    }

    private void handleSetCommand(OutputStream writer,Object key,Object value,Object... expiryVariable) throws IOException {
        if (expiryVariable != null && expiryVariable.length >= 2) {
            TimeUnit timeUnit;

            String expiryType = expiryVariable[0].toString().toUpperCase();

            if (expiryType.equals(C_PX)) {
                timeUnit = TimeUnit.MILLISECONDS;
            } else if (expiryType.equals(C_EX)) {
                timeUnit = TimeUnit.SECONDS;
            } else {
                throw new IllegalArgumentException("Expiry type not supported: " + expiryType);
            }

            long delay;
            try {
                delay = Long.parseLong(expiryVariable[1].toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid expiry time: " + expiryVariable[1], e);
            }

            expiringMap.put(key, value, delay, timeUnit);

        } else if (expiryVariable == null || expiryVariable.length == 0) {
            // No expiry set
            expiringMap.put(key, value);
        } else {
            throw new IllegalArgumentException("Expiry options require both a type and a value.");
        }

        String response = SIMPLE_STRING + C_OK + C_CRLF;
        writer.write(response.getBytes());
    }
    private void handleGetCommand(OutputStream writer,Object key) throws IOException {
        String response = BULK_STRING;
        if (expiringMap.containsKey(key)) {
            String value = expiringMap.get(key).toString();
            response += value.length() + C_CRLF + value + C_CRLF;
        } else {
            response += NULL_VALUE;
        }
        writer.write(response.getBytes());
    }
}
