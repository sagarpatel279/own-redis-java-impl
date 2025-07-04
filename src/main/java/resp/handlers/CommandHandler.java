package resp.handlers;

import repos.ExpiringMap;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import static resp.constants.RESPCommandsConstants.*;
import static resp.constants.RESPEncodingConstants.*;
import static resp.constants.RESPParserConstants.*;


public class CommandHandler {
    private final ExpiringMap<Object,Object> expiringMap=new ExpiringMap<>();
    private ConcurrentLinkedQueue<Object> commandQueue;

    public CommandHandler(List<String> commandList){
        this.commandQueue=new ConcurrentLinkedQueue<>(commandList);
    }
    public String handleEchoCommand(){
        String returnValue= pullCommand().toString();
        return BULK_STRING+returnValue.length()+C_CRLF+returnValue+C_CRLF;
    }

    public String handlePingCommand(){
        return SIMPLE_STRING+C_PONG+C_CRLF;
    }

    public Object pullCommand(){
       if(!isCommandExist())
            throw new NoSuchElementException("Queue is Empty");
        return commandQueue.poll();
    }
    public Object fetchCommand(){
        if(!isCommandExist())
            throw new NoSuchElementException("Queue is Empty");
        return commandQueue.peek();
    }
    public boolean isCommandExist(){
        return !commandQueue.isEmpty();
    }
    public int countCommands(){
        return commandQueue.size();
    }
    public String handleSetCommand(){
        Object key = pullCommand();
        Object value = pullCommand();
        if(isCommandExist()) {
            Object expiryType= pullCommand();
            TimeUnit timeUnit;
            if (expiryType.equals(C_PX)) {
                timeUnit = TimeUnit.MILLISECONDS;
            } else if (expiryType.equals(C_EX)) {
                timeUnit = TimeUnit.SECONDS;
            } else {
                throw new IllegalArgumentException("Expiry type not supported: " + expiryType);
            }
            long delay;
            try {
                delay = Long.parseLong(pullCommand().toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid expiry time" , e);
            }
            expiringMap.put(key, value, delay, timeUnit);
        } else {
            expiringMap.put(key, value);
        }
        return SIMPLE_STRING + C_OK + C_CRLF;
    }
    public String handleGetCommand(){
        String response = BULK_STRING;
        Object key = pullCommand();
        if (expiringMap.containsKey(key)) {
            String value = expiringMap.get(key).toString();
            response += value.length() + C_CRLF + value + C_CRLF;
        } else {
            response += NULL_VALUE;
        }
        return response;
    }
}
