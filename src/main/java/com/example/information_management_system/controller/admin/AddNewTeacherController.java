package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.TeacherInfo;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class AddNewTeacherController {

    private final Gson gson = new Gson();
    private TeacherInfo editingTeacher;

    @FXML private Label dialogTitle;
    @FXML private TextField sduidField;
    @FXML private TextField nameField;
    @FXML private TextField collegeField;
    @FXML private TextField contactField;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    @FXML
    public void initialize() {
        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> closeDialog());
    }

    public void setEditMode(TeacherInfo teacher) {
        this.editingTeacher = teacher;
        if (dialogTitle != null) dialogTitle.setText("编辑教师");
        if (sduidField != null) sduidField.setText(teacher.getSduid());
        if (nameField != null) nameField.setText(teacher.getName());
        if (collegeField != null) collegeField.setText(teacher.getCollege());
        if (contactField != null) contactField.setText(teacher.getContactInfo());
    }

    private void handleSubmit() {
        String sduid = sduidField.getText().trim();
        String name = nameField.getText().trim();
        String college = collegeField.getText().trim();
        String contact = contactField.getText().trim();

        if (StringUtil.isEmpty(sduid) || StringUtil.isEmpty(name)) {
            ShowMessage.showWarningMessage("提示", "工号和姓名不能为空");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("sduid", sduid);
        body.put("name", name);
        body.put("college", college);
        body.put("contactInfo", contact);

        if (editingTeacher != null) {
            body.put("id", editingTeacher.getId());
        }

        String json = gson.toJson(body);
        String endpoint = editingTeacher != null ? "/admin/updateUser" : "/admin/addUser";

        btnSubmit.setDisable(true);
        NetworkUtils.post(endpoint, json, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功",
                                    editingTeacher != null ? "已成功更新" : "已成功添加");
                            closeDialog();
                        });
                    } else {
                        Platform.runLater(() -> {
                            ShowMessage.showErrorMessage("错误",
                                    res.has("msg") ? res.get("msg").getAsString() : "操作失败");
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
