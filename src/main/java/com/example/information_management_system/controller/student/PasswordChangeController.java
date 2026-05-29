package com.example.information_management_system.controller.student;

import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class PasswordChangeController {

    private final Gson gson = new Gson();

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button submitBtn;
    @FXML private Button cancelBtn;

    @FXML
    public void initialize() {
        submitBtn.setOnAction(e -> handleSubmit());
        cancelBtn.setOnAction(e -> handleCancel());
    }

    private void handleSubmit() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (StringUtil.isEmpty(oldPassword)) {
            ShowMessage.showWarningMessage("提示", "请输入旧密码");
            return;
        }
        if (StringUtil.isEmpty(newPassword)) {
            ShowMessage.showWarningMessage("提示", "请输入新密码");
            return;
        }
        if (StringUtil.isEmpty(confirmPassword)) {
            ShowMessage.showWarningMessage("提示", "请确认新密码");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            ShowMessage.showWarningMessage("提示", "两次输入的新密码不一致");
            return;
        }
        if (newPassword.equals(oldPassword)) {
            ShowMessage.showWarningMessage("提示", "新密码不能与旧密码相同");
            return;
        }
        if (newPassword.length() < 6) {
            ShowMessage.showWarningMessage("提示", "新密码长度不能少于6位");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("oldPassword", oldPassword);
        body.addProperty("newPassword", newPassword);

        NetworkUtils.post("/user/updatePassword", gson.toJson(body), new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "密码已修改成功");
                            oldPasswordField.clear();
                            newPasswordField.clear();
                            confirmPasswordField.clear();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "操作失败，请稍后重试";
                            ShowMessage.showErrorMessage("错误", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"));
            }
        });
    }

    private void handleCancel() {
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();

        if (submitBtn.getScene() != null && submitBtn.getScene().getWindow() != null) {
            submitBtn.getScene().getWindow().hide();
        }
    }
}
