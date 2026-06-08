package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.model.Student;
import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class AddNewStudentController {

    private final Gson gson = new Gson();
    private Student editingStudent;
    private Runnable onStudentAddedListener;
    private final Map<String, Integer> sectionIdMap = new HashMap<>(); // 班级名 → sectionId
    private final Map<Integer, String> sectionNameMap = new HashMap<>(); // sectionId → 班级名

    @FXML private Label dialogTitle;
    @FXML private TextField sduidField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> majorCombo;
    @FXML private ComboBox<String> gradeCombo;
    @FXML private ComboBox<String> classCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> collegeCombo;
    @FXML private ComboBox<String> nationCombo;
    @FXML private ComboBox<String> ethnicCombo;
    @FXML private ComboBox<String> politicsCombo;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    private final Map<Object, Label> fieldErrors = new HashMap<>();

    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("男","女"); genderCombo.getSelectionModel().selectFirst();
        majorCombo.getItems().addAll("软件工程","数字媒体技术","大数据","AI"); majorCombo.getSelectionModel().selectFirst();
        gradeCombo.getItems().addAll("2021","2022","2023","2024","2025","2026"); gradeCombo.getSelectionModel().select("2025");
        statusCombo.getItems().addAll("在读","休学","降转","退学"); statusCombo.getSelectionModel().selectFirst();
        collegeCombo.getItems().addAll("软件学院","计算机科学与技术学院","网络空间安全学院","人工智能学院","数学学院","物理学院");
        collegeCombo.getSelectionModel().selectFirst();
        nationCombo.getItems().addAll("中国","美国","英国","日本","韩国","法国","德国","俄罗斯","加拿大","澳大利亚");
        nationCombo.getSelectionModel().select("中国");
        ethnicCombo.getItems().addAll("汉族","蒙古族","回族","藏族","维吾尔族","苗族","彝族","壮族","布依族","朝鲜族","满族","侗族","瑶族","白族","土家族","哈尼族");
        ethnicCombo.getSelectionModel().select("汉族");
        politicsCombo.getItems().addAll("群众","共青团员","中共预备党员","中共党员","民主党派");
        politicsCombo.getSelectionModel().selectFirst();
        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> closeDialog());
        loadSections();
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

    /** 从后端实时加载班级列表 */
    private void loadSections() {
        Map<String, String> p = new HashMap<>();
        p.put("page", "1");
        p.put("size", "200");
        NetworkUtils.get("/section/getSectionListAll", p, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Platform.runLater(() -> {
                            classCombo.getItems().clear();
                            sectionIdMap.clear();
                            sectionNameMap.clear();
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject obj = arr.get(i).getAsJsonObject();
                                int id = JsonUtil.safeGetInt(obj, "id");
                                String major = JsonUtil.safeGetString(obj, "major");
                                String number = JsonUtil.safeGetString(obj, "number");
                                String name = major + number + "班";
                                sectionIdMap.put(name, id);
                                sectionNameMap.put(id, name);
                                Data.getInstance().getSectionNameMap().put(number, name);
                                classCombo.getItems().add(name);
                            }
                            if (editingStudent != null && editingStudent.getClassName() != null && !editingStudent.getClassName().isEmpty()) {
                                String clsNum = editingStudent.getClassName();  // API 返回 sec.number，如 "2025-1"
                                // 在班级下拉选项中模糊匹配编号
                                for (String item : classCombo.getItems()) {
                                    if (item.contains(clsNum)) { classCombo.setValue(item); break; }
                                }
                            }
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
        });
    }

    public void setEditMode(Student student) {
        this.editingStudent = student;
        if (dialogTitle != null) dialogTitle.setText("编辑学生");
        if (sduidField != null) sduidField.setText(student.getSduid());
        if (nameField != null) nameField.setText(student.getName());
        if (genderCombo != null) genderCombo.setValue(student.getGender());
        if (majorCombo != null) majorCombo.setValue(student.getMajor());
        if (gradeCombo != null) gradeCombo.setValue(student.getGrade());
        if (statusCombo != null && student.getStatus() != null) statusCombo.setValue(student.getStatus());

        // 从后端获取完整用户信息以填充学院/国籍/民族/政治面貌
        Map<String, String> q = new HashMap<>();
        q.put("userId", String.valueOf(student.getId()));
        NetworkUtils.get("/admin/getUserInfo", q, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonObject data = res.getAsJsonObject("data");
                        JsonObject user = data.getAsJsonObject("user");
                        Platform.runLater(() -> {
                            if (user.has("college") && !user.get("college").isJsonNull())
                                collegeCombo.setValue(user.get("college").getAsString());
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

    public void setOnStudentAddedListener(Runnable listener) { this.onStudentAddedListener = listener; }

    private void handleSubmit() {
        String sduid = sduidField.getText().trim();
        String name = nameField.getText().trim();
        String gender = genderCombo.getValue();
        String major = majorCombo.getValue();
        String grade = gradeCombo.getValue();
        String className = classCombo.getValue();

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
        params.put("major", major != null ? major : "软件工程");
        // 仅在新增时设置初始密码，编辑时不修改密码
        if (editingStudent == null) params.put("password", "123456");
        if (grade != null && !grade.isEmpty()) params.put("grade", grade);
        params.put("permission", "2");
        params.put("college", collegeCombo.getValue() != null ? collegeCombo.getValue() : "软件学院");
        params.put("ethnic", ethnicCombo.getValue() != null ? ethnicCombo.getValue() : "汉族");
        params.put("nation", nationCombo.getValue() != null ? nationCombo.getValue() : "中国");
        params.put("PoliticsStatus", politicsCombo.getValue() != null ? politicsCombo.getValue() : "群众");
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        params.put("email", email.isEmpty() ? sduid + "@mail.sdu.edu.cn" : email);
        params.put("phone", phone);
        if (statusCombo.getValue() != null) params.put("status", mapStatus(statusCombo.getValue()));
        if (className != null && !className.isEmpty()) {
            Integer sid = sectionIdMap.get(className);
            if (sid != null && sid > 0) params.put("sectionId", String.valueOf(sid));
        }

        if (editingStudent != null) params.put("id", String.valueOf(editingStudent.getId()));

        String endpoint = editingStudent != null ? "/admin/updateUser" : "/admin/addUser";
        // 新增学生时，若选了班级，需要在 addUser 后再次调用 updateUser 写入班级
        final Integer sidForNew = (editingStudent == null && className != null) ? sectionIdMap.get(className) : null;
        btnSubmit.setDisable(true);

        NetworkUtils.postWithQueryParams(endpoint, params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        if (sidForNew != null && sidForNew > 0) {
                            // 搜索新用户获取 ID，然后调用 updateUser 写入班级
                            Map<String, String> q = new HashMap<>();
                            q.put("keyword", sduid);
                            q.put("permission", "2");
                            NetworkUtils.get("/admin/searchSdu", q, new NetworkUtils.Callback<String>() {
                                @Override public void onSuccess(String r2) {
                                    try {
                                        JsonArray arr = JsonUtil.extractArray(gson.fromJson(r2, JsonObject.class), "data");
                                        if (arr.size() > 0) {
                                            int uid = JsonUtil.safeGetInt(arr.get(0).getAsJsonObject(), "id");
                                            if (uid > 0) {
                                                // 复用 addUser 的参数，补上 id 和 sectionId（移除密码避免明文覆盖 Bcrypt 密文）
                                                Map<String, String> up = new HashMap<>(params);
                                                up.remove("password");
                                                up.put("id", String.valueOf(uid));
                                                up.put("sectionId", String.valueOf(sidForNew));
                                                NetworkUtils.postWithQueryParams("/admin/updateUser", up, new NetworkUtils.Callback<String>() {
                                                    @Override public void onSuccess(String r3) { closeAndNotify(); }
                                                    @Override public void onFailure(Exception e) { closeAndNotify(); }
                                                });
                                                return;
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                    closeAndNotify();
                                }
                                @Override public void onFailure(Exception e) { closeAndNotify(); }
                            });
                        } else {
                            closeAndNotify();
                        }
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

    private void closeAndNotify() {
        Platform.runLater(() -> {
            ShowMessage.showInfoMessage("成功", editingStudent != null ? "已更新" : "已添加");
            if (onStudentAddedListener != null) onStudentAddedListener.run();
            closeDialog();
        });
    }

    private String mapStatus(String s) { return switch(s){ case "休学"->"SUSPENDED"; case "降转"->"TRANSFERRED"; case "退学"->"DROPPED_OUT"; default->"STUDYING"; }; }
    private void closeDialog() { ((Stage) btnSubmit.getScene().getWindow()).close(); }
}
