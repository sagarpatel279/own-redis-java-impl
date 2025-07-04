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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandle implements Runnable {
    private final Socket clientSocket;
    private final ExpiringMap<Object,Object> expiringMap=new ExpiringMap<>();
    private final ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);
    private RESPJSONParser RESPJSONParser;
    private final AtomicInteger currentCommandIndex;
    private int currentIndx=0;
    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
        currentCommandIndex=new AtomicInteger(0);
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

                List<Object> cmdParts = (List<Object>) commands;

                System.out.println("Size of Commands: "+cmdParts.size());
                System.out.println("Commands List: "+commands);

                System.out.println("Before Command Name Index is: "+currentIndx);
                String commandName = cmdParts.get(currentIndx++).toString();
                System.out.println("After Command Name Index is: "+currentIndx);
                if(commandName.equalsIgnoreCase(C_PING)) {
                    handlePingCommand(writer);
                }else if(commandName.equalsIgnoreCase(C_ECHO)){

                    System.out.println("Before ECHO Command's Value Index is: "+currentIndx);
                    String returnValue=cmdParts.get(currentIndx++).toString();
                    System.out.println("After ECHO Command's Value Index is: "+currentIndx);
                    handleEchoCommand(writer,returnValue);
                }else if (commandName.equalsIgnoreCase(C_SET)) {
                    System.out.println("Before SET Command'S KEY Index is: "+currentIndx);
                    Object key = cmdParts.get(currentIndx++);
                    System.out.println("After SET Command'S KEY Index is: "+currentIndx);


                    System.out.println("Before SET Command'S VALUE Index is: "+currentIndx);
                    Object value = cmdParts.get(currentIndx++);
                    System.out.println("After SET Command'S VALUE Index is: "+currentIndx);

                    handleSetCommand(writer,key,value);
                }else if (commandName.equalsIgnoreCase(C_GET)) {

                    System.out.println("Before GET Command'S KEY Index is: "+currentIndx);
                    Object key = cmdParts.get(currentIndx++);
                    System.out.println("After GET Command'S KEY Index is: "+currentIndx);
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

    private void handleEchoCommand(OutputStream writer,String returnValue) throws IOException {
        String response=BULK_STRING+returnValue.length()+C_CRLF+returnValue+C_CRLF;
        writer.write(response.getBytes());
    }

    private void handlePingCommand(OutputStream writer) throws IOException {
        String response= SIMPLE_STRING+C_PONG+C_CRLF;
        writer.write(response.getBytes());
    }

    private void handleSetCommand(OutputStream writer,Object key,Object value,Object... expiryVariable) throws IOException {
        if(expiryVariable!=null){
            TimeUnit timeUnit = null;
            if(expiryVariable[0].toString().equalsIgnoreCase(C_PX)){
                timeUnit=TimeUnit.MILLISECONDS;
            }else if(expiryVariable[0].toString().equalsIgnoreCase(C_EX)){
                timeUnit=TimeUnit.SECONDS;
            }else{
                throw new IllegalArgumentException("Expiry Variable mismatched..");
            }

            long delay=Long.parseLong(expiryVariable[1].toString());
            expiringMap.put(key,value,delay,timeUnit);
        }else{
            expiringMap.put(key, value);
        }
        String response =SIMPLE_STRING+C_OK+C_CRLF;
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
