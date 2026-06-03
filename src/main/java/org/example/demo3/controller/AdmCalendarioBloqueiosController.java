package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;
import org.example.demo3.DatabaseConnection;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.*;
import org.example.demo3.entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

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
    @FXML private FlowPane flowHorarios;
    @FXML private VBox painelHorarios;
    @FXML private CheckBox checkFeriado;

    private final SprintDAO sprintDAO = new SprintDAO();
    private final CancelamentoAdmDAO cancelamentoDAO = new CancelamentoAdmDAO();
    private final DataBloqueadaDAO databDao = new DataBloqueadaDAO();
    private final SemestreLetivoDAO slDao = new SemestreLetivoDAO();
    private final TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();

    private final UsuarioAtual logado = UsuarioAtual.getInstancia();

    private final ObservableList<Sprint> listaSprintsFX = FXCollections.observableArrayList();
    private final ObservableList<CancelamentoAdm> listaCancelamentosFX = FXCollections.observableArrayList();
    private LocalDate dataSelecionadaNoCalendario;

    private List<String> listaMeses;
    private SemestreLetivo slAtual;
    private String mesSelecionado;
    private List<LocalDate> listaDiaBotaoPressionado = new ArrayList<>();
    private List<TemplateHorarioTurno> listaHorariosSelecionados = new ArrayList<>();
    private Map<LocalDate, String> mapaEstadoBotaoDia = new LinkedHashMap<>();

    private int idSemestreAtual;
    private int ID_ADM_LOGADO;

    @FXML
    public void initialize() {
        //REMOVER ESSA VARIAVEIS DEPOIS:
        logado.setId_usuario(1);
        logado.setTipo("ADM");

        ID_ADM_LOGADO = logado.getId_usuario();
        configurarTabelaSprints();

        int anoFiltro = (logado.getAno() != null) ? logado.getAno() : LocalDate.now().getYear();
        int numeroSemestre = (logado.getAnoSemestre() != null) ? logado.getAnoSemestre() : 1;

        carregarDadosPorAnoESemestre(anoFiltro, numeroSemestre);

        carregarMesesCancelamento(anoFiltro, numeroSemestre);
    }

    // --- METODOS DA ABA CALENDARIO DO SEMESTRE ---

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

    // --- METODOS DA ABA BLOQUEIOS E CANCELAMENTOS ---

    public List<String> listarNomesDosMeses(LocalDate inicio, LocalDate fim) {
        listaMeses = new ArrayList<>();

        LocalDate atual = inicio.withDayOfMonth(1);

        while (!atual.isAfter(fim.withDayOfMonth(1))) {
            String nomeMes = atual.getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

            nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1);
            listaMeses.add(nomeMes);
            atual = atual.plusMonths(1);
        }

        return listaMeses;
    }

    public void carregarMesesCancelamento(int ano, int anoSemestre){
        try {
            idSemestreAtual = slDao.getIdSemestreLetivo(ano, anoSemestre);
            slAtual = slDao.listarSLPorId(idSemestreAtual);

            LocalDate dataInicio = slAtual.getData_inicio();
            LocalDate dataFim = slAtual.getData_fim();
            ObservableList opcoesMeses = FXCollections.observableArrayList
                    (listarNomesDosMeses(dataInicio, dataFim));
            cbMes.setItems(opcoesMeses);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static Month converterNomeParaMonth(String nome) {
        for (Month m : Month.values()) {
            String nomePt = m.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            if (nomePt.equalsIgnoreCase(nome)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Mês inválido: " + nome);
    }

    public void configurarComboboxCbTurno(){
        ObservableList<String> opcoesTurno = FXCollections.observableArrayList("Dia inteiro", "manha", "noite");
        cbTurno.setItems(opcoesTurno);
    }

    public void recupearCancelamentos(){
        List<LocalDate> datasBloqueadasRecuperadas = databDao.listarDatasBloqueadasPorSemestre(idSemestreAtual);
        List<CancelamentoAdm> datasCanceladasRecuperadas = cancelamentoDAO.listarPorSemestre(idSemestreAtual);

        for (LocalDate data: datasBloqueadasRecuperadas){
            mapaEstadoBotaoDia.put(data, "-fx-border-color: transparent; -fx-background-color: #FFA500;");
        }

        for (CancelamentoAdm cadm: datasCanceladasRecuperadas){
            LocalDate data = cadm.getData();
            if (!mapaEstadoBotaoDia.containsKey(data)){
                mapaEstadoBotaoDia.put(data, "-fx-border-color: transparent; -fx-background-color: #FF0000;");
            }
        }
    }

    @FXML
    public void handleFeriados(){
        if (checkFeriado.isSelected()){
            cbTurno.setValue("Dia inteiro");
            cbTurno.setDisable(true);
            painelHorarios.setManaged(false);
            painelHorarios.setVisible(false);
            listaHorariosSelecionados.clear();
        } else {
            cbTurno.setDisable(false);
            painelHorarios.setManaged(false);
            painelHorarios.setVisible(false);
        }
    }

    @FXML
    public void handleSelecaoTurno(){
        if (cbTurno.getValue() == null) return;
        flowHorarios.getChildren().clear();

        painelHorarios.setManaged(true);
        painelHorarios.setVisible(true);

        if (!cbTurno.getValue().equals("Dia inteiro")){
            try{
                List<TemplateHorarioTurno> listaHorarios = thtDao.listarPorTurno(cbTurno.getValue());
                for (TemplateHorarioTurno tht: listaHorarios){
                    CheckBox horario = new CheckBox();
                    String hora_inicio = String.valueOf(tht.getHora_inicio());
                    String hora_fim = String.valueOf(tht.getHora_fim());

                    horario.setText(hora_inicio + " - "+ hora_fim);
                    flowHorarios.getChildren().add(horario);
                    listaHorariosSelecionados.add(tht);
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleCancelamentoSelecaoMes(){
        if (cbMes.getValue() == null) return;
        gridDias.getChildren().clear();
        if (mapaEstadoBotaoDia.isEmpty()) recupearCancelamentos();

        Month mesEnum = converterNomeParaMonth(cbMes.getValue());
        int anoRef = slAtual.getData_inicio().getYear();

        LocalDate dataInicioReal = slAtual.getData_inicio();
        LocalDate atual;

        if (mesEnum == dataInicioReal.getMonth()) {
            atual = dataInicioReal;
        } else {
            atual = LocalDate.of(anoRef, mesEnum, 1);
        }

        LocalDate dataFinalReal = slAtual.getData_fim();
        LocalDate ultimoDiaDoMes = atual.withDayOfMonth(atual.lengthOfMonth());

        LocalDate dataLimiteLoop;
        if (dataFinalReal.getMonth() == mesEnum) {
            dataLimiteLoop = dataFinalReal;
        } else {
            dataLimiteLoop = ultimoDiaDoMes;
        }

        int pos_linha = 0;
        int pos_coluna = 0;

        // 4. Loop Robusto: Enquanto a data atual não passar da data limite
        while (!atual.isAfter(dataLimiteLoop)) {
            if (pos_coluna > 5) {
                pos_linha += 1;
                pos_coluna = 0;
            }

            String numeroData = String.valueOf(atual.getDayOfMonth());
            Button teste = new Button(numeroData);

            if (listaDiaBotaoPressionado.contains(atual)) {
                teste.setStyle("-fx-background-color: #FFFF00;");
            } else {
                teste.setStyle("");
            }

            LocalDate dataDesteBotao = LocalDate.of(anoRef, mesEnum, Integer.parseInt(teste.getText()));

            teste.setOnAction(e -> {
                if (listaDiaBotaoPressionado.contains(dataDesteBotao)) {
                    teste.setStyle("");
                    listaDiaBotaoPressionado.remove(dataDesteBotao);
                } else {
                    teste.setStyle("-fx-background-color: #FFFF00;");
                    listaDiaBotaoPressionado.add(dataDesteBotao);
                }
                if (listaDiaBotaoPressionado.isEmpty()){
                    boxConfigCancelamento.setManaged(false);
                    boxConfigCancelamento.setVisible(false);
                } else {
                    boxConfigCancelamento.setManaged(true);
                    boxConfigCancelamento.setVisible(true);
                    configurarComboboxCbTurno();
                }
            });

            teste.setId("btn-" + mesEnum.name() + "-" + numeroData);

            gridDias.add(teste, pos_coluna, pos_linha);

            pos_coluna++;
            atual = atual.plusDays(1);
        }
    }

    @FXML
    public void handleCancelarDatas() {
        String motivo = tfMotivoCancelamento.getText();
        List<DataBloqueada> listaDatasBloqueadas = new ArrayList<>();
        try{
            if (checkFeriado.isSelected()){
                for (LocalDate data: listaDiaBotaoPressionado){
                    DataBloqueada datab = new DataBloqueada();
                    datab.setData(data);
                    datab.setAdmId(logado.getId_usuario());
                    datab.setMotivo(motivo);
                    datab.setRecorrente(true);
                    datab.setSemestreLetivoId(idSemestreAtual);
                    listaDatasBloqueadas.add(datab);
                }
                databDao.salvarEmLote(listaDatasBloqueadas);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    @FXML
    public void handleDeletarCancelamento() {

    }

    // --- METODOS UTILITARIOS GENERICOS ---

    private void exibirFeedback(Label label, String mensagem, boolean isErro) {
        label.setText(mensagem);
        label.setVisible(true);
        label.setManaged(true);
        label.setStyle(isErro ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
