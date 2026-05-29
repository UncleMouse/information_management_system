package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.ShowMessage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PersonalCenterController {

    @FXML private Label usernameLabel;
    @FXML private Label sduidLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label sexLabel;
    @FXML private Label collegeLabel;
    @FXML private Label majorLabel;
    @FXML private Label nationLabel;
    @FXML private Label ethnicLabel;
    @FXML private Label politicsStatusLabel;
    @FXML private Label admissionLabel;
    @FXML private Label graduationLabel;
    @FXML private Label identityLabel;

    @FXML private Button btnEditInfo;
    @FXML private Button btnChangePassword;

    @FXML
    public void initialize() {
        loadUserInfo();

        btnEditInfo.setOnAction(e -> openEditDialog());
        btnChangePassword.setOnAction(e -> handleChangePassword());
    }

    private void loadUserInfo() {
        UserSession session = UserSession.getInstance();
        setOrDefault(usernameLabel, session.getUsername(), "管理员");
        setOrDefault(sduidLabel, session.getSduid(), "--");
        setOrDefault(phoneLabel, session.getPhone(), "未设置");
        setOrDefault(emailLabel, session.getEmail(), "未设置");
        setOrDefault(sexLabel, session.getSex(), "--");
        setOrDefault(collegeLabel, session.getCollege(), "--");
        setOrDefault(majorLabel, session.getMajor(), "--");
        setOrDefault(nationLabel, session.getNation(), "--");
        setOrDefault(ethnicLabel, session.getEthnic(), "--");
        setOrDefault(politicsStatusLabel, session.getPoliticsStatus(), "--");
        setOrDefault(admissionLabel, session.getAdmission(), "--");
        setOrDefault(graduationLabel, session.getGraduation(), "--");

        Integer identity = session.getIdentity();
        if (identity != null) {
            switch (identity) {
                case 0 -> identityLabel.setText("系统管理员");
                case 1 -> identityLabel.setText("教师");
                case 2 -> identityLabel.setText("学生");
                default -> identityLabel.setText("未知");
            }
        } else {
            identityLabel.setText("--");
        }
    }

    private void setOrDefault(Label label, String value, String defaultValue) {
        if (value != null && !value.isEmpty()) {
            label.setText(value);
        } else {
            label.setText(defaultValue);
        }
    }

    private void openEditDialog() {
        try {
            String path = "/com/example/information_management_system/admin/EditPersonalInfo.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("编辑个人信息");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            EditPersonalInfoController controller = loader.getController();
            controller.setOnInfoUpdatedListener(this::loadUserInfo);

            stage.showAndWait();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开编辑窗口: " + e.getMessage());
        }
    }

    private void handleChangePassword() {
        ShowMessage.showInfoMessage("提示", "请联系系统管理员重置密码，或通过邮箱/手机验证码自助修改。");
    }
}
