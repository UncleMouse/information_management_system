package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class EditPersonalInfoController {

    private final Gson gson = new Gson();
    private Runnable onInfoUpdatedListener;

    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session.getPhone() != null) phoneField.setText(session.getPhone());
        if (session.getEmail() != null) emailField.setText(session.getEmail());

        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> closeDialog());
    }

    public void setOnInfoUpdatedListener(Runnable listener) {
        this.onInfoUpdatedListener = listener;
    }

    private void handleSubmit() {
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (StringUtil.isEmpty(phone) && StringUtil.isEmpty(email)) {
            ShowMessage.showWarningMessage("提示", "请填写需要修改的信息");
            return;
        }

        btnSubmit.setDisable(true);

        // 分别调用 updatePhone 和 updateEmail（后端无 /user/updateInfo）
        final int[] completed = {0};
        final int total = (StringUtil.isEmpty(phone) ? 0 : 1) + (StringUtil.isEmpty(email) ? 0 : 1);
        final boolean[] failed = {false};

        Runnable checkAllDone = () -> {
            completed[0]++;
            if (completed[0] >= total) {
                Platform.runLater(() -> {
                    if (!failed[0]) {
                        UserSession session = UserSession.getInstance();
                        if (!StringUtil.isEmpty(phone)) session.setPhone(phone);
                        if (!StringUtil.isEmpty(email)) session.setEmail(email);
                        ShowMessage.showInfoMessage("成功", "已成功更新");
                        if (onInfoUpdatedListener != null) onInfoUpdatedListener.run();
                        closeDialog();
                    } else {
                        ShowMessage.showErrorMessage("错误", "更新失败");
                        btnSubmit.setDisable(false);
                    }
                });
            }
        };

        if (!StringUtil.isEmpty(phone)) {
            Map<String, String> phoneParams = new HashMap<>();
            phoneParams.put("phone", phone);
            NetworkUtils.postWithQueryParams("/user/updatePhone", phoneParams, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            checkAllDone.run();
                        } else {
                            failed[0] = true;
                            checkAllDone.run();
                        }
                    } catch (Exception e) {
                        failed[0] = true;
                        checkAllDone.run();
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    failed[0] = true;
                    checkAllDone.run();
                }
            });
        }

        if (!StringUtil.isEmpty(email)) {
            Map<String, String> emailParams = new HashMap<>();
            emailParams.put("email", email);
            NetworkUtils.postWithQueryParams("/user/updateEmail", emailParams, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            checkAllDone.run();
                        } else {
                            failed[0] = true;
                            checkAllDone.run();
                        }
                    } catch (Exception e) {
                        failed[0] = true;
                        checkAllDone.run();
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    failed[0] = true;
                    checkAllDone.run();
                }
            });
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }
}
