package socket.client;

import resp.parser.RESPArrayParser;
import resp.parser.RESPJSONParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientHandle implements Runnable {
    private final Socket clientSocket;
    private final ConcurrentMap<Object,Object> concurrentMap=new ConcurrentHashMap<>();
    private RESPJSONParser RESPJSONParser;
    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private void handleClient() {
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

        try(OutputStream writer = clientSocket.getOutputStream()) {
            RESPArrayParser parser= RESPArrayParser.getBuilder().setInputStream(clientSocket.getInputStream()).build();
            Object commands=parser.parse();
            if(commands instanceof List){
                String written="";
                List<Object> cmdParts = (List<Object>) commands;
                String commandName=cmdParts.get(0).toString();
                String key=cmdParts.get(1).toString();

                if(commandName.equalsIgnoreCase("set")){
                    String value=cmdParts.get(2).toString();
                    concurrentMap.put(key,value);
                    written+="+OK\r\n";
                }
                if(commandName.equalsIgnoreCase("get")){
                    written="$";
                    if(concurrentMap.containsKey(key)){
                        String value=concurrentMap.get(key).toString();
                        written+=value.length()+"\r\n"+value+"\r\n";
                    }else{
                        written+="-1\r\n";
                    }
                }
                writer.write(written.getBytes());
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

    @Override
    public void run() {
        handleClient();
    }
}
