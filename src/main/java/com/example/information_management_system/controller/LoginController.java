package com.example.information_management_system.controller;

import com.example.information_management_system.MainApplication;
import com.example.information_management_system.entity.Data;
import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    private final Gson gson = new Gson();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Hyperlink studentQuickLogin;
    @FXML private Hyperlink teacherQuickLogin;
    @FXML private Hyperlink adminQuickLogin;
    @FXML private Label errorMessageLabel;

    @FXML
    public void initialize() {
        loginBtn.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("请输入账号和密码");
            return;
        }
        authenticateUser(username, password);
    }

    private void authenticateUser(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("stuId", username);
        body.put("password", password);
        String json = gson.toJson(body);

        NetworkUtils.post("/login/simpleLogin", json, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonObject data = res.getAsJsonObject("data");
                        UserSession.getInstance().setIdentity(data.get("permission").getAsInt());
                        UserSession.getInstance().setToken(data.get("accessToken").getAsString());
                        UserSession.getInstance().setRefreshToken(data.get("refreshToken").getAsString());
                        UserSession.getInstance().setUsername(data.get("username").getAsString());
                        MainApplication.saveSession(
                            data.get("permission").getAsInt(),
                            data.get("username").getAsString(),
                            data.get("accessToken").getAsString(),
                            data.get("refreshToken").getAsString());
                        fetchInitialData();
                        MainApplication.startTokenRefreshTimer();
                        navigateToMain();
                    } else {
                        showErrorMessage(res.has("msg") ? res.get("msg").getAsString() : "操作失败，请稍后重试");
                    }
                } catch (Exception e) {
                    showErrorMessage("登录验证失败，请检查账号密码");
                }
            }

            @Override
            public void onFailure(Exception e) {
                String msg = e.getMessage();
                try {
                    int idx = msg.indexOf("{");
                    if (idx >= 0) {
                        JsonObject err = gson.fromJson(msg.substring(idx), JsonObject.class);
                        showErrorMessage(err.has("msg") ? err.get("msg").getAsString() : msg);
                        return;
                    }
                } catch (Exception ignored) {}
                showErrorMessage(msg);
            }
        });
    }

    private void fetchInitialData() {
        fetchUserInfo();
        fetchSemesters();
        fetchCurrentTerm();
        if (UserSession.getInstance().getIdentity() != 2) {
            fetchClassRooms();
        }
    }

    private void fetchUserInfo() {
        NetworkUtils.post("/user/getInfo", "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonObject data = res.getAsJsonObject("data");
                        UserSession session = UserSession.getInstance();
                        session.setId(data.has("id") ? data.get("id").getAsInt() : null);
                        session.setSduid(data.has("sduid") ? data.get("sduid").getAsString() : null);
                        session.setPhone(data.has("phone") ? data.get("phone").getAsString() : null);
                        session.setEmail(data.has("email") ? data.get("email").getAsString() : null);
                        session.setSex(data.has("sex") ? data.get("sex").getAsString() : null);
                        session.setCollege(data.has("college") ? data.get("college").getAsString() : null);
                        session.setMajor(data.has("major") ? data.get("major").getAsString() : null);
                        session.setNation(data.has("nation") ? data.get("nation").getAsString() : null);
                        session.setEthnic(data.has("ethnic") ? data.get("ethnic").getAsString() : null);
                        session.setPoliticsStatus(data.has("politicsStatus") ? data.get("politicsStatus").getAsString() : null);
                    }
                } catch (Exception e) {
                    System.err.println("获取用户信息失败: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("获取用户信息失败: " + e.getMessage());
            }
        });
    }

    private void fetchSemesters() {
        NetworkUtils.get("/term/getTermList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonArray arr = res.getAsJsonArray("data");
                    ObservableList<String> list = FXCollections.observableArrayList();
                    for (int i = 0; i < arr.size(); i++)
                        list.add(arr.get(i).getAsJsonObject().get("term").getAsString());
                    Data.getInstance().setSemesterList(list);
                }
            }
            @Override
            public void onFailure(Exception e) {
                System.err.println("加载学期列表失败");
            }
        });
    }

    private void fetchCurrentTerm() {
        NetworkUtils.get("/term/getCurrentTerm", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    Data.getInstance().setCurrentTerm(res.get("data").getAsString());
                }
            }
            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void fetchClassRooms() {
        NetworkUtils.get("/Teacher/getClassRoom", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                JsonObject res = gson.fromJson(result, JsonObject.class);
                if (res.get("code").getAsInt() == 200) {
                    JsonArray arr = res.getAsJsonArray("data");
                    ObservableList<String> list = FXCollections.observableArrayList();
                    for (int i = 0; i < arr.size(); i++)
                        list.add(arr.get(i).getAsJsonObject().get("location").getAsString());
                    Data.getInstance().setClassRoomList(list);
                }
            }
            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void navigateToMain() {
        try {
            MainApplication.showMainView();
        } catch (IOException e) {
            showErrorMessage("页面加载失败，请重启应用");
        }
    }

    private void showErrorMessage(String msg) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(msg);
            errorMessageLabel.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }

    @FXML
    public void studentQuickLogin(javafx.event.ActionEvent event) {
        usernameField.setText("202500000001");
        passwordField.setText("123456");
        doLogin();
    }

    @FXML
    public void teacherQuickLogin(javafx.event.ActionEvent event) {
        usernameField.setText("20250001");
        passwordField.setText("123456");
        doLogin();
    }

    @FXML
    public void adminQuickLogin(javafx.event.ActionEvent event) {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        doLogin();
    }

    private void doLogin() { handleLogin(); }
}
