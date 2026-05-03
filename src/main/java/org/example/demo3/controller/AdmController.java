package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.demo3.dao.DataRestritaTodosDAO;
import org.example.demo3.entity.DataRestritaTodos;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdmController {

    @FXML private TextField txtNomeDataRestrita;
    @FXML private DatePicker dpDataRestritaGeral;
    @FXML private Button btnSalvarRestricaoGeral, btnExcluirFeriado;
    @FXML private ComboBox<DataRestritaTodos> cbAlterarFeriado;

    @FXML private TableView<DataRestritaTodos> tbDatasCriticas;
    @FXML private TableColumn<DataRestritaTodos, LocalDate> colCriticaData;
    @FXML private TableColumn<DataRestritaTodos, String> colCriticaDescricao;

    private ObservableList<DataRestritaTodos> listaDatas = FXCollections.observableArrayList();
    private DataRestritaTodosDAO dao = new DataRestritaTodosDAO();

    // Mock ID do usuário logado
    private final Integer USUARIO_LOGADO_ID = 1;

    @FXML
    public void initialize() {
        configurarTabela();
        configurarComboBox();
        carregarDados();

    }

    private void configurarTabela() {
        colCriticaData.setCellValueFactory(new PropertyValueFactory<>("data_bloqueio"));
        colCriticaDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        tbDatasCriticas.setItems(listaDatas);
    }

    private void configurarComboBox() {
        cbAlterarFeriado.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DataRestritaTodos item, boolean empty) {
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
            List<DataRestritaTodos> dados = dao.listarTodos();
            listaDatas.addAll(dados);
            cbAlterarFeriado.getItems().addAll(dados);
        } catch (SQLException e) {
            alerta("Erro", "Falha ao ler dados.");
        }
    }

    @FXML
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

            // Atualizado: Passando USUARIO_LOGADO_ID para o adm_id da entidade
            DataRestritaTodos nova = new DataRestritaTodos(null, USUARIO_LOGADO_ID, data, desc);
            dao.salvar(nova);

            carregarDados();
            txtNomeDataRestrita.clear();
            dpDataRestritaGeral.setValue(null);
        } catch (SQLException e) {
            alerta("Erro SQL", "Erro ao salvar no banco.");
        }
    }

    @FXML
    private void excluirData() {
        DataRestritaTodos sel = cbAlterarFeriado.getValue();
        if (sel == null) return;

        try {
            dao.excluir(sel.getId_data_restrita());
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