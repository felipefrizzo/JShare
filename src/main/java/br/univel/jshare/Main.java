package br.univel.jshare;

import br.univel.jshare.client.ClientConnection;
import br.univel.jshare.controller.ClientLayoutController;
import br.univel.jshare.controller.ServerLayoutController;
import br.univel.jshare.server.ServerConnection;
import br.univel.jshare.comum.Cliente;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.management.PlatformLoggingMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Created by felipefrizzo on 05/04/17.
 */
public class Main extends Application {
    private final static Integer PORT = 8080;
    private static String IP;

    private ClientConnection client = new ClientConnection();
    private ServerConnection server = new ServerConnection();

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Cliente defaultClient;

    public Main() {
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        defaultClient = new Cliente();

        defaultClient.setNome("Felipe Frizzo");
        defaultClient.setIp(this.IP);
        defaultClient.setPorta(this.PORT);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Objects.requireNonNull(primaryStage, "Stage cannot be null");

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Peer to Peer Project");
        this.primaryStage.setOnCloseRequest(e -> System.exit(0));

        server.setMain(this);
        client.setMain(this);

        initRootLayout();
        showConnectServerLayout();
        showConnectClientLayout();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Cliente getDefaultClient() {
        return defaultClient;
    }

    public static String getIP() {
        return IP;
    }

    public static Integer getPort() {
        return PORT;
    }

    public ClientConnection getClientConnection() {
        return client;
    }

    public ServerConnection getServerConnection() {
        return server;
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(br.univel.jshare.Main.class.getResource("view/RootLayout.fxml"));

            rootLayout = loader.load();
            primaryStage.setScene(new Scene(rootLayout));
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showConnectServerLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/ServerLayout.fxml"));

            AnchorPane layout = loader.load();
            rootLayout.setLeft(layout);

            ServerLayoutController controller = loader.getController();

            controller.setMain(this);
            server.addObserver(controller);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showConnectClientLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/ClientLayout.fxml"));

            AnchorPane layout = loader.load();
            rootLayout.setCenter(layout);

            ClientLayoutController controller = loader.getController();

            controller.setMain(this);
            client.addObserver(controller);
            server.addClientObserver(controller);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
