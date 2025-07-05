package com.codecrafts.ownredis.resp.encoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codecrafts.ownredis.resp.constants.RESPEncodingConstants;
import com.codecrafts.ownredis.resp.constants.RESPParserConstants;

import java.util.Iterator;

public class RESPJSONEncoder {
    private static final Logger logger = LoggerFactory.getLogger(RESPJSONEncoder.class);
    private final StringBuilder encodedString = new StringBuilder();
    private JSONArray parsedArray = new JSONArray();

    public RESPJSONEncoder(String parsedArray) {
        try {
            this.parsedArray = new JSONArray(parsedArray);
        } catch (Exception exception) {
            logger.error("An exception occurred while parsing parsed array string :", exception);
        }
    }

    public RESPJSONEncoder(JSONArray jsonArray) {
        this.parsedArray = jsonArray;
    }

    public String encode() {
        for (int idx = 0; idx < this.parsedArray.length(); idx++) {
            JSONObject parsedItem = this.parsedArray.getJSONObject(idx);
            this.encodedString.append(this.encodeParsedItem(parsedItem));
        }
        return encodedString.toString();
    }

    public String encodeParsedItem(JSONObject parsedItem) {
        try {
            String type = parsedItem.optString(RESPParserConstants.TYPE);

            if (type.startsWith(RESPEncodingConstants.SIMPLE_STRING_TYPE)) {
                return this.encodeSimpleString(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.BULK_STRING_TYPE)) {
                return this.encodeBulkString(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.INTEGER_TYPE)) {
                return this.encodeInteger(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.ERROR_STRING_TYPE)) {
                return this.encodeErrorString(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.DOUBLE_TYPE)) {
                return this.encodeDouble(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.ARRAY_TYPE)) {
                return this.encodeArray(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.BLOB_ERROR_TYPE)) {
                return this.encodeBlobError(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.BOOLEAN_TYPE)) {
                return this.encodeBoolean(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.BIG_NUMBER_TYPE)) {
                return this.encodeBigNumber(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.SET_TYPE)) {
                return this.encodeSet(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.VERBATIM_STRING_TYPE)) {
                return this.encodeVerbatimString(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.MAP_TYPE)) {
                return this.encodeMap(parsedItem);
            } else if (type.startsWith(RESPEncodingConstants.NULL_TYPE)) {
                return this.encodeNull(parsedItem);
            } else {
                throw new IllegalStateException(String.format("The parsedItem type %s is invalid", type));
            }

        } catch (Exception exception) {
            logger.error("Exception occurred : ", exception);
        }
        return null;
    }

    private String encodeSimpleString(JSONObject parsedItem) {
        return RESPParserConstants.SIMPLE_STRING +
                parsedItem.optString(RESPParserConstants.VALUE) +
                RESPParserConstants.CRLF;
    }

    private String encodeBulkString(JSONObject parsedItem) {
        return RESPParserConstants.BULK_STRING +
                parsedItem.optString(RESPParserConstants.LENGTH) +
                RESPParserConstants.CRLF +
                parsedItem.optString(RESPParserConstants.VALUE) +
                RESPParserConstants.CRLF;
    }

    private String encodeInteger(JSONObject parsedItem) {
        return RESPParserConstants.INTEGER +
                parsedItem.optString(RESPParserConstants.VALUE) +
                RESPParserConstants.CRLF;
    }

    private String encodeErrorString(JSONObject parsedItem) {
        return RESPParserConstants.ERROR_STRING +
                parsedItem.optString(RESPParserConstants.VALUE) +
                RESPParserConstants.CRLF;
    }

    private String encodeDouble(JSONObject parsedItem) {
        return RESPParserConstants.DOUBLE +
                parsedItem.optString(RESPParserConstants.VALUE) +
                RESPParserConstants.CRLF;
    }

    private String encodeBlobError(JSONObject parsedItem) {
        return RESPParserConstants.BLOB_ERROR +
                parsedItem.optString(RESPParserConstants.LENGTH) +
                RESPParserConstants.CRLF +
                parsedItem.optString(RESPParserConstants.VALUE) +
                RESPParserConstants.CRLF;
    }

    private String encodeBoolean(JSONObject parsedItem) {
        String value = Boolean.toString(parsedItem.optBoolean(RESPParserConstants.VALUE)).equalsIgnoreCase("true") ? "t" : "f";
        return RESPParserConstants.BOOLEAN +
                value +
                RESPParserConstants.CRLF;
    }

    private String encodeBigNumber(JSONObject parsedItem) {
        return RESPParserConstants.BIG_NUMBER +
                parsedItem.optString(RESPParserConstants.VALUE) +
                RESPParserConstants.CRLF;
    }

    private String encodeVerbatimString(JSONObject parsedItem) {
        return RESPParserConstants.VERBATIM_STRING +
                parsedItem.optString(RESPParserConstants.LENGTH) +
                RESPParserConstants.CRLF +
                parsedItem.optString(RESPParserConstants.VALUE) +
                RESPParserConstants.CRLF;
    }

    private String encodeArray(JSONObject parsedItem) {
        StringBuilder arrayString = new StringBuilder(RESPParserConstants.ARRAY + parsedItem.optString(RESPParserConstants.LENGTH) + RESPParserConstants.CRLF);
        JSONArray recordsJsonArray = parsedItem.optJSONArray(RESPParserConstants.VALUE);
        for (int idx = 0; idx < Integer.parseInt(parsedItem.optString(RESPParserConstants.LENGTH)); idx++) {
            JSONObject recordObject = (JSONObject) recordsJsonArray.get(idx);
            arrayString.append(this.encodeParsedItem(recordObject));
        }
        return arrayString.toString();
    }

    private String encodeSet(JSONObject parsedItem) {
        StringBuilder setString = new StringBuilder(RESPParserConstants.SET + parsedItem.optString(RESPParserConstants.LENGTH) + RESPParserConstants.CRLF);
        JSONArray recordsSetJsonArray = (JSONArray) parsedItem.get(RESPParserConstants.VALUE);
        for (int idx = 0; idx < Integer.parseInt(parsedItem.optString(RESPParserConstants.LENGTH)); idx++) {
            JSONObject recordObject = (JSONObject) recordsSetJsonArray.get(idx);
            setString.append(this.encodeParsedItem(recordObject));
        }
        return setString.toString();
    }

    private String encodeMap(JSONObject parsedItem) {
        StringBuilder mapString = new StringBuilder(RESPParserConstants.MAP + parsedItem.optString(RESPParserConstants.LENGTH) + RESPParserConstants.CRLF);
        JSONObject recordsMapObject = (JSONObject) parsedItem.get(RESPParserConstants.VALUE);
        Iterator<String> recordsMapObjectIterator = recordsMapObject.keySet().iterator();
        while (recordsMapObjectIterator.hasNext()) {
            String recordObjectKey = recordsMapObjectIterator.next();
            mapString.append(this.encodeParsedItem(new JSONObject(recordObjectKey)));
            mapString.append(this.encodeParsedItem(recordsMapObject.getJSONObject(recordObjectKey)));
        }
        return mapString.toString();
    }

    private String encodeNull(JSONObject parsedItem) {
        String nullitemType = parsedItem.optString(RESPParserConstants.VALUE);
        if (nullitemType.equalsIgnoreCase(RESPEncodingConstants.BULK_STRING_TYPE)) {
            return RESPParserConstants.BULK_STRING + RESPEncodingConstants.NULL_VALUE;
        } else if (nullitemType.equalsIgnoreCase(RESPEncodingConstants.ARRAY_TYPE)) {
            return RESPParserConstants.ARRAY + RESPEncodingConstants.NULL_VALUE;
        } else if (nullitemType.equalsIgnoreCase(RESPEncodingConstants.SET_TYPE)) {
            return RESPParserConstants.SET + RESPEncodingConstants.NULL_VALUE;
        } else if (nullitemType.equalsIgnoreCase(RESPEncodingConstants.BLOB_ERROR_TYPE)) {
            return RESPParserConstants.BLOB_ERROR + RESPEncodingConstants.NULL_VALUE;
        } else if (nullitemType.equalsIgnoreCase(RESPEncodingConstants.VERBATIM_STRING_TYPE)) {
            return RESPParserConstants.VERBATIM_STRING + RESPEncodingConstants.NULL_VALUE;
        } else if (nullitemType.equalsIgnoreCase(RESPEncodingConstants.MAP_TYPE)) {
            return RESPParserConstants.MAP + RESPEncodingConstants.NULL_VALUE;
        } else {
            throw new IllegalStateException("Null value does not belong to any valid types");
        }
    }
}
