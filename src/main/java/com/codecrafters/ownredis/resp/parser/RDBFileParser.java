package com.codecrafters.ownredis.resp.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RDBFileParser {
    private final InputStream input;

    public Map<String, Pair<String,Long>> parse() throws IOException {
        Map<String, Pair<String,Long>> result = new HashMap<>();

        input.skip(9); // skip header: REDIS000X
        long expiryTime = -1;
        while (((int)input.read())!=-1) {
            int type = input.read();
            if (type == 0xFC) {
                expiryTime = readExpirySeconds();
                type = input.read(); // read actual type after expiry
            } else if (type == 0xFD) {
                expiryTime = readExpiryMillis();
                type = input.read(); // read actual type after expiry
            }
            if (type == 0x00) { // type string
                String key = readString();
                String value = readString();
                System.out.println("Key: "+key+" Value: "+value+" Expiry: "+expiryTime);
                result.put(key, Pair.of(value,expiryTime));
                expiryTime = -1;
            } else if (type == 0xFF) {
                break; // end of file
            } else if (type == 0xFE) {
                input.read(); //
            } else {
                // skip unknown type or metadata
                skipUnknown(input);
            }
        }

        return result;
    }
    private long readExpirySeconds() throws IOException {
        return readUnsignedIntLE() * 1000L;
    }

    private long readExpiryMillis() throws IOException {
        return readUnsignedLongLE();
    }

    private long readUnsignedIntLE() throws IOException {
        int b0 = input.read();
        int b1 = input.read();
        int b2 = input.read();
        int b3 = input.read();

        if ((b0 | b1 | b2 | b3) < 0) {
            throw new EOFException("Unexpected end of stream while reading expiry");
        }

        return ((long) b0 & 0xFF)
                | (((long) b1 & 0xFF) << 8)
                | (((long) b2 & 0xFF) << 16)
                | (((long) b3 & 0xFF) << 24);
    }

    private long readUnsignedLongLE() throws IOException {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            int b = input.read();
            if (b == -1) throw new EOFException("Unexpected end of stream while reading 8-byte expiry");
            result |= ((long) b & 0xFF) << (8 * i);
        }
        return result;
    }
    private int readLength() throws IOException {
        int first = input.read();
        int type = (first & 0xC0) >> 6;
        if (type == 0) return first & 0x3F;
        else if (type == 1) return ((first & 0x3F) << 8) | input.read();
        else throw new IOException("Unsupported length type");
    }

    private String readString() throws IOException {
        int len = readLength();
        byte[] buf = input.readNBytes(len);
        return new String(buf, StandardCharsets.UTF_8);
    }

    private void skipUnknown(InputStream input) throws IOException {
        // Skip unknown bytes safely
        while (input.read() != -1) {}
    }
}
