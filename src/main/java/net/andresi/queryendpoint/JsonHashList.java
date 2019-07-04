package net.andresi.queryendpoint;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonHashList {

    Object jsonRoot = null;

    public JsonHashList(Object jsonObjIn) {
        this.jsonRoot = jsonObjIn;
    }

    public Object getJsonItem(String... keys) {
        Object returnObj = this.jsonRoot;
        try {
            for (int i = 0; i < keys.length; i++) {
                if (returnObj.getClass() == ArrayList.class) {
                    ArrayList<Object> list = (ArrayList<Object>) returnObj;
                    try {
                        returnObj = list.get(Integer.parseInt(keys[i]));
                    } catch (NumberFormatException nfe) {
                        returnObj = null;
                    }
                } else if (returnObj.getClass() == HashMap.class) {
                    HashMap<String, Object> hash = (HashMap<String, Object>) returnObj;
                    returnObj = hash.get(keys[i]);
                }
            }
        } catch (NullPointerException npe) {
            returnObj = null;
        }
        return returnObj;
   }
}
