package com.codecrafters.ownredis.resp.parser;

import com.codecrafts.ownredis.resp.constants.RESPEncodingConstants;
import com.codecrafts.ownredis.resp.constants.RESPParserConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;


public class RESPJSONParser {

    private static final Logger logger = LoggerFactory.getLogger(RESPJSONParser.class);

    private final List<String> dataStore;

    private final JSONArray parsedTree = new JSONArray();
    private int currentParseIndex = 0;

    private String stringData;

    private InputStream inputStream;
    private int length = 0;

    private RESPJSONParser(String builder) {
        this.stringData = stringData;
        this.dataStore = this.formatStringData();
    }
    private RESPJSONParser(InputStream inputStream) {
        this.inputStream=inputStream;
        this.dataStore = this.readAndFormatInputStream();
    }
    public static Builder getBuilder(){
        return new Builder();
    }
    public static class Builder{
        private String stringData;
        private InputStream inputStream;
        private Builder(){}
        public Builder setStringData(String stringData){
            this.stringData=stringData;
            return this;
        }
        public Builder setInputStream(InputStream inputStream){
            this.inputStream=inputStream;
            return this;
        }

        public RESPJSONParser build(){
            if((stringData==null || stringData.isBlank()) && inputStream==null){
                throw new IllegalArgumentException("String data or bytes of stream is needed...!");
            }

            if(stringData!=null && !stringData.isBlank())
                return new RESPJSONParser(this.stringData);
            return new RESPJSONParser(this.inputStream);
        }
    }


    private List<String> formatStringData() {
        List<String> respTokens = new ArrayList<>();
        if (this.stringData != null) {
            String[] tokens = this.stringData.split(RESPParserConstants.CRLF);
            for (String token : tokens) {
                respTokens.add(token);
                this.length++;
            }
            return respTokens;
        }
        return Collections.emptyList();
    }

    private List<String> readAndFormatInputStream() {
        try {
            List<String> respTokens = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                respTokens.add(line);
            }
            return respTokens;
        } catch (IOException ioException) {
            logger.error("IOException occurred while reading and formatting input stream: ", ioException);
        }

        return Collections.emptyList();
    }

    private JSONObject parseData() {
        try {
            String token = this.dataStore.get(this.currentParseIndex);

            if (token.startsWith(RESPParserConstants.SIMPLE_STRING)) {
                return this.parseSimpleString();
            } else if (token.startsWith(RESPParserConstants.BULK_STRING)) {
                return this.parseBulkString();
            } else if (token.startsWith(RESPParserConstants.INTEGER)) {
                return this.parseInteger();
            } else if (token.startsWith(RESPParserConstants.ERROR_STRING)) {
                return this.parseErrorString();
            } else if (token.startsWith(RESPParserConstants.DOUBLE)) {
                return this.parseDouble();
            } else if (token.startsWith(RESPParserConstants.ARRAY)) {
                return this.parseArray();
            } else if (token.startsWith(RESPParserConstants.BLOB_ERROR)) {
                return this.parseBlobError();
            } else if (token.startsWith(RESPParserConstants.BOOLEAN)) {
                return this.parseBoolean();
            } else if (token.startsWith(RESPParserConstants.BIG_NUMBER)) {
                return this.parseBigNumber();
            } else if (token.startsWith(RESPParserConstants.SET)) {
                return this.parseSet();
            } else if (token.startsWith(RESPParserConstants.VERBATIM_STRING)) {
                return this.parseVerbatimString();
            } else if (token.startsWith(RESPParserConstants.MAP)) {
                return this.parseMap();
            } else {
                throw new IllegalStateException(String.format("The token %s is not a valid token", token));
            }

        } catch (Exception exception) {
            logger.error("Exception occurred : ", exception);
        }
        return null;
    }

    public JSONArray parse() {

        while (this.currentParseIndex < this.dataStore.size()) {
            JSONObject data = this.parseData();
            if (data != null) {
                this.parsedTree.put(data);
            }
            this.currentParseIndex++;
        }

        return this.parsedTree;
    }

    private JSONObject buildParsedTreeItem(String type, Object value, String length) {
        return new JSONObject()
                .put(RESPParserConstants.TYPE, type)
                .put(RESPParserConstants.VALUE, value)
                .put(RESPParserConstants.LENGTH, length);
    }

    private JSONObject parseBoolean() {
        String token = this.dataStore.get(this.currentParseIndex);
        String booleanString = null;
        if (token.length() >= 2) {
            booleanString = token.substring(1);
            if (booleanString.equalsIgnoreCase("t") || booleanString.equalsIgnoreCase("f")) {
                boolean booleanValue = booleanString.equalsIgnoreCase("t");
                return this.buildParsedTreeItem(RESPEncodingConstants.BOOLEAN_TYPE, booleanValue, "1");
            } else {
                throw new IllegalStateException(String.format("The boolean value is not valid i.e %s", booleanString));
            }
        } else {
            logger.warn("The boolean value is empty, so defaulting it to false");
            return this.buildParsedTreeItem(RESPEncodingConstants.BOOLEAN_TYPE, false, "1");

        }
    }

    private JSONObject parseSimpleString() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            String simpleString = token.substring(1);
            return this.buildParsedTreeItem(RESPEncodingConstants.SIMPLE_STRING_TYPE, simpleString, String.valueOf(simpleString.length()));
        } else {
            logger.warn("An empty simple string is provided, so defaulting it to empty string");
            return this.buildParsedTreeItem(RESPEncodingConstants.SIMPLE_STRING_TYPE, "", "0");
        }
    }

    private JSONObject parseErrorString() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            String errorString = token.substring(1);
            return this.buildParsedTreeItem(RESPEncodingConstants.ERROR_STRING_TYPE, errorString, String.valueOf(errorString.length()));
        } else {
            logger.warn("An empty error string is provided, so defaulting it to empty string");
            return this.buildParsedTreeItem(RESPEncodingConstants.ERROR_STRING_TYPE, "", "0");
        }
    }

    private JSONObject parseInteger() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            int number = Integer.parseInt(token.substring(1));
            return this.buildParsedTreeItem(RESPEncodingConstants.INTEGER_TYPE, number, "NA");
        } else {
            logger.warn("An empty integer is provided, so defaulting it to 0");
            return this.buildParsedTreeItem(RESPEncodingConstants.INTEGER_TYPE, "0", "NA");
        }
    }

    private JSONObject parseDouble() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            Double number = Double.parseDouble(token.substring(1));
            return this.buildParsedTreeItem(RESPEncodingConstants.DOUBLE_TYPE, number, "NA");
        } else {
            logger.warn("An empty double is provided, so defaulting it to 0.0");
            return this.buildParsedTreeItem(RESPEncodingConstants.DOUBLE_TYPE, "0.0", "0");
        }
    }

    private JSONObject parseBigNumber() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            BigInteger number = new BigInteger(token.substring(1));
            return this.buildParsedTreeItem(RESPEncodingConstants.BIG_NUMBER_TYPE, number, "NA");
        } else {
            logger.warn("An empty big number is provided, so defaulting it to 0");
            return this.buildParsedTreeItem(RESPEncodingConstants.BIG_NUMBER_TYPE, "0", "0");
        }
    }

    private JSONObject parseBulkString() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            int bulkStringLength = Integer.parseInt(token.substring(1));
            if (bulkStringLength != -1) {
                this.currentParseIndex++;
                String bulkString = this.dataStore.get(currentParseIndex);
                if (bulkStringLength != bulkString.length()) {
                    throw new IllegalStateException(String.format("The bulk string length defined i.e $%d and actual provided string length does not match i.e %s : %d", bulkStringLength, bulkString, bulkString.length()));
                } else {
                    return this.buildParsedTreeItem(RESPEncodingConstants.BULK_STRING_TYPE, bulkString, String.valueOf(bulkStringLength));
                }
            } else {
                logger.warn("An empty bulk string is provided, so defaulting it to null");
                return this.buildParsedTreeItem(RESPEncodingConstants.NULL_TYPE, RESPEncodingConstants.BULK_STRING_TYPE, "0");
            }
        } else {
            throw new IllegalStateException(String.format("The bulk string length is not defined i.e %s", token));
        }
    }

    private JSONObject parseBlobError() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            int blobStringLength = Integer.parseInt(token.substring(1));
            if (blobStringLength != -1) {
                this.currentParseIndex++;
                String blobString = this.dataStore.get(currentParseIndex);
                if (blobStringLength != blobString.length()) {
                    throw new IllegalStateException(String.format("The blob string length defined i.e $%d and actual provided string length does not match i.e %s : %d", blobStringLength, blobString, blobString.length()));
                } else {
                    return this.buildParsedTreeItem(RESPEncodingConstants.BLOB_ERROR_TYPE, blobString, String.valueOf(blobStringLength));
                }
            } else {
                logger.warn("An empty blob string is provided, so defaulting it to null");
                return this.buildParsedTreeItem(RESPEncodingConstants.NULL_TYPE, RESPEncodingConstants.BLOB_ERROR_TYPE, "0");
            }
        } else {
            throw new IllegalStateException(String.format("The blob string length is not defined i.e %s", token));
        }
    }

    private JSONObject parseArray() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            int number = Integer.parseInt(token.substring(1));
            if (number > 0) {
                this.currentParseIndex++;
                List<JSONObject> arrayItems = new ArrayList<>();
                int idx = 0;
                while (idx < number) {
                    if (this.currentParseIndex == this.dataStore.size()) {
                        throw new IllegalStateException(String.format("The number of items declared v/s present does not match i.e %s", token));
                    }
                    JSONObject parsedData = this.parseData();
                    if (parsedData == null) {
                        throw new IllegalStateException(String.format("The number of items declared v/s present does not match i.e %s", token));
                    } else {
                        arrayItems.add(parsedData);
                        this.currentParseIndex++;
                        idx++;
                    }
                }
                //If in a map, the key value is array, then we land into index out of bounds exception
                //So putting the index penultimate level, just for that
                this.currentParseIndex--;
                return this.buildParsedTreeItem(RESPEncodingConstants.ARRAY_TYPE, arrayItems, String.valueOf(arrayItems.size()));

            } else if (number < 0) {
                return this.buildParsedTreeItem(RESPEncodingConstants.NULL_TYPE, RESPEncodingConstants.ARRAY_TYPE, "0");
            } else {
                return this.buildParsedTreeItem(RESPEncodingConstants.ARRAY_TYPE, new ArrayList<JSONObject>(), String.valueOf(number));
            }
        } else {
            return this.buildParsedTreeItem(RESPEncodingConstants.ARRAY_TYPE, new ArrayList<JSONObject>(), "0");
        }
    }

    private JSONObject parseSet() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            int number = Integer.parseInt(token.substring(1));
            if (number > 0) {
                this.currentParseIndex++;
                Set<JSONObject> setItems = new HashSet<>();
                int idx = 0;
                while (idx < number) {
                    if (this.currentParseIndex == this.dataStore.size()) {
                        throw new IllegalStateException(String.format("The number of items declared v/s present does not match i.e %s", token));
                    }
                    JSONObject parsedData = this.parseData();
                    if (parsedData == null) {
                        throw new IllegalStateException(String.format("The number of items declared v/s present does not match i.e %s", token));
                    } else {
                        setItems.add(parsedData);
                        this.currentParseIndex++;
                        idx++;
                    }
                }
                //If in a map, the key value is set, then we land into index out of bounds exception
                //So putting the index penultimate level, just for that
                this.currentParseIndex--;
                return this.buildParsedTreeItem(RESPEncodingConstants.SET_TYPE, setItems, String.valueOf(setItems.size()));

            } else if (number < 0) {
                return this.buildParsedTreeItem(RESPEncodingConstants.NULL_TYPE, RESPEncodingConstants.SET_TYPE, "0");
            } else {
                return this.buildParsedTreeItem(RESPEncodingConstants.SET_TYPE, new HashSet<JSONObject>(), String.valueOf(number));
            }
        } else {
            return this.buildParsedTreeItem(RESPEncodingConstants.SET_TYPE, new HashSet<JSONObject>(), "0");
        }
    }

    private JSONObject parseVerbatimString() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            int verbatimStringLength = Integer.parseInt(token.substring(1));
            if (verbatimStringLength != -1) {
                this.currentParseIndex++;
                String verbatimString = this.dataStore.get(currentParseIndex);
                if (verbatimStringLength != verbatimString.length()) {
                    throw new IllegalStateException(String.format("The verbatim string length defined i.e $%d and actual provided string length does not match i.e %s : %d", verbatimStringLength, verbatimString, verbatimString.length()));
                } else {
                    return this.buildParsedTreeItem(RESPEncodingConstants.VERBATIM_STRING_TYPE, verbatimString, String.valueOf(verbatimStringLength));
                }
            } else {
                logger.warn("An empty verbatim string is provided, so defaulting it to null");
                return this.buildParsedTreeItem(RESPEncodingConstants.NULL_TYPE, RESPEncodingConstants.VERBATIM_STRING_TYPE, "0");
            }
        } else {
            throw new IllegalStateException(String.format("The verbatim string length is not defined i.e %s", token));
        }
    }

    private JSONObject parseMap() {
        String token = this.dataStore.get(this.currentParseIndex);
        if (token.length() >= 2) {
            int number = Integer.parseInt(token.substring(1));
            if (number > 0) {
                this.currentParseIndex++;
                Map<JSONObject, JSONObject> map = new HashMap<>();
                int idx = 0;
                while (idx < number * 2) {
                    if (this.currentParseIndex == this.dataStore.size()) {
                        throw new IllegalStateException(String.format("The number of items declared v/s present does not match i.e %s", token));
                    }
                    JSONObject parsedKey = this.parseData();
                    this.currentParseIndex++;
                    JSONObject parsedValue = this.parseData();
                    if (parsedKey == null || parsedValue == null) {
                        throw new IllegalStateException("The provided map seems to be invalid...");
                    } else {
                        map.put(parsedKey, parsedValue);
                        this.currentParseIndex++;
                        idx = idx + 2;
                    }
                }
                return this.buildParsedTreeItem(RESPEncodingConstants.MAP_TYPE, map, String.valueOf(map.size()));

            } else if (number < 0) {
                return this.buildParsedTreeItem(RESPEncodingConstants.NULL_TYPE, RESPEncodingConstants.MAP_TYPE, "0");
            } else {
                return this.buildParsedTreeItem(RESPEncodingConstants.MAP_TYPE, new JSONObject(), String.valueOf(number));
            }
        } else {
            return this.buildParsedTreeItem(RESPEncodingConstants.MAP_TYPE, new JSONObject(""), "0");
        }
    }

}
