package com.example.information_management_system.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtil {

    public static JsonArray extractArray(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return new JsonArray();
        if (el.isJsonArray()) return el.getAsJsonArray();
        if (el.isJsonObject()) {
            JsonObject dataObj = el.getAsJsonObject();
            if (dataObj.has("records")) return dataObj.getAsJsonArray("records");
            if (dataObj.has("data")) return dataObj.getAsJsonArray("data");
            if (dataObj.has("list")) return dataObj.getAsJsonArray("list");
        }
        return new JsonArray();
    }
}
