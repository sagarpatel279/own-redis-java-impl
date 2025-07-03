import resp.parser.RESPArrayParser;
import resp.parser.RESPJSONParser;
import socket.server.RedisServer;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        RedisServer redisServer=new RedisServer(6379);
        redisServer.startServer();


        String input = "*3\r\n$3\r\nSET\r\n$9\r\npineapple\r\n$4\r\npear\r\n";
        InputStream stream = new ByteArrayInputStream(input.getBytes());
        RESPArrayParser parser=RESPArrayParser.getBuilder().setEncodedString(input).build();
        Object result = parser.parse();
        System.out.println(result);
    }
}

