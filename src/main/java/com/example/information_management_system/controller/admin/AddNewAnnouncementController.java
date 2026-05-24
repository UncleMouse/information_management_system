package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class AddNewAnnouncementController {

    private final Gson gson = new Gson();

    // 通知列表
    @FXML private TableView<NoticeItem> noticeTable;
    @FXML private TableColumn<NoticeItem, String> colId;
    @FXML private TableColumn<NoticeItem, String> colTitle;
    @FXML private TableColumn<NoticeItem, String> colCreator;
    @FXML private TableColumn<NoticeItem, String> colPublishTime;
    @FXML private TableColumn<NoticeItem, String> colScope;
    @FXML private TableColumn<NoticeItem, String> colStatus;
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
        // 列表初始化
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creatorName"));
        colPublishTime.setCellValueFactory(new PropertyValueFactory<>("publishTime"));
        colScope.setCellValueFactory(new PropertyValueFactory<>("visibleScope"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        noticeTable.setItems(noticeList);

        btnSearch.setOnAction(e -> searchNotices());
        btnNewNotice.setOnAction(e -> showForm());
        btnRefresh.setOnAction(e -> loadNotices());
        btnBack.setOnAction(e -> showList());

        // 表单初始化
        visibilityCombo.setItems(FXCollections.observableArrayList(
                "全部可见", "学生可见", "教师可见", "管理员可见"));
        visibilityCombo.getSelectionModel().selectFirst();
        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> showList());

        // 初始加载通知列表
        loadNotices();
    }

    private void loadNotices() {
        NetworkUtils.get("/notice/getAdminNoticeList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray records = res.get("data").isJsonArray() ? res.getAsJsonArray("data")
                                : res.getAsJsonObject("data").getAsJsonArray("records");
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() ->
                        ShowMessage.showErrorMessage("加载失败", "获取通知列表失败"));
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
                Platform.runLater(() -> ShowMessage.showErrorMessage("搜索失败", e.getMessage()));
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

        if (StringUtil.isEmpty(title) || StringUtil.isEmpty(content)) {
            ShowMessage.showWarningMessage("提示", "标题和内容不能为空");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("content", content);
        body.put("publisherId", UserSession.getInstance().getId());
        body.put("publisherName", UserSession.getInstance().getUsername());
        if (visibility != null) body.put("visibility", visibility);

        String json = gson.toJson(body);
        btnSubmit.setDisable(true);

        NetworkUtils.post("/notice/set", json, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "通知已发布");
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
                        ShowMessage.showErrorMessage("错误", "解析响应失败");
                        btnSubmit.setDisable(false);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("错误", "请求失败: " + e.getMessage());
                    btnSubmit.setDisable(false);
                });
            }
        });
    }

    private String safeString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    public static class NoticeItem {
        private String id;
        private String title;
        private String creatorName;
        private String publishTime;
        private String visibleScope;
        private String status;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
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
