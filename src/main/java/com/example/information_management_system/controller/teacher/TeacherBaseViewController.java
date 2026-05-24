package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.ViewTransitionAnimation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

public class TeacherBaseViewController {

    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentArea;
    @FXML private Label teacherNameLabel;
    @FXML private Label welcomeLabel;

    @FXML private Button homeButton;
    @FXML private Button courseMgmtButton;
    @FXML private Button scheduleButton;
    @FXML private Button scoreInputButton;
    @FXML private Button personalCenterButton;

    private Button activeButton;

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username == null || username.isEmpty()) {
            username = "教师";
        }
        if (teacherNameLabel != null) {
            teacherNameLabel.setText(username);
        }
        if (welcomeLabel != null) {
            welcomeLabel.setText("欢迎您，" + username);
        }

        if (homeButton != null) {
            homeButton.setOnAction(e -> switchToView("teacher/TeacherHomePage.fxml", homeButton));
        }
        if (courseMgmtButton != null) {
            courseMgmtButton.setOnAction(e -> switchToView("teacher/CourseManagementContent.fxml", courseMgmtButton));
        }
        if (scheduleButton != null) {
            scheduleButton.setOnAction(e -> switchToView("teacher/CourseScheduleContent_teacher.fxml", scheduleButton));
        }
        if (scoreInputButton != null) {
            scoreInputButton.setOnAction(e -> switchToView("teacher/ScoreInputContent.fxml", scoreInputButton));
        }
        if (personalCenterButton != null) {
            personalCenterButton.setOnAction(e -> switchToView("teacher/PersonalCenterContent.fxml", personalCenterButton));
        }

        switchToView("teacher/TeacherHomePage.fxml", homeButton);
    }

    private void switchToView(String fxmlPath, Button button) {
        try {
            String path = "/com/example/information_management_system/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(path))
            );
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            ViewTransitionAnimation.slideInFromRight(view).play();
            setActiveButton(button);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-btn-active");
        }
        if (button != null) {
            button.getStyleClass().add("sidebar-btn-active");
            activeButton = button;
        }
    }

    @FXML
    private void handleLogout() {
        com.example.information_management_system.MainApplication.stopTokenRefreshTimer();
        UserSession.getInstance().clearSession();
        try {
            com.example.information_management_system.MainApplication.changeView("Login.fxml", "css/Login.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
