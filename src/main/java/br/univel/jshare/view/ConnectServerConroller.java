package br.univel.jshare.view;

import br.univel.jshare.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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

    public void setMain(Main main) {
        this.main = main;
    }

    @FXML
    void handleConnect() {
        changeState();
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
}
