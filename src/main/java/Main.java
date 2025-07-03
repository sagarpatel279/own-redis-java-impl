import resp.parser.RESPArrayParser;
import resp.parser.RESPJSONParser;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
//        RedisServer redisServer=new RedisServer(6379);
//        redisServer.startServer();
        String input = "*3\r\n$3\r\nSET\r\n$6\r\nnumber\r\n$4\r\n5656\r\n";
        InputStream stream = new ByteArrayInputStream(input.getBytes());
//        RESPJSONParser parser = RESPJSONParser.getBuilder().setInputStream(stream).build();
        RESPArrayParser parser=RESPArrayParser.getBuilder().setInputStream(stream).build();
        Object result = parser.parse();



        System.out.println(result); // Output: [foo, bar]
    }
}

