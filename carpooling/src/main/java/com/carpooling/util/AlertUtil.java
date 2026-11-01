package com.carpooling.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Helpers for showing JavaFX dialogs.
 */
public final class AlertUtil {

    private AlertUtil() {}

    public static void showError(String title, String message) {
        Alert a = new Alert(AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().getStylesheets().add(
                AlertUtil.class.getResource("/com/carpooling/css/style.css").toExternalForm());
        a.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert a = new Alert(AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().getStylesheets().add(
                AlertUtil.class.getResource("/com/carpooling/css/style.css").toExternalForm());
        a.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert a = new Alert(AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().getStylesheets().add(
                AlertUtil.class.getResource("/com/carpooling/css/style.css").toExternalForm());
        a.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert a = new Alert(AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().getStylesheets().add(
                AlertUtil.class.getResource("/com/carpooling/css/style.css").toExternalForm());
        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
