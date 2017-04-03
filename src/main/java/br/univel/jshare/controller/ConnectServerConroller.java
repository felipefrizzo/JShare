package br.univel.jshare.controller;

import br.univel.jshare.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.Objects;

/**
 * Created by felipefrizzo on 02/04/17.
 */
public class ConnectServerConroller {
    private Main main;

    @FXML
    private TextField textFieldIp;

    @FXML
    private TextField textFieldPort;

    @FXML
    private Button btnConnect;

    @FXML
    private Button btnDisconnect;

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");

        this.main = main;
    }

    @FXML
    void handleConnect() {
        if (isValid()) {
            changeState();
        }
    }

    @FXML
    void handleDisconnect() {
        changeState();
    }

    private void changeState() {
        this.btnConnect.setVisible(!this.btnConnect.isVisible());
        this.btnDisconnect.setVisible(!this.btnDisconnect.isVisible());
        this.textFieldIp.setDisable(!this.textFieldIp.isDisable());
        this.textFieldPort.setDisable(!this.textFieldPort.isDisable());
    }

    private boolean isValid() {
        StringBuilder error = new StringBuilder();

        if (this.textFieldIp.getLength() == 0 || this.textFieldIp.getText() == null) {
            error.append("IP address cannot be null or empty.\n");
        }

        if ((this.textFieldPort.getLength() != 2 && this.textFieldPort.getLength() != 4) || this.textFieldPort.getText() == null) {
            error.append("Port cannot be null or length need be two or four digits.\n");
        }

        if (error.length() == 0) {
            return true;
        } else {
            showError(
                "Validation",
                "Please correct invalid fields",
                error.toString(),
                Alert.AlertType.ERROR
            );
            return false;
        }
    }

    private void showError(final String title, final String headerText, final String contentText, final Alert.AlertType type) {
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
