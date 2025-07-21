package com.codecrafters.ownredis.resp.parser;

import lombok.RequiredArgsConstructor;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
@RequiredArgsConstructor
public class RDBFileParser {
    private final InputStream input;

    public Map<String, Pair<String,Long>> parse() throws IOException {
        Map<String,Pair<String,Long>> store=new HashMap<>();
        DataInputStream dis = new DataInputStream(input);;
        // skip header (9 bytes): REDISxxxx
        input.skip(9);

        int nextByte;
        while ((nextByte = input.read()) != -1) {
            long expiryTime=-1;
            if (nextByte == 0xFD) { // Millisecond expiry
                expiryTime = dis.readLong();    // 8 bytes
                nextByte = dis.readUnsignedByte();  // actual type
            } else if (nextByte == 0xFC) { // Second expiry
                expiryTime = dis.readInt() * 1000L; // 4 bytes, convert to ms
                nextByte = dis.readUnsignedByte();      // actual type
            }
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
                store.put(key, Pair.of(value,expiryTime));
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
