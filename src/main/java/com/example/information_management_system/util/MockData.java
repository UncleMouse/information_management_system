package com.example.information_management_system.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MockData {

    private static final Gson gson = new Gson();

    public static String getMockResponse(String url, String method, String body) {
        // 登录
        if (url.contains("/login/simpleLogin") || url.contains("/login/SDULogin")) return loginResponse(body);
        if (url.contains("/login/refreshToken")) return refreshTokenResponse();

        // 学期
        if (url.contains("/term/getTermList")) return termListResponse();
        if (url.contains("/term/getCurrentTerm")) return currentTermResponse();

        // 管理员 - 学生管理
        if (url.contains("/admin/student/list") || url.contains("/admin/searchSdu")) return studentListResponse();
        if (url.contains("/admin/addUser") || url.contains("/admin/updateUser")) return successResponse("操作成功");
        if (url.contains("/admin/deleteUser")) return successResponse("删除成功");
        if (url.contains("/admin/getNum") || url.contains("/admin/getUserInfo")) return successResponse("操作成功");
        if (url.contains("/admin/upload")) return successResponse("导入成功");

        // 管理员 - 教师管理
        if (url.contains("/admin/getTeacherList")) return teacherListResponse();

        // 管理员 - 课程管理(审批)
        if (url.contains("/class/pending")) return courseListResponse();
        if (url.contains("/class/approve") || url.contains("/class/adUpdate")
                || url.contains("/class/deleteAd")) return successResponse("审核完成");

        // 管理员 - 班级管理
        if (url.contains("/section/getSectionList") || url.contains("/section/search")) return sectionListResponse();
        if (url.contains("/section/addSection") || url.contains("/section/updateSection")) return successResponse("操作成功");
        if (url.contains("/section/deleteSection") || url.contains("/section/assign")) return successResponse("操作成功");

        // 通知(new backend)
        if (url.contains("/notice/get")) return noticeListResponse();
        if (url.contains("/notice/set") || url.contains("/notice/edit")) return successResponse("操作成功");
        if (url.contains("/notice/close")) return successResponse("操作成功");

        // 教师
        if (url.contains("/class/list") || url.contains("/class/searchTeacherCourses")) return teacherCourseListResponse();
        if (url.contains("/Teacher/getClassRoom")) return classRoomListResponse();
        if (url.contains("/class/") && url.contains("/students")) return studentListResponse();
        if (url.contains("/class/getClassSchedule")) return scheduleResponse();
        if (url.contains("/class/detail")) return successResponse("操作成功");

        // 课程CRUD
        if (url.contains("/class/create") || url.contains("/class/update")
                || url.contains("/class/delete")) return successResponse("操作成功");

        // 教师信息
        if (url.contains("/Teacher/getMessage") || url.contains("/Teacher/countClass")) return successResponse("操作成功");

        // 成绩
        if (url.contains("/grade/getGrade") || url.contains("/grade/getMessage")) return gradeListResponse();
        if (url.contains("/grade/setGrade") || url.contains("/grade/releaseGrade")
                || url.contains("/grade/getGradeList") || url.contains("/class/updateRank")
                || url.contains("/class/updatePointRank")) return successResponse("操作成功");

        // 学生 - 课表
        if (url.contains("/class/getClassSchedule")) return scheduleResponse();

        // 学生 - 选课
        if (url.contains("/course-selection/search") || url.contains("/course-selection/unChoose")) return availableCoursesResponse();
        if (url.contains("/course-selection/results")) return selectedCoursesResponse();
        if (url.contains("/course-selection/select")) return successResponse("选课成功");
        if (url.contains("/course-selection/drop")) return successResponse("退课成功");

        // 用户信息
        if (url.contains("/user/updatePhone") || url.contains("/user/updateEmail")
                || url.contains("/user/updatePassword") || url.contains("/user/resetPassword")
                || url.contains("/user/getInfo") || url.contains("/user/logout")) {
            return successResponse("更新成功");
        }

        // 学籍状态
        if (url.contains("/status/set") || url.contains("/status/getStatusCard")) return successResponse("操作成功");

        // 默认成功响应
        return successResponse("操作成功");
    }

    private static String successResponse(String msg) {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        res.addProperty("msg", msg);
        res.add("data", new JsonObject());
        return gson.toJson(res);
    }

    private static String loginResponse(String body) {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonObject data = new JsonObject();
        // 根据请求体判断身份
        int permission = 0;
        String username = "admin";
        if (body != null) {
            try {
                JsonObject req = gson.fromJson(body, JsonObject.class);
                String stuId = req.has("stuId") ? req.get("stuId").getAsString() : "";
                if (stuId.startsWith("2024")) { permission = 2; username = "张三"; }
                else if (stuId.startsWith("190")) { permission = 1; username = "李教授"; }
                else { permission = 0; username = "系统管理员"; }
            } catch (Exception ignored) {}
        }
        data.addProperty("accessToken", "mock-access-token-xxxxx");
        data.addProperty("refreshToken", "mock-refresh-token-xxxxx");
        data.addProperty("permission", permission);
        data.addProperty("username", username);
        res.add("data", data);
        res.addProperty("msg", "登录成功");
        return gson.toJson(res);
    }

    private static String refreshTokenResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonObject data = new JsonObject();
        data.addProperty("accessToken", "mock-access-token-refreshed");
        data.addProperty("refreshToken", "mock-refresh-token-refreshed");
        res.add("data", data);
        return gson.toJson(res);
    }

    private static String termListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray arr = new JsonArray();
        String[] terms = {"2024-2025-1", "2024-2025-2", "2025-2026-1"};
        for (String t : terms) {
            JsonObject obj = new JsonObject();
            obj.addProperty("term", t);
            arr.add(obj);
        }
        res.add("data", arr);
        return gson.toJson(res);
    }

    private static String currentTermResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        res.addProperty("data", "2024-2025-2");
        res.addProperty("msg", "获取成功");
        return gson.toJson(res);
    }

    private static String studentListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray arr = new JsonArray();
        String[][] students = {
            {"1", "202400000001", "张三", "男", "软件工程", "软工2201", "在读"},
            {"2", "202400000002", "李四", "女", "数字媒体技术", "数媒2201", "在读"},
            {"3", "202400000003", "王五", "男", "计算机科学", "计科2201", "在读"},
            {"4", "202400000004", "赵六", "女", "人工智能", "智能2201", "在读"},
            {"5", "202400000005", "刘七", "男", "大数据", "大数据2201", "休学"},
            {"6", "202400000006", "陈八", "女", "软件工程", "软工2202", "在读"},
            {"7", "202400000007", "周九", "男", "数字媒体技术", "数媒2202", "在读"},
            {"8", "202400000008", "吴十", "女", "计算机科学", "计科2202", "转出"},
        };
        for (String[] s : students) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Integer.parseInt(s[0]));
            obj.addProperty("sduid", s[1]);
            obj.addProperty("name", s[2]);
            obj.addProperty("sex", s[3]);
            obj.addProperty("major", s[4]);
            obj.addProperty("className", s[5]);
            obj.addProperty("status", s[6]);
            arr.add(obj);
        }
        res.add("data", arr);
        return gson.toJson(res);
    }

    private static String teacherListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray arr = new JsonArray();
        String[][] teachers = {
            {"1", "190100000001", "李教授", "计算机学院", "liz@campus.edu.cn", "在职"},
            {"2", "190100000002", "王副教授", "软件学院", "wangx@campus.edu.cn", "在职"},
            {"3", "190100000003", "张讲师", "数学学院", "zhangq@campus.edu.cn", "在职"},
            {"4", "190100000004", "刘教授", "人工智能学院", "liuw@campus.edu.cn", "在职"},
            {"5", "190100000005", "陈副教授", "数字媒体学院", "cheny@campus.edu.cn", "休假"},
        };
        for (String[] t : teachers) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Integer.parseInt(t[0]));
            obj.addProperty("sduid", t[1]);
            obj.addProperty("name", t[2]);
            obj.addProperty("college", t[3]);
            obj.addProperty("contactInfo", t[4]);
            obj.addProperty("status", t[5]);
            arr.add(obj);
        }
        res.add("data", arr);
        return gson.toJson(res);
    }

    private static String courseListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray arr = new JsonArray();
        String[][] courses = {
            {"1", "CS101", "数据结构与算法", "李教授", "4.0", "必修", "2024-2025-2", "已通过"},
            {"2", "CS102", "操作系统", "王副教授", "3.0", "必修", "2024-2025-2", "已通过"},
            {"3", "CS201", "Java程序设计", "张讲师", "3.0", "限选", "2024-2025-2", "待审批"},
            {"4", "CS202", "人工智能导论", "刘教授", "2.0", "任选", "2024-2025-2", "待审批"},
            {"5", "CS301", "计算机网络", "李教授", "3.5", "必修", "2024-2025-2", "已通过"},
            {"6", "CS302", "数据库原理", "陈副教授", "3.0", "必修", "2024-2025-2", "已拒绝"},
        };
        for (String[] c : courses) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Integer.parseInt(c[0]));
            obj.addProperty("code", c[1]);
            obj.addProperty("name", c[2]);
            obj.addProperty("teacherName", c[3]);
            obj.addProperty("credit", Double.parseDouble(c[4]));
            obj.addProperty("type", c[5]);
            obj.addProperty("term", c[6]);
            obj.addProperty("status", c[7]);
            obj.addProperty("classNum", 60);
            obj.addProperty("peopleNum", 45);
            arr.add(obj);
        }
        res.add("data", arr);
        return gson.toJson(res);
    }

    private static String sectionListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray arr = new JsonArray();
        String[][] sections = {
            {"1", "软工2201", "软件工程", "2022", "李教授", "32"},
            {"2", "软工2202", "软件工程", "2022", "王副教授", "30"},
            {"3", "数媒2201", "数字媒体技术", "2022", "张讲师", "28"},
            {"4", "计科2201", "计算机科学", "2022", "刘教授", "35"},
            {"5", "智能2201", "人工智能", "2022", "陈副教授", "25"},
        };
        for (String[] s : sections) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Integer.parseInt(s[0]));
            obj.addProperty("name", s[1]);
            obj.addProperty("major", s[2]);
            obj.addProperty("grade", s[3]);
            obj.addProperty("counselor", s[4]);
            obj.addProperty("studentCount", Integer.parseInt(s[5]));
            arr.add(obj);
        }
        res.add("data", arr);
        return gson.toJson(res);
    }

    private static String noticeListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray arr = new JsonArray();
        String[][] notices = {
            {"1", "关于2025春季学期选课通知", "各位同学：2025年春季学期选课将于3月1日开始，请登录系统在规定时间内完成选课操作。", "管理员", "2025-02-20", "所有人"},
            {"2", "期末考试安排通知", "2024-2025-2学期期末考试时间定于6月20日至7月5日，具体考试安排请查看个人课表。", "管理员", "2025-06-01", "学生"},
            {"3", "关于开展教学质量评估的通知", "请各位教师在6月15日前完成本学期教学质量自评工作。", "管理员", "2025-06-05", "教师"},
        };
        for (String[] n : notices) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Integer.parseInt(n[0]));
            obj.addProperty("title", n[1]);
            obj.addProperty("content", n[2]);
            obj.addProperty("creatorName", n[3]);
            obj.addProperty("publishTime", n[4]);
            obj.addProperty("visibleScope", n[5]);
            arr.add(obj);
        }
        res.add("data", arr);
        return gson.toJson(res);
    }

    private static String teacherCourseListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray data = new JsonArray();
        String[][] courses = {
            {"101", "CS101", "数据结构与算法", "4.0", "必修", "2024-2025-2", "45", "60"},
            {"102", "CS301", "计算机网络", "3.5", "必修", "2024-2025-2", "50", "55"},
            {"103", "CS401", "软件工程", "3.0", "限选", "2024-2025-2", "30", "40"},
            {"104", "CS501", "机器学习", "2.0", "任选", "2024-2025-2", "20", "35"},
        };
        for (String[] c : courses) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Integer.parseInt(c[0]));
            obj.addProperty("code", c[1]);
            obj.addProperty("name", c[2]);
            obj.addProperty("credit", Double.parseDouble(c[3]));
            obj.addProperty("type", c[4]);
            obj.addProperty("term", c[5]);
            obj.addProperty("peopleNum", Integer.parseInt(c[6]));
            obj.addProperty("classNum", Integer.parseInt(c[7]));
            obj.addProperty("teacherName", "李教授");
            obj.addProperty("status", "已通过");
            data.add(obj);
        }
        res.add("data", data);
        return gson.toJson(res);
    }

    private static String classRoomListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray data = new JsonArray();
        String[] rooms = {"教学楼A101", "教学楼A102", "教学楼B201", "教学楼B202",
                "实验楼C301", "实验楼C302", "综合楼D101", "综合楼D201"};
        for (String r : rooms) {
            JsonObject obj = new JsonObject();
            obj.addProperty("location", r);
            data.add(obj);
        }
        res.add("data", data);
        return gson.toJson(res);
    }

    private static String scheduleResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray data = new JsonArray();
        // 5个时间段，每个有7天的课程数据
        String[] times = {"第1-2节", "第3-4节", "第5-6节", "第7-8节", "第9-10节"};
        String[][] schedule = {
            {"数据结构\n教学楼A101", "操作系统\n教学楼B201", "", "计算机网络\n实验楼C301", "", "", ""},
            {"Java程序设计\n教学楼A102", "人工智能导论\n综合楼D101", "数据结构\n教学楼A101", "", "数据库原理\n实验楼C302", "", ""},
            {"", "计算机网络\n实验楼C301", "操作系统\n教学楼B201", "Java程序设计\n教学楼A102", "", "", ""},
            {"数据库原理\n实验楼C302", "", "人工智能导论\n综合楼D101", "", "", "", ""},
            {"", "", "", "", "", "", ""},
        };
        for (int i = 0; i < times.length; i++) {
            JsonObject obj = new JsonObject();
            obj.addProperty("time", times[i]);
            obj.addProperty("monday", schedule[i][0]);
            obj.addProperty("tuesday", schedule[i][1]);
            obj.addProperty("wednesday", schedule[i][2]);
            obj.addProperty("thursday", schedule[i][3]);
            obj.addProperty("friday", schedule[i][4]);
            obj.addProperty("saturday", schedule[i][5]);
            obj.addProperty("sunday", schedule[i][6]);
            data.add(obj);
        }
        res.add("data", data);
        return gson.toJson(res);
    }

    private static String availableCoursesResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray arr = new JsonArray();
        String[][] courses = {
            {"201", "CS201", "Java程序设计", "张讲师", "3.0", "限选", "40", "25", "周二 3-4节"},
            {"202", "CS202", "人工智能导论", "刘教授", "2.0", "任选", "35", "18", "周四 1-2节"},
            {"203", "CS401", "软件工程", "陈副教授", "3.0", "限选", "45", "30", "周三 5-6节"},
            {"204", "CS501", "机器学习", "周教授", "2.5", "任选", "30", "15", "周五 3-4节"},
            {"205", "CS601", "Web前端开发", "吴讲师", "2.0", "任选", "50", "40", "周一 7-8节"},
        };
        for (String[] c : courses) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Integer.parseInt(c[0]));
            obj.addProperty("code", c[1]);
            obj.addProperty("name", c[2]);
            obj.addProperty("teacherName", c[3]);
            obj.addProperty("credit", Double.parseDouble(c[4]));
            obj.addProperty("type", c[5]);
            obj.addProperty("classNum", Integer.parseInt(c[6]));
            obj.addProperty("peopleNum", Integer.parseInt(c[7]));
            obj.addProperty("time", c[8]);
            obj.addProperty("term", "2024-2025-2");
            arr.add(obj);
        }
        res.add("data", arr);
        return gson.toJson(res);
    }

    private static String selectedCoursesResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray data = new JsonArray();
        String[][] courses = {
            {"101", "CS101", "数据结构与算法", "李教授", "4.0", "必修", "周一 1-2节, 周三 3-4节", "2024-2025-2"},
            {"102", "CS102", "操作系统", "王副教授", "3.0", "必修", "周二 1-2节, 周四 5-6节", "2024-2025-2"},
            {"103", "CS301", "计算机网络", "李教授", "3.5", "必修", "周三 1-2节, 周五 3-4节", "2024-2025-2"},
            {"104", "CS302", "数据库原理", "陈副教授", "3.0", "必修", "周一 3-4节, 周四 7-8节", "2024-2025-2"},
            {"201", "CS201", "Java程序设计", "张讲师", "3.0", "限选", "周二 3-4节", "2024-2025-2"},
        };
        for (String[] c : courses) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Integer.parseInt(c[0]));
            obj.addProperty("code", c[1]);
            obj.addProperty("name", c[2]);
            obj.addProperty("teacherName", c[3]);
            obj.addProperty("credit", Double.parseDouble(c[4]));
            obj.addProperty("type", c[5]);
            obj.addProperty("time", c[6]);
            obj.addProperty("term", c[7]);
            obj.addProperty("isSelected", true);
            data.add(obj);
        }
        res.add("data", data);
        return gson.toJson(res);
    }

    private static String gradeListResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("code", 200);
        JsonArray arr = new JsonArray();
        String[][] grades = {
            {"1", "CS101", "数据结构与算法", "4.0", "必修", "李教授", "92", "4.0", "3"},
            {"2", "CS102", "操作系统", "3.0", "必修", "王副教授", "85", "3.3", "8"},
            {"3", "CS301", "计算机网络", "3.5", "必修", "李教授", "88", "3.7", "5"},
            {"4", "CS302", "数据库原理", "3.0", "必修", "陈副教授", "78", "2.7", "15"},
            {"5", "CS201", "Java程序设计", "3.0", "限选", "张讲师", "95", "4.0", "1"},
        };
        for (int i = 0; i < grades.length; i++) {
            String[] g = grades[i];
            JsonObject obj = new JsonObject();
            obj.addProperty("index", i + 1);
            obj.addProperty("id", Integer.parseInt(g[0]));
            obj.addProperty("courseName", g[1]);
            obj.addProperty("point", Double.parseDouble(g[2]));
            obj.addProperty("type", g[3]);
            obj.addProperty("teacher", g[4]);
            obj.addProperty("grade", g[5]);
            obj.addProperty("gpa", Double.parseDouble(g[6]));
            obj.addProperty("rank", Integer.parseInt(g[7]));
            obj.addProperty("regular", Math.round((Integer.parseInt(g[5]) - 40) * 100.0 / 100));
            obj.addProperty("finalScore", 60 + i * 3);
            arr.add(obj);
        }
        res.add("data", arr);
        return gson.toJson(res);
    }
}
