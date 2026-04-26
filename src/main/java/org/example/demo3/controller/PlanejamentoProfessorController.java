package org.example.demo3.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.demo3.dao.PlanejamentoDiaDao;
import org.example.demo3.entity.PlanejamentoDia;

import java.util.List;

public class PlanejamentoProfessorController {
    @FXML private MFXLegacyTableView<PlanejamentoDia> mfxTableView;
    @FXML private TableColumn<PlanejamentoDia, String> dataCol, disciCol, temaCol, obsCol, diaCol;
    @FXML private TableColumn<PlanejamentoDia, Integer> aulasCol;

    @FXML private MFXLegacyComboBox<Integer> comboSemestre;
    @FXML private MFXLegacyComboBox<String> comboCurso;

    @FXML private MFXButton editRow, deleteRow;

    @FXML private ListView<String> listaLogs; // Adicione este ID no seu FXML

    public void carregarDados() {
        PlanejamentoDiaDao dao = new PlanejamentoDiaDao();

        // 1. Executa a correção e recebe as mensagens do que mudou
        List<String> logs = dao.corrigirCronograma(1, 2);

        // 2. Exibe os logs na tela para o usuário
        if (logs.isEmpty()) {
            listaLogs.getItems().setAll("Nenhum conflito encontrado. Cronograma em dia.");
        } else {
            listaLogs.getItems().setAll(logs);
        }

        // 3. Atualiza a tabela principal
        List<PlanejamentoDia> lista = PlanejamentoDiaDao.atualizarPlanejamentoDia(1, 2);
        mfxTableView.setItems(FXCollections.observableArrayList(lista));
    }

    public void initialize() {
        carregarDados();
    }

    public void comboAction(){
        System.out.println(comboCurso.getSelectionModel().getSelectedItem().toString());
    }
}