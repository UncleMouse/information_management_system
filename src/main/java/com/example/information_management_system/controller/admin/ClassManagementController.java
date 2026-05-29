package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.Section;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class ClassManagementController {

    private final Gson gson = new Gson();
    private final ObservableList<Section> classList = FXCollections.observableArrayList();

    @FXML private TableView<Section> classTable;
    @FXML private TableColumn<Section, Integer> colId;
    @FXML private TableColumn<Section, String> colClassName;
    @FXML private TableColumn<Section, String> colMajor;
    @FXML private TableColumn<Section, String> colGrade;
    @FXML private TableColumn<Section, Integer> colNumber;

    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Button btnAddClass;
    @FXML private Button btnEditClass;
    @FXML private Button btnDeleteClass;
    @FXML private Button btnViewStudents;
    @FXML private Button btnRefresh;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        classTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double[] classRatios = {0.5, 2.0, 1.8, 1.0, 0.8};
        final double classTotalRatio = java.util.Arrays.stream(classRatios).sum();
        classTable.widthProperty().addListener((_obs, oldW, newW) -> {
            double w = newW.doubleValue() - 2;
            for (int i = 0; i < classRatios.length && i < classTable.getColumns().size(); i++)
                classTable.getColumns().get(i).setPrefWidth(w * classRatios[i] / classTotalRatio);
        });
        setupTableColumns();
        classTable.setItems(classList);

        btnSearch.setOnAction(e -> searchClasses());
        btnAddClass.setOnAction(e -> openAddClassDialog());
        btnEditClass.setOnAction(e -> handleEditClass());
        btnDeleteClass.setOnAction(e -> handleDeleteClass());
        btnViewStudents.setOnAction(e -> handleViewStudents());
        btnRefresh.setOnAction(e -> loadClasses());

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) searchClasses();
        });

        classTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            btnEditClass.setDisable(!hasSelection);
            btnDeleteClass.setDisable(!hasSelection);
            btnViewStudents.setDisable(!hasSelection);
        });
        btnEditClass.setDisable(true);
        btnDeleteClass.setDisable(true);
        btnViewStudents.setDisable(true);

        loadClasses();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colMajor.setCellValueFactory(new PropertyValueFactory<>("major"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
    }

    private void loadClasses() {
        statusLabel.setText("加载中…");
        NetworkUtils.get("/section/getSectionList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Type listType = new TypeToken<List<Section>>() {}.getType();
                        List<Section> list = gson.fromJson(arr, listType);
                        Platform.runLater(() -> {
                            classList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> statusLabel.setText("数据加载失败"));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("数据解析失败，请稍后重试"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("网络请求失败，请检查连接");
                    ShowMessage.showErrorMessage("错误", "数据加载失败: " + e.getMessage());
                });
            }
        });
    }

    private void searchClasses() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadClasses();
            return;
        }
        statusLabel.setText("加载中…");
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        NetworkUtils.get("/section/search", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Type listType = new TypeToken<List<Section>>() {}.getType();
                        List<Section> list = gson.fromJson(arr, listType);
                        Platform.runLater(() -> {
                            classList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("数据加载失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> statusLabel.setText("数据加载失败"));
            }
        });
    }

    private void openAddClassDialog() {
        try {
            String path = "/com/example/information_management_system/admin/AddNewClass.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("添加班级");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
            loadClasses();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开添加班级窗口: " + e.getMessage());
        }
    }

    private void handleEditClass() {
        Section selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一个班级");
            return;
        }
        try {
            String path = "/com/example/information_management_system/admin/AddNewClass.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("编辑班级");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            AddNewClassController controller = loader.getController();
            controller.setEditMode(selected);

            stage.showAndWait();
            loadClasses();
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开编辑窗口: " + e.getMessage());
        }
    }

    private void handleDeleteClass() {
        Section selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一个班级");
            return;
        }
        boolean confirmed = ShowMessage.showConfirmMessage("确认",
                "确定要删除班级 " + selected.getClassName() + " 吗？该操作不可撤销。");
        if (confirmed) {
        statusLabel.setText("加载中…");
            Map<String, String> params = new HashMap<>();
            params.put("id", String.valueOf(selected.getId()));
            NetworkUtils.post("/section/deleteSection", params, "{}", new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            Platform.runLater(() -> {
                                ShowMessage.showInfoMessage("成功", "已成功删除");
                                loadClasses();
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

    private void handleViewStudents() {
        Section selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一个班级");
            return;
        }
        ShowMessage.showInfoMessage("提示",
                "班级 " + selected.getClassName() + " 共有 " + selected.getNumber() + " 名学生。\n"
                        + "详细名单功能即将上线");
    }
}
