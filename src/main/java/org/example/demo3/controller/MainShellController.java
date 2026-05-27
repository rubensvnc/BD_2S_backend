package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import org.example.demo3.DatabaseConnection;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.CursoDAO;
import org.example.demo3.dao.DisciplinaDAO;
import org.example.demo3.dao.SemestreLetivoDAO;
import org.example.demo3.entity.Curso;
import org.example.demo3.entity.Disciplina;
import org.example.demo3.entity.SemestreLetivo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainShellController {
    @FXML private ComboBox<String> cbAno;
    @FXML private ToggleButton tbSem1;
    @FXML private ToggleButton tbSem2;
    @FXML private ComboBox<String> cbCurso;
    @FXML private ComboBox<String> cbSemestreCurso;
    @FXML private ComboBox<String> cbDisciplina;
    @FXML private Label lblNomeUsuario;
    @FXML private Label lblPerfilUsuario;
    @FXML private Label bannerReadOnly;
    @FXML private Label lblCurso;
    @FXML private Label lblSemestreCurso;
    @FXML private Label lblDisciplina;
    @FXML private VBox menuLateral;
    @FXML private VBox secaoAdm;
    @FXML private VBox secaoCoordenador;
    @FXML private VBox secaoProfessor;
    @FXML private StackPane areaConteudo;

    private List<SemestreLetivo> listaSl;
    private List<Disciplina> listaD;
    private List<Curso> listaCursos;

    private List<Integer> ordemIdDisciplinas = new ArrayList<>();

    UsuarioAtual logado = UsuarioAtual.getInstancia();

    @FXML
    public void initialize() {
        configurarResetInicial();

        // Simulação de login
        logado.setId_usuario(1);
        logado.setTipo("ADM");

        configurarInterfacePorPerfil(logado.getTipo());
        configurarValoresPreProgramados();
        processarDadosAnos();
    }

    private void configurarPreValoresAnos(){
        processarDadosAnos();
        popularComboBoxAnos();
        cbAno.setValue(listaSl.getLast().getAno().toString());
        logado.setAno(Integer.parseInt(cbAno.getValue()));
    }

    private void configurarPreValoresAnoSemestre(){
        if (listaSl.getLast().getNumero_semestre().equals(1)){
            tbSem1.setDisable(false);
            tbSem1.setSelected(true);
            logado.setAnoSemestre(1);
        } else {
            tbSem2.setDisable(false);
            tbSem2.setSelected(true);
            logado.setAnoSemestre(2);
        }
    }

    private void configurarPreValoresCursos(){
        try {
            CursoDAO cDAO = new CursoDAO();

            processarDadosCursos();
            popularComboboxCursos();
            cbCurso.setValue(listaCursos.getLast().getNome());
            logado.setIdCurso(cDAO.listarIdCurso(cbCurso.getValue()));

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void configurarPreValoresCursoSemestresEDisciplinas(){
        int index;

        processarDadosCursoSemestresEDisciplinas();
        popularComboboxCursoSemestres();
        cbSemestreCurso.setValue(listaD.getLast().getSemestre_curso().toString());
        logado.setSemestreCurso(Integer.parseInt(cbSemestreCurso.getValue()));
        popularComboboxDisciplinas();
        cbDisciplina.setValue(listaD.getLast().getNome());

        index = cbDisciplina.getSelectionModel().getSelectedIndex();
        logado.setIdDisciplina(ordemIdDisciplinas.get(index));
    }


    private void configurarValoresPreProgramados(){
        configurarPreValoresAnos();
        configurarPreValoresAnoSemestre();

        if (logado.getTipo().equals("PROF")){
            configurarPreValoresCursos();
            configurarPreValoresCursoSemestresEDisciplinas();
        }

    }

    private void configurarResetInicial() {
        tbSem1.setDisable(true);
        tbSem2.setDisable(true);
    }

    private void configurarInterfacePorPerfil(String tipo) {
        esconderTodasSecoes();

        if ("PROF".equals(tipo)) {
            carregarConteudo("/prof_temas.fxml");
            exibirSecao(secaoProfessor);
        } else if ("COORD".equals(tipo)) {
            carregarConteudo("/coord_painel.fxml");
            exibirSecao(secaoCoordenador);
            configurarVisibilidadeFiltros(false);
        } else if ("ADM".equals(tipo)) {
            carregarConteudo("/adm_cursos_horarios.fxml");
            exibirSecao(secaoAdm);
            configurarVisibilidadeFiltros(false);
        }
    }

    private void processarDadosAnos() {
        SemestreLetivoDAO slDao = new SemestreLetivoDAO();
        try {
            listaSl = switch (logado.getTipo()) {
                case "PROF" -> slDao.listarProfessorAnoESemestreAno(logado.getId_usuario());
                case "COORD" -> slDao.listarCoordenadorAnoESemestreAno(logado.getId_usuario());
                case "ADM" -> slDao.listarAdmsAnoESemestreAno();
                default -> new ArrayList<>();
            };

            if (listaSl != null && !listaSl.isEmpty()) {
                popularComboBoxAnos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processarDadosCursos(){
        CursoDAO cDao = new CursoDAO();
        try {
            listaCursos = cDao.listarCursosProfessor(
                    logado.getId_usuario(), logado.getAno(), logado.getAnoSemestre());

            if (listaCursos != null && !listaCursos.isEmpty()) {
                popularComboboxCursos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processarDadosCursoSemestresEDisciplinas(){
        DisciplinaDAO dDao = new DisciplinaDAO();
        try{
            listaD = dDao.listarDisciplinasCurso(
                    logado.getId_usuario(), logado.getAno(),
                    logado.getAnoSemestre(), logado.getIdCurso());

            if (listaD != null && !listaD.isEmpty()) {
                popularComboboxCursoSemestres();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    private void popularComboBoxAnos() {
        ObservableList<String> opcoesAno = FXCollections.observableArrayList();
        for (SemestreLetivo sl : listaSl) {
            String anoStr = sl.getAno().toString();
            if (!opcoesAno.contains(anoStr)) {
                opcoesAno.add(anoStr);
            }
        }
        cbAno.setItems(opcoesAno);
    }

    private void popularComboboxCursos(){
        ObservableList<String> opcoesCurso = FXCollections.observableArrayList();
        for (Curso c : listaCursos) {
            opcoesCurso.add(c.getNome());
        }
        cbCurso.setItems(opcoesCurso);
    }

    private void popularComboboxCursoSemestres(){
        ObservableList<String> opcoesSemestreCurso = FXCollections.observableArrayList();
        for (Disciplina d: listaD){
            if (!opcoesSemestreCurso.contains(d.getSemestre_curso().toString())) {
                opcoesSemestreCurso.add(d.getSemestre_curso().toString());
            }
        }
        cbSemestreCurso.setItems(opcoesSemestreCurso);
    }

    private void popularComboboxDisciplinas(){
        ObservableList<String> opcoesDisciplina = FXCollections.observableArrayList();
        ordemIdDisciplinas.clear();

        for (Disciplina d: listaD){
            if (d.getSemestre_curso().equals(logado.getSemestreCurso())){
                opcoesDisciplina.add(d.getNome());

                ordemIdDisciplinas.add(d.getId_disciplina());
            }
        }
        cbDisciplina.setItems(opcoesDisciplina);
    }

    private void esconderTodasSecoes() {
        VBox[] secoes = {secaoProfessor, secaoCoordenador, secaoAdm};
        for (VBox s : secoes) {
            if (s != null) {
                s.setVisible(false);
                s.setManaged(false);
            }
        }
    }

    private void exibirSecao(VBox secao) {
        secao.setVisible(true);
        secao.setManaged(true);
    }

    private void configurarVisibilidadeFiltros(boolean visivel) {
        lblCurso.setVisible(visivel); lblCurso.setManaged(visivel);
        cbCurso.setVisible(visivel);  cbCurso.setManaged(visivel);

        lblSemestreCurso.setVisible(visivel); lblSemestreCurso.setManaged(visivel);
        cbSemestreCurso.setVisible(visivel);  cbSemestreCurso.setManaged(visivel);

        lblDisciplina.setVisible(visivel); lblDisciplina.setManaged(visivel);
        cbDisciplina.setVisible(visivel);  cbDisciplina.setManaged(visivel);
    }

    private void carregarConteudo(String caminhoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Parent novoConteudo = loader.load();
            areaConteudo.getChildren().clear();
            areaConteudo.getChildren().add(novoConteudo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTrocaAno() {
        logado.setAno(Integer.parseInt(cbAno.getValue()));

        tbSem1.setDisable(true);
        tbSem2.setDisable(true);
        for (SemestreLetivo sl: listaSl){
            if (sl.getAno().equals(logado.getAno())){
                if (sl.getNumero_semestre() == 1){
                    tbSem1.setDisable(false);
                } else if (sl.getNumero_semestre() == 2){
                    tbSem2.setDisable(false);
                }
            }
        }
    }

    @FXML
    public void handleSemestreToggle(ActionEvent event) {
        Object ativador = event.getSource();
        if (ativador == tbSem1){
            logado.setAnoSemestre(1);
            tbSem2.setSelected(false);
        } else {
            logado.setAnoSemestre(2);
            tbSem1.setSelected(false);
        }

        processarDadosCursos();
    }

    @FXML
    public void handleTrocaCurso(){
        String cursoSelecionado = cbCurso.getValue();
        if (cursoSelecionado != null) {
            try {
                CursoDAO cDAO = new CursoDAO();
                logado.setIdCurso(cDAO.listarIdCurso(cursoSelecionado));
                processarDadosCursoSemestresEDisciplinas();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleTrocaSemestreCurso(){
        String valor = cbSemestreCurso.getValue();

        if (valor != null && !valor.isEmpty()) {
            logado.setSemestreCurso(Integer.parseInt(valor));
            popularComboboxDisciplinas();
        }
    }

    @FXML
    public void handleTrocaDisciplina(){
        int index = cbDisciplina.getSelectionModel().getSelectedIndex();

        if (index >= 0 && index < ordemIdDisciplinas.size()) {
            logado.setIdDisciplina(ordemIdDisciplinas.get(index));
        }
    }

    @FXML void handleLogout(ActionEvent event) {
        System.out.println("Botão clicado");
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML void navCalendario() { carregarConteudo("/adm_calendario_bloqueios.fxml"); }
    @FXML void navCursosHorarios() { carregarConteudo("/adm_cursos_horarios.fxml"); }
    @FXML void navCoordenaodresAdms() { carregarConteudo("/adm_coordenadores_adms.fxml"); }
    @FXML void navCoordPainel() { carregarConteudo("/coord_painel.fxml"); }
    @FXML void navTemas() {carregarConteudo("/prof_temas.fxml");}

    @FXML
    void navPlanejamento() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/prof_planejamento_stats_rotina.fxml"));
            Parent novoConteudo = loader.load();

            ProfPlanejamentoController profController = loader.getController();
            profController.setMainShellController(this);

            areaConteudo.getChildren().clear();
            areaConteudo.getChildren().add(novoConteudo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int descobrirIdCursoPorNome(String nomeCurso) {
        String sql = "SELECT id_curso FROM curso WHERE nome = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeCurso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_curso");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int descobrirIdDisciplinaPorNome(String nomeDisciplina) {
        String sql = "SELECT id_disciplina FROM disciplina WHERE nome = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_disciplina");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}