package com.codecrafters.ownredis.resp.parser;

import lombok.RequiredArgsConstructor;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class RDBFileParser {
    private final InputStream input;

    public Map<String, Pair<String, Long>> parse() throws IOException {
        Map<String, Pair<String, Long>> store = new HashMap<>();
        DataInputStream dis = new DataInputStream(input);

        dis.skipBytes(9); // Skip REDISxxx version string

        int nextByte;
        while ((nextByte = dis.read()) != -1) {
            long expiryTime = -1;

            if (nextByte == 0xFD) { // Millisecond expiry
                expiryTime = dis.readLong();
                nextByte = dis.readUnsignedByte();
            } else if (nextByte == 0xFC) { // Second expiry
                expiryTime = dis.readInt() * 1000L;
                nextByte = dis.readUnsignedByte();
            }

            if (nextByte == 0xFE) {
                dis.readUnsignedByte(); // Skip DB selector byte
            } else if (nextByte == 0xFB) {
                readLength(dis); // Skip hash table size
                readLength(dis); // Skip expire table size
            } else if (nextByte == 0x00) { // Type: String
                String key = readString(dis);
                String value = readString(dis);
                store.put(key, Pair.of(value, expiryTime));
            } else if (nextByte == 0xFF) {
                break; // End of file
            } else {
                throw new IOException("Unsupported type: " + nextByte);
            }
        }

        return store;
    }

    private int readLength(DataInputStream dis) throws IOException {
        int firstByte = dis.readUnsignedByte();
        int type = (firstByte & 0xC0) >> 6;

        if (type == 0) {
            return firstByte & 0x3F;
        } else if (type == 1) {
            return ((firstByte & 0x3F) << 8) | dis.readUnsignedByte();
        } else if (type == 2) {
            return dis.readInt();
        } else {
            throw new IOException("Unsupported length encoding.");
        }
    }

    private String readString(DataInputStream dis) throws IOException {
        int len = readLength(dis);
        byte[] buffer = new byte[len];
        dis.readFully(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }
}
