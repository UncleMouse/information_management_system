package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.UserSession;
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
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class AddNewAnnouncementController {

    private static final Map<String, String> VISIBILITY_MAP = new HashMap<>();
    static {
        VISIBILITY_MAP.put("全部可见", "1");
        VISIBILITY_MAP.put("学生可见", "2");
    }

    private final Gson gson = new Gson();

    // 通知列表
    @FXML private TableView<NoticeItem> noticeTable;
    @FXML private TableColumn<NoticeItem, String> colId;
    @FXML private TableColumn<NoticeItem, String> colTitle;
    @FXML private TableColumn<NoticeItem, String> colCreator;
    @FXML private TableColumn<NoticeItem, String> colPublishTime;
    @FXML private TableColumn<NoticeItem, String> colScope;
    @FXML private TableColumn<NoticeItem, String> colStatus;
    @FXML private TableColumn<NoticeItem, Void> colAction;
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Button btnNewNotice;
    @FXML private Button btnRefresh;

    // 新建表单
    @FXML private VBox noticeListSection;
    @FXML private VBox formSection;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<String> visibilityCombo;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;
    @FXML private Button btnBack;

    private ObservableList<NoticeItem> noticeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        noticeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // 数据绑定
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creatorName"));
        colPublishTime.setCellValueFactory(new PropertyValueFactory<>("publishTime"));
        colScope.setCellValueFactory(new PropertyValueFactory<>("visibleScope"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // 样式 + 格式化
        colId.setCellFactory(col -> new TableCell<NoticeItem, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
                setStyle("-fx-alignment: CENTER;");
            }
        });
        colTitle.setCellFactory(col -> new TableCell<NoticeItem, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item); setStyle("-fx-alignment: CENTER-LEFT;"); }
        });
        colCreator.setCellFactory(col -> new TableCell<NoticeItem, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : item); setStyle("-fx-alignment: CENTER;"); }
        });
        colPublishTime.setCellFactory(col -> new TableCell<NoticeItem, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : formatTime(item)); setStyle("-fx-alignment: CENTER;"); }
        });
        colScope.setCellFactory(col -> new TableCell<NoticeItem, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText(mapScope(item)); setStyle("-fx-alignment: CENTER;"); }
            }
        });
        colStatus.setCellFactory(col -> new TableCell<NoticeItem, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText(mapStatus(item)); setStyle("-fx-alignment: CENTER;"); }
            }
        });
        // 操作列
        colAction.setCellFactory(col -> new TableCell<NoticeItem, Void>() {
            private final Button viewBtn = new Button("查看");
            {
                viewBtn.setStyle("-fx-background-color: #4f6ef7; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 18; -fx-background-radius: 6; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> {
                    NoticeItem item = getTableView().getItems().get(getIndex());
                    viewNotice(item);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox hbox = new HBox(viewBtn);
                hbox.setStyle("-fx-alignment: CENTER;");
                setGraphic(hbox);
            }
        });
        noticeTable.setItems(noticeList);

        btnSearch.setOnAction(e -> searchNotices());
        btnNewNotice.setOnAction(e -> showForm());
        btnRefresh.setOnAction(e -> loadNotices());
        btnBack.setOnAction(e -> showList());

        // 表单初始化
        visibilityCombo.setItems(FXCollections.observableArrayList(
                "全部可见", "学生可见"));
        visibilityCombo.getSelectionModel().selectFirst();
        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> showList());

        // 初始加载通知列表
        loadNotices();
    }

    private void loadNotices() {
        Map<String, String> params = new HashMap<>();
        params.put("Status", "1");

        NetworkUtils.get("/notice/getAdminNoticeList", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray records = null;
                        if (res.get("data").isJsonArray()) {
                            records = res.getAsJsonArray("data");
                        } else if (res.getAsJsonObject("data").has("records")) {
                            records = res.getAsJsonObject("data").getAsJsonArray("records");
                        }
                        ObservableList<NoticeItem> items = FXCollections.observableArrayList();
                        if (records != null) {
                            for (int i = 0; i < records.size(); i++) {
                                JsonObject obj = records.get(i).getAsJsonObject();
                                NoticeItem item = new NoticeItem();
                                item.setId(safeString(obj, "id"));
                                item.setTitle(safeString(obj, "title"));
                                item.setContent(safeString(obj, "content"));
                                item.setCreatorName(safeString(obj, "creatorName"));
                                item.setPublishTime(safeString(obj, "publishTime"));
                                item.setVisibleScope(safeString(obj, "visibleScope"));
                                item.setStatus(safeString(obj, "status"));
                                items.add(item);
                            }
                        }
                        Platform.runLater(() -> noticeList.setAll(items));
                    } else {
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "加载失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败: " + e.getMessage()));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败: " + e.getMessage()));
            }
        });
    }

    private void searchNotices() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadNotices();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("Status", "1");
        NetworkUtils.get("/notice/getAdminNoticeList", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    JsonArray records = null;
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonObject data = res.getAsJsonObject("data");
                        if (data.has("records")) records = data.getAsJsonArray("records");
                        else if (data.has("data")) records = data.getAsJsonArray("data");
                    }
                    ObservableList<NoticeItem> items = FXCollections.observableArrayList();
                    if (records != null) {
                        for (int i = 0; i < records.size(); i++) {
                            JsonObject obj = records.get(i).getAsJsonObject();
                            NoticeItem item = new NoticeItem();
                            item.setId(safeString(obj, "id"));
                            item.setTitle(safeString(obj, "title"));
                            item.setCreatorName(safeString(obj, "creatorName"));
                            item.setPublishTime(safeString(obj, "publishTime"));
                            item.setVisibleScope(safeString(obj, "visibleScope"));
                            item.setStatus(safeString(obj, "status"));
                            items.add(item);
                        }
                    }
                    Platform.runLater(() -> noticeList.setAll(items));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据加载失败: " + e.getMessage()));
            }
        });
    }

    private void showForm() {
        titleField.clear();
        contentArea.clear();
        visibilityCombo.getSelectionModel().selectFirst();
        noticeListSection.setVisible(false);
        noticeListSection.setManaged(false);
        formSection.setVisible(true);
        formSection.setManaged(true);
    }

    private void showList() {
        formSection.setVisible(false);
        formSection.setManaged(false);
        noticeListSection.setVisible(true);
        noticeListSection.setManaged(true);
        loadNotices();
    }

    private void handleSubmit() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String visibility = visibilityCombo.getValue();

        if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
            ShowMessage.showWarningMessage("提示", "标题和内容不能为空");
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("content", content);
        params.put("creatorName", UserSession.getInstance().getUsername());
        if (visibility != null) {
            params.put("visibleScope", VISIBILITY_MAP.getOrDefault(visibility, "1"));
        }

        btnSubmit.setDisable(true);

        NetworkUtils.postWithQueryParams("/notice/set", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "已成功发布");
                            showList();
                        });
                    } else {
                        Platform.runLater(() -> {
                            ShowMessage.showErrorMessage("错误",
                                    res.has("msg") ? res.get("msg").getAsString() : "发布失败");
                            btnSubmit.setDisable(false);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        ShowMessage.showErrorMessage("错误", "数据解析失败: " + e.getMessage());
                        btnSubmit.setDisable(false);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("错误", "网络请求失败: " + e.getMessage());
                    btnSubmit.setDisable(false);
                });
            }
        });
    }

    private String safeString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    /** 将 ISO 8601 时间格式转为可读格式: 2026-05-28T23:24:01 → 2026-05-28 23:24:01 */
    private String formatTime(String time) {
        if (time == null || time.isEmpty()) return "";
        return time.replace("T", " ");
    }

    private String mapScope(String val) {
        if (val == null) return "";
        switch (val) {
            case "2": return "学生可见";
            case "1": return "全部可见";
            default: return val;
        }
    }

    private String mapStatus(String val) {
        if (val == null) return "";
        switch (val) {
            case "1": return "已发布";
            case "0": return "已关闭";
            default: return val;
        }
    }

    private void viewNotice(NoticeItem item) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("查看公告");
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));

        VBox box = new VBox(12);
        box.setPrefWidth(500);
        box.setStyle("-fx-padding: 16;");

        Label titleLbl = new Label(item.getTitle());
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-wrap-text: true;");

        String info = "发布者: " + (item.getCreatorName() != null ? item.getCreatorName() : "未知")
                + "    时间: " + formatTime(item.getPublishTime());
        Label infoLbl = new Label(info);
        infoLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Separator sep = new Separator();

        TextArea contentArea = new TextArea(item.getContent() != null ? item.getContent() : "");
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(15);
        contentArea.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");

        box.getChildren().addAll(titleLbl, infoLbl, sep, contentArea);
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    public static class NoticeItem {
        private String id;
        private String title;
        private String content;
        private String creatorName;
        private String publishTime;
        private String visibleScope;
        private String status;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getCreatorName() { return creatorName; }
        public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
        public String getPublishTime() { return publishTime; }
        public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
        public String getVisibleScope() { return visibleScope; }
        public void setVisibleScope(String visibleScope) { this.visibleScope = visibleScope; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
