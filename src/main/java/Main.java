import resp.parser.RESPArrayParser;
import resp.parser.RESPJSONParser;
import socket.server.RedisServer;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
//        RedisServer redisServer=new RedisServer(6379);
//        redisServer.startServer();
        String input = "*2\r\n$3\r\nGET\r\n$4\r\npear\r\n";
        InputStream stream = new ByteArrayInputStream(input.getBytes());
//        RESPJSONParser parser = RESPJSONParser.getBuilder().setInputStream(stream).build();
        RESPArrayParser parser=RESPArrayParser.getBuilder().setInputStream(stream).build();
        Object result = parser.parse();
//
//
//
        System.out.println(result); // Output: [foo, bar]
    }
}

