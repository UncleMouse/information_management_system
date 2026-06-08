package com.example.information_management_system.controller.admin;

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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

public class TermManagementController {

    private final Gson gson = new Gson();
    private final ObservableList<TermItem> termList = FXCollections.observableArrayList();

    @FXML private TableView<TermItem> termTable;
    @FXML private TableColumn<TermItem, String> colTerm;
    @FXML private TableColumn<TermItem, String> colSelectionStatus;
    @FXML private TableColumn<TermItem, String> colCurrentStatus;
    @FXML private TableColumn<TermItem, Void> colActions;

    @FXML private Button btnAddTerm;
    @FXML private Button btnEditTerm;
    @FXML private Button btnDeleteTerm;
    @FXML private Button btnRefresh;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        termTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colTerm.setCellValueFactory(new PropertyValueFactory<>("term"));
        colSelectionStatus.setCellValueFactory(new PropertyValueFactory<>("selectionStatus"));
        colCurrentStatus.setCellValueFactory(new PropertyValueFactory<>("currentStatus"));

        // 列居中
        colTerm.setCellFactory(col -> new TableCell<TermItem, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });

        // 选课状态 彩色 + 居中
        colSelectionStatus.setCellFactory(col -> new TableCell<TermItem, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("已开放".equals(item) ? "-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER;"
                        : "-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        // 当前学期 彩色 + 居中
        colCurrentStatus.setCellFactory(col -> new TableCell<TermItem, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item.isEmpty() ? "—" : item);
                setStyle(item.isEmpty() ? "-fx-alignment: CENTER;" : "-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        // 操作列
        colActions.setCellFactory(col -> new TableCell<TermItem, Void>() {
            private final Button actionBtn = new Button();
            {
                String base = "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-min-width: 90;";
                actionBtn.setStyle("-fx-background-color: #4f6ef7; " + base);
                actionBtn.setOnAction(e -> { TermItem item = getTableView().getItems().get(getIndex()); handleTermAction(item); });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                TermItem it = getTableView().getItems().get(getIndex());
                boolean isCur = "当前学期".equals(it.getCurrentStatus());
                if (isCur) {
                    actionBtn.setText("取消当前");
                    actionBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-min-width: 90;");
                } else {
                    actionBtn.setText("设为当前");
                    actionBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-min-width: 90;");
                }
                HBox hbox = new HBox(actionBtn);
                hbox.setStyle("-fx-alignment: CENTER;");
                setGraphic(hbox);
            }
        });

        termTable.setItems(termList);
        btnAddTerm.setOnAction(e -> showTermDialog(null));
        btnEditTerm.setOnAction(e -> {
            TermItem sel = termTable.getSelectionModel().getSelectedItem();
            if (sel != null) showTermDialog(sel.getTerm());
        });
        btnDeleteTerm.setOnAction(e -> {
            TermItem sel = termTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            if (!ShowMessage.showConfirmMessage("确认", "确定要删除学期 " + sel.getTerm() + " 吗？")) return;
            Map<String, String> p = new HashMap<>();
            p.put("term", sel.getTerm());
            NetworkUtils.postWithQueryParams("/term/deleteTerm", p, new NetworkUtils.Callback<String>() {
                @Override public void onSuccess(String r) {
                    try { JsonObject res = gson.fromJson(r, JsonObject.class);
                        int code = res.has("code") ? res.get("code").getAsInt() : -1;
                        if (code == 200) { ShowMessage.showInfoMessage("成功", "已删除"); loadTerms(); }
                        else if (code == 409) { ShowMessage.showErrorMessage("错误", "该学期下有课程已安排，无法删除"); }
                        else ShowMessage.showErrorMessage("错误", res.has("msg")?res.get("msg").getAsString():"删除失败");
                    } catch (Exception ex) { ShowMessage.showErrorMessage("错误", "解析失败"); }
                }
                @Override public void onFailure(Exception ex) { ShowMessage.showErrorMessage("错误", ex.getMessage()); }
            });
        });
        btnRefresh.setOnAction(e -> loadTerms());
        termTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean has = newV != null;
            btnEditTerm.setDisable(!has);
            btnDeleteTerm.setDisable(!has);
        });
        loadTerms();
    }

    private void showTermDialog(String oldTerm) {
        TextInputDialog dialog = new TextInputDialog(oldTerm != null ? oldTerm : "");
        dialog.setTitle(oldTerm != null ? "编辑学期" : "添加学期");
        dialog.setHeaderText(oldTerm != null ? "修改学期名称" : "输入新学期名称");
        dialog.setContentText("格式: 2025-2026-1");
        dialog.showAndWait().ifPresent(input -> {
            String term = input.trim();
            if (term.isEmpty()) return;
            if (!term.matches("\\d{4}-\\d{4}-[12]")) {
                ShowMessage.showWarningMessage("提示", "格式: 2025-2026-1");
                return;
            }
            Map<String, String> params = new HashMap<>();
            params.put("term", term);
            NetworkUtils.postWithQueryParams("/term/addTerm", params, new NetworkUtils.Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JsonObject res = gson.fromJson(result, JsonObject.class);
                        Platform.runLater(() -> {
                            if (res.has("code") && res.get("code").getAsInt() == 200) {
                                ShowMessage.showInfoMessage("成功", (oldTerm != null ? "编辑" : "添加") + "成功");
                                loadTerms();
                            } else {
                                ShowMessage.showErrorMessage("错误", res.has("msg") ? res.get("msg").getAsString() : "操作失败");
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "解析失败"));
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", e.getMessage()));
                }
            });
        });
    }

    private void loadTerms() {
        statusLabel.setText("加载中...");
        NetworkUtils.get("/term/getTermList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Platform.runLater(() -> {
                            termList.clear();
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject obj = arr.get(i).getAsJsonObject();
                                TermItem item = new TermItem();
                                item.setTerm(JsonUtil.safeGetString(obj, "term"));
                                boolean selOpen = obj.has("open") && !obj.get("open").isJsonNull() && obj.get("open").getAsBoolean();
                                item.setSelectionStatus(selOpen ? "已开放" : "已关闭");
                                boolean isCur = obj.has("current") && !obj.get("current").isJsonNull() && obj.get("current").getAsBoolean();
                                item.setCurrentStatus(isCur ? "当前学期" : "");
                                termList.add(item);
                            }
                            statusLabel.setText("共 " + termList.size() + " 个学期");
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
                Platform.runLater(() -> statusLabel.setText("网络失败"));
            }
        });
    }

    private void handleTermAction(TermItem item) {
        boolean isCurrent = "当前学期".equals(item.getCurrentStatus());
        String action = isCurrent ? "取消当前学期" : "设为当前学期";
        if (!ShowMessage.showConfirmMessage("确认", action + "？")) return;

        Map<String, String> params = new HashMap<>();
        params.put("term", item.getTerm());
        params.put("current", isCurrent ? "false" : "true");
        // 设为当前时自动开放选课，取消当前时保持原有开放状态
        params.put("open", isCurrent ? "false" : "true");

        NetworkUtils.postWithQueryParams("/term/editSelection", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", action + "成功");
                            loadTerms();
                        } else {
                            ShowMessage.showErrorMessage("错误", res.has("msg")?res.get("msg").getAsString():"操作失败");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "解析失败"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", e.getMessage()));
            }
        });
    }

    public static class TermItem {
        private String term, selectionStatus, currentStatus;
        public String getTerm() { return term; }
        public void setTerm(String t) { term = t; }
        public String getSelectionStatus() { return selectionStatus; }
        public void setSelectionStatus(String s) { selectionStatus = s; }
        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String s) { currentStatus = s; }
    }
}
