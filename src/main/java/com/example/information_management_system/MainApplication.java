package com.example.information_management_system;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.Refresh;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
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

        // 设置应用图标（内置生成 / 外部 logo.png）
        stage.getIcons().add(loadAppIcon());

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
        clearSession();
        return false;
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

    /** 加载应用图标：优先使用 images/logo.png，否则生成内置图标 */
    private static Image loadAppIcon() {
        // 1. 尝试加载用户自定义 logo
        try {
            InputStream is = MainApplication.class.getResourceAsStream("/com/example/information_management_system/images/logo.png");
            if (is != null) {
                Image img = new Image(is);
                is.close();
                return img;
            }
        } catch (Exception ignored) {}
        // 2. 内置生成：深蓝底色 + 白色"校"字图标
        Canvas c = new Canvas(64, 64);
        GraphicsContext g = c.getGraphicsContext2D();
        g.setFill(Color.web("#1e3a5f"));
        g.fillRoundRect(0, 0, 64, 64, 14, 14);
        g.setFill(Color.web("#4f6ef7"));
        g.fillRoundRect(4, 4, 56, 56, 12, 12);
        g.setFill(Color.WHITE);
        g.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 30));
        g.fillText("校", 14, 44);
        WritableImage wi = new WritableImage(64, 64);
        c.snapshot(new SnapshotParameters(), wi);
        return wi;
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
