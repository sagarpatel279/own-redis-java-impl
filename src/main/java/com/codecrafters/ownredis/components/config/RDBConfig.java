package com.codecrafters.ownredis.components.config;

import org.springframework.stereotype.Component;

@Component
public class RDBConfig {
    private String dir;
    private String dbFileName;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDbFileName() {
        return dbFileName;
    }

    public void setDbFileName(String dbFileName) {
        this.dbFileName = dbFileName;
    }
}
