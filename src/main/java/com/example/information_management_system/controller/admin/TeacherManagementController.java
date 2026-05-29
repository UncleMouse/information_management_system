package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.TeacherInfo;
import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class TeacherManagementController {

    private final Gson gson = new Gson();
    private final ObservableList<TeacherInfo> teacherList = FXCollections.observableArrayList();

    @FXML private TableView<TeacherInfo> teacherTable;
    @FXML private TableColumn<TeacherInfo, Integer> colId;
    @FXML private TableColumn<TeacherInfo, String> colSduid;
    @FXML private TableColumn<TeacherInfo, String> colName;
    @FXML private TableColumn<TeacherInfo, String> colCollege;
    @FXML private TableColumn<TeacherInfo, String> colContact;
    @FXML private TableColumn<TeacherInfo, String> colStatus;

    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Button btnAddTeacher;
    @FXML private Button btnEditTeacher;
    @FXML private Button btnDeleteTeacher;
    @FXML private Button btnRefresh;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        teacherTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        teacherTable.setItems(teacherList);

        btnSearch.setOnAction(e -> searchTeachers());
        btnAddTeacher.setOnAction(e -> openAddTeacherDialog());
        btnEditTeacher.setOnAction(e -> handleEditTeacher());
        btnDeleteTeacher.setOnAction(e -> handleDeleteTeacher());
        btnRefresh.setOnAction(e -> loadTeachers());

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                searchTeachers();
            }
        });

        teacherTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            btnEditTeacher.setDisable(!hasSelection);
            btnDeleteTeacher.setDisable(!hasSelection);
        });
        btnEditTeacher.setDisable(true);
        btnDeleteTeacher.setDisable(true);

        loadTeachers();
    }

    private void setupTableColumns() {
        colSduid.setCellValueFactory(new PropertyValueFactory<>("sduid"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCollege.setCellValueFactory(new PropertyValueFactory<>("college"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactInfo"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colId.setCellFactory(col -> new TableCell<TeacherInfo, Integer>() {
            @Override protected void updateItem(Integer item, boolean empty) { super.updateItem(item, empty); setText(empty?null:String.valueOf(getIndex()+1)); setStyle("-fx-alignment: CENTER;"); }
        });
        colSduid.setCellFactory(col -> new TableCell<TeacherInfo, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        colContact.setCellFactory(col -> new TableCell<TeacherInfo, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        colStatus.setCellFactory(column -> new TableCell<TeacherInfo, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if ("在职".equals(item) || "ACTIVE".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else {
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });
    }

    private void loadTeachers() {
        statusLabel.setText("加载中…");
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "100");

        NetworkUtils.get("/admin/getTeacherList", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<TeacherInfo> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            TeacherInfo t = new TeacherInfo();
                            t.setId(JsonUtil.safeGetInt(obj, "id"));
                            t.setSduid(JsonUtil.safeGetString(obj, "sduid"));
                            t.setName(JsonUtil.safeGetString(obj, "username"));
                            t.setCollege(JsonUtil.safeGetString(obj, "college"));
                            t.setContactInfo(JsonUtil.safeGetString(obj, "email"));
                            t.setStatus("在职");
                            list.add(t);
                        }
                        Platform.runLater(() -> {
                            teacherList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> {
                            statusLabel.setText("数据加载失败");
                            ShowMessage.showErrorMessage("错误", "数据加载失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误"));
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("数据解析失败");
                        ShowMessage.showErrorMessage("错误", "数据解析失败: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("网络请求失败");
                    ShowMessage.showErrorMessage("错误", "网络请求失败: " + e.getMessage());
                });
            }
        });
    }

    private void searchTeachers() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadTeachers();
            return;
        }
        statusLabel.setText("加载中…");
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("permission", "1");

        NetworkUtils.get("/admin/searchSdu", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<TeacherInfo> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            TeacherInfo t = new TeacherInfo();
                            t.setId(JsonUtil.safeGetInt(obj, "id"));
                            t.setSduid(JsonUtil.safeGetString(obj, "sduid"));
                            t.setName(JsonUtil.safeGetString(obj, "username"));
                            t.setCollege(JsonUtil.safeGetString(obj, "college"));
                            t.setContactInfo(JsonUtil.safeGetString(obj, "email"));
                            t.setStatus("在职");
                            list.add(t);
                        }
                        Platform.runLater(() -> {
                            teacherList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> {
                            statusLabel.setText("搜索失败");
                            ShowMessage.showErrorMessage("错误", "搜索失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误"));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("数据解析失败");
                        ShowMessage.showErrorMessage("错误", "数据解析失败: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("网络请求失败");
                    ShowMessage.showErrorMessage("错误", "网络请求失败: " + e.getMessage());
                });
            }
        });
    }

    private void openAddTeacherDialog() {
        try {
            String path = "/com/example/information_management_system/admin/AddNewTeacher.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("添加教师");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
            loadTeachers();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开添加教师窗口: " + e.getMessage());
        }
    }

    private void handleEditTeacher() {
        TeacherInfo selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一名教师");
            return;
        }
        try {
            String path = "/com/example/information_management_system/admin/AddNewTeacher.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("编辑教师");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            AddNewTeacherController controller = loader.getController();
            controller.setEditMode(selected);

            stage.showAndWait();
            loadTeachers();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开编辑窗口: " + e.getMessage());
        }
    }

    private void handleDeleteTeacher() {
        TeacherInfo selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一名教师");
            return;
        }
        boolean confirmed = ShowMessage.showConfirmMessage("确认",
                "确定要删除教师 " + selected.getName() + " (" + selected.getSduid() + ") 吗？");
        if (confirmed) {
            statusLabel.setText("加载中…");
            Map<String, String> params = new HashMap<>();
            params.put("userId", String.valueOf(selected.getId()));
            NetworkUtils.postWithQueryParams("/admin/deleteUser", params, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            Platform.runLater(() -> {
                                ShowMessage.showInfoMessage("成功", "已成功删除");
                                loadTeachers();
                            });
                        } else {
                            Platform.runLater(() -> ShowMessage.showErrorMessage("错误",
                                    res.has("msg") ? res.get("msg").getAsString() : "操作失败，请稍后重试"));
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "解析响应失败"));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("数据加载失败");
                        ShowMessage.showErrorMessage("错误", "操作失败，请稍后重试");
                    });
                }
            });
        }
    }
}
