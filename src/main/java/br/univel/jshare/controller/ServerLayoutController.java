package br.univel.jshare.controller;

import br.univel.jshare.Main;
import br.univel.jshare.observers.ServerObserver;
import br.univel.jshare.server.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.rmi.RemoteException;
import java.util.Objects;

/**
 * Created by felipefrizzo on 05/04/17.
 */
public class ServerLayoutController implements ServerObserver {
    private Main main;

    @FXML
    private Button btnStartServer;

    @FXML
    private Button btnStopServer;

    @FXML
    private Button btnConnect;

    @FXML
    private Button btnDisconnect;

    @FXML
    private TextField fieldIp;

    @FXML
    private TextField fieldPort;

    @FXML
    private TextFlow flow;

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");
        this.main = main;
    }

    @FXML
    void handleConnect() {
        if (isValid()) {
            handleClientChangeStatus();
            this.main.getClientConnection().connect(fieldIp.getText(), Integer.parseInt(fieldPort.getText()));
        }
    }

    @FXML
    void handleDisconnect() {
        try {
            handleClientChangeStatus();
            this.main.getClientConnection().getService().desconectar(main.getDefaultClient());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void handleStartServer() {
        handleServerChangeStatus();

        this.main.getServerConnection().startServer();
    }

    @FXML
    void handleStopServer() {
        handleServerChangeStatus();

        this.main.getServerConnection().stopServer();
    }

    private void handleClientChangeStatus() {
        btnConnect.setVisible(!btnConnect.isVisible());
        btnDisconnect.setVisible(!btnDisconnect.isVisible());
        fieldIp.setDisable(!fieldIp.isDisable());
        fieldPort.setDisable(!fieldPort.isDisable());
    }

    private void handleServerChangeStatus() {
        btnStartServer.setVisible(!btnStartServer.isVisible());
        btnStopServer.setVisible(!btnStopServer.isVisible());
    }

    private boolean isValid() {
        StringBuilder error = new StringBuilder();

        if (fieldIp.getText() == null || fieldIp.getLength() == 0) {
            error.append("IP address cannot be null or empty.\n");
        }
        if ((this.fieldPort.getLength() != 2 && this.fieldPort.getLength() != 4) || this.fieldIp.getText() == null) {
            error.append("Port cannot be null or length need be two or four digits.\n");
        }

        if (error.length() == 0) {
            return true;
        } else {
            AlertController.showError(
                this.main,
                "Validation",
                "Please correct invalid fields",
                error.toString(),
                Alert.AlertType.ERROR
            );
            return false;
        }
    }

    @Override
    public void showLogInformation(String text) {
        Text t = new Text();
        t.setText(text + "\n");
        t.setFont(Font.font(null, FontWeight.EXTRA_BOLD, 14));
        t.setFill(Color.GREEN);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                flow.getChildren().addAll(t);
            }
        });
    }
}
