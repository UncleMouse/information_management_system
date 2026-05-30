package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PersonalCenterContent {

    private final Gson gson = new Gson();

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label nameLabel2;
    @FXML private Label idLabel;
    @FXML private Label roleBadge;
    @FXML private Label collegeLabel;
    @FXML private Label sduidLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Button btnEditInfo;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;

    @FXML
    public void initialize() {
        fetchAndDisplay();

        if (btnEditInfo != null) btnEditInfo.setOnAction(e -> openEditDialog());
        if (changePasswordButton != null) changePasswordButton.setOnAction(e -> handleChangePassword());
    }

    private void fetchAndDisplay() {
        NetworkUtils.post("/user/getInfo", "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonObject data = res.getAsJsonObject("data");
                        UserSession session = UserSession.getInstance();
                        if (data.has("username") && !data.get("username").isJsonNull())
                            session.setUsername(data.get("username").getAsString());
                        if (data.has("sduid") && !data.get("sduid").isJsonNull())
                            session.setSduid(data.get("sduid").getAsString());
                        if (data.has("college") && !data.get("college").isJsonNull())
                            session.setCollege(data.get("college").getAsString());
                        if (data.has("phone") && !data.get("phone").isJsonNull())
                            session.setPhone(data.get("phone").getAsString());
                        if (data.has("email") && !data.get("email").isJsonNull())
                            session.setEmail(data.get("email").getAsString());
                    }
                } catch (Exception ignored) {}
                Platform.runLater(() -> displayUserInfo());
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> displayUserInfo());
            }
        });
    }

    private void displayUserInfo() {
        UserSession session = UserSession.getInstance();
        String name = session.getUsername() != null ? session.getUsername() : "-";
        String sid = session.getSduid() != null ? session.getSduid() : "-";
        if (nameLabel != null) nameLabel.setText(name);
        if (nameLabel2 != null) nameLabel2.setText(name);
        if (avatarLabel != null) avatarLabel.setText(name.isEmpty() || name.equals("-") ? "?" : name.substring(0, 1));
        if (idLabel != null) idLabel.setText("工号: " + sid);
        if (roleBadge != null) roleBadge.setText("教师");
        if (collegeLabel != null) collegeLabel.setText(session.getCollege() != null ? session.getCollege() : "-");
        if (sduidLabel != null) sduidLabel.setText(sid);
        if (phoneLabel != null) phoneLabel.setText(session.getPhone() != null && !session.getPhone().isEmpty() ? session.getPhone() : "未设置");
        if (emailLabel != null) emailLabel.setText(session.getEmail() != null && !session.getEmail().isEmpty() ? session.getEmail() : "未设置");
    }

    private void openEditDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/information_management_system/admin/EditPersonalInfo.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("编辑个人信息");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
            fetchAndDisplay();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开编辑窗口");
        }
    }

    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (StringUtil.isEmpty(oldPassword)) { ShowMessage.showWarningMessage("提示", "请输入原密码"); return; }
        if (StringUtil.isEmpty(newPassword)) { ShowMessage.showWarningMessage("提示", "请输入新密码"); return; }
        if (newPassword.length() < 6) { ShowMessage.showWarningMessage("提示", "新密码长度不能少于6位"); return; }
        if (!newPassword.equals(confirmPassword)) { ShowMessage.showWarningMessage("提示", "两次输入的新密码不一致"); return; }

        Map<String, String> pwdParams = new HashMap<>();
        pwdParams.put("oldPassword", oldPassword);
        pwdParams.put("newPassword", newPassword);

        NetworkUtils.postWithQueryParams("/user/updatePassword", pwdParams, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "密码已修改成功");
                            oldPasswordField.clear();
                            newPasswordField.clear();
                            confirmPasswordField.clear();
                        });
                    } else {
                        String msg = res.has("msg") ? res.get("msg").getAsString() : "修改失败";
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误", msg));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "响应处理失败"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"));
            }
        });
    }
}
