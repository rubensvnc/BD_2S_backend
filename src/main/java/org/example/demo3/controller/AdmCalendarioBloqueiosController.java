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
import org.example.demo3.DatabaseConnection;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.CancelamentoAdmDAO;
import org.example.demo3.dao.SprintDAO;
import org.example.demo3.entity.CancelamentoAdm;
import org.example.demo3.entity.Sprint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class AdmCalendarioBloqueiosController {

    @FXML private DatePicker dpInicioSemestre;
    @FXML private DatePicker dpFimSemestre;
    @FXML private DatePicker dpTcc;
    @FXML private DatePicker dpFeira;
    @FXML private TableView<Sprint> tabelaSprints;
    @FXML private TableColumn<Sprint, Integer> colSprintNum;
    @FXML private TableColumn<Sprint, LocalDate> colSprintInicio;
    @FXML private TableColumn<Sprint, LocalDate> colSprintReview;
    @FXML private Label lblFeedbackCalendario;

    @FXML private ComboBox<String> cbMes;
    @FXML private GridPane gridDias;
    @FXML private VBox boxConfigCancelamento;
    @FXML private ComboBox<String> cbTurno;
    @FXML private TextField tfMotivoCancelamento;
    @FXML private Label lblFeedbackCancelamento;
    @FXML private ListView<CancelamentoAdm> listCancelamentos;
    @FXML private Button btnDeletar;

    private final SprintDAO sprintDAO = new SprintDAO();
    private final CancelamentoAdmDAO cancelamentoDAO = new CancelamentoAdmDAO();
    private final UsuarioAtual logado = UsuarioAtual.getInstancia();

    private final ObservableList<Sprint> listaSprintsFX = FXCollections.observableArrayList();
    private final ObservableList<CancelamentoAdm> listaCancelamentosFX = FXCollections.observableArrayList();
    private LocalDate dataSelecionadaNoCalendario;

    private int idSemestreAtual = 1;
    private int ID_ADM_LOGADO = logado.getId_usuario();

    @FXML
    public void initialize() {
        ID_ADM_LOGADO = logado.getId_usuario();
        configurarTabelaSprints();
        configurarAbaBloqueios();

        int anoFiltro = (logado.getAno() != null) ? logado.getAno() : LocalDate.now().getYear();
        int numeroSemestre = (logado.getAnoSemestre() != null) ? logado.getAnoSemestre() : 1;

        carregarDadosPorAnoESemestre(anoFiltro, numeroSemestre);
    }

    public void carregarDadosPorAnoESemestre(int anoFiltro, int numeroSemestre) {
        listaSprintsFX.clear();

        this.idSemestreAtual = buscarIdSemestreDoBanco(anoFiltro, numeroSemestre);

        buscarEPreencherDatasMacro(this.idSemestreAtual, anoFiltro, numeroSemestre);

        var sprintsBanco = sprintDAO.buscarSprintsPorSemestre(this.idSemestreAtual);

        if (sprintsBanco.isEmpty()) {
            int mesBase = (numeroSemestre == 2) ? 8 : 2;
            for (int i = 1; i <= 3; i++) {
                Sprint s = new Sprint();
                s.setNumero(i);
                s.setSemestre_letivo_id(this.idSemestreAtual);
                s.setData_inicio(LocalDate.of(anoFiltro, mesBase, 10).plusMonths(i - 1));
                s.setData_fim(LocalDate.of(anoFiltro, mesBase, 10).plusMonths(i - 1).plusDays(25));
                s.setData_review(LocalDate.of(anoFiltro, mesBase, 10).plusMonths(i - 1).plusDays(27));
                listaSprintsFX.add(s);
            }
        } else {
            listaSprintsFX.addAll(sprintsBanco);
        }
        tabelaSprints.setItems(listaSprintsFX);
        carregarListaCancelamentos();
    }

    private int buscarIdSemestreDoBanco(int ano, int numeroSemestre) {
        String sql = "SELECT id_semestre_letivo FROM semestre_letivo WHERE ano = ? AND numero_semestre = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ano);
            stmt.setInt(2, numeroSemestre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_semestre_letivo");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar id_semestre_letivo dinamico: " + e.getMessage());
        }
        return -1;
    }

    private void buscarEPreencherDatasMacro(int idSemestre, int anoFiltro, int numeroSemestre) {
        String sql = "SELECT data_inicio, data_fim, data_tg, data_feira FROM semestre_letivo WHERE id_semestre_letivo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSemestre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dpInicioSemestre.setValue(rs.getDate("data_inicio").toLocalDate());
                    dpFimSemestre.setValue(rs.getDate("data_fim").toLocalDate());
                    dpTcc.setValue(rs.getDate("data_tg").toLocalDate());
                    dpFeira.setValue(rs.getDate("data_feira").toLocalDate());
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar datas macro do semestre: " + e.getMessage());
        }

        int mesInicio = (numeroSemestre == 2) ? 8 : 2;
        int mesFim = (numeroSemestre == 2) ? 12 : 6;
        dpInicioSemestre.setValue(LocalDate.of(anoFiltro, mesInicio, 1));
        dpFimSemestre.setValue(LocalDate.of(anoFiltro, mesFim, 30));
        dpTcc.setValue(LocalDate.of(anoFiltro, mesFim, 15));
        dpFeira.setValue(LocalDate.of(anoFiltro, mesFim, 20));
    }

    private void configurarTabelaSprints() {
        tabelaSprints.setEditable(true);
        colSprintNum.setCellValueFactory(new PropertyValueFactory<>("numero"));

        colSprintInicio.setCellValueFactory(new PropertyValueFactory<>("data_inicio"));
        colSprintInicio.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        colSprintInicio.setOnEditCommit(event -> {
            Sprint sprint = event.getRowValue();
            if (sprint != null) {
                sprint.setData_inicio(event.getNewValue());
                tabelaSprints.refresh();
            }
        });

        colSprintReview.setCellValueFactory(new PropertyValueFactory<>("data_review"));
        colSprintReview.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        colSprintReview.setOnEditCommit(event -> {
            Sprint sprint = event.getRowValue();
            if (sprint != null) {
                sprint.setData_review(event.getNewValue());
                tabelaSprints.refresh();
            }
        });
    }

    @FXML
    public void handleSalvarCalendario() {
        if (dpInicioSemestre.getValue() == null || dpFimSemestre.getValue() == null ||
                dpFeira.getValue() == null || dpTcc.getValue() == null) {
            exibirFeedback(lblFeedbackCalendario, "Erro: Datas macro do semestre e marcos obrigatorios sao obrigatorios!", true);
            return;
        }

        LocalDate inicio = dpInicioSemestre.getValue();
        LocalDate fim = dpFimSemestre.getValue();
        LocalDate feira = dpFeira.getValue();
        LocalDate tcc = dpTcc.getValue();

        if (inicio.isAfter(fim)) {
            exibirFeedback(lblFeedbackCalendario, "Erro: Data de inicio nao pode ser apos o fim do semestre!", true);
            return;
        }

        if (feira.isBefore(inicio) || feira.isAfter(fim) || tcc.isBefore(inicio) || tcc.isAfter(fim)) {
            exibirFeedback(lblFeedbackCalendario, "Erro: As datas da feira e do TG devem estar contidas dentro do intervalo do semestre!", true);
            return;
        }

        if (listaSprintsFX.size() != 3) {
            exibirFeedback(lblFeedbackCalendario, "Erro: O sistema exige a configuracao de exatamente 3 sprints obrigatorias!", true);
            return;
        }

        for (Sprint sprint : listaSprintsFX) {
            sprint.setSemestre_letivo_id(this.idSemestreAtual);
            if (sprint.getData_inicio() == null || sprint.getData_review() == null) {
                exibirFeedback(lblFeedbackCalendario, "Erro: Todas as datas das 3 sprints devem estar preenchidas!", true);
                return;
            }
            if (sprint.getData_inicio().isBefore(inicio) || sprint.getData_review().isAfter(fim)) {
                exibirFeedback(lblFeedbackCalendario, "Erro: Os prazos das sprints nao podem extrapolar os limites do semestre!", true);
                return;
            }
            if (sprint.getData_inicio().isAfter(sprint.getData_review())) {
                exibirFeedback(lblFeedbackCalendario, "Erro: Data de inicio da Sprint " + sprint.getNumero() + " nao pode ser posterior a Review!", true);
                return;
            }
            if (sprint.getData_fim() == null) {
                sprint.setData_fim(sprint.getData_review());
            }
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int anoSemestre = inicio.getMonthValue() >= 7 ? 2 : 1;

                String sqlUpsert = """
                    INSERT INTO semestre_letivo
                        (criado_por_adm_id, ano, numero_semestre, data_inicio, data_fim, data_tg, data_feira)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        data_inicio = VALUES(data_inicio),
                        data_fim    = VALUES(data_fim),
                        data_tg     = VALUES(data_tg),
                        data_feira  = VALUES(data_feira)
                """;

                try (PreparedStatement stmtSem = connection.prepareStatement(
                        sqlUpsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmtSem.setInt(1, ID_ADM_LOGADO);
                    stmtSem.setInt(2, inicio.getYear());
                    stmtSem.setInt(3, anoSemestre);
                    stmtSem.setDate(4, java.sql.Date.valueOf(inicio));
                    stmtSem.setDate(5, java.sql.Date.valueOf(fim));
                    stmtSem.setDate(6, java.sql.Date.valueOf(tcc));
                    stmtSem.setDate(7, java.sql.Date.valueOf(feira));
                    stmtSem.executeUpdate();

                    ResultSet keys = stmtSem.getGeneratedKeys();
                    if (keys.next()) {
                        this.idSemestreAtual = keys.getInt(1);
                    } else {
                        this.idSemestreAtual = buscarIdSemestreDoBanco(inicio.getYear(), anoSemestre);
                    }
                }

                for (Sprint sprint : listaSprintsFX) {
                    sprint.setSemestre_letivo_id(this.idSemestreAtual);
                }

                sprintDAO.salvarOuAtualizarSprintsEmLote(connection, listaSprintsFX);

                connection.commit();

                logado.setAno(inicio.getYear());
                logado.setAnoSemestre(anoSemestre);

                carregarDadosPorAnoESemestre(inicio.getYear(), anoSemestre);
                exibirFeedback(lblFeedbackCalendario, "Calendario e as 3 Sprints salvos com sucesso!", false);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            exibirFeedback(lblFeedbackCalendario, "Falha ao salvar lote do calendario: " + e.getMessage(), true);
        }
    }

    private void configurarAbaBloqueios() {
        for (Month month : Month.values()) {
            cbMes.getItems().add(month.getDisplayName(TextStyle.FULL, new Locale("pt", "BR")));
        }
        cbMes.setOnAction(e -> atualizarGridDias(cbMes.getSelectionModel().getSelectedIndex() + 1));

        cbTurno.setItems(FXCollections.observableArrayList("Dia inteiro", "Manhã", "Noite"));
        cbTurno.getSelectionModel().selectFirst();

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

        int anoAtual = (logado.getAno() != null) ? logado.getAno() : LocalDate.now().getYear();
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
            var doBanco = cancelamentoDAO.listarCancelamentos(this.idSemestreAtual);
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

        CancelamentoAdm novoCancelamento = new CancelamentoAdm();
        novoCancelamento.setAdm_id(ID_ADM_LOGADO);
        novoCancelamento.setSemestre_letivo_id(this.idSemestreAtual);
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