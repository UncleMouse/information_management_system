package com.example.information_management_system.controller.student;

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

public class UserInfoController {

    private final Gson gson = new Gson();

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label nameLabel2;
    @FXML private Label roleTag;
    @FXML private Label sduidTag;
    @FXML private Label sduidLabel;
    @FXML private Label genderLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label collegeLabel;
    @FXML private Label majorLabel;
    @FXML private Label classNameLabel;
    @FXML private Label nationInfoLabel;
    @FXML private Label ethnicLabel;
    @FXML private Label politicsLabel;
    @FXML private Label admissionLabel;
    @FXML private Label graduationLabel;
    @FXML private Button btnEditInfo;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordBtn;

    @FXML
    public void initialize() {
        fetchAndDisplay();
        if (btnEditInfo != null) btnEditInfo.setOnAction(e -> openEditDialog());
        if (changePasswordBtn != null) changePasswordBtn.setOnAction(e -> handleChangePassword());
    }

    private void fetchAndDisplay() {
        NetworkUtils.post("/status/getStatusCard", "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonObject data = res.getAsJsonObject("data");
                        UserSession session = UserSession.getInstance();
                        // user 字段
                        if (data.has("user") && !data.get("user").isJsonNull()) {
                            JsonObject u = data.getAsJsonObject("user");
                            session.setSduid(jsonStr(u, "sduid"));
                            session.setUsername(jsonStr(u, "username"));
                            session.setSex(jsonStr(u, "sex"));
                            session.setCollege(jsonStr(u, "college"));
                            session.setMajor(jsonStr(u, "major"));
                            session.setNation(jsonStr(u, "nation"));
                            session.setEthnic(jsonStr(u, "ethnic"));
                            session.setPoliticsStatus(jsonStr(u, "politicsStatus"));
                            session.setPhone(jsonStr(u, "phone"));
                            session.setEmail(jsonStr(u, "email"));
                        }
                        // status 字段
                        String secNum = null;
                        String grade = null;
                        if (data.has("status") && !data.get("status").isJsonNull()) {
                            JsonObject st = data.getAsJsonObject("status");
                            if (st.has("admission") && !st.get("admission").isJsonNull())
                                session.setAdmission(String.valueOf(st.get("admission").getAsInt()));
                            if (st.has("graduation") && !st.get("graduation").isJsonNull())
                                session.setGraduation(String.valueOf(st.get("graduation").getAsInt()));
                            secNum = jsonStr(st, "section");
                            if (st.has("grade") && !st.get("grade").isJsonNull())
                                grade = String.valueOf(st.get("grade").getAsInt());
                        }
                        // 从 status.section + user.major 拼装班级名称
                        String major = null;
                        if (data.has("user") && !data.get("user").isJsonNull()) {
                            major = jsonStr(data.getAsJsonObject("user"), "major");
                        }
                        if (secNum != null && !secNum.isEmpty() && grade != null) {
                            String prefix = major != null && !major.isEmpty() ? major : "";
                            session.setSection(prefix + grade + "级" + secNum + "班");
                        }
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

    private String jsonStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private void displayUserInfo() {
        UserSession session = UserSession.getInstance();
        String name = nullToEmpty(session.getUsername());
        String sduid = nullToEmpty(session.getSduid());
        String initial = name.isEmpty() ? "学" : name.substring(0, 1);

        setText(avatarLabel, initial);
        setText(nameLabel, name);
        setText(nameLabel2, name);
        setText(sduidLabel, sduid);
        setText(sduidTag, "学号: " + (sduid.isEmpty() ? "-" : sduid));
        setText(genderLabel, nullToEmpty(session.getSex()));
        setText(phoneLabel, session.getPhone(), "未设置");
        setText(emailLabel, session.getEmail(), "未设置");
        collegeLabel.setText(nullToEmpty(session.getCollege()));
        majorLabel.setText(nullToEmpty(session.getMajor()));
        classNameLabel.setText(nullToEmpty(session.getSection()));
        nationInfoLabel.setText(nullToEmpty(session.getNation()));
        ethnicLabel.setText(nullToEmpty(session.getEthnic()));
        politicsLabel.setText(nullToEmpty(session.getPoliticsStatus()));
        String sid = nullToEmpty(session.getSduid());
        admissionLabel.setText(sid.length() >= 4 ? sid.substring(0, 4) : "-");
        if (sid.length() >= 4) {
            try { graduationLabel.setText(String.valueOf(Integer.parseInt(sid.substring(0,4)) + 4)); }
            catch (NumberFormatException e) { graduationLabel.setText("-"); }
        } else graduationLabel.setText("-");
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
            stage.showAndWait();
            fetchAndDisplay();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开编辑窗口");
        }
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

    private String nullToEmpty(String value) { return value == null ? "" : value; }

    private void setText(Label label, String value) {
        if (label != null && value != null) label.setText(value);
    }

    private void setText(Label label, String value, String defaultVal) {
        if (label == null) return;
        label.setText(value != null && !value.isEmpty() ? value : defaultVal);
    }
}
