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

    UsuarioAtual logado = UsuarioAtual.getInstancia();

    //TAMBÉM FAZ PARTE DO PROF TEMAS CONTROLLER
    private ProfTemasController controllerAtivo;

    @FXML
    public void initialize(){
        tbSem1.setDisable(true);
        tbSem2.setDisable(true);

        logado.setId_usuario(2);
        logado.setTipo("PROF");

        ObservableList<String> opcoesAno = FXCollections.observableArrayList();
        SemestreLetivoDAO slDao = new SemestreLetivoDAO();

        if (logado.getTipo() == "PROF"){
            carregarConteudo("/prof_temas.fxml");
            secaoProfessor.setVisible(true);
            secaoProfessor.setManaged(true);

            try{
                listaSl = slDao.listarProfessorAnoESemestreAno(logado.getId_usuario());
                for (SemestreLetivo sl: listaSl){
                    String anoStr = sl.getAno().toString();
                    if (!opcoesAno.contains(anoStr)) {
                        opcoesAno.add(anoStr);
                    }
                }
                cbAno.setItems(opcoesAno);

            } catch (SQLException e){
                e.printStackTrace();
            }

        } else {
            if (logado.getTipo() == "COORD") {
                carregarConteudo("/coord_painel.fxml");
                secaoCoordenador.setVisible(true);
                secaoCoordenador.setManaged(true);
                lblSemestreCurso.setVisible(false);
                lblSemestreCurso.setManaged(false);
                cbSemestreCurso.setVisible(false);
                cbSemestreCurso.setManaged(false);
                lblDisciplina.setVisible(false);
                lblDisciplina.setManaged(false);
                cbDisciplina.setVisible(false);
                cbDisciplina.setManaged(false);

                try{
                    listaSl = slDao.listarCoordenadorAnoESemestreAno(logado.getId_usuario());
                    for (SemestreLetivo sl: listaSl){
                        if (!opcoesAno.contains(sl.getAno().toString())){
                            opcoesAno.add(sl.getAno().toString());
                        }
                    }
                    cbAno.setItems(opcoesAno);

                } catch (SQLException e){
                    e.printStackTrace();
                }

            } else {
                carregarConteudo("/adm_cursos_horarios.fxml");
                secaoAdm.setVisible(true);
                secaoAdm.setManaged(true);

                lblCurso.setVisible(false);
                lblCurso.setManaged(false);
                cbCurso.setVisible(false);
                cbCurso.setManaged(false);
                lblSemestreCurso.setVisible(false);
                lblSemestreCurso.setManaged(false);
                cbSemestreCurso.setVisible(false);
                cbSemestreCurso.setManaged(false);
                lblDisciplina.setVisible(false);
                lblDisciplina.setManaged(false);
                cbDisciplina.setVisible(false);
                cbDisciplina.setManaged(false);

                try{
                    listaSl = slDao.listarAdmsAnoESemestreAno();
                    for (SemestreLetivo sl: listaSl){
                        if (!opcoesAno.contains(sl.getAno().toString())){
                            opcoesAno.add(sl.getAno().toString());
                        }
                    }
                    cbAno.setItems(opcoesAno);

                } catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void carregarConteudo(String caminhoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Parent novoConteudo = loader.load();
            controllerAtivo = loader.getController();
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
        } else {
            logado.setAnoSemestre(2);
        }

        ObservableList<String> opcoesCurso = FXCollections.observableArrayList();
        CursoDAO cDao = new CursoDAO();

        if (logado.getTipo() == "PROF"){
            try{
                listaCursos = cDao.listarCursosProfessor(logado.getId_usuario(), logado.getAno(), logado.getAnoSemestre());
                for (Curso c: listaCursos){
                    opcoesCurso.add(c.getNome());
                }
                cbCurso.setItems(opcoesCurso);

            } catch (SQLException e){
                e.printStackTrace();
            }
        } else if (logado.getTipo() == "COORD"){
            try{
                Curso c = cDao.buscarCursoCoordenador(logado.getId_usuario());
                listaCursos = new ArrayList<>();
                listaCursos.add(c);
                opcoesCurso.add(c.getNome());
                cbCurso.setItems(opcoesCurso);

            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleTrocaCurso(){
        logado.setCurso(cbCurso.getValue());

        ObservableList<String> opcoesSemestreCurso = FXCollections.observableArrayList();
        DisciplinaDAO dDao = new DisciplinaDAO();
        try{
            listaD = dDao.listarDisciplinasCurso(logado.getId_usuario(), logado.getAno(), logado.getAnoSemestre(), logado.getCurso());
            for (Disciplina d: listaD){
                if (!opcoesSemestreCurso.contains(d.getSemestre_curso().toString())) {
                    opcoesSemestreCurso.add(d.getSemestre_curso().toString());
                }
            }
            cbSemestreCurso.setItems(opcoesSemestreCurso);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTrocaSemestreCurso(){
        logado.setSemestreCurso(Integer.parseInt(cbSemestreCurso.getValue()));

        ObservableList<String> opcoesDisciplina = FXCollections.observableArrayList();
        for (Disciplina d: listaD){
            if (d.getSemestre_curso().equals(logado.getSemestreCurso())){
                opcoesDisciplina.add(d.getNome());
            }
        }
        cbDisciplina.setItems(opcoesDisciplina);
    }

    @FXML
    public void handleTrocaDisciplina(){
        logado.setDisciplina(cbDisciplina.getValue());
        descobrirIdDisciplina();
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


    @FXML void navTemas() {
        carregarConteudo("/prof_temas.fxml");
        // Como o carregarConteudo cria um controller do zero, isso força ele a receber os dados atuais do comboBox
        descobrirIdDisciplina();
    }

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

    public Integer getAnoSelecionado() { return logado.getAno(); }
    public Integer getSemestreAnoEscolhido() { return logado.getAnoSemestre(); }
    public String getCursoEscolhido() { return logado.getCurso(); }
    public String getDisciplinaEscolhida() { return logado.getDisciplina(); }

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


    // ------------------PROF TEMAS CONTROLLER ---------------------------------------
    //MÉTODOS UTILIZADOS PARA CAPTURAR O ID DO SEMESTRE E DISCIPLINA QUE ESTÃO SETADOS NA TELA PRINCIPAL
    private Integer descobrirIdSemestreLetivoAtivo() {
        if (listaSl == null || logado == null || logado.getAno() == null || logado.getAnoSemestre() == null) {
            return null;
        }
        for (SemestreLetivo sl : listaSl) {
            // Compara Ano e Número do semestre (1 ou 2) com os dados do usuário logado
            if (sl.getAno().equals(logado.getAno()) && sl.getNumero_semestre() == logado.getAnoSemestre()) {
                return sl.getId_semestre_letivo(); // <-- Ajuste o nome do getter se na sua entidade for diferente
            }
        }
        return null;
    }
    private void descobrirIdDisciplina() {
        String disciplinaSelecionada = cbDisciplina.getValue();
        if (disciplinaSelecionada == null || listaD == null) return;

        // Busca o objeto Disciplina correspondente ao texto selecionado no ComboBox
        Disciplina disciplina = listaD.stream()
                .filter(d -> d.getNome().equals(disciplinaSelecionada))
                .findFirst()
                .orElse(null);

        // Se achou a disciplina e a tela atual ativa for a de Temas, envia os dados
        if (disciplina != null && controllerAtivo instanceof ProfTemasController) {
            ProfTemasController telaTemas = (ProfTemasController) controllerAtivo;

            Integer idSemestre = descobrirIdSemestreLetivoAtivo();
            telaTemas.setDadosIniciais(disciplina.getId_disciplina(), idSemestre);
        }
    }
}