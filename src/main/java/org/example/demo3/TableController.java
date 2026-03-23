package org.example.demo3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class TableController {
    @FXML
    private TableView<PlanejamentoDia> tabelaPlanejamento;

    @FXML
    private TableColumn<PlanejamentoDia, String> dataCol;

    @FXML
    private TableColumn<PlanejamentoDia, String> disciCol;

    @FXML
    private TableColumn<PlanejamentoDia, String> temaCol;

    @FXML
    private TableColumn<PlanejamentoDia, String> xCol;

    @FXML
    private TableColumn<PlanejamentoDia, Integer> aulasCol;

    @FXML
    private TableColumn<PlanejamentoDia, String> diaCol;

    public void initialize3(){
        dataCol.setCellValueFactory(new PropertyValueFactory<>("data"));
        disciCol.setCellValueFactory(new PropertyValueFactory<>("disciplina"));
        temaCol.setCellValueFactory(new PropertyValueFactory<>("tema"));
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        aulasCol.setCellValueFactory(new PropertyValueFactory<>("aulas"));
        diaCol.setCellValueFactory(new PropertyValueFactory<>("dia_semana"));

        ObservableList<PlanejamentoDia> planejamento = FXCollections.observableArrayList();
        //planejamento.add(new PlanejamentoDia("03/04/20", "Arquitetura", "Introdução", "X", 1, "Segunda"));

        tabelaPlanejamento.setItems(planejamento);
    }
}
