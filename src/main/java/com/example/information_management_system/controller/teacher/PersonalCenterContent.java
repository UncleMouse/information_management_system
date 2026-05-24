package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PersonalCenterContent {

    private final Gson gson = new Gson();

    @FXML private Label nameLabel;
    @FXML private Label userIdLabel;
    @FXML private Label collegeLabel;
    @FXML private Label sduidLabel;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button saveInfoButton;
    @FXML private Button changePasswordButton;

    @FXML
    public void initialize() {
        loadUserInfo();

        if (saveInfoButton != null) {
            saveInfoButton.setOnAction(e -> handleSaveInfo());
        }
        if (changePasswordButton != null) {
            changePasswordButton.setOnAction(e -> handleChangePassword());
        }
    }

    private void loadUserInfo() {
        UserSession session = UserSession.getInstance();
        if (nameLabel != null) {
            nameLabel.setText(session.getUsername() != null ? session.getUsername() : "-");
        }
        if (userIdLabel != null) {
            userIdLabel.setText(session.getSduid() != null ? session.getSduid() : "-");
        }
        if (collegeLabel != null) {
            collegeLabel.setText(session.getCollege() != null ? session.getCollege() : "-");
        }
        if (sduidLabel != null) {
            sduidLabel.setText(session.getSduid() != null ? session.getSduid() : "-");
        }
        if (phoneField != null) {
            phoneField.setText(session.getPhone() != null ? session.getPhone() : "");
        }
        if (emailField != null) {
            emailField.setText(session.getEmail() != null ? session.getEmail() : "");
        }
    }

    private void handleSaveInfo() {
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (StringUtil.isEmpty(phone) && StringUtil.isEmpty(email)) {
            ShowMessage.showWarningMessage("提示", "请至少填写一项联系信息");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("phone", phone);
        body.addProperty("email", email);

        NetworkUtils.put("/user/getInfo", gson.toJson(body), new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        UserSession.getInstance().setPhone(phone);
                        UserSession.getInstance().setEmail(email);
                        Platform.runLater(() ->
                                ShowMessage.showInfoMessage("成功", "个人信息更新成功！"));
                    } else {
                        String msg = res.has("msg") ? res.get("msg").getAsString() : "更新失败";
                        Platform.runLater(() -> ShowMessage.showErrorMessage("更新失败", msg));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "响应处理失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() ->
                        ShowMessage.showErrorMessage("更新失败", e.getMessage()));
            }
        });
    }

    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (StringUtil.isEmpty(oldPassword)) {
            ShowMessage.showWarningMessage("提示", "请输入原密码");
            return;
        }
        if (StringUtil.isEmpty(newPassword)) {
            ShowMessage.showWarningMessage("提示", "请输入新密码");
            return;
        }
        if (newPassword.length() < 6) {
            ShowMessage.showWarningMessage("提示", "新密码长度不能少于6位");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            ShowMessage.showWarningMessage("提示", "两次输入的新密码不一致");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("oldPassword", oldPassword);
        body.addProperty("newPassword", newPassword);

        NetworkUtils.put("/user/updatePassword", gson.toJson(body), new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "密码修改成功，下次登录时请使用新密码。");
                            oldPasswordField.clear();
                            newPasswordField.clear();
                            confirmPasswordField.clear();
                        });
                    } else {
                        String msg = res.has("msg") ? res.get("msg").getAsString() : "修改失败";
                        Platform.runLater(() -> ShowMessage.showErrorMessage("修改失败", msg));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "响应处理失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() ->
                        ShowMessage.showErrorMessage("修改失败", e.getMessage()));
            }
        });
    }
}
