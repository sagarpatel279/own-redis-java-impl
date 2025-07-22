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

    public Map<String, Pair<String, Long>> parse() throws IOException {
        Map<String, Pair<String, Long>> result = new HashMap<>();
        System.out.println("=============Inside parser");

        // Skip header: REDIS000X (9 bytes)
        input.skip(9);

        long expiryTime = -1;
        int type;

        while ((type = input.read()) != -1) {
            System.out.println("=====Type: " + String.format("0x%02X", type));
            expiryTime = -1; // Reset expiry after use

            // Handle expiry time markers
            if (type == 0xFC) {
                expiryTime = readExpirySeconds();
                type = input.read(); // read actual type after expiry
                if (type == -1) break;
            } else if (type == 0xFD) {
                expiryTime = readExpiryMillis();
                type = input.read(); // read actual type after expiry
                if (type == -1) break;
            }

            if (type == 0x00) { // String type
                String key = readString();
                String value = readString();
                System.out.println("Key: " + key + " Value: " + value + " Expiry: " + expiryTime);
                result.put(key, Pair.of(value, expiryTime));
            } else if (type == 0xFF) {
                // End of RDB file
                break;
            } else if (type == 0xFE) {
                // Database selector - read database number
                int dbNumber = input.read();
                if (dbNumber == -1) break;
                System.out.println("Switching to database: " + dbNumber);
            } else if (type == 0xFB) {
                // Hash table size information - skip
                skipHashTableSizes();
            } else if (type == 0xFA) {
                // Auxiliary field - skip
                skipAuxiliaryField();
            } else {
                // For other types, try to skip the key-value pair
                System.out.println("Skipping unknown type: 0x" + String.format("%02X", type));
                skipKeyValuePair();
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
            throw new EOFException("Unexpected end of stream while reading 4-byte integer");
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
            if (b == -1) throw new EOFException("Unexpected end of stream while reading 8-byte long");
            result |= ((long) b & 0xFF) << (8 * i);
        }
        return result;
    }

    private int readLength() throws IOException {
        int first = input.read();
        if (first == -1) throw new EOFException("Unexpected end of stream while reading length");

        int type = (first & 0xC0) >> 6;
        if (type == 0) {
            return first & 0x3F;
        } else if (type == 1) {
            int second = input.read();
            if (second == -1) throw new EOFException("Unexpected end of stream while reading length");
            return ((first & 0x3F) << 8) | second;
        } else if (type == 2) {
            // 32-bit length
            return (int) readUnsignedIntLE();
        } else {
            // Special format - could be compressed integers
            int subType = first & 0x3F;
            if (subType == 0) return 1; // 8-bit int follows
            if (subType == 1) return 2; // 16-bit int follows
            if (subType == 2) return 4; // 32-bit int follows
            throw new IOException("Unsupported special length format: " + subType);
        }
    }

    private String readString() throws IOException {
        int len = readLength();
        if (len == 0) return "";

        byte[] buf = new byte[len];
        int bytesRead = 0;
        while (bytesRead < len) {
            int result = input.read(buf, bytesRead, len - bytesRead);
            if (result == -1) {
                throw new EOFException("Unexpected end of stream while reading string of length " + len);
            }
            bytesRead += result;
        }
        return new String(buf, StandardCharsets.UTF_8);
    }

    private void skipHashTableSizes() throws IOException {
        // Skip hash table size info (usually 2 length-encoded values)
        readLength(); // database hash table size
        readLength(); // expiry hash table size
    }

    private void skipAuxiliaryField() throws IOException {
        // Skip auxiliary field (key-value pair)
        readString(); // key
        readString(); // value
    }

    private void skipKeyValuePair() throws IOException {
        try {
            // Try to read key and skip based on type
            String key = readString();
            // For simplicity, just read one more string (works for string types)
            // In a full implementation, you'd need to handle different value types
            readString();
        } catch (IOException e) {
            // If we can't parse it properly, just consume remaining bytes until we hit a known marker
            System.out.println("Error skipping key-value pair, seeking to next known marker");
        }
    }
}