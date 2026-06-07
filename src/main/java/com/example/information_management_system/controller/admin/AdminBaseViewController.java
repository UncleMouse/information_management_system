package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class AdminBaseViewController {

    @FXML private VBox contentArea;
    @FXML private VBox navContainer;
    @FXML private Button logoutBtn;
    @FXML private Label userDisplayName;
    @FXML private Label userRoleLabel;

    private Button activeButton;

    @FXML
    public void initialize() {
        userDisplayName.setText(UserSession.getInstance().getUsername());
        userRoleLabel.setText("系统管理员");

        Button homeBtn = addNavBtn("首  页", "AdminHomePage.fxml");
        addNavBtn("学生管理", "StudentManagement.fxml");
        addNavBtn("教师管理", "TeacherManagement.fxml");
        addNavBtn("课程管理", "CourseManagement.fxml");
        addNavBtn("班级管理", "ClassManagement.fxml");
        addNavBtn("学期设置", "TermManagement.fxml");
        addNavBtn("通知管理", "AddNewAnnouncement.fxml");
        addNavBtn("个人中心", "PersonalCenter.fxml");

        logoutBtn.setOnAction(e -> handleLogout());

        // 主题切换按钮
        Button themeBtn = new Button("🌙 暗色");
        themeBtn.getStyleClass().add("sidebar-btn");
        themeBtn.setMaxWidth(Double.MAX_VALUE);
        themeBtn.setOnAction(e -> {
            boolean dark = !com.example.information_management_system.util.ThemeManager.isDark();
            com.example.information_management_system.util.ThemeManager.setDark(dark);
            themeBtn.setText(dark ? "☀ 亮色" : "🌙 暗色");
            if (navContainer.getScene() != null)
                com.example.information_management_system.util.ThemeManager.applyTheme(navContainer.getScene());
        });
        navContainer.getChildren().add(themeBtn);
        navContainer.sceneProperty().addListener((obs, o, s) -> {
            if (s != null) com.example.information_management_system.util.ThemeManager.applyTheme(s);
        });

        loadContent("AdminHomePage.fxml");
        setActiveButton(homeBtn);
    }

    private Button addNavBtn(String text, String fxmlName) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> { setActiveButton(btn); loadContent(fxmlName); });
        navContainer.getChildren().add(btn);
        return btn;
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) activeButton.getStyleClass().remove("sidebar-btn-active");
        activeButton = button;
        button.getStyleClass().add("sidebar-btn-active");
    }

    private void loadContent(String fxmlName) {
        try {
            String path = "/com/example/information_management_system/admin/" + fxmlName;
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(path)));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            VBox.setVgrow(view, Priority.ALWAYS);
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
            pt.setOnFinished(ev -> { contentArea.requestLayout(); contentArea.getParent().requestLayout(); });
            pt.play();
        } catch (IOException e) {
            e.printStackTrace();
            Label errorLabel = new Label("页面加载失败: " + fxmlName);
            errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-padding: 40;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }

    private void handleLogout() {
        com.example.information_management_system.MainApplication.clearSession();
        com.example.information_management_system.MainApplication.stopTokenRefreshTimer();
        UserSession.getInstance().clearSession();
        try {
            com.example.information_management_system.MainApplication.changeView("Login.fxml", "css/Login.css");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
