package com.codecrafts.ownredis.resp.constants;

public interface RESPParserConstants {
    String SIMPLE_STRING = "+";
    String BULK_STRING = "$";
    String INTEGER = ":";
    String DOUBLE = ",";
    String ARRAY = "*";
    String ERROR_STRING = "-";
    String BLOB_ERROR = "!";
    String CRLF = "\r\n";
    String BOOLEAN = "#";
    String BIG_NUMBER = "(";
    String SET = "~";
    String VERBATIM_STRING = "=";
    String MAP = "%";
    String VALUE = "value";
    String TYPE = "type";
    String LENGTH = "length";
}
