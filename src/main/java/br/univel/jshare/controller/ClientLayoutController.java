package br.univel.jshare.controller;

import br.univel.jshare.Main;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.comum.TipoFiltro;
import br.univel.jshare.observers.ClientObserver;
import br.univel.jshare.validator.MD5Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by felipefrizzo on 06/04/17.
 */
public class ClientLayoutController implements ClientObserver {
    private Main main;
    private Map<Cliente, List<Arquivo>> map;

    private TreeView<String> treeView;
    private TreeItem<String> root;
    private TreeItem<String> parent;
    private TreeItem<String> chieldren;

    @FXML
    private TextField fieldFilter;

    @FXML
    private TextField fieldSearch;

    @FXML
    private ComboBox<TipoFiltro> fieldTypeFilter;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnDownload;

    @FXML
    private StackPane stackPane;

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");
        this.main = main;
    }

    @FXML
    void initialize() {
        this.fieldTypeFilter.setItems(FXCollections.observableArrayList(TipoFiltro.values()));
        this.fieldTypeFilter.getSelectionModel().select(0);
    }

    @FXML
    void handleDownload() {
        String name = String.valueOf(treeView.getSelectionModel().getSelectedItem().getParent().getValue());
        String file = String.valueOf(treeView.getSelectionModel().getSelectedItem().getValue());

        map.forEach((key, value) -> {
            if (key.getNome().equals(name)) {
                value.forEach(v -> {
                    if (v.getNome().equals(file.substring(0, file.lastIndexOf(".")))) {
                        try {
                            Registry registry = LocateRegistry.getRegistry(key.getIp(), key.getPorta());
                            IServer service = (IServer) registry.lookup(IServer.NOME_SERVICO);

                            byte[] bytes = service.baixarArquivo(this.main.getDefaultClient(), v);
                            String fileName = v.getNome() + "." + name.replace(" ", "").toLowerCase() + "." + v.getExtensao();


                            File f = new File(fileName);
                            String filePath = "." + File.separatorChar + "shared" + File.separatorChar + f;

                            Path path = Files.write(Paths.get(filePath), bytes, StandardOpenOption.CREATE);

                            String md5 = MD5Validator.getMD5Checksum(String.valueOf(path));

                            if (md5.equals(v.getMd5())) {
                                AlertController.showError(
                                    this.main,
                                    "Download successful",
                                    "Download successful",
                                    "The file has been successfully downloaded.",
                                    Alert.AlertType.INFORMATION
                                );
                            } else {
                            	Files.delete(path);
                                AlertController.showError(
                                    this.main,
                                    "Download problem",
                                    "Download problem",
                                    "Downloaded file is corrupted, try downloading again",
                                    Alert.AlertType.INFORMATION
                                );
                            }

                        } catch (IOException | NotBoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
    }

    @FXML
    void handleSearch() {
        try {
            map = this.main.getClientConnection().getService().procurarArquivo(fieldSearch.getText(), fieldTypeFilter.getValue(), fieldFilter.getText());
            root = new TreeItem<>("List of files");

            map.forEach((key, value) -> {
                parent = new TreeItem<>(key.getNome());

                value.forEach(v -> {
                    chieldren = new TreeItem<>(v.getNome() + "." + v.getExtensao());
                    parent.getChildren().add(chieldren);
                });

                root.getChildren().add(parent);
            });

            treeView = new TreeView<>(root);
            treeView.getEditingItem();
            stackPane.getChildren().add(treeView);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public void enableClientFields() {
		fieldFilter.setDisable(!fieldFilter.isDisable());
		fieldSearch.setDisable(!fieldSearch.isDisable());
		fieldTypeFilter.setDisable(!fieldTypeFilter.isDisable());
		btnDownload.setDisable(!btnDownload.isDisable());
		btnSearch.setDisable(!btnSearch.isDisable());
	}
}
