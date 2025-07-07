package com.codecrafts.ownredis.components.handlers;

import com.codecrafts.ownredis.components.repos.ExpiringMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import static com.codecrafts.ownredis.resp.constants.RESPCommandsConstants.*;
import static com.codecrafts.ownredis.resp.constants.RESPEncodingConstants.*;
import static com.codecrafts.ownredis.resp.constants.RESPParserConstants.*;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class CommandHandler {
    private final ExpiringMap<Object, Object> expiringMap;
    private final Queue<Object> commandQueue = new LinkedList<>();
    @Value("${ownredis.dir}")
    private String dir;
    @Value("${ownredis.dbfilename}")
    private String dbfilename;
    public void setCommandQueue(List<String> commandList) {
        this.commandQueue.clear();
        this.commandQueue.addAll(commandList);
        System.out.println(commandList);
    }

    public Object pullCommand() {
        return commandQueue.poll();
    }

    public String handleCommand(String commandName) {
        return switch (commandName.toUpperCase()) {
            case C_PING -> SIMPLE_STRING + C_PONG + C_CRLF;
            case C_ECHO -> {
                String echoVal = pullCommand().toString();
                yield BULK_STRING + echoVal.length() + C_CRLF + echoVal + C_CRLF;
            }
            case C_SET -> handleSetCommand();
            case C_GET -> handleGetCommand();
            case C_CONFIG -> handleConfigCommand();
            default -> BULK_STRING + NULL_VALUE;
        };
    }

    private String handleConfigCommand() {
        String response=BULK_STRING+NULL_VALUE;
        if(pullCommand().toString().equalsIgnoreCase(C_GET)){
            if(pullCommand().toString().equalsIgnoreCase(C_DIR)){
                response=ARRAY+2+CRLF+BULK_STRING+C_DIR.length()+CRLF+C_DIR+CRLF+BULK_STRING+dir.length()+CRLF+dir+CRLF;
            }else{
                response=ARRAY+2+CRLF+BULK_STRING+C_DBFILENAME.length()+CRLF+C_DBFILENAME+ CRLF+BULK_STRING+dbfilename.length()+CRLF+dbfilename+CRLF;
            }
        }
        return response;
    }

    private String handleSetCommand() {
        Object key = pullCommand();
        Object value = pullCommand();
        if (!commandQueue.isEmpty()) {
            String expiryType = pullCommand().toString();
            TimeUnit timeUnit = expiryType.equalsIgnoreCase(C_PX) ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS;
            long delay = Long.parseLong(pullCommand().toString());
            expiringMap.put(key, value, delay, timeUnit);
        } else {
            expiringMap.put(key, value);
        }
        return SIMPLE_STRING + C_OK + C_CRLF;
    }

    private String handleGetCommand() {
        Object key = pullCommand();
        if (expiringMap.containsKey(key)) {
            String value = expiringMap.get(key).toString();
            return BULK_STRING + value.length() + C_CRLF + value + C_CRLF;
        }
        return BULK_STRING + NULL_VALUE;
    }
}
