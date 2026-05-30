package com.example.information_management_system.util;

import com.example.information_management_system.entity.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkUtils {
    private static final Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
    private static final int TIMEOUT = 30000;
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    public static String BaseUrl;
    public static boolean offlineMode = false;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        try (InputStream in = NetworkUtils.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                BaseUrl = props.getProperty("server.url", "http://localhost:8081");
                offlineMode = "offline".equalsIgnoreCase(props.getProperty("app.mode", "online"));
                if (offlineMode) {
                    System.out.println("=== 离线演示模式已启用，所有数据均为模拟数据 ===");
                }
                System.out.println("配置加载成功，BaseUrl = " + BaseUrl);
            } else {
                LOGGER.warning("application.properties 未找到，使用默认地址");
                BaseUrl = "http://localhost:8081";
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "加载配置失败", e);
            BaseUrl = "http://localhost:8081";
        }
    }

    public enum HttpMethod { GET, POST, PUT, DELETE }

    public interface Callback<T> {
        void onSuccess(T result) throws IOException;
        void onFailure(Exception e);
    }

    public static void request(String urlString, HttpMethod method, Map<String, String> headers,
                               String body, Callback<String> callback) {
        // 离线模式：直接返回模拟数据
        if (offlineMode) {
            Task<String> mockTask = new Task<>() {
                @Override
                protected String call() {
                    return MockData.getMockResponse(urlString, method.name(), body);
                }
            };
            mockTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    try { callback.onSuccess(mockTask.getValue()); }
                    catch (IOException e) { throw new RuntimeException(e); }
                });
            });
            EXECUTOR.submit(mockTask);
            return;
        }

        String finalUrlString = BaseUrl + urlString;
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return sendRequest(finalUrlString, method, headers, body);
            }
        };
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try { callback.onSuccess(task.getValue()); }
                catch (IOException e) { throw new RuntimeException(e); }
            });
        });
        task.setOnFailed(event -> {
            Platform.runLater(() -> callback.onFailure((Exception) task.getException()));
        });
        EXECUTOR.submit(task);
    }

    public static CompletableFuture<String> requestAsync(String urlString, HttpMethod method,
                                                         Map<String, String> headers, String body) {
        // 离线模式：直接返回模拟数据
        if (offlineMode) {
            return CompletableFuture.supplyAsync(() ->
                    MockData.getMockResponse(urlString, method.name(), body), EXECUTOR);
        }
        String finalUrlString = BaseUrl + urlString;
        return CompletableFuture.supplyAsync(() -> {
            try { return sendRequest(finalUrlString, method, headers, body); }
            catch (IOException e) { throw new RuntimeException("网络请求失败", e); }
        }, EXECUTOR);
    }

    // --- GET 请求 ---
    public static void get(String urlString, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        request(urlString, HttpMethod.GET, headers, null, callback);
    }

    public static void get(String urlString, Map<String, String> params, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        request(appendQueryParams(urlString, params), HttpMethod.GET, headers, null, callback);
    }

    public static CompletableFuture<String> getAsync(String urlString) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        return requestAsync(urlString, HttpMethod.GET, headers, null);
    }

    public static CompletableFuture<String> getAsync(String urlString, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        return requestAsync(appendQueryParams(urlString, params), HttpMethod.GET, headers, null);
    }

    // --- POST 请求 ---
    public static void post(String urlString, String body, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        request(urlString, HttpMethod.POST, headers, body, callback);
    }

    public static void post(String urlString, Map<String, String> params, String body, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        request(appendQueryParams(urlString, params), HttpMethod.POST, headers, body, callback);
    }

    public static CompletableFuture<String> postAsync(String urlString, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        return requestAsync(urlString, HttpMethod.POST, headers, body);
    }

    public static CompletableFuture<String> postAsync(String urlString, Map<String, String> params, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        String fullUrl = appendQueryParams(urlString, params);
        return requestAsync(fullUrl, HttpMethod.POST, headers, body != null ? body : "");
    }

    // --- Form提交 (application/x-www-form-urlencoded) ---
    public static void postForm(String urlString, Map<String, String> params, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        String body = params.entrySet().stream()
                .map(e -> {
                    try {
                        return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
                    } catch (Exception ex) {
                        return e.getKey() + "=" + e.getValue();
                    }
                })
                .reduce((a, b) -> a + "&" + b).orElse("");
        request(urlString, HttpMethod.POST, headers, body, callback);
    }

    // --- 发送参数作为查询参数 (params in URL, body=null) ---
    public static void postWithQueryParams(String urlString, Map<String, String> params, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        String fullUrl = appendQueryParams(urlString, params);
        System.out.println("POST URL: " + fullUrl);
        request(fullUrl, HttpMethod.POST, headers, null, callback);
    }

    // --- PUT 请求 ---
    public static void put(String urlString, String body, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        request(urlString, HttpMethod.PUT, headers, body, callback);
    }

    public static CompletableFuture<String> putAsync(String urlString, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        return requestAsync(urlString, HttpMethod.PUT, headers, body);
    }

    // --- DELETE 请求 ---
    public static void delete(String urlString, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        request(urlString, HttpMethod.DELETE, headers, null, callback);
    }

    public static CompletableFuture<String> deleteAsync(String urlString) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
        return requestAsync(urlString, HttpMethod.DELETE, headers, null);
    }

    // --- 文件上传 ---
    public static CompletableFuture<String> postMultipartFileAsync(String urlString, File file) {
        if (offlineMode) {
            return CompletableFuture.supplyAsync(() ->
                    MockData.getMockResponse(urlString, "POST", null), EXECUTOR);
        }
        String finalUrlString = BaseUrl + urlString;
        String boundary = "===" + System.currentTimeMillis() + "===";
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(finalUrlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                String token = "Bearer " + UserSession.getInstance().getToken();
                if (token != null && !token.equals("Bearer null")) {
                    connection.setRequestProperty("Authorization", token);
                }

                try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                     FileInputStream fis = new FileInputStream(file)) {
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + lineEnd);
                    dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                    dos.writeBytes(lineEnd);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    dos.flush();
                }

                int responseCode = connection.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder response = new StringBuilder();
                if (is != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) response.append(line);
                    }
                }
                if (responseCode >= 400) throw new IOException("HTTP " + responseCode + ": " + response);
                return response.toString();
            } catch (IOException e) {
                throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, EXECUTOR);
    }

    public static void executeRequest(String urlString, HttpMethod method,
                                      Consumer<String> onSuccess, Consumer<Exception> onError) {
        requestAsync(urlString, method, null, null)
                .thenAccept(result -> Platform.runLater(() -> onSuccess.accept(result)))
                .exceptionally(ex -> { Platform.runLater(() -> onError.accept((Exception) ex.getCause())); return null; });
    }

    private static String appendQueryParams(String urlString, Map<String, String> params) {
        if (params == null || params.isEmpty()) return urlString;
        StringBuilder sb = new StringBuilder(urlString);
        if (!urlString.contains("?")) sb.append("?");
        else if (!urlString.endsWith("&")) sb.append("&");
        String query = params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> { try { return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8"); } catch (Exception ex) { return e.getKey() + "=" + e.getValue(); } })
                .reduce((a, b) -> a + "&" + b).orElse("");
        return sb.append(query).toString();
    }

    private static String sendRequest(String urlString, HttpMethod method,
                                      Map<String, String> headers, String body) throws IOException {
        HttpURLConnection connection = null;
        try {
            System.out.println("[NetworkUtils] >>> " + method + " " + urlString);
            if (body != null && !body.isEmpty()) {
                System.out.println("[NetworkUtils] Body: " + body);
            }
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method.name());
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setDoInput(true);
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet())
                    connection.setRequestProperty(e.getKey(), e.getValue());
            }
            // POST/PUT must setDoOutput(true) for HttpURLConnection to work correctly
            if (method == HttpMethod.POST || method == HttpMethod.PUT) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length", "0");
                if (body != null && !body.isEmpty()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    connection.setRequestProperty("Content-Length", String.valueOf(input.length));
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(input, 0, input.length);
                    }
                }
            }
            int code = connection.getResponseCode();
            System.out.println("[NetworkUtils] HTTP Status: " + code);
            InputStream is = (code >= 200 && code < 300) ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder response = new StringBuilder();
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);
                }
            }
            System.out.println("[NetworkUtils] Response: " + response);
            if (code >= 400) throw new IOException(mapHttpError(code, response.toString()));
            return response.toString();
        } catch (IOException e) {
            System.out.println("[NetworkUtils] 网络异常: " + e.getMessage());
            throw e;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String mapHttpError(int code, String body) {
        String prefix;
        switch (code) {
            case 401: prefix = "登录验证失效，请重新登录"; break;
            case 403: prefix = "登录验证失效，请重新登录"; break;
            case 404: prefix = "请求的资源不存在"; break;
            case 500: prefix = "服务器内部错误，请稍后重试"; break;
            default:  prefix = "请求失败 (HTTP " + code + ")"; break;
        }
        // 如果有后端返回的提示信息，拼接到后面
        if (body != null && !body.isEmpty()) {
            try {
                com.google.gson.JsonObject obj = new com.google.gson.Gson().fromJson(body, com.google.gson.JsonObject.class);
                if (obj.has("msg")) {
                    return prefix + ": " + obj.get("msg").getAsString();
                }
            } catch (Exception ignored) {}
        }
        return prefix;
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}
