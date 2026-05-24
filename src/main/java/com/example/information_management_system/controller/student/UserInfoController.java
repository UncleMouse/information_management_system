package com.example.information_management_system.controller.student;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserInfoController {

    private final Gson gson = new Gson();

    @FXML private Label sduidLabel;
    @FXML private Label nameLabel;
    @FXML private Label genderLabel;
    @FXML private Label collegeLabel;
    @FXML private Label majorLabel;
    @FXML private Label classNameLabel;
    @FXML private Label nationLabel;
    @FXML private Label politicsLabel;
    @FXML private Label admissionLabel;
    @FXML private Label graduationLabel;

    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Button saveContactBtn;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordBtn;

    @FXML
    public void initialize() {
        loadUserInfo();

        saveContactBtn.setOnAction(e -> handleSaveContact());
        changePasswordBtn.setOnAction(e -> handleChangePassword());
    }

    private void loadUserInfo() {
        UserSession session = UserSession.getInstance();
        Platform.runLater(() -> {
            sduidLabel.setText(nullToEmpty(session.getSduid()));
            nameLabel.setText(nullToEmpty(session.getUsername()));
            genderLabel.setText(nullToEmpty(session.getSex()));
            collegeLabel.setText(nullToEmpty(session.getCollege()));
            majorLabel.setText(nullToEmpty(session.getMajor()));
            classNameLabel.setText(nullToEmpty(session.getSection()));
            nationLabel.setText(nullToEmpty(session.getNation()));
            politicsLabel.setText(nullToEmpty(session.getPoliticsStatus()));
            admissionLabel.setText(nullToEmpty(session.getAdmission()));
            graduationLabel.setText(nullToEmpty(session.getGraduation()));

            phoneField.setText(nullToEmpty(session.getPhone()));
            emailField.setText(nullToEmpty(session.getEmail()));
        });
    }

    private void handleSaveContact() {
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        boolean phoneChanged = !phone.equals(nullToEmpty(UserSession.getInstance().getPhone()));
        boolean emailChanged = !email.equals(nullToEmpty(UserSession.getInstance().getEmail()));

        if (!phoneChanged && !emailChanged) {
            ShowMessage.showInfoMessage("提示", "联系方式未做修改");
            return;
        }

        if (phoneChanged) {
            updatePhone(phone);
        }
        if (emailChanged) {
            updateEmail(email);
        }
    }

    private void updatePhone(String phone) {
        JsonObject body = new JsonObject();
        body.addProperty("phone", phone);

        NetworkUtils.post("/user/updatePhone", gson.toJson(body), new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            UserSession.getInstance().setPhone(phone);
                            ShowMessage.showInfoMessage("成功", "手机号更新成功");
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "更新失败";
                            ShowMessage.showErrorMessage("更新失败", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("更新失败", "响应解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("更新失败", e.getMessage()));
            }
        });
    }

    private void updateEmail(String email) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);

        NetworkUtils.post("/user/updateEmail", gson.toJson(body), new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            UserSession.getInstance().setEmail(email);
                            ShowMessage.showInfoMessage("成功", "邮箱更新成功");
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "更新失败";
                            ShowMessage.showErrorMessage("更新失败", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("更新失败", "响应解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("更新失败", e.getMessage()));
            }
        });
    }

    private void handleChangePassword() {
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
        if (oldPassword.equals(newPassword)) {
            ShowMessage.showWarningMessage("提示", "新密码不能与旧密码相同");
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
                            ShowMessage.showInfoMessage("成功", "密码修改成功");
                            oldPasswordField.clear();
                            newPasswordField.clear();
                            confirmPasswordField.clear();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "密码修改失败";
                            ShowMessage.showErrorMessage("修改失败", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("修改失败", "响应解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("修改失败", e.getMessage()));
            }
        });
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
