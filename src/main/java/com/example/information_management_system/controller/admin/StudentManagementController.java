package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.Student;
import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;

import com.example.information_management_system.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
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
        final double[] studentRatios = {0.5, 1.2, 1.0, 0.6, 1.5, 1.2, 0.8};
        final double studentTotalRatio = java.util.Arrays.stream(studentRatios).sum();
        studentTable.widthProperty().addListener((_obs, oldW, newW) -> {
            double w = newW.doubleValue() - 2;
            for (int i = 0; i < studentRatios.length && i < studentTable.getColumns().size(); i++)
                studentTable.getColumns().get(i).setPrefWidth(w * studentRatios[i] / studentTotalRatio);
        });
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
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSduid.setCellValueFactory(new PropertyValueFactory<>("sduid"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colMajor.setCellValueFactory(new PropertyValueFactory<>("major"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 状态列样式
        colStatus.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("在读".equals(item) || "正常".equals(item) || "ACTIVE".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if ("休学".equals(item) || "退学".equals(item) || "INACTIVE".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });
    }

    private void loadStudents() {
        statusLabel.setText("加载中…");
        NetworkUtils.get("/admin/student/list", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Type listType = new TypeToken<List<Student>>() {}.getType();
                        List<Student> list = gson.fromJson(arr, listType);
                        Platform.runLater(() -> {
                            studentList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> {
                            statusLabel.setText("数据加载失败");
                            ShowMessage.showErrorMessage("错误", "数据加载失败");
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("数据解析失败，请稍后重试");
                        ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试");
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("网络请求失败，请检查连接");
                    ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接");
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
        NetworkUtils.get("/admin/searchSdu", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Type listType = new TypeToken<List<Student>>() {}.getType();
                        List<Student> list = gson.fromJson(arr, listType);
                        Platform.runLater(() -> {
                            studentList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> statusLabel.setText("数据加载失败"));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("数据加载失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("数据加载失败");
                    ShowMessage.showErrorMessage("错误", "数据加载失败: " + e.getMessage());
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
            NetworkUtils.post("/admin/deleteUser", params, "{}", new NetworkUtils.Callback<String>() {
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
}
