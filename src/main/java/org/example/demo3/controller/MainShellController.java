package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.CursoDAO;
import org.example.demo3.dao.SemestreLetivoDAO;
import org.example.demo3.entity.Curso;
import org.example.demo3.entity.SemestreLetivo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainShellController {
    @FXML private ComboBox<String> cbAno; // Substitua <String> pelo tipo de dado correto se necessário
    @FXML private ToggleButton tbSem1;
    @FXML private ToggleButton tbSem2;
    @FXML private ComboBox<String> cbCurso; // Substitua <String> pelo tipo de dado correto se necessário
    @FXML private ComboBox<String> cbSemestreCurso; // Substitua <String> pelo tipo de dado correto se necessário
    @FXML private Label lblNomeUsuario;
    @FXML private Label lblPerfilUsuario;
    @FXML private Label bannerReadOnly;
    @FXML private VBox menuLateral;
    @FXML private VBox secaoAdm;
    @FXML private VBox secaoCoordenador;
    @FXML private VBox secaoProfessor;
    @FXML private StackPane areaConteudo;

    private List<SemestreLetivo> listaSl;
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
                System.out.println("Ano: "+sl.getAno().toString()+" - Semestre: "+sl.getNumero_semestre().toString());
            }
            cbAno.setItems(opcoesAno);

        } catch (SQLException e){
            e.printStackTrace();
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
    public void handleSemestreToggle1() {
        semestreAnoEscolhido = 1;
    }

    @FXML
    public void handleSemestreToggle2() {
        semestreAnoEscolhido = 2;
    }

    @FXML
    public void handleTrocaCurso(){
        ObservableList<String> opcoesCurso = FXCollections.observableArrayList();
        CursoDAO slDao = new CursoDAO();
        try{
            List<Curso> listaCursos = slDao.listarCursos(logado.getId_usuario(),anoSelecionado,semestreAnoEscolhido);
            for (Curso c: listaCursos){
                opcoesCurso.add(c.getNome());
            }
            cbAno.setItems(opcoesCurso);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleContextChange(){

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
