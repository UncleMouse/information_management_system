package com.example.information_management_system.controller.student;

import com.example.information_management_system.MainApplication;
import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.ShowMessage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class StudentBaseViewController {

    @FXML private Label studentNameLabel;
    @FXML private StackPane contentArea;
    @FXML private VBox navContainer;
    @FXML private Button logoutBtn;

    private Button activeButton;

    @FXML
    public void initialize() {
        studentNameLabel.setText(UserSession.getInstance().getUsername());

        Button homeBtn = addNavBtn("首  页", "student/StudentHomeContent.fxml");
        addNavBtn("课表查询", "student/CourseScheduleContent.fxml");
        addNavBtn("选课中心", "student/CourseSelectionContent.fxml");
        addNavBtn("成绩查询", "student/ScoreSearchContent.fxml");
        addNavBtn("公告查看", "student/StudentAnnouncement.fxml");
        addNavBtn("个人中心", "student/UserInfo.fxml");

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

        loadContent("student/StudentHomeContent.fxml");
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
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "页面加载失败，请重启应用");
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        boolean confirmed = ShowMessage.showConfirmMessage("确认", "确定要退出登录吗？");
        if (confirmed) {
            UserSession.getInstance().clearSession();
            try {
                MainApplication.changeView("Login.fxml", "css/Login.css");
            } catch (IOException e) {
                ShowMessage.showErrorMessage("错误", "页面加载失败，请重启应用");
            }
        }
    }
}
