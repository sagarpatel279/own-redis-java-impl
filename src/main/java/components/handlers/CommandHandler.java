package components.handlers;

import components.repos.ExpiringMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import static resp.constants.RESPCommandsConstants.*;
import static resp.constants.RESPEncodingConstants.*;
import static resp.constants.RESPParserConstants.*;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class CommandHandler {
    private final ExpiringMap<Object, Object> expiringMap;
    private final Queue<Object> commandQueue = new LinkedList<>();
//
//    public void setCommandQueue(List<String> commandList) {
//        System.out.println("Command List: "+commandList);
//        this.commandQueue.addAll(commandList);
//    }
//
//    public String handleEchoCommand(){
//        String returnValue= pullCommand().toString();
//        return BULK_STRING+returnValue.length()+C_CRLF+returnValue+C_CRLF;
//    }
//
//    public String handlePingCommand(){
//        return SIMPLE_STRING+C_PONG+C_CRLF;
//    }
//
//    public Object pullCommand(){
//       if(!isCommandExist())
//            throw new NoSuchElementException("Queue is Empty");
//        return commandQueue.poll();
//    }
//    public Object fetchCommand(){
//        if(!isCommandExist())
//            throw new NoSuchElementException("Queue is Empty");
//        return commandQueue.peek();
//    }
//    public boolean isCommandExist(){
//        return !commandQueue.isEmpty();
//    }
//    public int countCommands(){
//        return commandQueue.size();
//    }
//    public String handleSetCommand(){
//        Object key = pullCommand();
//        Object value = pullCommand();
//        if(isCommandExist()) {
//            String expiryType= pullCommand().toString();
//            TimeUnit timeUnit;
//            if (expiryType.equalsIgnoreCase(C_PX)) {
//                timeUnit = TimeUnit.MILLISECONDS;
//            } else if (expiryType.equalsIgnoreCase(C_EX)) {
//                timeUnit = TimeUnit.SECONDS;
//            } else {
//                throw new IllegalArgumentException("Expiry type not supported: " + expiryType);
//            }
//            long delay;
//            try {
//                delay = Long.parseLong(pullCommand().toString());
//            } catch (NumberFormatException e) {
//                throw new IllegalArgumentException("Invalid expiry time" , e);
//            }
//            expiringMap.put(key, value, delay, timeUnit);
//        } else {
//            expiringMap.put(key, value);
//        }
//        return SIMPLE_STRING + C_OK + C_CRLF;
//    }
//    public String handleGetCommand(){
//        String response = BULK_STRING;
//        Object key = pullCommand();
//        if (expiringMap.containsKey(key)) {
//            String value = expiringMap.get(key).toString();
//            response += value.length() + C_CRLF + value + C_CRLF;
//        } else {
//            response += NULL_VALUE;
//        }
//        return response;
//    }
public void setCommandQueue(List<String> commandList) {
    this.commandQueue.clear();
    this.commandQueue.addAll(commandList);
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
            default -> BULK_STRING + NULL_VALUE;
        };
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
