package br.univel.jshare.controller;

import br.univel.jshare.Main;
import javafx.scene.control.Alert;

import java.util.Objects;

/**
 * Created by felipefrizzo on 06/04/17.
 */
public class AlertController {
    public static void showError(final Main main, final String title, final String headerText, final String contentText, final Alert.AlertType type) {
        Objects.requireNonNull(main, "Main class cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(headerText, "Header Text cannot be null");
        Objects.requireNonNull(contentText, "Content Text cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");

        final Alert alert = new Alert(type);
        alert.initOwner(main.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }
}
