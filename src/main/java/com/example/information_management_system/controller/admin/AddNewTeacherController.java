package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.TeacherInfo;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
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

        if (sduid.isEmpty() || name.isEmpty()) {
            ShowMessage.showWarningMessage("提示", "工号和姓名不能为空");
            return;
        }

        // 发送所有后端需要的字段（参考示例项目）
        Map<String, String> params = new HashMap<>();
        params.put("SDUId", sduid);
        params.put("username", name);
        params.put("college", college.isEmpty() ? "软件学院" : college);
        params.put("email", contact.isEmpty() ? sduid + "@sdu.edu.cn" : contact);
        params.put("phone", contact.isEmpty() ? "" : contact);
        params.put("password", "123456");
        params.put("permission", "1");  // 教师权限
        params.put("major", "0");        // 教师不需要专业，填默认值
        params.put("sex", "男");
        params.put("ethnic", "汉族");
        params.put("nation", "中国");
        params.put("PoliticsStatus", "群众");

        if (editingTeacher != null) {
            params.put("id", String.valueOf(editingTeacher.getId()));
        }

        String endpoint = editingTeacher != null ? "/admin/updateUser" : "/admin/addUser";

        btnSubmit.setDisable(true);
        NetworkUtils.postWithQueryParams(endpoint, params, new NetworkUtils.Callback<String>() {
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
                        ShowMessage.showErrorMessage("错误", "数据解析失败");
                        btnSubmit.setDisable(false);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("错误", "网络请求失败: " + e.getMessage());
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
