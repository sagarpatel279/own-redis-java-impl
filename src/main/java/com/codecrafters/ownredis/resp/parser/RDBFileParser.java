package com.codecrafters.ownredis.resp.parser;

import lombok.RequiredArgsConstructor;

import java.io.BufferedInputStream;
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
        DataInputStream dis = new DataInputStream(new BufferedInputStream(input));

        dis.skipBytes(9); // Skip REDIS000x

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
            } else if (nextByte == 0xFA) { // NEW: AUX field
                readString(dis); // AUX key
                readString(dis); // AUX value
            } else if (nextByte == 0x00) { // Type: String
                String key = readString(dis);
                String value = readString(dis);
                store.put(key, Pair.of(value, expiryTime));
            } else if (nextByte == 0xFF) {
                break; // End of file
            } else {
                throw new IOException("Unsupported object type: " + nextByte);
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
            throw new IOException("Unknown length encoding type.");
        }
    }

    private String readString(DataInputStream dis) throws IOException {
        dis.mark(1);
        int firstByte = dis.readUnsignedByte();
        int type = (firstByte & 0xC0) >> 6;

        if (type == 3) {
            int encodingType = firstByte & 0x3F;
            if (encodingType == 0) { // 8-bit signed integer
                return String.valueOf(dis.readByte());
            } else if (encodingType == 1) { // 16-bit signed integer
                return String.valueOf(dis.readShort());
            } else if (encodingType == 2) { // 32-bit signed integer
                return String.valueOf(dis.readInt());
            } else {
                throw new IOException("Unsupported special string encoding type: " + encodingType);
            }
        } else {
            // Normal string, reset to read length again
            dis.reset();
            int len = readLength(dis);
            byte[] buffer = new byte[len];
            dis.readFully(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        }
    }
}
