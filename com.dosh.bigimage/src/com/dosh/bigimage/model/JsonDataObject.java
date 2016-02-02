
package com.dosh.bigimage.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class JsonDataObject {
    protected static String PARSE_ERROR = "Problem parsing API response";

    protected static String UNKNOWN_ERROR = "Unknown error";

    public JsonDataObject() {
    }

    public JsonDataObject(String jsonStr) throws Exception {
        initFromJsonString(jsonStr);
    }

    public JsonDataObject(JSONObject jsonObj) throws Exception {
        initFromJsonObject(jsonObj);
    }

    public JsonDataObject initFromJsonString(String jsonStr) throws Exception {
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
            try {
                JSONArray jsonArr = new JSONArray(jsonStr);

                initFromJsonArray(jsonArr);

                return this;
            } catch (JSONException e1) {
                e1.printStackTrace();
                throw new Exception(PARSE_ERROR);
            }
        }

        initFromJsonObject(jsonObj);

        return this;
    }

    public abstract JsonDataObject initFromJsonObject(JSONObject jsonObj) throws Exception;

    protected JsonDataObject initFromJsonArray(JSONArray jsonArr) throws Exception {
        return null;
    }
}
