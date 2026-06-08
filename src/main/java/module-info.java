module com.example.information_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.desktop;
    requires java.logging;
    requires java.prefs;
    requires transitive com.google.gson;
    requires fastexcel.core;

    opens com.example.information_management_system to javafx.fxml;
    opens com.example.information_management_system.controller to javafx.fxml;
    opens com.example.information_management_system.controller.admin to javafx.fxml, javafx.base;
    opens com.example.information_management_system.controller.teacher to javafx.fxml, javafx.base;
    opens com.example.information_management_system.controller.student to javafx.fxml, javafx.base;
    opens com.example.information_management_system.component to javafx.fxml;
    opens com.example.information_management_system.model to javafx.base, com.google.gson;

    exports com.example.information_management_system;
    exports com.example.information_management_system.component;
    exports com.example.information_management_system.controller;
    exports com.example.information_management_system.controller.admin;
    exports com.example.information_management_system.controller.teacher;
    exports com.example.information_management_system.controller.student;
    exports com.example.information_management_system.entity;
    exports com.example.information_management_system.model;
    exports com.example.information_management_system.util;
}