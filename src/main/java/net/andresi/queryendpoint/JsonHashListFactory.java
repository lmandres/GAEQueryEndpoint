package net.andresi.queryendpoint;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;

import com.fasterxml.jackson.core.JsonParseException;

public class JsonHashListFactory {

    public static JsonHashList parseJsonToHashList(
        JsonParser jsonParser
    ) throws JsonParseException {
        Object jsonRoot = null;
        try {
            if (jsonParser.nextToken() != null) {
                jsonRoot = parseTokenItem(jsonParser);
            }
        } catch (JsonParseException jpe) {
            jpe.printStackTrace();
            throw jpe;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return new JsonHashList(jsonRoot);
    }

    private static Object parseTokenItem(
        JsonParser jsonParser
    ) throws JsonParseException {
        switch (jsonParser.getCurrentToken()) {
        case VALUE_FALSE:
            return false;
        case VALUE_TRUE:
            return true;
        case VALUE_NULL:
            return null;
        case VALUE_NUMBER_FLOAT:
            Float returnFloat = null;
            try {
                returnFloat = jsonParser.getFloatValue();
            } catch (IOException ioe) {
            }
            return returnFloat;
        case VALUE_NUMBER_INT:
            Integer returnInteger = null;
            try {
                returnInteger = jsonParser.getIntValue();
            } catch (IOException ioe) {
            }
            return returnInteger;
        case VALUE_STRING:
            String returnString = null;
            try {
                returnString = jsonParser.getText();
            } catch (IOException ioe) {
            }
            return returnString;
        case START_ARRAY:
            ArrayList<Object> returnList = new ArrayList<Object>();
            try {
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    returnList.add(parseTokenItem(jsonParser));
                }
            } catch (JsonParseException jpe) {
                returnList = null;
                throw jpe;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return returnList;
        case START_OBJECT:
            HashMap<String, Object> returnHash = new HashMap<String, Object>();
            try {
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    jsonParser.nextToken();
                    returnHash.put(jsonParser.getCurrentName(), parseTokenItem(jsonParser));
                }
            } catch (JsonParseException jpe) {
                returnHash = null;
                throw jpe;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return returnHash;
        default:
            return null;
        }
    }
}
