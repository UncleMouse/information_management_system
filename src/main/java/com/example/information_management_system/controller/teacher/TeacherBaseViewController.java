package com.example.information_management_system.controller.teacher;

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

public class TeacherBaseViewController {

    @FXML private VBox contentArea;
    @FXML private VBox navContainer;
    @FXML private Button logoutBtn;
    @FXML private Label teacherNameLabel;

    private Button activeButton;

    @FXML
    public void initialize() {
        teacherNameLabel.setText(UserSession.getInstance().getUsername());

        Button homeBtn = addNavBtn("首  页", "teacher/TeacherHomePage.fxml");
        addNavBtn("课程管理", "teacher/CourseManagementContent.fxml");
        addNavBtn("课表查看", "teacher/CourseScheduleContent_teacher.fxml");
        addNavBtn("成绩录入", "teacher/ScoreInputContent.fxml");
        addNavBtn("公告查看", "teacher/TeacherAnnouncement.fxml");
        addNavBtn("个人中心", "teacher/PersonalCenterContent.fxml");

        logoutBtn.setOnAction(e -> handleLogout());

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

        loadContent("teacher/TeacherHomePage.fxml");
        setActiveButton(homeBtn);
    }

    private Button addNavBtn(String text, String fxmlPath) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> { setActiveButton(btn); loadContent(fxmlPath); });
        navContainer.getChildren().add(btn);
        return btn;
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) activeButton.getStyleClass().remove("sidebar-btn-active");
        activeButton = button;
        button.getStyleClass().add("sidebar-btn-active");
    }

    private void loadContent(String fxmlPath) {
        try {
            String path = "/com/example/information_management_system/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(path)));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            VBox.setVgrow(view, Priority.ALWAYS);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleLogout() {
        com.example.information_management_system.MainApplication.stopTokenRefreshTimer();
        UserSession.getInstance().clearSession();
        try {
            com.example.information_management_system.MainApplication.changeView("Login.fxml", "css/Login.css");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
