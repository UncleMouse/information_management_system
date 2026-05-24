package com.example.information_management_system.util;

import com.example.information_management_system.entity.UserSession;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Refresh {

    public static void refreshtoken() {
        if (NetworkUtils.offlineMode) return;
        try {
            String refreshToken = UserSession.getInstance().getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) return;

            String jsonBody = "{\"refreshToken\":\"" + refreshToken + "\"}";
            URL url = new URL(NetworkUtils.BaseUrl + "/login/refreshToken");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            Gson gson = new Gson();
            JsonObject res = gson.fromJson(response.toString(), JsonObject.class);
            if (res.has("code") && res.get("code").getAsInt() == 200) {
                JsonObject data = res.getAsJsonObject("data");
                UserSession.getInstance().setToken(data.get("accessToken").getAsString());
                UserSession.getInstance().setRefreshToken(data.get("refreshToken").getAsString());
                System.out.println("Token refreshed");
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("Token refresh failed: " + e.getMessage());
        }
    }
}
