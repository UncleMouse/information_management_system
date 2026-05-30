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
            if (dataObj.has("list")) return dataObj.getAsJsonArray("list");
            if (dataObj.has("user")) return dataObj.getAsJsonArray("user");
            if (dataObj.has("data")) return dataObj.getAsJsonArray("data");
            if (dataObj.has("records")) return dataObj.getAsJsonArray("records");
            if (dataObj.has("section")) return dataObj.getAsJsonArray("section");
        }
        return new JsonArray();
    }

    /** 安全获取字符串，null 时返回空字符串 */
    public static String safeGetString(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) return "";
        return obj.get(key).getAsString();
    }

    /** 安全获取整数，null 时返回 0 */
    public static int safeGetInt(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) return 0;
        return obj.get(key).getAsInt();
    }
}
