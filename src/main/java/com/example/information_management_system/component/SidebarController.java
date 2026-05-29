package com.example.information_management_system.component;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.ShowMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SidebarController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private VBox navContainer;

    private Button activeButton;
    private Runnable onLogout;
    private NavHandler onNavigate;

    @FunctionalInterface
    public interface NavHandler {
        void navigate(String fxmlPath);
    }

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username == null || username.isEmpty()) username = "用户";
        userNameLabel.setText(username);
    }

    public void setRole(String role) { userRoleLabel.setText(role); }
    public void setLogoutHandler(Runnable handler) { this.onLogout = handler; }
    public void setNavHandler(NavHandler handler) { this.onNavigate = handler; }

    public Button addNavButton(String text, String fxmlPath) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            if (onNavigate != null) onNavigate.navigate(fxmlPath);
            setActiveButton(btn);
        });
        navContainer.getChildren().add(btn);
        return btn;
    }

    public void setActiveFirst() {
        if (!navContainer.getChildren().isEmpty()
                && navContainer.getChildren().get(0) instanceof Button first) {
            setActiveButton(first);
            first.fire(); // 自动加载首页
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) activeButton.getStyleClass().remove("sidebar-btn-active");
        if (button != null) {
            button.getStyleClass().add("sidebar-btn-active");
            activeButton = button;
        }
    }

    @FXML
    private void handleLogout() {
        boolean confirmed = ShowMessage.showConfirmMessage("确认", "确定要退出登录吗？");
        if (confirmed) {
            UserSession.getInstance().clearSession();
            if (onLogout != null) {
                onLogout.run();
            } else {
                try {
                    com.example.information_management_system.MainApplication.stopTokenRefreshTimer();
                    com.example.information_management_system.MainApplication.changeView("Login.fxml", "css/Login.css");
                } catch (Exception e) {
                    ShowMessage.showErrorMessage("错误", "页面加载失败，请重启应用");
                }
            }
        }
    }
}
