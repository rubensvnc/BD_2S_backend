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
    private Integer anoSelecionado;
    private Integer semestreAnoEscolhido;
    private String cursoEscolhido;
    private Integer semestreCursoEscolhido;
    private String disciplinaEscolhida;

    @FXML
    public void initialize(){
        tbSem1.setDisable(true);
        tbSem2.setDisable(true);

        logado.setId_usuario(4);
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
            areaConteudo.getChildren().clear();
            areaConteudo.getChildren().add(novoConteudo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTrocaAno() {
        anoSelecionado = Integer.parseInt(cbAno.getValue());
        tbSem1.setDisable(true);
        tbSem2.setDisable(true);
        for (SemestreLetivo sl: listaSl){
            if (sl.getAno().equals(anoSelecionado)){
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
            semestreAnoEscolhido = 1;
        } else {
            semestreAnoEscolhido = 2;
        }

        ObservableList<String> opcoesCurso = FXCollections.observableArrayList();
        CursoDAO cDao = new CursoDAO();

        if (logado.getTipo() == "PROF"){
            try{
                listaCursos = cDao.listarCursosProfessor(logado.getId_usuario(), anoSelecionado, semestreAnoEscolhido);
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
        cursoEscolhido = cbCurso.getValue();

        ObservableList<String> opcoesSemestreCurso = FXCollections.observableArrayList();
        DisciplinaDAO dDao = new DisciplinaDAO();
        try{
            listaD = dDao.listarDisciplinasCurso(logado.getId_usuario(), anoSelecionado, semestreAnoEscolhido, cursoEscolhido);
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
        semestreCursoEscolhido = Integer.parseInt(cbSemestreCurso.getValue());

        ObservableList<String> opcoesDisciplina = FXCollections.observableArrayList();
        for (Disciplina d: listaD){
            if (d.getSemestre_curso().equals(semestreCursoEscolhido)){
                opcoesDisciplina.add(d.getNome());
            }
        }
        cbDisciplina.setItems(opcoesDisciplina);
    }

    @FXML
    public void handleTrocaDisciplina(){
        disciplinaEscolhida = cbDisciplina.getValue();
    }

    @FXML void handleLogout() {}
    @FXML void navCalendario() { carregarConteudo("/adm_calendario_bloqueios.fxml"); }
    @FXML void navCursosHorarios() { carregarConteudo("/adm_cursos_horarios.fxml"); }
    @FXML void navCoordenaodresAdms() { carregarConteudo("/adm_coordenadores_adms.fxml"); }
    @FXML void navCoordPainel() { carregarConteudo("/coord_painel.fxml"); }
    @FXML void navTemas() { carregarConteudo("/prof_temas.fxml"); }

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

    public Integer getAnoSelecionado() { return anoSelecionado; }
    public Integer getSemestreAnoEscolhido() { return semestreAnoEscolhido; }
    public String getCursoEscolhido() { return cursoEscolhido; }
    public String getDisciplinaEscolhida() { return disciplinaEscolhida; }

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