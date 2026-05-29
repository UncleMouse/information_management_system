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
    @FXML private ComboBox<String> sexCombo;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    @FXML
    public void initialize() {
        sexCombo.getItems().addAll("男", "女");
        sexCombo.getSelectionModel().selectFirst();

        // 预填当前用户信息
        UserSession session = UserSession.getInstance();
        if (session.getPhone() != null) phoneField.setText(session.getPhone());
        if (session.getEmail() != null) emailField.setText(session.getEmail());
        if (session.getSex() != null) sexCombo.setValue(session.getSex());

        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> closeDialog());
    }

    public void setOnInfoUpdatedListener(Runnable listener) {
        this.onInfoUpdatedListener = listener;
    }

    private void handleSubmit() {
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String sex = sexCombo.getValue();

        if (StringUtil.isEmpty(phone) && StringUtil.isEmpty(email)) {
            ShowMessage.showWarningMessage("提示", "请填写需要修改的信息");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);
        body.put("email", email);
        body.put("sex", sex);

        String json = gson.toJson(body);

        btnSubmit.setDisable(true);
        NetworkUtils.post("/user/updateInfo", json, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            // 更新本地会话
                            UserSession session = UserSession.getInstance();
                            session.setPhone(phone);
                            session.setEmail(email);
                            session.setSex(sex);

                            ShowMessage.showInfoMessage("成功", "已成功更新");
                            if (onInfoUpdatedListener != null) {
                                onInfoUpdatedListener.run();
                            }
                            closeDialog();
                        });
                    } else {
                        Platform.runLater(() -> {
                            ShowMessage.showErrorMessage("错误",
                                    res.has("msg") ? res.get("msg").getAsString() : "更新失败");
                            btnSubmit.setDisable(false);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试");
                        btnSubmit.setDisable(false);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接");
                    btnSubmit.setDisable(false);
                });
            }
        });
    }

    private void closeDialog() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }
}
