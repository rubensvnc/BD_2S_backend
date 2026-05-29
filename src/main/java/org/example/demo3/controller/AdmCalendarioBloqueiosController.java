package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;
import org.example.demo3.dao.CancelamentoAdmDAO;
import org.example.demo3.dao.SprintDAO;
import org.example.demo3.entity.CancelamentoAdm;
import org.example.demo3.entity.Sprint;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class AdmCalendarioBloqueiosController {

    // Aba 1 — Componentes FXML (Calendário / Sprints)
    @FXML private DatePicker dpInicioSemestre;
    @FXML private DatePicker dpFimSemestre;
    @FXML private DatePicker dpTcc;
    @FXML private DatePicker dpFeira;
    @FXML private TableView<Sprint> tabelaSprints;
    @FXML private TableColumn<Sprint, Integer> colSprintNum;
    @FXML private TableColumn<Sprint, LocalDate> colSprintInicio;
    @FXML private TableColumn<Sprint, LocalDate> colSprintReview;
    @FXML private Label lblFeedbackCalendario;

    // Aba 2 — Componentes FXML (Cancelamentos / Bloqueios)
    @FXML private ComboBox<String> cbMes;
    @FXML private GridPane gridDias;
    @FXML private VBox boxConfigCancelamento;
    @FXML private ComboBox<String> cbTurno;
    @FXML private TextField tfMotivoCancelamento;
    @FXML private Label lblFeedbackCancelamento;
    @FXML private ListView<CancelamentoAdm> listCancelamentos; // Tipado com a entidade real
    @FXML private Button btnDeletar;

    // DAOs do Projeto
    private final SprintDAO sprintDAO = new SprintDAO();
    private final CancelamentoAdmDAO cancelamentoDAO = new CancelamentoAdmDAO();

    private final ObservableList<Sprint> listaSprintsFX = FXCollections.observableArrayList();
    private final ObservableList<CancelamentoAdm> listaCancelamentosFX = FXCollections.observableArrayList();
    private LocalDate dataSelecionadaNoCalendario;

    // ID do semestre ativo no escopo da tela (pode ser obtido dinamicamente se necessário)
    private final int ID_SEMESTRE_ATUAL = 1;
    private final int ID_ADM_LOGADO = 1;

    @FXML
    public void initialize() {
        configurarTabelaSprints();
        carregarDadosSprints();
        configurarAbaBloqueios();
        carregarListaCancelamentos();
    }

    // ═══════════════════════════════════════════════
    // LÓGICA DA ABA 1 — CALENDÁRIO E SPRINTS
    // ═══════════════════════════════════════════════

    private void configurarTabelaSprints() {
        tabelaSprints.setEditable(true);
        colSprintNum.setCellValueFactory(new PropertyValueFactory<>("numero"));

        colSprintInicio.setCellValueFactory(new PropertyValueFactory<>("data_inicio"));
        colSprintInicio.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        colSprintInicio.setOnEditCommit(event -> event.getRowValue().setData_inicio(event.getNewValue()));

        colSprintReview.setCellValueFactory(new PropertyValueFactory<>("data_review"));
        colSprintReview.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        colSprintReview.setOnEditCommit(event -> event.getRowValue().setData_review(event.getNewValue()));
    }

    private void carregarDadosSprints() {
        listaSprintsFX.clear();
        var sprintsBanco = sprintDAO.listarSprints();

        if (sprintsBanco.isEmpty()) {
            for (int i = 1; i <= 3; i++) {
                Sprint s = new Sprint();
                s.setNumero(i);
                s.setSemestre_letivo_id(ID_SEMESTRE_ATUAL);
                s.setData_inicio(LocalDate.now());
                s.setData_fim(LocalDate.now().plusDays(15));
                s.setData_review(LocalDate.now().plusDays(14));
                listaSprintsFX.add(s);
            }
        } else {
            listaSprintsFX.addAll(sprintsBanco);
        }
        tabelaSprints.setItems(listaSprintsFX);
    }

    @FXML
    public void handleSalvarCalendario() {
        try {
            if (dpInicioSemestre.getValue() != null && dpFimSemestre.getValue() != null) {
                if (dpInicioSemestre.getValue().isAfter(dpFimSemestre.getValue())) {
                    exibirFeedback(lblFeedbackCalendario, "Erro: Data de início não pode ser após o fim!", true);
                    return;
                }
            }

            for (Sprint sprint : listaSprintsFX) {
                if (sprint.getId_sprint() == null || sprint.getId_sprint() == 0) {
                    sprintDAO.inserirSprint(sprint);
                } else {
                    sprintDAO.atualizarSprint(sprint);
                }
            }
            carregarDadosSprints();
            exibirFeedback(lblFeedbackCalendario, "Calendário atualizado com sucesso!", false);
        } catch (Exception e) {
            exibirFeedback(lblFeedbackCalendario, "Falha ao salvar: " + e.getMessage(), true);
        }
    }

    // ═══════════════════════════════════════════════
    // LÓGICA DA ABA 2 — BLOQUEIOS E CANCELAMENTOS
    // ═══════════════════════════════════════════════

    private void configurarAbaBloqueios() {
        // Popula os meses em PT-BR
        for (Month month : Month.values()) {
            cbMes.getItems().add(month.getDisplayName(TextStyle.FULL, new Locale("pt", "BR")));
        }
        cbMes.setOnAction(e -> atualizarGridDias(cbMes.getSelectionModel().getSelectedIndex() + 1));

        cbTurno.setItems(FXCollections.observableArrayList("Dia inteiro", "Manhã", "Noite"));
        cbTurno.getSelectionModel().selectFirst();

        // Customiza a exibição das células do ListView para mostrar os dados do CancelamentoAdm legíveis
        listCancelamentos.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(CancelamentoAdm item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String abrangencia = item.getDia_inteiro() ? "Dia Inteiro" : item.getTurno();
                    setText(String.format("%s - %s [%s] Motivo: %s",
                            item.getId_cancelamento_adm(), item.getData(), abrangencia, item.getMotivo()));
                }
            }
        });

        listCancelamentos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnDeletar.setDisable(newVal == null);
        });
    }

    private void atualizarGridDias(int mesId) {
        gridDias.getChildren().clear();
        if (mesId < 1 || mesId > 12) return;

        int anoAtual = LocalDate.now().getYear();
        LocalDate primeiroDiaDoMes = LocalDate.of(anoAtual, mesId, 1);
        int diasNoMes = primeiroDiaDoMes.lengthOfMonth();

        int coluna = 0;
        int linha = 0;

        for (int dia = 1; dia <= diasNoMes; dia++) {
            final int diaAtual = dia;
            Button btnDia = new Button(String.valueOf(dia));
            btnDia.setPrefSize(40, 40);

            btnDia.setOnAction(e -> {
                dataSelecionadaNoCalendario = LocalDate.of(anoAtual, mesId, diaAtual);
                boxConfigCancelamento.setVisible(true);
                boxConfigCancelamento.setManaged(true);
                tfMotivoCancelamento.setPromptText("Motivo para o dia " + dataSelecionadaNoCalendario);
            });

            gridDias.add(btnDia, coluna, dia);
            coluna++;
            if (coluna > 6) {
                coluna = 0;
                linha++;
            }
        }
    }

    private void carregarListaCancelamentos() {
        try {
            listaCancelamentosFX.clear();
            var doBanco = cancelamentoDAO.listarCancelamentos(ID_SEMESTRE_ATUAL);
            listaCancelamentosFX.addAll(doBanco);
            listCancelamentos.setItems(listaCancelamentosFX);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar lista de cancelamentos visuais: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancelarDatas() {
        if (dataSelecionadaNoCalendario == null) {
            exibirFeedback(lblFeedbackCancelamento, "Selecione um dia no grid.", true);
            return;
        }

        String motivo = tfMotivoCancelamento.getText().trim();
        if (motivo.isEmpty()) {
            exibirFeedback(lblFeedbackCancelamento, "O campo Motivo é obrigatório.", true);
            return;
        }

        String turnoSelecionado = cbTurno.getValue();
        boolean ehDiaInteiro = "Dia inteiro".equalsIgnoreCase(turnoSelecionado);

        // Instancia a Entidade Oficial do projeto
        CancelamentoAdm novoCancelamento = new CancelamentoAdm();
        novoCancelamento.setAdm_id(ID_ADM_LOGADO);
        novoCancelamento.setSemestre_letivo_id(ID_SEMESTRE_ATUAL);
        novoCancelamento.setData(dataSelecionadaNoCalendario);
        novoCancelamento.setTurno(ehDiaInteiro ? null : turnoSelecionado);
        novoCancelamento.setDia_inteiro(ehDiaInteiro);
        novoCancelamento.setMotivo(motivo);

        try {
            cancelamentoDAO.salvar(novoCancelamento);

            tfMotivoCancelamento.clear();
            boxConfigCancelamento.setVisible(false);
            boxConfigCancelamento.setManaged(false);

            carregarListaCancelamentos();
            exibirFeedback(lblFeedbackCancelamento, "Cancelamento salvo com sucesso!", false);
        } catch (SQLException e) {
            exibirFeedback(lblFeedbackCancelamento, "Erro ao salvar no banco.", true);
        }
    }

    @FXML
    public void handleDeletarCancelamento() {
        CancelamentoAdm selecionado = listCancelamentos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            try {
                cancelamentoDAO.excluir(selecionado.getId_cancelamento_adm());
                carregarListaCancelamentos();
                exibirFeedback(lblFeedbackCancelamento, "Cancelamento excluído com sucesso.", false);
            } catch (SQLException e) {
                exibirFeedback(lblFeedbackCancelamento, "Erro ao excluir do banco de dados.", true);
            }
        }
    }

    private void exibirFeedback(Label label, String mensagem, boolean isErro) {
        label.setText(mensagem);
        label.setVisible(true);
        label.setManaged(true);
        label.setStyle(isErro ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}