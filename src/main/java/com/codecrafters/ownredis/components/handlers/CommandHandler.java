package com.codecrafters.ownredis.components.handlers;

import com.codecrafters.ownredis.components.repos.ExpiringMap;
import com.codecrafters.ownredis.components.config.RDBConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.codecrafters.ownredis.resp.constants.RESPCommandsConstants.*;
import static com.codecrafters.ownredis.resp.constants.RESPEncodingConstants.*;
import static com.codecrafters.ownredis.resp.constants.RESPParserConstants.*;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class CommandHandler {
    private final ExpiringMap<Object, Object> expiringMap;
    private final Queue<Object> commandQueue = new LinkedList<>();
    private final RDBConfig rdbConfig;
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
            case C_KEYS -> handleKeysCommand();
            default -> BULK_STRING + NULL_VALUE;
        };
    }

    private String handleKeysCommand() {
        String response=BULK_STRING+NULL_VALUE;
        if(pullCommand().toString().equalsIgnoreCase(ARRAY)){
            response=encodeBulkyString(expiringMap.keySet().stream().map(Object::toString).collect(Collectors.toSet()));
        }
        return response;
    }
    private String encodeBulkyString(Set<String> keys){
        StringBuilder builder=new StringBuilder();
        builder.append(ARRAY).append(keys.size()).append(CRLF);
        for(String key:keys){
            builder.append(BULK_STRING).append(key.length()).append(CRLF).append(key).append(CRLF);
        }
        return builder.toString();
    }
    private String handleConfigCommand() {
        String response=BULK_STRING+NULL_VALUE;
        if(pullCommand().toString().equalsIgnoreCase(C_GET)){
            if(pullCommand().toString().equalsIgnoreCase(C_DIR)){
                response=ARRAY+2+CRLF+BULK_STRING+C_DIR.length()+CRLF+C_DIR+CRLF+BULK_STRING+rdbConfig.getDir().length()+CRLF+rdbConfig.getDir()+CRLF;
            }else{
                response=ARRAY+2+CRLF+BULK_STRING+C_DBFILENAME.length()+CRLF+C_DBFILENAME+ CRLF+BULK_STRING+rdbConfig.getDbFileName().length()+CRLF+rdbConfig.getDbFileName()+CRLF;
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
