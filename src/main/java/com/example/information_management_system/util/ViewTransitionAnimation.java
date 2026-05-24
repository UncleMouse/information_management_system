package com.example.information_management_system.util;

import javafx.animation.*;
import javafx.scene.Parent;
import javafx.util.Duration;

public class ViewTransitionAnimation {

    public static void fadeIn(Parent node, Duration duration) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_IN);
        ft.play();
    }

    public static void fadeOut(Parent node, Duration duration) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    public static ParallelTransition slideInFromRight(Parent node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
        tt.setFromX(50);
        tt.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        return new ParallelTransition(tt, ft);
    }
}
