package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.Student;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StudentManagementController {

    private final Gson gson = new Gson();
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colSduid;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colGender;
    @FXML private TableColumn<Student, String> colMajor;
    @FXML private TableColumn<Student, String> colClassName;
    @FXML private TableColumn<Student, String> colStatus;

    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Button btnAddStudent;
    @FXML private Button btnBatchImport;
    @FXML private Button btnExportExcel;
    @FXML private Button btnEditStudent;
    @FXML private Button btnDeleteStudent;
    @FXML private Button btnRefresh;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        studentTable.setItems(studentList);

        btnSearch.setOnAction(e -> searchStudents());
        btnAddStudent.setOnAction(e -> openAddStudentDialog());
        btnBatchImport.setOnAction(e -> handleBatchImport());
        btnExportExcel.setOnAction(e -> handleExportExcel());
        btnEditStudent.setOnAction(e -> handleEditStudent());
        btnDeleteStudent.setOnAction(e -> handleDeleteStudent());
        btnRefresh.setOnAction(e -> loadStudents());

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                searchStudents();
            }
        });

        // 监听表格行选择
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            btnEditStudent.setDisable(!hasSelection);
            btnDeleteStudent.setDisable(!hasSelection);
        });
        btnEditStudent.setDisable(true);
        btnDeleteStudent.setDisable(true);

        loadStudents();
    }

    private void setupTableColumns() {
        colSduid.setCellValueFactory(new PropertyValueFactory<>("sduid"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colMajor.setCellValueFactory(new PropertyValueFactory<>("major"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 序号列
        colId.setCellFactory(col -> new TableCell<Student, Integer>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
                setStyle("-fx-alignment: CENTER;");
            }
        });
        // 居中对齐列
        colSduid.setCellFactory(col -> new TableCell<Student, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        colGender.setCellFactory(col -> new TableCell<Student, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        colClassName.setCellFactory(col -> new TableCell<Student, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        // 状态列样式 + 居中
        colStatus.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if ("在读".equals(item) || "正常".equals(item) || "ACTIVE".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else if ("休学".equals(item) || "退学".equals(item) || "INACTIVE".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else {
                    setStyle("-fx-text-fill: #64748b; -fx-alignment: CENTER;");
                }
            }
        });
    }

    private void loadStudents() {
        statusLabel.setText("加载中…");
        Map<String, String> params = new HashMap<>();
        params.put("pageNum", "1");
        params.put("pageSize", "100");

        NetworkUtils.get("/admin/student/list", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<Student> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Student s = new Student();
                            s.setId(JsonUtil.safeGetInt(obj, "id"));
                            s.setSduid(JsonUtil.safeGetString(obj, "sduid"));
                            s.setName(JsonUtil.safeGetString(obj, "username"));
                            s.setGender(JsonUtil.safeGetString(obj, "sex"));
                            s.setMajor(mapMajor(JsonUtil.safeGetString(obj, "major")));
                            s.setClassName(JsonUtil.safeGetString(obj, "section"));
                            s.setStatus(JsonUtil.safeGetString(obj, "status"));
                            s.setGrade(String.valueOf(JsonUtil.safeGetInt(obj, "grade")));
                            list.add(s);
                        }
                        list.sort((a, b) -> a.getSduid().compareTo(b.getSduid()));
                        Platform.runLater(() -> {
                            studentList.setAll(list);
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

    private void searchStudents() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadStudents();
            return;
        }
        statusLabel.setText("加载中…");
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("permission", "2");
        params.put("pageNum", "1");
        params.put("pageSize", "100");

        NetworkUtils.get("/admin/searchSdu", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<Student> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Student s = new Student();
                            s.setId(JsonUtil.safeGetInt(obj, "id"));
                            s.setSduid(JsonUtil.safeGetString(obj, "sduid"));
                            s.setName(JsonUtil.safeGetString(obj, "username"));
                            s.setGender(JsonUtil.safeGetString(obj, "sex"));
                            s.setMajor(mapMajor(JsonUtil.safeGetString(obj, "major")));
                            s.setClassName(JsonUtil.safeGetString(obj, "section"));
                            s.setStatus(JsonUtil.safeGetString(obj, "status"));
                            s.setGrade(String.valueOf(JsonUtil.safeGetInt(obj, "grade")));
                            list.add(s);
                        }
                        list.sort((a, b) -> a.getSduid().compareTo(b.getSduid()));
                        Platform.runLater(() -> {
                            studentList.setAll(list);
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

    private void openAddStudentDialog() {
        try {
            String path = "/com/example/information_management_system/admin/AddNewStudent.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("添加学生");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            AddNewStudentController controller = loader.getController();
            controller.setOnStudentAddedListener(() -> loadStudents());

            stage.showAndWait();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开添加学生窗口: " + e.getMessage());
        }
    }

    private void handleBatchImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择Excel文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx", "*.xls"));
        File file = fileChooser.showOpenDialog(studentTable.getScene().getWindow());
        if (file != null) {
            statusLabel.setText("加载中…");
            NetworkUtils.postMultipartFileAsync("/admin/upload", file)
                    .thenAccept(result -> Platform.runLater(() -> {
                        try {
                            JsonObject res = gson.fromJson(result, JsonObject.class);
                            if (res.has("code") && res.get("code").getAsInt() == 200) {
                                ShowMessage.showInfoMessage("成功", "已成功导入");
                                loadStudents();
                            } else {
                                ShowMessage.showErrorMessage("错误", res.has("msg") ? res.get("msg").getAsString() : "操作失败，请稍后重试");
                            }
                        } catch (Exception e) {
                            ShowMessage.showErrorMessage("错误", "解析响应失败");
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"));
                        return null;
                    });
        }
    }

    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存Excel文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("学生信息.xlsx");
        File file = fileChooser.showSaveDialog(studentTable.getScene().getWindow());
        if (file != null) {
            statusLabel.setText("加载中…");
            NetworkUtils.get("/admin/exportStudent", new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    Platform.runLater(() -> {
                        statusLabel.setText("已成功导出");
                        ShowMessage.showInfoMessage("成功", "已成功导出至: " + file.getName());
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("数据加载失败");
                        ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接");
                    });
                }
            });
        }
    }

    private void handleEditStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一名学生");
            return;
        }
        try {
            String path = "/com/example/information_management_system/admin/AddNewStudent.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("编辑学生");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            AddNewStudentController controller = loader.getController();
            controller.setEditMode(selected);
            controller.setOnStudentAddedListener(() -> loadStudents());

            stage.showAndWait();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开编辑窗口: " + e.getMessage());
        }
    }

    private void handleDeleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一名学生");
            return;
        }
        boolean confirmed = ShowMessage.showConfirmMessage("确认",
                "确定要删除学生 " + selected.getName() + " (" + selected.getSduid() + ") 吗？");
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
                                loadStudents();
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

    private String mapMajor(String v) {
        if (v == null) return "";
        switch (v) {
            case "MAJOR_0": case "0": return "软件工程";
            case "MAJOR_1": case "1": return "数字媒体技术";
            case "MAJOR_2": case "2": return "大数据";
            case "MAJOR_3": case "3": return "AI";
            default: return v; // 可能是后端直接返回的中文
        }
    }
}
