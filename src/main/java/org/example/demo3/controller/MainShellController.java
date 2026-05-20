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
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.CursoDAO;
import org.example.demo3.dao.DisciplinaDAO;
import org.example.demo3.dao.SemestreLetivoDAO;
import org.example.demo3.entity.Curso;
import org.example.demo3.entity.Disciplina;
import org.example.demo3.entity.SemestreLetivo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainShellController {
    @FXML private ComboBox<String> cbAno; // Substitua <String> pelo tipo de dado correto se necessário
    @FXML private ToggleButton tbSem1;
    @FXML private ToggleButton tbSem2;
    @FXML private ComboBox<String> cbCurso; // Substitua <String> pelo tipo de dado correto se necessário
    @FXML private ComboBox<String> cbSemestreCurso; // Substitua <String> pelo tipo de dado correto se necessário
    @FXML private ComboBox<String> cbDisciplina;
    @FXML private Label lblNomeUsuario;
    @FXML private Label lblPerfilUsuario;
    @FXML private Label bannerReadOnly;
    @FXML private VBox menuLateral;
    @FXML private VBox secaoAdm;
    @FXML private VBox secaoCoordenador;
    @FXML private VBox secaoProfessor;
    @FXML private StackPane areaConteudo;

    private List<SemestreLetivo> listaSl;
    private List<Disciplina> listaD;

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
        try{
            listaSl = slDao.listarAnoESemestreAno(logado.getId_usuario());
            for (SemestreLetivo sl: listaSl){
                opcoesAno.add(sl.getAno().toString());
            }
            cbAno.setItems(opcoesAno);

        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    private void carregarConteudo(String caminhoFxml) {
        try {
            // 1. Localiza e carrega o arquivo FXML secundário
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Parent novoConteudo = loader.load();

            // 2. Limpa o que está atualmente na área de conteúdo
            areaConteudo.getChildren().clear();

            // 3. Adiciona a nova tela na StackPane
            areaConteudo.getChildren().add(novoConteudo);

        } catch (IOException e) {
            e.printStackTrace();
            // Dica: Trate o erro de forma visual se achar necessário, colocando um aviso na tela
        }
    }

    @FXML
    public void handleTrocaAno() {
        anoSelecionado = Integer.parseInt(cbAno.getValue());
        for (SemestreLetivo sl: listaSl){
            if (sl.getAno().equals(anoSelecionado)){
                if (sl.getNumero_semestre() == 1){
                    tbSem1.setDisable(false);
                } else {
                    if (sl.getNumero_semestre() == 2){
                        tbSem2.setDisable(false);
                    }
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
        CursoDAO slDao = new CursoDAO();
        try{
            List<Curso> listaCursos = slDao.listarCursos(logado.getId_usuario(),anoSelecionado,semestreAnoEscolhido);
            for (Curso c: listaCursos){
                opcoesCurso.add(c.getNome());
            }
            cbCurso.setItems(opcoesCurso);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTrocaCurso(){
        cursoEscolhido = cbCurso.getValue();

        ObservableList<String> opcoesSemestreCurso = FXCollections.observableArrayList();
        DisciplinaDAO dDao = new DisciplinaDAO();
        try{
            listaD = dDao.listarDisciplinasCurso
                    (logado.getId_usuario(),anoSelecionado,semestreAnoEscolhido, cursoEscolhido);
            for (Disciplina d: listaD){
                opcoesSemestreCurso.add(d.getSemestre_curso().toString());
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

    @FXML
    void handleLogout() {

    }

    @FXML
    void navCalendario() {

    }

    @FXML
    void navCursosHorarios() {

    }

    @FXML
    void navCoordenaodresAdms() {

    }

    @FXML
    void navCoordPainel() {

    }

    @FXML
    void navTemas() {

    }

    @FXML
    void navPlanejamento() {

    }

}
