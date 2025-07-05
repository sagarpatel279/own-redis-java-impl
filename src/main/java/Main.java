import configurations.ApplicationConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sockets.server.RedisServer;

import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException {

        RedisServer redisServer=new RedisServer(6389);
        redisServer.startServer();
//        String input = "*1\r\n$4\r\nPING\r\n";
//        InputStream stream = new ByteArrayInputStream(input.getBytes());
//        RESPArrayParser parser=RESPArrayParser.getBuilder().setEncodedString(input).build();
//        Object result = parser.parse();
//        System.out.println(result);
    }
}

