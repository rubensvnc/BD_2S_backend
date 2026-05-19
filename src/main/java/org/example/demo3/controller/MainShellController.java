package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.SemestreLetivoDAO;
import org.example.demo3.entity.SemestreLetivo;

import java.sql.SQLException;
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

    @FXML
    public void initialize(){
        UsuarioAtual logado = UsuarioAtual.getInstancia();
        logado.setId_usuario(4);
        logado.setTipo("PROF");

        SemestreLetivoDAO slDao = new SemestreLetivoDAO();
        try{
            List<SemestreLetivo> listaSl = slDao.listarAnoESemestreAno(logado.getId_usuario());
            for (SemestreLetivo sl: listaSl){
                System.out.println("Ano: "+sl.getAno().toString()+" - Semestre: "+sl.getNumero_semestre().toString());
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    @FXML
    void handleContextChange() {

    }

    @FXML
    void handleSemestreToggle() {

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
