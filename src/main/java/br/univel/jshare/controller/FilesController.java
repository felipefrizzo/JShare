package br.univel.jshare.controller;

import br.univel.jshare.Main;
import br.univel.jshare.comum.TipoFiltro;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;

import java.util.Objects;

/**
 * Created by felipefrizzo on 04/04/17.
 */
public class FilesController {
    private Main main;

    @FXML
    private TextField textFieldSearch;

    @FXML
    private ComboBox<TipoFiltro> boxTypeFilter;

    @FXML
    private TextField textFieldFilter;

    @FXML
    private TreeView<String> treeView;

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");
        this.main = main;
    }

    @FXML
    void initialize() {
        boxTypeFilter.setItems(FXCollections.observableArrayList(TipoFiltro.values()));
    }

    @FXML
    void handleDownload() {

    }

    @FXML
    void handleSearch() {

    }
}
