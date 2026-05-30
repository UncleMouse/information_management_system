package com.example.information_management_system.controller.admin;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PersonalCenterController {

    private final Gson gson = new Gson();

    @FXML private Label avatarLabel;
    @FXML private Label usernameLabel;
    @FXML private Label usernameLabel2;
    @FXML private Label identityLabel;
    @FXML private Label identityLabel2;
    @FXML private Label statusLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label sexLabel;
    @FXML private Button btnEditInfo;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordBtn;

    @FXML
    public void initialize() {
        fetchAndDisplay();
        btnEditInfo.setOnAction(e -> openEditDialog());
        if (changePasswordBtn != null) changePasswordBtn.setOnAction(e -> handleChangePassword());
    }

    /** 从后端拉取最新用户信息 */
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
                        if (data.has("phone") && !data.get("phone").isJsonNull())
                            session.setPhone(data.get("phone").getAsString());
                        if (data.has("email") && !data.get("email").isJsonNull())
                            session.setEmail(data.get("email").getAsString());
                        if (data.has("sex") && !data.get("sex").isJsonNull())
                            session.setSex(data.get("sex").getAsString());
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

        String name = session.getUsername() != null ? session.getUsername() : "管理员";
        String initial = name.isEmpty() ? "管" : name.substring(0, 1);

        setText(avatarLabel, initial);
        setText(usernameLabel, name);
        setText(usernameLabel2, name);

        String role = "系统管理员";
        Integer identity = session.getIdentity();
        if (identity != null) {
            role = identity == 0 ? "系统管理员" : identity == 1 ? "教师" : "学生";
        }
        setText(identityLabel, role);
        setText(identityLabel2, role);

        setText(phoneLabel, session.getPhone(), "未设置");
        setText(emailLabel, session.getEmail(), "未设置");
        setText(sexLabel, session.getSex(), "--");
    }

    private void setText(Label label, String value) {
        if (label != null && value != null) label.setText(value);
    }

    private void setText(Label label, String value, String defaultVal) {
        if (label == null) return;
        label.setText(value != null && !value.isEmpty() ? value : defaultVal);
    }

    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (StringUtil.isEmpty(oldPassword)) { ShowMessage.showWarningMessage("提示", "请输入旧密码"); return; }
        if (StringUtil.isEmpty(newPassword)) { ShowMessage.showWarningMessage("提示", "请输入新密码"); return; }
        if (!newPassword.equals(confirmPassword)) { ShowMessage.showWarningMessage("提示", "两次密码不一致"); return; }

        Map<String, String> pwdParams = new HashMap<>();
        pwdParams.put("oldPassword", oldPassword);
        pwdParams.put("newPassword", newPassword);
        NetworkUtils.postWithQueryParams("/user/updatePassword", pwdParams, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "密码已修改");
                            oldPasswordField.clear(); newPasswordField.clear(); confirmPasswordField.clear();
                        } else {
                            ShowMessage.showErrorMessage("错误", res.has("msg")?res.get("msg").getAsString():"修改失败");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "解析失败"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败"));
            }
        });
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
            EditPersonalInfoController ctrl = loader.getController();
            ctrl.setOnInfoUpdatedListener(this::fetchAndDisplay);
            stage.showAndWait();
            fetchAndDisplay();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开编辑窗口");
        }
    }
}
