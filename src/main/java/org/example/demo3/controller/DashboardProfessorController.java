package org.example.demo3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.example.demo3.dao.*;
import org.example.demo3.dto.CronogramaExibicaoDTO;
import org.example.demo3.entity.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardProfessorController {

    // --- COMPONENTES FXML ---

    //CRONOGRAMA AQUI
    @FXML private ComboBox<String> cbFiltroCurso;
    @FXML private ComboBox<String> cbFiltroSemestre;

    @FXML private Button btnGerarCronograma;

    @FXML private TableView<CronogramaExibicaoDTO> tvCronograma;
    @FXML private TableColumn<CronogramaExibicaoDTO, Date> colData;
    @FXML private TableColumn<CronogramaExibicaoDTO, String> colDisciplina;
    @FXML private TableColumn<CronogramaExibicaoDTO, String> colAvaliacao;
    @FXML private TableColumn<CronogramaExibicaoDTO, String> colTema;
    @FXML private TableColumn<CronogramaExibicaoDTO, Integer> colQtdAulas;
    @FXML private TableColumn<CronogramaExibicaoDTO, String> colStatus;
    @FXML private TableColumn<CronogramaExibicaoDTO, String> colMotivo;

    // Mock ID
    private final Integer MOCK_ID_PROFESSOR = 5;
    private Integer idCursoAtual = null;
    private Integer idSemestreAtual = null;


    //TEMAS E DEPENDENCIAS AQUI
    @FXML private ComboBox<Disciplina> cbDisciplinaTema;
    @FXML private TextField txtNomeTema;
    @FXML private Spinner<Integer> spPrioridadeTema;
    @FXML private Spinner<Integer> spMinAulasTema;
    @FXML private Spinner<Integer> spMaxAulasTema;
    @FXML private CheckBox chkTemaOpcional, chkAvaliacao;
    @FXML private ComboBox<Tema> cbTemaDependencia;
    @FXML private Label lblDependenciasPendentes;

    @FXML private TableView<Tema> tbTemas;
    @FXML private TableColumn<Tema, String> colTemaDisciplina, colTemaTema, colTemaAvaliacao, colTemaOpcional, colTemaDependencias;
    @FXML private TableColumn<Tema, Integer> colTemaMinHoras, colTemaMaxHoras, colTemaPrioridade;

    // --- LÓGICA E DADOS ---
    private final TemaDAO temaDAO = new TemaDAO();
    private final ObservableList<Tema> listaTemas = FXCollections.observableArrayList();
    private final ObservableList<Tema> listaComboDependencias = FXCollections.observableArrayList();
    private final ObservableList<Tema> dependenciasPendentes = FXCollections.observableArrayList();

    private GeradorCronograma gerador = new GeradorCronograma();

    @FXML
    public void initialize() {
        //CRONOGRAMA
        configurarColunasCronograma();
        inserirCursosCBox();

        //TEMAS E DEPENDENCIAS
        configurarTabela();
        configurarSpinners();
        configurarEstilos();
        configurarConverters();
        configurarVisualizacaoCombos();
        carregarDadosIniciais();
    }

    public void configurarColunasCronograma(){
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colDisciplina.setCellValueFactory(new PropertyValueFactory<>("nomeDisciplina"));
        colTema.setCellValueFactory(new PropertyValueFactory<>("nomeTema"));
        colQtdAulas.setCellValueFactory(new PropertyValueFactory<>("qtdAulas"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colAvaliacao.setCellValueFactory(cellData -> {
            boolean ehAvaliacao = cellData.getValue().isAvaliacao();
            return new SimpleStringProperty(ehAvaliacao ? "Sim" : "Não");
        });
    }

    public void inserirCursosCBox(){
        try {
            CursoDAO cursoDAO = new CursoDAO();
            List<String> cursos = cursoDAO.listarCursosProfessor(MOCK_ID_PROFESSOR);

            ObservableList<String> nomesCursos = FXCollections.observableArrayList(cursos);

            cbFiltroCurso.setItems(nomesCursos);

        } catch (SQLException e){
            System.err.println("Erro ao listar cursos de um professor: " + e.getMessage());
        }
    }

    public void selecionarCurso(){
        if (cbFiltroCurso.getValue() != null){
            try {
                CursoDAO cursoDAO = new CursoDAO();

                idCursoAtual = cursoDAO.getIdCurso(cbFiltroCurso.getValue());
                inserirSemestresCBox();
            } catch (SQLException e){
                System.err.println("Erro ao recuperar ID de um curso: " + e.getMessage());
            }

        }
    }

    public void inserirSemestresCBox(){
        try {
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO();
            List<String> semestres = disciplinaDAO.listarSemestresProfessorCurso(MOCK_ID_PROFESSOR, idCursoAtual);

            ObservableList<String> nomesSemestres = FXCollections.observableArrayList(semestres);

            cbFiltroSemestre.setItems(nomesSemestres);
        } catch (SQLException e){
            System.err.println("Erro ao listar semestres de um professor para um curso: " + e.getMessage());
        }
    }

    public void selecionarSemestre(){
        if (cbFiltroSemestre.getValue() != null){
            idSemestreAtual = Integer.parseInt(cbFiltroSemestre.getValue());

            DisciplinaDAO disciplinaDAO = new DisciplinaDAO();

            List<Tema> dadosBanco = disciplinaDAO.listarTemasPorProfessorECurso
                    (MOCK_ID_PROFESSOR, idCursoAtual, idSemestreAtual);
            ObservableList<Tema> listaObservable = FXCollections.observableArrayList(dadosBanco);
            tbTemas.setItems(listaObservable);

        }
    }

    public void preencherCronograma(){
        CronogramaDAO cronogramaDAO = new CronogramaDAO();
        try {
            List<CronogramaExibicaoDTO> dadosBanco = cronogramaDAO.listarCronogramaCursoSemestre
                    (MOCK_ID_PROFESSOR, idCursoAtual, idSemestreAtual);
            ObservableList<CronogramaExibicaoDTO> listaObservable = FXCollections.observableArrayList(dadosBanco);
            tvCronograma.setItems(listaObservable);
        } catch (SQLException e){
            System.err.println("Erro ao preencher cronograma de um professor, curso e semestre: " + e.getMessage());
        }

    }

    public void gerarCronograma(){
        try {
            gerador.gerarCronograma(MOCK_ID_PROFESSOR, idCursoAtual, idSemestreAtual);
            preencherCronograma();
        } catch (SQLException e){

        }

    }


    //TEMAS E DEPENDENCIAS

    // --- CONFIGURAÇÕES DE UI (INTERFACE) ---
    private void configurarVisualizacaoCombos() {
        // Callback para evitar repetição de código na criação de células
        cbDisciplinaTema.setButtonCell(criarListCellDisciplina());
        cbTemaDependencia.setButtonCell(criarListCellTema());
    }

    private ListCell<Disciplina> criarListCellDisciplina() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Disciplina item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? cbDisciplinaTema.getPromptText() : item.getNome());
            }
        };
    }

    private ListCell<Tema> criarListCellTema() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Tema item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? cbTemaDependencia.getPromptText() : item.getNome());
            }
        };
    }

    private void configurarConverters() {
        cbDisciplinaTema.setConverter(new StringConverter<>() {
            @Override public String toString(Disciplina d) { return (d == null) ? "" : d.getNome(); }
            @Override public Disciplina fromString(String s) { return null; }
        });

        cbTemaDependencia.setConverter(new StringConverter<>() {
            @Override public String toString(Tema t) { return (t == null) ? "" : t.getNome(); }
            @Override public Tema fromString(String s) { return null; }
        });
    }

    private void configurarTabela() {
        colTemaDisciplina.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomeDisciplina()));
        colTemaTema.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTemaMinHoras.setCellValueFactory(new PropertyValueFactory<>("qtdMinAulas"));
        colTemaMaxHoras.setCellValueFactory(new PropertyValueFactory<>("qtdMaxAulas"));
        colTemaPrioridade.setCellValueFactory(new PropertyValueFactory<>("prioridade"));
        colTemaOpcional.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isEhOpcional() ? "Sim" : "Não"));
        colTemaAvaliacao.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isEhAvaliacao() ? "Sim" : "Não"));

        // Renderiza a lista de dependências como uma String separada por vírgulas na tabela
        colTemaDependencias.setCellValueFactory(data -> {
            List<Tema> deps = data.getValue().getDependencias();
            if (deps == null || deps.isEmpty()) return new SimpleStringProperty("-");
            return new SimpleStringProperty(deps.stream().map(Tema::getNome).collect(Collectors.joining(", ")));
        });

        tbTemas.setItems(null);
        cbTemaDependencia.setItems(listaComboDependencias);
    }

    private void configurarSpinners() {
        spPrioridadeTema.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        spMinAulasTema.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        spMaxAulasTema.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 4));
    }

    private void configurarEstilos() {
        colTemaAvaliacao.setStyle("-fx-alignment: CENTER;");
        colTemaOpcional.setStyle("-fx-alignment: CENTER;");
    }

    // --- AÇÕES DO USUÁRIO (HANDLERS) ---

    @FXML
    private void handleAdicionarDependencia() {
        Tema temaSelecionado = cbTemaDependencia.getSelectionModel().getSelectedItem();

        if (temaSelecionado == null) {
            mostrarAlerta("Atenção", "Selecione um tema para adicionar como dependência.");
            return;
        }

        // Regra de Negócio: Não permite auto-dependência
        if (temaSelecionado.getNome().equalsIgnoreCase(txtNomeTema.getText().trim())) {
            mostrarAlerta("Erro", "Um tema não pode ser dependente de si mesmo.");
            return;
        }

        // Regra de Negócio: Não permite duplicatas na lista de pendentes
        boolean jaAdicionado = dependenciasPendentes.stream().anyMatch(t -> t.getId() == temaSelecionado.getId());
        if (jaAdicionado) {
            mostrarAlerta("Atenção", "Esse tema já foi adicionado.");
            return;
        }

        dependenciasPendentes.add(temaSelecionado);
        atualizarExibicaoDependenciasPendentes();

        // Reseta o combo de seleção para a próxima adição
        cbTemaDependencia.getSelectionModel().clearSelection();
        cbTemaDependencia.setValue(null);
    }

    @FXML
    private void handleRemoverDependenciaSelecionada() {
        //CAPTURA A O TEMA SELECIONADO (AINDA NÃO ADICIONADO) NO COMBOBOX
        Tema temaParaRemover = cbTemaDependencia.getSelectionModel().getSelectedItem();

        if (temaParaRemover == null) {
            mostrarAlerta("Atenção", "Selecione no campo 'Selecione um tema' qual dependência você deseja remover.");
            return;
        }

        //REMOVE O TEMA SELECIONADO NO COMBOBOX DA LISTA DE TEMAS DEPENDENTES
        boolean removido = dependenciasPendentes.removeIf(t -> t.getId() == temaParaRemover.getId());

        if (removido) {
            //SE REMOVEU, O COMBOBOX É REINICIADO PARA NOVA ADIÇÃO/REMOÇÃO DE DEPENDÊNCIA
            atualizarExibicaoDependenciasPendentes();
            cbTemaDependencia.getSelectionModel().clearSelection();
            cbTemaDependencia.setValue(null);
        } else {
            //CASO O USUÁRIO TENTE REMOVER UM TEMA QUE AINDA NÃO FOI ADICIONADO
            mostrarAlerta("Informação", "Este tema não consta na lista de dependências acumuladas.");
        }
    }

    @FXML
    private void handleSalvarTema() {
        try {
            Disciplina discSelecionada = cbDisciplinaTema.getSelectionModel().getSelectedItem();
            String nomeTema = txtNomeTema.getText().trim();

            if (discSelecionada == null || nomeTema.isEmpty()) {
                mostrarAlerta("Atenção", "Preencha a disciplina e o nome do tema!");
                return;
            }

            // Validação de segurança: Impede salvar se houver seleção no combo mas o usuário esqueceu de clicar no "+"
            if (cbTemaDependencia.getSelectionModel().getSelectedItem() != null) {
                mostrarAlerta("Ação Pendente", "Você selecionou um tema mas não clicou em 'Adicionar'. O campo foi limpo.");
                cbTemaDependencia.getSelectionModel().clearSelection();
                cbTemaDependencia.setValue(null);
                return;
            }

            int min = spMinAulasTema.getValue();
            int max = spMaxAulasTema.getValue();
            if (min > max) {
                mostrarAlerta("Erro de Consistência", "Mínimo (" + min + ") não pode ser maior que máximo (" + max + ")!");
                return;
            }

            // Montagem do Objeto
            Tema novoTema = new Tema();
            novoTema.setNome(nomeTema);
            novoTema.setDisciplinaId(discSelecionada.getId());
            novoTema.setPrioridade(spPrioridadeTema.getValue());
            novoTema.setQtdMinAulas(min);
            novoTema.setQtdMaxAulas(max);
            novoTema.setEhOpcional(chkTemaOpcional.isSelected());
            novoTema.setEhAvaliacao(chkAvaliacao.isSelected());
            novoTema.setDependencias(new ArrayList<>(dependenciasPendentes));

            temaDAO.salvar(novoTema);

            mostrarAlerta("Sucesso", "Tema salvo com " + dependenciasPendentes.size() + " dependência(s)!");
            carregarTodosOsTemas();
            limparFormulario();

        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao salvar no banco: " + e.getMessage());
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void atualizarExibicaoDependenciasPendentes() {
        if (dependenciasPendentes.isEmpty()) {
            lblDependenciasPendentes.setText("Nenhuma dependência selecionada.");
            lblDependenciasPendentes.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        } else {
            String nomes = dependenciasPendentes.stream().map(Tema::getNome).collect(Collectors.joining(", "));
            lblDependenciasPendentes.setText("Dependências acumuladas: " + nomes);
            lblDependenciasPendentes.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        }
    }

    private void limparFormulario() {
        txtNomeTema.clear();
        chkTemaOpcional.setSelected(false);
        chkAvaliacao.setSelected(false);
        cbDisciplinaTema.getSelectionModel().clearSelection();
        cbDisciplinaTema.setValue(null);
        cbTemaDependencia.getSelectionModel().clearSelection();
        cbTemaDependencia.setValue(null);
        spPrioridadeTema.getValueFactory().setValue(1);
        spMinAulasTema.getValueFactory().setValue(2);
        spMaxAulasTema.getValueFactory().setValue(4);
        dependenciasPendentes.clear();
        atualizarExibicaoDependenciasPendentes();
        txtNomeTema.requestFocus();
    }

    // --- CARREGAMENTO DE DADOS (DAO) ---

    private void carregarDadosIniciais() {
            carregarDisciplinas();
        carregarTodosOsTemas();
    }

    private void carregarDisciplinas() {
        try { cbDisciplinaTema.setItems(FXCollections.observableArrayList(temaDAO.buscarTodasDisciplinasPorProfessor(MOCK_ID_PROFESSOR))); }
        catch (SQLException e) { mostrarAlerta("Erro", e.getMessage()); }
    }

    private void carregarTodosOsTemas() {
        try {
            List<Tema> todos = temaDAO.buscarTodos();
            listaTemas.setAll(todos);
            listaComboDependencias.setAll(todos); // Alimenta o combo de dependências com os temas existentes
        } catch (SQLException e) { mostrarAlerta("Erro", e.getMessage()); }
    }


    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}