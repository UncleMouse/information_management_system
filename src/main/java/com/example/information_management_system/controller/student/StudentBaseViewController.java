package com.example.information_management_system.controller.student;

import com.example.information_management_system.MainApplication;
import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.ShowMessage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StudentBaseViewController {

    @FXML private Label studentNameLabel;
    @FXML private VBox contentArea;
    @FXML private Button homeBtn;
    @FXML private Button scheduleBtn;
    @FXML private Button courseSelectionBtn;
    @FXML private Button scoreSearchBtn;
    @FXML private Button personalBtn;

    private final Map<String, String> btnFxmlMap = new HashMap<>();
    private static final String FXML_BASE = "student/";
    private String currentActiveBtn = null;

    @FXML
    public void initialize() {
        studentNameLabel.setText(UserSession.getInstance().getUsername());

        btnFxmlMap.put("homeBtn", "StudentHomeContent.fxml");
        btnFxmlMap.put("scheduleBtn", "CourseScheduleContent.fxml");
        btnFxmlMap.put("courseSelectionBtn", "CourseSelectionContent.fxml");
        btnFxmlMap.put("scoreSearchBtn", "ScoreSearchContent.fxml");
        btnFxmlMap.put("personalBtn", "UserInfo.fxml");

        homeBtn.setOnAction(e -> switchContent("homeBtn"));
        scheduleBtn.setOnAction(e -> switchContent("scheduleBtn"));
        courseSelectionBtn.setOnAction(e -> switchContent("courseSelectionBtn"));
        scoreSearchBtn.setOnAction(e -> switchContent("scoreSearchBtn"));
        personalBtn.setOnAction(e -> switchContent("personalBtn"));

        switchContent("homeBtn");
    }

    private void switchContent(String btnName) {
        if (btnName.equals(currentActiveBtn)) return;
        currentActiveBtn = btnName;

        resetAllButtonStyles();
        highlightButton(btnName);

        String fxmlFile = btnFxmlMap.get(btnName);
        if (fxmlFile == null) return;

        try {
            String path = "/com/example/information_management_system/" + FXML_BASE + fxmlFile;
            FXMLLoader loader = new FXMLLoader(
                    java.util.Objects.requireNonNull(getClass().getResource(path)));
            VBox content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            ShowMessage.showErrorMessage("加载失败", "无法加载页面: " + fxmlFile);
            e.printStackTrace();
        }
    }

    private void resetAllButtonStyles() {
        homeBtn.getStyleClass().remove("sidebar-btn-active");
        scheduleBtn.getStyleClass().remove("sidebar-btn-active");
        courseSelectionBtn.getStyleClass().remove("sidebar-btn-active");
        scoreSearchBtn.getStyleClass().remove("sidebar-btn-active");
        personalBtn.getStyleClass().remove("sidebar-btn-active");
    }

    private void highlightButton(String btnName) {
        switch (btnName) {
            case "homeBtn" -> homeBtn.getStyleClass().add("sidebar-btn-active");
            case "scheduleBtn" -> scheduleBtn.getStyleClass().add("sidebar-btn-active");
            case "courseSelectionBtn" -> courseSelectionBtn.getStyleClass().add("sidebar-btn-active");
            case "scoreSearchBtn" -> scoreSearchBtn.getStyleClass().add("sidebar-btn-active");
            case "personalBtn" -> personalBtn.getStyleClass().add("sidebar-btn-active");
        }
    }

    @FXML
    private void handleLogout() {
        boolean confirmed = ShowMessage.showConfirmMessage("退出登录", "确定要退出登录吗？");
        if (confirmed) {
            UserSession.getInstance().clearSession();
            try {
                MainApplication.changeView("Login.fxml", "css/Login.css");
            } catch (IOException e) {
                ShowMessage.showErrorMessage("错误", "无法加载登录页面");
            }
        }
    }
}
