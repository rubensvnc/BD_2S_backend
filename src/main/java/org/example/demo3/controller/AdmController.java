package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.demo3.dao.DataCriticaDAO;
import org.example.demo3.entity.Datas_Restritas;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdmController {

    @FXML private TextField txtNomeDataRestrita;
    @FXML private DatePicker dpDataRestritaGeral;
    @FXML private Button btnSalvarRestricaoGeral, btnExcluirFeriado;
    @FXML private ComboBox<Datas_Restritas> cbAlterarFeriado;

    @FXML private TableView<Datas_Restritas> tbDatasCriticas;
    @FXML private TableColumn<Datas_Restritas, LocalDate> colCriticaData;
    @FXML private TableColumn<Datas_Restritas, String> colCriticaDescricao;

    private ObservableList<Datas_Restritas> listaDatas = FXCollections.observableArrayList();
    private DataCriticaDAO dao = new DataCriticaDAO();

    @FXML
    public void initialize() {
        configurarTabela();
        configurarComboBox();
        carregarDados();

        btnSalvarRestricaoGeral.setOnAction(e -> salvarData());
        btnExcluirFeriado.setOnAction(e -> excluirData());
    }

    private void configurarTabela() {
        colCriticaData.setCellValueFactory(new PropertyValueFactory<>("data_bloqueio"));
        colCriticaDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        tbDatasCriticas.setItems(listaDatas);
    }

    private void configurarComboBox() {
        cbAlterarFeriado.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Datas_Restritas item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getDescricao() + " (" + item.getData_bloqueio() + ")");
            }
        });
        cbAlterarFeriado.setButtonCell(cbAlterarFeriado.getCellFactory().call(null));
    }

    private void carregarDados() {
        listaDatas.clear();
        cbAlterarFeriado.getItems().clear();
        try {
            List<Datas_Restritas> dados = dao.listarTodos();
            listaDatas.addAll(dados);
            cbAlterarFeriado.getItems().addAll(dados);
        } catch (SQLException e) {
            alerta("Erro", "Falha ao ler dados.");
        }
    }

    private void salvarData() {
        LocalDate data = dpDataRestritaGeral.getValue();
        String desc = txtNomeDataRestrita.getText();

        if (data == null || desc.isEmpty()) {
            alerta("Aviso", "Preencha a data e descrição.");
            return;
        }

        try {
            if (dao.existeData(data)) {
                alerta("Erro", "Esta data já está bloqueada.");
                return;
            }

            // ID é null porque é Auto_increment; id_referencia null para geral
            Datas_Restritas nova = new Datas_Restritas(null, data, desc, null);
            dao.salvar(nova);
            carregarDados();
            txtNomeDataRestrita.clear();
            dpDataRestritaGeral.setValue(null);
        } catch (SQLException e) {
            alerta("Erro SQL", "Erro ao salvar no banco.");
        }
    }

    private void excluirData() {
        Datas_Restritas sel = cbAlterarFeriado.getValue();
        if (sel == null) return;

        try {
            dao.excluir(sel.getId_restricao());
            carregarDados();
        } catch (SQLException e) {
            alerta("Erro SQL", "Erro ao excluir.");
        }
    }

    private void alerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}