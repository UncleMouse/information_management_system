package com.example.information_management_system.util;

import com.google.gson.JsonObject;

public class ResUtil {

    public static String getMsg(String errorBody) {
        try {
            int idx = errorBody.indexOf("{");
            if (idx >= 0) {
                String json = errorBody.substring(idx);
                JsonObject obj = new com.google.gson.Gson().fromJson(json, JsonObject.class);
                return obj.has("msg") ? obj.get("msg").getAsString() : "操作失败，请稍后重试";
            }
        } catch (Exception ignored) {}
        return errorBody;
    }
}
