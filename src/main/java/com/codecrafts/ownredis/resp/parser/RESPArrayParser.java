package com.codecrafts.ownredis.resp.parser;

import static com.codecrafts.ownredis.resp.constants.RESPParserConstants.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RESPArrayParser {
    private final BufferedReader reader;

    private RESPArrayParser(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    private RESPArrayParser(String encodedString) {
        this.reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(encodedString.getBytes()))
        );
    }

    public List<String> getCommandList() {
        try {
            Object parsed = parse();
            if (parsed instanceof List<?> list) {
                return list.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.err.println("Error in RESP command parser: " + e.getMessage());
        }
        return List.of();
    }

    private Object parse() throws IOException {
        int prefix = reader.read();
        if (prefix == -1) return null;
        String prefixStr=String.valueOf((char) prefix);
        return switch (prefixStr) {
            case SIMPLE_STRING -> parseSimpleString();
            case ERROR_STRING -> parseError();
            case INTEGER -> parseInteger();
            case BULK_STRING -> parseBulkString();
            case ARRAY -> parseArray();
            default -> throw new IOException("Unknown RESP type: " + (char) prefix);
        };
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private InputStream stream;
        private String encodedString;

        private Builder() {}

        public Builder setInputStream(InputStream stream) {
            this.stream = stream;
            return this;
        }

        public Builder setEncodedString(String encodedString) {
            this.encodedString = encodedString;
            return this;
        }

        public RESPArrayParser build() {
            if (stream != null) {
                return new RESPArrayParser(stream);
            } else if (encodedString != null && !encodedString.isBlank()) {
                return new RESPArrayParser(encodedString);
            }
            throw new IllegalStateException("Either InputStream or EncodedString must be provided.");
        }
    }

    private String parseSimpleString() throws IOException {
        return readLine();
    }

    private String parseError() throws IOException {
        throw new IOException("RESP Error from client: " + readLine());
    }

    private Long parseInteger() throws IOException {
        return Long.parseLong(readLine());
    }

    private String parseBulkString() throws IOException {
        int length = Integer.parseInt(readLine());
        if (length == -1) return null;

        char[] buffer = new char[length];
        int read = reader.read(buffer, 0, length);
        reader.readLine(); // consume trailing \r\n
        return new String(buffer, 0, read);
    }

    private List<Object> parseArray() throws IOException {
        int count = Integer.parseInt(readLine());
        if (count == -1) return null;

        List<Object> elements = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            elements.add(parse());
        }
        return elements;
    }

    private String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) throw new EOFException("Unexpected end of stream while reading RESP line.");
        return line;
    }
}
