package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class AdminBaseViewController {

    @FXML private BorderPane rootPane;
    @FXML private VBox contentArea;

    @FXML private Label userDisplayName;
    @FXML private Label userRoleLabel;

    @FXML private Button btnHome;
    @FXML private Button btnStudentMgmt;
    @FXML private Button btnTeacherMgmt;
    @FXML private Button btnCourseMgmt;
    @FXML private Button btnClassMgmt;
    @FXML private Button btnNoticeMgmt;
    @FXML private Button btnPersonalCenter;

    private Button activeButton;

    @FXML
    public void initialize() {
        loadUserInfo();
        setActiveButton(btnHome);
        loadContent("AdminHomePage.fxml");

        btnHome.setOnAction(e -> { setActiveButton(btnHome); loadContent("AdminHomePage.fxml"); });
        btnStudentMgmt.setOnAction(e -> { setActiveButton(btnStudentMgmt); loadContent("StudentManagement.fxml"); });
        btnTeacherMgmt.setOnAction(e -> { setActiveButton(btnTeacherMgmt); loadContent("TeacherManagement.fxml"); });
        btnCourseMgmt.setOnAction(e -> { setActiveButton(btnCourseMgmt); loadContent("CourseManagement.fxml"); });
        btnClassMgmt.setOnAction(e -> { setActiveButton(btnClassMgmt); loadContent("ClassManagement.fxml"); });
        btnNoticeMgmt.setOnAction(e -> { setActiveButton(btnNoticeMgmt); loadContent("AddNewAnnouncement.fxml"); });
        btnPersonalCenter.setOnAction(e -> { setActiveButton(btnPersonalCenter); loadContent("PersonalCenter.fxml"); });
    }

    private void loadUserInfo() {
        UserSession session = UserSession.getInstance();
        String username = session.getUsername();
        if (username != null && !username.isEmpty()) {
            userDisplayName.setText(username);
        } else {
            userDisplayName.setText("管理员");
        }
        userRoleLabel.setText("系统管理员");
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-btn-active");
        }
        activeButton = button;
        activeButton.getStyleClass().add("sidebar-btn-active");
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

    private void loadContent(String fxmlName) {
        try {
            String path = "/com/example/information_management_system/admin/" + fxmlName;
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(path)));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            Label errorLabel = new Label("页面加载失败: " + fxmlName);
            errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-padding: 40;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }
}
