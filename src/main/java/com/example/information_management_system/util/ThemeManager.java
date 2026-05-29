package com.example.information_management_system.util;

import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String KEY = "theme";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    public static boolean isDark() {
        return "dark".equals(prefs.get(KEY, "light"));
    }

    public static void setDark(boolean dark) {
        prefs.put(KEY, dark ? "dark" : "light");
    }

    /** Replace '-fx-' color rules in the scene's stylesheets to switch theme */
    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        if (isDark()) {
            scene.getStylesheets().add(
                ThemeManager.class.getResource("/com/example/information_management_system/css/theme-dark.css").toExternalForm());
        } else {
            scene.getStylesheets().removeIf(s ->
                s.contains("theme-dark.css"));
        }
    }
}
