# 校园信息管理系统

基于 JavaFX 21 开发的校园信息管理桌面客户端，涵盖教务管理、教师教学、学生选课等核心功能，支持管理员、教师、学生三种角色。

## 技术栈

| 类别 | 技术 |
|---|---|
| UI 框架 | JavaFX 21 (FXML + CSS) |
| 编程语言 | Java 21 |
| 构建工具 | Maven |
| HTTP 通信 | HttpURLConnection |
| JSON 解析 | Gson 2.10 |
| Excel 处理 | Apache POI 5.4 |
| UI 增强 | ControlsFX 11.2, BootstrapFX 0.4, TilesFX 21 |

## 环境要求

- **JDK 21** 或更高版本
- **Maven 3.8+**
- 后端 API 服务需启动（Spring Boot 项目，默认端口 `22222`）

## 快速启动

```bash
# 开发模式运行
mvnw clean javafx:run

# IDE 中运行
# 主入口：com.example.information_management_system.Launcher
```

后端 API 地址在 `src/main/resources/application.properties` 中配置：

```properties
server.url=http://localhost:22222
```

## 项目架构

```
src/main/java/com/example/information_management_system/
│
├── Launcher.java                          # 程序入口
├── MainApplication.java                   # JavaFX Application 主类
│
├── controller/                            # 控制器层
│   ├── admin/                             # 管理员端（15个控制器）
│   │   ├── AdminBaseViewController        # 管理员主框架（侧边栏导航）
│   │   ├── AdminHomePageController        # 首页仪表盘
│   │   ├── StudentManagementController    # 学生管理
│   │   ├── TeacherManagementController    # 教师管理
│   │   ├── CourseManagementController     # 课程管理（审批、排课入口）
│   │   ├── ScheduleManagementController   # 排课管理
│   │   ├── ClassManagementController      # 班级管理
│   │   ├── TermManagementController       # 学期设置
│   │   ├── AddNewAnnouncementController   # 通知发布与列表
│   │   ├── AddNewStudentController        # 学生添加/编辑弹窗
│   │   ├── AddNewTeacherController        # 教师添加/编辑弹窗
│   │   ├── AddNewClassController          # 班级添加/编辑弹窗
│   │   ├── PersonalCenterController       # 管理员个人中心
│   │   └── EditPersonalInfoController     # 编辑个人信息
│   │
│   ├── teacher/                           # 教师端（10个控制器）
│   │   ├── TeacherBaseViewController      # 教师主框架
│   │   ├── TeacherHomePageController      # 首页教学概览
│   │   ├── CourseManagementContent        # 课程管理
│   │   ├── editCourseController           # 课程编辑
│   │   ├── ApplyNewCourseController       # 申请新课
│   │   ├── CourseScheduleManagementContent# 课表查看
│   │   ├── ScoreInputController           # 成绩录入
│   │   ├── StudentListViewController      # 学生名单
│   │   ├── TeacherAnnouncementController  # 公告查看
│   │   └── PersonalCenterContent          # 个人中心
│   │
│   ├── student/                           # 学生端（7个控制器）
│   │   ├── StudentBaseViewController      # 学生主框架
│   │   ├── HomeContentController          # 首页工作台
│   │   ├── CourseSelectionContentController# 选课中心
│   │   ├── CourseScheduleContentController# 课表查询
│   │   ├── ScoreSearchContentController   # 成绩查询
│   │   ├── StudentAnnouncementController  # 公告查看
│   │   └── UserInfoController             # 个人中心
│   │
│   └── LoginController.java               # 登录页
│
├── model/                                 # 数据模型
│   ├── Course.java                        # 课程
│   ├── Student.java                       # 学生
│   ├── TeacherInfo.java                   # 教师
│   ├── StudentInfo.java                   # 学生详情
│   ├── CourseRow.java                     # 课表行
│   ├── ScoreEntry.java                    # 成绩条目
│   ├── Section.java                       # 班级
│   ├── Term.java                          # 学期
│   └── ...
│
├── entity/                                # 全局状态
│   ├── UserSession.java                   # 用户会话（单例，存储 token/用户信息）
│   └── Data.java                          # 共享缓存（学期列表、教室列表等）
│
└── util/                                  # 工具类
    ├── NetworkUtils.java                  # HTTP 请求封装（GET/POST/文件上传）
    ├── ThemeManager.java                  # 暗色/亮色主题切换
    ├── ExportUtils.java                   # Excel 导出
    ├── JsonUtil.java                      # JSON 安全读取
    ├── ShowMessage.java                   # 对话框封装（信息/警告/错误/确认）
    ├── StringUtil.java                    # 字符串工具
    ├── MockData.java                      # 离线模拟数据
    ├── Refresh.java                       # Token 自动刷新
    ├── FieldValidator.java                # 表单验证
    └── ResUtil.java                       # 响应工具
```

## 功能详情

### 管理员端

| 模块 | 功能 | 关键交互 |
|---|---|---|
| **首页** | 学生/教师/课程/班级总数统计、在读/在职人数、待审核课程数、最近通知列表 | 通知可滚动查看、点击弹窗详情 |
| **学生管理** | 学生列表分页、搜索（学号/姓名/专业）、添加、编辑、删除、Excel 批量导入、Excel 导出 | 双击行编辑、选中后编辑/删除按钮启用 |
| **教师管理** | 教师列表、搜索（工号/姓名/学院）、添加、编辑、删除 | 编辑走 `/user/updateStudent` 避免更新 Status 表 |
| **课程管理** | 课程列表、按学期/状态筛选、审批通过/拒绝（需填写拒绝理由）、排课入口 | 课序号列显示、状态颜色标记 |
| **排课管理** | 加载课程列表、设置每门课的星期/节次/教室、单行保存、全部保存、自动排课 | 排课数据自动预填当前值 |
| **班级管理** | 班级列表、搜索、添加/编辑/删除、查看班级学生名单 | 客户端本地过滤 |
| **学期设置** | 添加/删除学期、设置选课开放状态 | |
| **通知管理** | 发布/编辑/关闭公告、可见范围选择（全体可见/教师可见） | 列表中显示可见范围和状态 |

### 教师端

| 模块 | 功能 | 关键交互 |
|---|---|---|
| **首页** | 本学期课程数、学生总数、待录入成绩、近期课程列表、最新公告 | 公告可滚动展示全部 |
| **课程管理** | 课程列表、搜索、申请新课、编辑课程、查看选课学生 | 已通过课程不可编辑（后端限制） |
| **课表查看** | 按学期和教学周查看课表、5天×5节次表格 | 课程用彩色标签显示 |
| **成绩录入** | 下拉选课、加载学生名单、逐行录入平时/期末成绩、保存、发布、导出 Excel | 总成绩按比例自动计算 |
| **公告查看** | 搜索标题、查看公告详情 | 支持滚动 |
| **个人中心** | 查看和编辑个人信息 | |

### 学生端

| 模块 | 功能 | 关键交互 |
|---|---|---|
| **首页** | 已选课程数、已修学分、GPA、当前学期、课程卡片、最新公告 | 点击公告查看详情 |
| **课表查询** | 按学期和周次查看课表、5天×5节次表格 | 点击课程卡片查看课程详情 |
| **选课中心** | 可选课程/已选课程双标签、搜索课程、选课（含时间冲突检测提醒）、退课 | 时间列显示为"周一 1-2节"格式 |
| **成绩查询** | 按学期查看成绩、学分和绩点 | |
| **公告查看** | 搜索标题、查看公告详情 | 支持滚动 |
| **个人中心** | 查看学籍信息（学院/专业/班级/民族等）、修改密码 | 头像、姓名、学号展示 |

## 网络通信

`NetworkUtils` 封装了所有 HTTP 请求：

```java
// GET 请求（带参数）
Map<String, String> params = new HashMap<>();
params.put("pageNum", "1");
params.put("pageSize", "100");
NetworkUtils.get("/admin/student/list", params, new Callback<String>() {
    @Override public void onSuccess(String result) { /* 解析 JSON */ }
    @Override public void onFailure(Exception e) { /* 错误处理 */ }
});

// POST 请求（JSON body）
NetworkUtils.post("/class/update/" + id, jsonBody, callback);

// POST 请求（Query 参数）
NetworkUtils.postWithQueryParams("/notice/set", params, callback);
```

- 自动附加 `Authorization: Bearer <token>` 请求头
- 支持 `application/json` 和 `application/x-www-form-urlencoded` 两种 Content-Type
- 文件上传使用 `multipart/form-data`

## 主题系统

通过 `ThemeManager` 管理暗色/亮色主题切换：

- 侧边栏底部 **暗色/亮色** 按钮切换
- 偏好使用 `java.util.prefs.Preferences` 持久化
- 暗色主题 CSS：[theme-dark.css](src/main/resources/com/example/information_management_system/css/theme-dark.css)
- 动态创建的 UI 组件使用 CSS class 实现主题适配（`home-notice-card`、`home-course-card` 等）

## 页面导航

三端均采用**侧边栏 + 内容区**布局：

- 侧边栏按钮点击 → `BaseViewController.loadContent(fxmlPath)` → FXMLLoader 加载视图
- 部分页面内子页面通过 `findContentArea()` 直接替换内容区
- 内容区为 `StackPane`，保证子视图自动填充避免表格坍缩

## 常用调试

- **后端地址**：`src/main/resources/application.properties` 中 `server.url`
- **离线模式**：设置 `app.mode=offline` 使用 `MockData` 模拟数据
- **Token 刷新**：`Refresh.java` 自动定时刷新 access token

## 后端项目

后端为 Spring Boot 项目，主配置见 `application.yml`：
- 数据库：MySQL（`api.hoshsl.com:3306/cms`）
- 端口：`22222`
- ORM：MyBatis Plus
- 认证：JWT Token
- 缓存：Redis
