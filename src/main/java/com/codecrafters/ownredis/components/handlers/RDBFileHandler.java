package com.codecrafters.ownredis.components.handlers;

import com.codecrafters.ownredis.components.config.RDBConfig;
import com.codecrafters.ownredis.components.repos.ExpiringMap;
import com.codecrafters.ownredis.resp.parser.Pair;
import com.codecrafters.ownredis.resp.parser.RDBFileParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RDBFileHandler {
    private final RDBConfig rdbConfig;
    private final ExpiringMap<Object,Object> store;
    public void readKeysFromRDBFile(){
        if(rdbConfig.getDir()==null || rdbConfig.getDir().isBlank()|| rdbConfig.getDbFileName()==null || rdbConfig.getDbFileName().isBlank()) return;
        System.out.println("===========File Handling Part");
        Path path= Paths.get(rdbConfig.getDir(),rdbConfig.getDbFileName());
        if(Files.exists(path)){
            try(InputStream inputStream=Files.newInputStream(path);) {
                RDBFileParser parser=new RDBFileParser(inputStream);
                Map<String,Pair<String,Long>> keysStore=parser.parse();
                System.out.println("Got the Keys: "+keysStore);
                for(Map.Entry<String, Pair<String,Long>> entry:keysStore.entrySet()){
                    String key=entry.getKey();
                    Pair<String,Long> valuePair=entry.getValue();
                    String value=valuePair.getFirst();
                    long expiry=valuePair.getSecond();
                    if(expiry!=-1){
                        store.put(key,value,expiry, TimeUnit.MILLISECONDS);
                    }else{
                        store.put(key,value);
                    }
                }
            } catch (IOException e) {
                System.out.println("=========Error in RDB File Reading...: "+e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
