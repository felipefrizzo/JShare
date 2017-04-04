package br.univel.jshare;

import br.univel.jshare.comum.IServer;
import br.univel.jshare.controller.FilesController;
import br.univel.jshare.controller.ServerController;
import br.univel.jshare.controller.ConnectServerConroller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.Objects;

/**
 * Created by felipefrizzo on 02/04/17.
 */
public class Main extends Application {
    private Registry registryClient;
    private Registry registryServer;
    private IServer server;
    private IServer client;

    private Stage primaryStage;
    private BorderPane rootLayout;
    private final ServerController serverController = new ServerController();

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ServerController getServerController() {
        return serverController;
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Objects.requireNonNull(primaryStage, "Stage cannot be null");

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Peer to Peer Project");

        serverController.setMain(this);

        initRootLayout();
        showConnectServerLayout();
        showFilesLayout();
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/RootLayout.fxml"));

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
            loader.setLocation(Main.class.getResource("view/ConnectServer.fxml"));

            AnchorPane layout = loader.load();
            rootLayout.setLeft(layout);

            ConnectServerConroller controller = loader.getController();

            controller.setMain(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showFilesLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/FilesLayout.fxml"));

            AnchorPane layout = loader.load();
            rootLayout.setCenter(layout);

            FilesController controller = loader.getController();

            controller.setMain(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Registry getRegistryClient() {
        return registryClient;
    }

    public void setRegistryClient(Registry registryClient) {
        this.registryClient = registryClient;
    }

    public Registry getRegistryServer() {
        return registryServer;
    }

    public void setRegistryServer(Registry registryServer) {
        this.registryServer = registryServer;
    }

    public IServer getServer() {
        return server;
    }

    public void setServer(IServer server) {
        this.server = server;
    }

    public IServer getClient() {
        return client;
    }

    public void setClient(IServer client) {
        this.client = client;
    }
}
