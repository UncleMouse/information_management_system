package com.example.information_management_system.controller.admin;

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
    @FXML private TableColumn<TermItem, String> colActions;

    @FXML private TextField newTermField;
    @FXML private Button btnAddTerm;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        setupTableColumns();
        termTable.setItems(termList);
        btnAddTerm.setOnAction(e -> handleAddTerm());
        loadTerms();
    }

    private void setupTableColumns() {
        colTerm.setCellValueFactory(new PropertyValueFactory<>("term"));
        colSelectionStatus.setCellValueFactory(new PropertyValueFactory<>("selectionStatus"));
        colCurrentStatus.setCellValueFactory(new PropertyValueFactory<>("currentStatus"));

        colSelectionStatus.setCellFactory(column -> new TableCell<TermItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("已开放".equals(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if ("已关闭".equals(item)) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });

        colCurrentStatus.setCellFactory(column -> new TableCell<TermItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("当前学期".equals(item)) {
                        setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<TermItem, String>() {
            private final Button toggleBtn = new Button();
            private final Button currentBtn = new Button();

            {
                toggleBtn.getStyleClass().add("action-btn-small");
                toggleBtn.setOnAction(e -> {
                    TermItem item = getTableView().getItems().get(getIndex());
                    handleToggleSelection(item);
                });
            }

            {
                currentBtn.getStyleClass().add("action-btn-small");
                currentBtn.setOnAction(e -> {
                    TermItem item = getTableView().getItems().get(getIndex());
                    handleSetCurrent(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    TermItem termItem = getTableView().getItems().get(getIndex());
                    HBox hbox = new HBox(5);
                    hbox.setStyle("-fx-alignment: CENTER;");

                    String toggleText = "已开放".equals(termItem.getSelectionStatus()) ? "关闭选课" : "开放选课";
                    toggleBtn.setText(toggleText);
                    currentBtn.setText("设为当前");

                    hbox.getChildren().addAll(toggleBtn, currentBtn);
                    setGraphic(hbox);
                }
            }
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
                        JsonArray arr = res.getAsJsonArray("data");
                        Platform.runLater(() -> {
                            termList.clear();
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject obj = arr.get(i).getAsJsonObject();
                                TermItem item = new TermItem();
                                item.setTerm(obj.has("term") ? obj.get("term").getAsString() : "");

                                boolean selectionOpen = obj.has("selectionOpen") && obj.get("selectionOpen").getAsBoolean();
                                item.setSelectionStatus(selectionOpen ? "已开放" : "已关闭");

                                boolean isCurrent = obj.has("isCurrent") && obj.get("isCurrent").getAsBoolean();
                                item.setCurrentStatus(isCurrent ? "当前学期" : "");

                                termList.add(item);
                            }
                            statusLabel.setText("共 " + termList.size() + " 个学期");
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("数据加载失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> statusLabel.setText("网络请求失败"));
            }
        });
    }

    private void handleAddTerm() {
        String term = newTermField.getText().trim();
        if (term.isEmpty()) {
            ShowMessage.showWarningMessage("提示", "请输入学期名称");
            return;
        }

        if (!term.matches("\\d{4}-\\d{4}-[12]")) {
            ShowMessage.showWarningMessage("提示", "格式应为: 2023-2024-1");
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
                            ShowMessage.showInfoMessage("成功", "学期添加成功");
                            newTermField.clear();
                            loadTerms();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "添加失败";
                            ShowMessage.showErrorMessage("错误", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败"));
            }
        });
    }

    private void handleToggleSelection(TermItem item) {
        String action = "已开放".equals(item.getSelectionStatus()) ? "关闭" : "开放";
        boolean confirmed = ShowMessage.showConfirmMessage("确认", "确定要" + action + "选课吗？");
        if (!confirmed) return;

        boolean newStatus = !"已开放".equals(item.getSelectionStatus());
        Map<String, String> params = new HashMap<>();
        params.put("term", item.getTerm());
        params.put("open", String.valueOf(newStatus));
        params.put("current", "当前学期".equals(item.getCurrentStatus()) ? "true" : "false");

        NetworkUtils.postWithQueryParams("/term/editSelection", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", action + "选课成功");
                            loadTerms();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "操作失败";
                            ShowMessage.showErrorMessage("错误", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败"));
            }
        });
    }

    private void handleSetCurrent(TermItem item) {
        boolean confirmed = ShowMessage.showConfirmMessage("确认", "确定要设为当前学期吗？");
        if (!confirmed) return;

        Map<String, String> params = new HashMap<>();
        params.put("term", item.getTerm());
        params.put("open", "已开放".equals(item.getSelectionStatus()) ? "true" : "false");
        params.put("current", "true");

        NetworkUtils.postWithQueryParams("/term/editSelection", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "已设为当前学期");
                            loadTerms();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "操作失败";
                            ShowMessage.showErrorMessage("错误", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败"));
            }
        });
    }

    public static class TermItem {
        private String term;
        private String selectionStatus;
        private String currentStatus;

        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }
        public String getSelectionStatus() { return selectionStatus; }
        public void setSelectionStatus(String selectionStatus) { this.selectionStatus = selectionStatus; }
        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    }
}
