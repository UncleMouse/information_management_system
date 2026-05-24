package com.example.information_management_system.controller;

import com.example.information_management_system.MainApplication;
import com.example.information_management_system.entity.Data;
import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.util.*;

public class LoginController {

    private final Gson gson = new Gson();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorMessageLabel;
    @FXML private Hyperlink toggleLoginLink;
    @FXML private Hyperlink studentQuickLogin;
    @FXML private Hyperlink teacherQuickLogin;
    @FXML private Hyperlink adminQuickLogin;

    private boolean isOAuthMode = false;

    @FXML
    public void initialize() {
        if (errorMessageLabel != null) errorMessageLabel.setVisible(false);
        if (loginButton != null) loginButton.setOnAction(this::handleLogin);

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.DOWN) {
                passwordField.requestFocus();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                doLogin();
            }
        });
    }

    private void handleLogin(javafx.event.ActionEvent event) {
        doLogin();
    }

    private void doLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
            showErrorMessage("用户名和密码不能为空");
            return;
        }
        authenticateUser(username, password);
    }

    private void authenticateUser(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("stuId", username);
        body.put("password", password);
        String json = gson.toJson(body);

        String endpoint = isOAuthMode ? "/login/SDULogin" : "/login/simpleLogin";

        NetworkUtils.post(endpoint, json, new NetworkUtils.Callback<String>() {
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
                        fetchInitialData();
                        MainApplication.startTokenRefreshTimer();
                        navigateToMain();
                    } else {
                        showErrorMessage(res.has("msg") ? res.get("msg").getAsString() : "登录失败");
                    }
                } catch (Exception e) {
                    showErrorMessage("登录响应处理失败");
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
        fetchSemesters();
        fetchCurrentTerm();
        if (UserSession.getInstance().getIdentity() != 2) {
            fetchClassRooms();
        }
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
            showErrorMessage("无法加载主界面");
        }
    }

    private void showErrorMessage(String msg) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(msg);
            errorMessageLabel.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("登录错误");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }

    @FXML
    public void handleToggleLogin(javafx.event.ActionEvent event) {
        isOAuthMode = !isOAuthMode;
        if (isOAuthMode) {
            usernameField.setPromptText("请输入统一认证账号");
            toggleLoginLink.setText("普通密码登录");
        } else {
            usernameField.setPromptText("请输入学号或工号");
            toggleLoginLink.setText("统一认证登录");
        }
    }

    @FXML
    public void studentQuickLogin(javafx.event.ActionEvent event) {
        usernameField.setText("202400000001");
        passwordField.setText("123456");
        doLogin();
    }

    @FXML
    public void teacherQuickLogin(javafx.event.ActionEvent event) {
        usernameField.setText("190100000000");
        passwordField.setText("123456");
        doLogin();
    }

    @FXML
    public void adminQuickLogin(javafx.event.ActionEvent event) {
        usernameField.setText("1");
        passwordField.setText("123456");
        doLogin();
    }
}
