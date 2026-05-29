package com.example.information_management_system;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.Refresh;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class MainApplication extends Application {
    private static Stage stage;
    private static final double APP_WIDTH = 1000.0;
    private static final double APP_HEIGHT = 680.0;

    @Override
    public void start(Stage stage) throws Exception {
        MainApplication.stage = stage;
        stage.setTitle("校园信息管理系统");
        stage.setWidth(APP_WIDTH);
        stage.setHeight(APP_HEIGHT);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // 自动登录检查
        if (tryAutoLogin()) {
            try {
                showMainView();
            } catch (IOException e) {
                changeView("Login.fxml", "css/Login.css");
            }
        } else {
            changeView("Login.fxml", "css/Login.css");
        }
        stage.show();
    }

    public static void saveSession(int identity, String username, String token, String refreshToken) {
        Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
        prefs.putInt("identity", identity);
        prefs.put("username", username);
        prefs.put("token", token);
        prefs.put("refreshToken", refreshToken);
    }

    public static void clearSession() {
        Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
        prefs.remove("identity"); prefs.remove("username");
        prefs.remove("token"); prefs.remove("refreshToken");
    }

    private boolean tryAutoLogin() {
        Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
        String token = prefs.get("token", null);
        if (token == null || token.isEmpty()) return false;
        UserSession.getInstance().setIdentity(prefs.getInt("identity", 0));
        UserSession.getInstance().setUsername(prefs.get("username", ""));
        UserSession.getInstance().setToken(token);
        UserSession.getInstance().setRefreshToken(prefs.get("refreshToken", ""));
        startTokenRefreshTimer();
        return true;
    }

    @Override
    public void stop() {
        stopTokenRefreshTimer();
        UserSession.getInstance().clearSession();
        NetworkUtils.shutdown();
        Platform.exit();
        System.exit(0);
    }

    public static void changeView(String fxml, String css) throws IOException {
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxml));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        if (css != null && !css.isEmpty()) {
            scene.getStylesheets().add(
                    Objects.requireNonNull(MainApplication.class.getResource(css)).toExternalForm());
        }
        scene.setUserData(loader.getController());
        stage.setScene(scene);
        stage.setWidth(currentWidth);
        stage.setHeight(currentHeight);
    }

    public static void showMainView() throws IOException {
        switch (UserSession.getInstance().getIdentity()) {
            case 2 -> changeView("student/StudentBaseView.fxml", "css/student/BaseView.css");
            case 1 -> changeView("teacher/TeacherBaseView.fxml", "css/teacher/TeacherBaseView.css");
            case 0 -> changeView("admin/AdminBaseView.fxml", "css/admin/AdminBaseView.css");
        }
    }

    private static Timer tokenRefreshTimer;

    public static void startTokenRefreshTimer() {
        stopTokenRefreshTimer();
        tokenRefreshTimer = new Timer(true);
        tokenRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Refresh.refreshtoken();
            }
        }, 29 * 60 * 1000, 29 * 60 * 1000);
    }

    public static void stopTokenRefreshTimer() {
        if (tokenRefreshTimer != null) {
            tokenRefreshTimer.cancel();
            tokenRefreshTimer = null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
