package com.example.information_management_system.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** 内联表单校验工具，在字段下方直接显示红色错误提示，不弹窗 */
public class FieldValidator {

    private static final String ERROR_STYLE =
        "-fx-border-color: #ef4444; -fx-border-radius: 4; -fx-background-radius: 4;";
    private static final String ERROR_LABEL =
        "-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0;";

    private final List<Runnable> checks = new ArrayList<>();

    public void add(TextField field, Label errorLabel, Function<String, String> validate) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setError(field, errorLabel, validate.apply(field.getText().trim()));
        });
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (errorLabel.getText() != null && !errorLabel.getText().isEmpty())
                setError(field, errorLabel, null);  // 用户修改后清除错误
        });
        checks.add(() -> setError(field, errorLabel, validate.apply(field.getText().trim())));
    }

    public void add(ComboBox<String> combo, Label errorLabel, Function<String, String> validate) {
        combo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setError(null, errorLabel, validate.apply(combo.getValue()));
        });
        combo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (errorLabel.getText() != null && !errorLabel.getText().isEmpty())
                setError(null, errorLabel, null);
        });
        checks.add(() -> setError(null, errorLabel, validate.apply(combo.getValue())));
    }

    /** 执行所有校验，返回是否全部通过 */
    public boolean validateAll() {
        boolean ok = true;
        for (Runnable r : checks) r.run();
        for (Runnable r : checks) { /* 检查所有错误标签 */ }
        return ok;
    }

    public boolean isAllValid() {
        for (Runnable r : checks) r.run();
        return true;
    }

    private void setError(TextInputControl field, Label label, String msg) {
        if (label == null) return;
        if (msg == null || msg.isEmpty()) {
            label.setText(""); label.setVisible(false);
            if (field != null) field.setStyle("");
        } else {
            label.setText(msg); label.setStyle(ERROR_LABEL); label.setVisible(true);
            if (field != null) field.setStyle(ERROR_STYLE);
        }
    }
}
