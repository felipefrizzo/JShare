package br.univel.jshare.controller;

import br.univel.jshare.Main;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.TipoFiltro;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by felipefrizzo on 04/04/17.
 */
public class FilesController {
    private Main main;
    private TreeView<String> treeView;
    private TreeItem<String> treeRoot;
    private TreeItem<String> treeParent;
    private TreeItem<String> treeChildren;

    @FXML
    private TextField textFieldSearch;

    @FXML
    private ComboBox<TipoFiltro> boxTypeFilter;

    @FXML
    private TextField textFieldFilter;

    @FXML
    private StackPane rootView;

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");
        this.main = main;
    }

    @FXML
    void initialize() {
        boxTypeFilter.setItems(FXCollections.observableArrayList(TipoFiltro.values()));
        boxTypeFilter.getSelectionModel().select(0);
    }

    @FXML
    void handleDownload() {

    }

    @FXML
    void handleSearch() {
        try {
            Map<Cliente, List<Arquivo>> map = this.main.getClient().procurarArquivo(textFieldSearch.getText(), boxTypeFilter.getValue(), textFieldFilter.getText());
            treeRoot = new TreeItem<>("Files");

            map.forEach((key, value) -> {
                treeParent = new TreeItem<>(key.getNome());

                value.forEach(v -> {
                    treeChildren = new TreeItem<>(v.getNome());
                    treeParent.getChildren().add(treeChildren);
                });

                treeRoot.getChildren().add(treeParent);
            });

            treeView = new TreeView<>(treeRoot);

            rootView.getChildren().add(treeView);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
