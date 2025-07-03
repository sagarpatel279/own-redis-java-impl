import resp.parser.RESPArrayParser;
import resp.parser.RESPJSONParser;
import socket.server.RedisServer;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        RedisServer redisServer=new RedisServer(6379);
        redisServer.startServer();


//        String input = "*1\r\n$4\r\nPING\r\n";
//        InputStream stream = new ByteArrayInputStream(input.getBytes());
//        RESPArrayParser parser=RESPArrayParser.getBuilder().setEncodedString(input).build();
//        Object result = parser.parse();
//        System.out.println(result);
    }
}

