package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.Section;
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
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colMajor.setCellValueFactory(new PropertyValueFactory<>("major"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        classTable.setItems(classList);

        colId.setCellFactory(col -> new TableCell<Section, Integer>() {
            @Override protected void updateItem(Integer item, boolean empty) { super.updateItem(item, empty); setText(empty?null:String.valueOf(getIndex()+1)); setStyle("-fx-alignment: CENTER;"); }
        });
        colGrade.setCellFactory(col -> new TableCell<Section, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        colNumber.setCellFactory(col -> new TableCell<Section, Integer>() {
            @Override protected void updateItem(Integer item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:String.valueOf(item)); setStyle("-fx-alignment: CENTER;"); }
        });

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

    private void loadClasses() {
        statusLabel.setText("加载中...");
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "100");

        NetworkUtils.get("/section/getSectionListAll", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<Section> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Section s = new Section();
                            s.setId(JsonUtil.safeGetInt(obj, "id"));
                            String clsNum = JsonUtil.safeGetString(obj, "number");
                            s.setClassName(clsNum.endsWith("班") ? clsNum : clsNum + "班");
                            s.setMajor(JsonUtil.safeGetString(obj, "major"));
                            String grd = JsonUtil.safeGetString(obj, "grade");
                            s.setGrade(grd.length() >= 4 ? grd.substring(0, 4) : grd);
                            s.setNumber(JsonUtil.safeGetInt(obj, "studentCount"));
                            list.add(s);
                        }
                        Platform.runLater(() -> {
                            classList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> statusLabel.setText("数据加载失败"));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("数据解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> statusLabel.setText("网络请求失败"));
            }
        });
    }

    private void searchClasses() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadClasses(); return; }
        statusLabel.setText("搜索中...");
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("page", "1");
        params.put("size", "100");

        NetworkUtils.get("/section/getSectionListAll", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<Section> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Section s = new Section();
                            s.setId(JsonUtil.safeGetInt(obj, "id"));
                            String clsNum = JsonUtil.safeGetString(obj, "number");
                            s.setClassName(clsNum.endsWith("班") ? clsNum : clsNum + "班");
                            s.setMajor(JsonUtil.safeGetString(obj, "major"));
                            String grd = JsonUtil.safeGetString(obj, "grade");
                            s.setGrade(grd.length() >= 4 ? grd.substring(0, 4) : grd);
                            s.setNumber(JsonUtil.safeGetInt(obj, "studentCount"));
                            list.add(s);
                        }
                        Platform.runLater(() -> {
                            classList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("数据解析失败"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> statusLabel.setText("网络请求失败"));
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
            ShowMessage.showErrorMessage("错误", "无法打开添加班级窗口");
        }
    }

    private void handleEditClass() {
        Section selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) { ShowMessage.showWarningMessage("提示", "请先选择一个班级"); return; }
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
            ShowMessage.showErrorMessage("错误", "无法打开编辑窗口");
        }
    }

    private void handleDeleteClass() {
        Section selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) { ShowMessage.showWarningMessage("提示", "请先选择一个班级"); return; }
        boolean confirmed = ShowMessage.showConfirmMessage("确认", "确定要删除 " + selected.getClassName() + " 吗？");
        if (!confirmed) return;
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(selected.getId()));
        NetworkUtils.postWithQueryParams("/section/deleteSection", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> { ShowMessage.showInfoMessage("成功", "已删除"); loadClasses(); });
                    } else {
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "操作失败"));
                    }
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

    private void handleViewStudents() {
        Section selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) { ShowMessage.showWarningMessage("提示", "请先选择一个班级"); return; }
        statusLabel.setText("加载中...");
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(selected.getId()));
        NetworkUtils.get("/section/getSectionMember", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Platform.runLater(() -> {
                            statusLabel.setText("共 " + arr.size() + " 名学生");
                            showStudentsDialog(selected, arr);
                        });
                    } else {
                        Platform.runLater(() -> statusLabel.setText("加载失败"));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("解析失败"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> statusLabel.setText("网络请求失败"));
            }
        });
    }

    private void showStudentsDialog(Section section, JsonArray students) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("班级学生名单");
        dialog.setHeaderText(section.getClassName() + " - 学生名单 (" + students.size() + "人)");
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));

        TableView<String[]> tv = new TableView<>();
        TableColumn<String[], String> c1 = new TableColumn<>("学号");
        c1.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue()[0]));
        TableColumn<String[], String> c2 = new TableColumn<>("姓名");
        c2.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue()[1]));
        tv.getColumns().addAll(c1, c2);

        ObservableList<String[]> data = FXCollections.observableArrayList();
        for (int i = 0; i < students.size(); i++) {
            JsonObject obj = students.get(i).getAsJsonObject();
            data.add(new String[]{JsonUtil.safeGetString(obj, "sduid"), JsonUtil.safeGetString(obj, "username")});
        }
        tv.setItems(data);
        tv.setPrefHeight(400);
        dialog.getDialogPane().setContent(tv);
        dialog.showAndWait();
    }

}
