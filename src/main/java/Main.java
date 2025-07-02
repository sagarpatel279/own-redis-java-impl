import resp.parser.RESPParser;
import socket.server.RedisServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        RedisServer redisServer=new RedisServer(6379);
        redisServer.startServer();
        String input = "$3\r\nbar\r\n";
//        InputStream stream = new ByteArrayInputStream(input.getBytes());
//
//        RESPParser parser = new RESPParser(stream);
//        Object result = parser.parse();
//
//        System.out.println(result); // Output: [foo, bar]
    }
}

