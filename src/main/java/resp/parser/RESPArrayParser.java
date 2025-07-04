package resp.parser;

import static resp.constants.RESPParserConstants.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RESPArrayParser {
    private final BufferedReader reader;
    private RESPArrayParser(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
    }
    private RESPArrayParser(String encodedString){
        this.reader= new BufferedReader(new InputStreamReader(new ByteArrayInputStream(encodedString.getBytes())));
    }
    public List<String> getCommandList(){
        try {
            Object object=parse();
            if(!(object instanceof List))return null;
            return (List<String>)object;
        } catch (IOException e) {
            System.out.println("Error in Commands parser");
            return null;
        }
    }
    private Object parse() throws IOException {
        int prefix = reader.read();
        if (prefix == -1) return null;

        String prefixStr=String.valueOf((char)prefix);
        return switch (prefixStr) {
            case SIMPLE_STRING -> parseSimpleString();
            case ERROR_STRING -> parseError();
            case INTEGER -> parseInteger();
            case BULK_STRING -> parseBulkString();
            case ARRAY -> parseArray();
            default -> throw new IOException("Unknown RESP type: " + (char) prefix);
        };
    }
    public static Builder getBuilder(){
        return new Builder();
    }
    public static class Builder{
        private InputStream stream;
        private String encodedString;
        private Builder(){}
        public Builder setInputStream(InputStream stream){
            this.stream=stream;
            return this;
        }
        public Builder setEncodedString(String encodedString){
            this.encodedString=encodedString;
            return this;
        }
        public RESPArrayParser build(){
            if(stream==null && (encodedString==null || encodedString.isBlank())){
                throw new NullPointerException("InputStream must not be null");
            }
            if(stream!=null)
                return new RESPArrayParser(this.stream);
            return new RESPArrayParser(this.encodedString);
        }
    }
    private String parseSimpleString() throws IOException {
        return readLine(); // after '+'
    }

    private String parseError() throws IOException {
        throw new IOException("RESP Error: " + readLine());
    }

    private Long parseInteger() throws IOException {
        return Long.parseLong(readLine());
    }

    private String parseBulkString() throws IOException {
        int length = Integer.parseInt(readLine());
        if (length == -1) return null; // Null bulk string
        char[] buffer = new char[length];
        int read = reader.read(buffer, 0, length);
        reader.readLine(); // consume trailing \r\n
        return new String(buffer, 0, read);
    }

    private List<Object> parseArray() throws IOException {
        int count = Integer.parseInt(readLine());
        if (count == -1) return null; // Null array
        List<Object> elements = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            elements.add(parse());
        }
        return elements;
    }

    private String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) throw new IOException("Unexpected end of stream");
        return line;
    }
}
