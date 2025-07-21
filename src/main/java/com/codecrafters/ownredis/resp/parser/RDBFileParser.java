package com.codecrafters.ownredis.resp.parser;

import com.codecrafters.ownredis.components.repos.ExpiringMap;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class RDBFileParser {
    private final InputStream input;

    public Map<String, String> parse() throws IOException {
        Map<String,String> store=new HashMap<>();
        // skip header (9 bytes): REDISxxxx
        input.skip(9);

        int nextByte;
        while ((nextByte = input.read()) != -1) {
            if (nextByte == 0xFE) {
                // DB selector, skip 1 byte (DB index)
                input.read();
            } else if (nextByte == 0xFB) {
                // skip hash table sizes
                readLength(); // key count
                readLength(); // expire count
            } else if (nextByte == 0x00) {
                // type: string
                String key = readString();
                String value = readString();
                store.put(key, value);
            } else if (nextByte == 0xFF) {
                break; // end of file
            }
        }

        return store;
    }

    private int readLength() throws IOException {
        int firstByte = input.read();
        int type = (firstByte & 0xC0) >> 6;
        if (type == 0) {
            return firstByte & 0x3F;
        } else if (type == 1) {
            return ((firstByte & 0x3F) << 8) | input.read();
        } else if (type == 2) {
            return (input.read() << 24) | (input.read() << 16) | (input.read() << 8) | input.read();
        } else {
            throw new IOException("Unsupported length encoding.");
        }
    }

    private String readString() throws IOException {
        int len = readLength();
        byte[] buffer = input.readNBytes(len);
        return new String(buffer);
    }
}
