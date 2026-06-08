package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.TeacherInfo;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class AddNewTeacherController {

    private final Gson gson = new Gson();
    private TeacherInfo editingTeacher;

    @FXML private Label dialogTitle;
    @FXML private TextField sduidField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> collegeCombo;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> nationCombo;
    @FXML private ComboBox<String> ethnicCombo;
    @FXML private ComboBox<String> politicsCombo;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    private final Map<Object, Label> fieldErrors = new HashMap<>();

    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("男","女"); genderCombo.getSelectionModel().selectFirst();
        collegeCombo.getItems().addAll("软件学院","计算机科学与技术学院","数学学院","物理学院","外国语学院","集成电路学院","文学院","历史学院","法学院","医学院","生命科学学院");
        collegeCombo.getSelectionModel().selectFirst();
        nationCombo.getItems().addAll("中国","美国","英国","日本","韩国","法国","德国","俄罗斯","加拿大","澳大利亚");
        nationCombo.getSelectionModel().select("中国");
        ethnicCombo.getItems().addAll("汉族","蒙古族","回族","藏族","维吾尔族","苗族","彝族","壮族","布依族","朝鲜族","满族","侗族","瑶族","白族","土家族","哈尼族");
        ethnicCombo.getSelectionModel().select("汉族");
        politicsCombo.getItems().addAll("群众","共青团员","中共预备党员","中共党员","民主党派");
        politicsCombo.getSelectionModel().selectFirst();
        btnSubmit.setOnAction(e -> handleSubmit()); btnCancel.setOnAction(e -> closeDialog());
        addErr(sduidField); addErr(nameField);
    }

    private void addErr(Object f) {
        javafx.scene.Node n = (javafx.scene.Node) f;
        if (n == null || n.getParent() == null) return;
        Label e = new Label(); e.setStyle("-fx-text-fill:#ef4444;-fx-font-size:10px;-fx-padding:2 0 0 0;"); e.setVisible(false);
        if (n.getParent() instanceof javafx.scene.layout.VBox vb) vb.getChildren().add(e);
        fieldErrors.put(f, e);
    }
    private void setFieldErr(Object f, String m) {
        Label e = fieldErrors.get(f); if (e == null || f == null) return;
        if (m == null || m.isEmpty()) { e.setText(""); e.setVisible(false); if (f instanceof javafx.scene.control.TextInputControl tf) tf.setStyle(""); }
        else { e.setText("⚠ "+m); e.setVisible(true); if (f instanceof javafx.scene.control.TextInputControl tf) tf.setStyle("-fx-border-color:#ef4444;"); }
    }

    public void setEditMode(TeacherInfo teacher) {
        this.editingTeacher = teacher;
        if (dialogTitle != null) dialogTitle.setText("编辑教师");
        if (sduidField != null) sduidField.setText(teacher.getSduid());
        if (nameField != null) nameField.setText(teacher.getName());
        if (genderCombo != null) genderCombo.setValue(teacher.getSex() != null ? teacher.getSex() : "男");
        if (collegeCombo != null) collegeCombo.setValue(teacher.getCollege());

        // 从后端获取完整用户信息
        Map<String, String> q = new HashMap<>();
        q.put("userId", String.valueOf(teacher.getId()));
        NetworkUtils.get("/admin/getUserInfo", q, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonObject data = res.getAsJsonObject("data");
                        JsonObject user = data.getAsJsonObject("user");
                        Platform.runLater(() -> {
                            if (user.has("nation") && !user.get("nation").isJsonNull())
                                nationCombo.setValue(user.get("nation").getAsString());
                            if (user.has("ethnic") && !user.get("ethnic").isJsonNull())
                                ethnicCombo.setValue(user.get("ethnic").getAsString());
                            if (user.has("politicsStatus") && !user.get("politicsStatus").isJsonNull())
                                politicsCombo.setValue(user.get("politicsStatus").getAsString());
                            if (user.has("phone") && !user.get("phone").isJsonNull())
                                phoneField.setText(user.get("phone").getAsString());
                            if (user.has("email") && !user.get("email").isJsonNull())
                                emailField.setText(user.get("email").getAsString());
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
        });
    }

    private void handleSubmit() {
        String sduid = sduidField.getText().trim();
        String name = nameField.getText().trim();
        String college = collegeCombo.getValue();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String gender = genderCombo.getValue();

        boolean err = false;
        setFieldErr(sduidField, null); setFieldErr(nameField, null);
        if (sduid.isEmpty()) { setFieldErr(sduidField, "必填"); err = true; }
        else if (!sduid.matches("\\d{5,12}")) { setFieldErr(sduidField, "须5-12位数字"); err = true; }
        if (name.isEmpty()) { setFieldErr(nameField, "必填"); err = true; }
        if (err) return;

        Map<String, String> params = new HashMap<>();
        params.put("SDUId", sduid);
        params.put("username", name);
        params.put("sex", gender != null ? gender : "男");
        params.put("college", college != null ? college : "软件学院");
        params.put("email", email.isEmpty() ? sduid + "@mail.sdu.edu.cn" : email);
        params.put("phone", phone);
        params.put("nation", nationCombo.getValue() != null ? nationCombo.getValue() : "中国");
        params.put("ethnic", ethnicCombo.getValue() != null ? ethnicCombo.getValue() : "汉族");
        params.put("PoliticsStatus", politicsCombo.getValue() != null ? politicsCombo.getValue() : "群众");
        params.put("permission", "1");

        if (editingTeacher != null) {
            params.put("id", String.valueOf(editingTeacher.getId()));
        } else {
            params.put("password", "123456");
            params.put("major", "MAJOR_0");
        }

        String endpoint = editingTeacher != null ? "/user/updateStudent" : "/admin/addUser";
        btnSubmit.setDisable(true);

        NetworkUtils.postWithQueryParams(endpoint, params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> { ShowMessage.showInfoMessage("成功", editingTeacher != null ? "已更新" : "已添加"); closeDialog(); });
                    } else {
                        Platform.runLater(() -> { ShowMessage.showErrorMessage("错误", res.has("msg")?res.get("msg").getAsString():"操作失败"); btnSubmit.setDisable(false); });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> { ShowMessage.showErrorMessage("错误", "解析失败"); btnSubmit.setDisable(false); });
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> { ShowMessage.showErrorMessage("错误", e.getMessage()); btnSubmit.setDisable(false); });
            }
        });
    }

    private void closeDialog() { ((Stage) btnSubmit.getScene().getWindow()).close(); }
}
