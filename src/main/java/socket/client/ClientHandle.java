package socket.client;

import repos.ExpiringMap;
import resp.parser.RESPArrayParser;
import resp.parser.RESPJSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandle implements Runnable {
    private final Socket clientSocket;
    private final ExpiringMap<String,String> expiringMap=new ExpiringMap<>();
    private final ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);
    private RESPJSONParser RESPJSONParser;
    private final AtomicInteger currentCommandIndex;
    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
        currentCommandIndex=new AtomicInteger(0);
    }

    private void handleClient() {
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

        try(OutputStream writer = clientSocket.getOutputStream()) {
            while(true) {
                RESPArrayParser parser = RESPArrayParser.getBuilder().setInputStream(clientSocket.getInputStream()).build();
                Object commands = parser.parse();
                if (commands instanceof List) {
                    String written = "";
                    List<Object> cmdParts = (List<Object>) commands;
                    String commandName = cmdParts.get(currentCommandIndex.get()).toString();
                    if(commandName.equalsIgnoreCase("ping")) {
                        written="+PONG\r\n";
                    }else if(commandName.equalsIgnoreCase("echo")){
                        String value=cmdParts.get(currentCommandIndex.addAndGet(1)).toString();
                        written="$"+value.length()+"\r\n"+value+"\r\n";
                    }else if (commandName.equalsIgnoreCase("set")) {
                        String key = cmdParts.get(currentCommandIndex.addAndGet(1)).toString();
                        String value = cmdParts.get(currentCommandIndex.addAndGet(1)).toString();
                        if(currentCommandIndex.addAndGet(1)<cmdParts.size()){
                            TimeUnit timeUnit = TimeUnit.MILLISECONDS;
                            if(cmdParts.get(currentCommandIndex.get()).equals("px")){
                                timeUnit=TimeUnit.MILLISECONDS;
                            }else if(cmdParts.get(currentCommandIndex.get()).equals("ex")){
                                timeUnit=TimeUnit.SECONDS;
                            }

                            long delay=Long.parseLong(cmdParts.get(currentCommandIndex.addAndGet(1)).toString());
                            expiringMap.put(key,value,delay,timeUnit);
                        }else{
                            expiringMap.put(key, value);
                        }
                        written += "+OK\r\n";
                    }else if (commandName.equalsIgnoreCase("get")) {
                        written = "$";
                        String key = cmdParts.get(currentCommandIndex.addAndGet(1)).toString();
                        if (expiringMap.containsKey(key)) {
                            String value = expiringMap.get(key);
                            written += value.length() + "\r\n" + value + "\r\n";
                        } else {
                            written += "-1\r\n";
                        }
                    }
                    writer.write(written.getBytes());
                    writer.flush();
                }
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

    @Override
    public void run() {
        handleClient();
    }
}
