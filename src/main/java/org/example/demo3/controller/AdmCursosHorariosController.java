package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.entity.Usuario;

import java.sql.SQLException;
import java.util.List;

public class AdmCursosHorariosController {

    @FXML private TextField tfCursoNome;
    @FXML private ToggleButton tbManha;
    @FXML private ToggleButton tbNoite;
    @FXML private Spinner<Integer> spQtdSemestres;
    @FXML private ComboBox<String> cbCoordenadorCurso;
    @FXML private ComboBox<String> cbProfessorCurso;
    @FXML private TitledPane painelFormCurso;
    @FXML private Button btnCancelarCurso;
    @FXML private Button btnSalvarCurso;

    private UsuarioAtual logado = UsuarioAtual.getInstancia();

    private Integer anoAntes = 0;
    private Integer anoSemestreAntes = 0;

    @FXML
    public void initialize(){
        logado.usuarioAdm();
    }

    @FXML
    public void handleNovoCurso() {
        // TODO: Preparar e expandir o formulário lateral para a criação de um novo curso
    }

    @FXML
    public void handleSelecionarCurso(MouseEvent event) {
        // TODO: Capturar o curso selecionado na tabela e atualizar o formulário e a tabela de horários
    }

    @FXML
    public void handleTurnoChange() {
        // TODO: Gerenciar o estado de seleção mútua dos botões de alternância de turno (Manhã/Noite)
    }

    @FXML
    public void handleCancelarCurso() {
        // TODO: Limpar o formulário de dados do curso e recolher o TitledPane
    }

    @FXML
    public void fillComboboxCoordenador(){
        if (!anoAntes.equals(logado.getAno()) || !anoSemestreAntes.equals(logado.getAnoSemestre())){
            System.out.println("ANO: "+anoAntes);
            System.out.println("SEMESTRE_ANO: "+anoSemestreAntes);
            ObservableList<String> opcoesCoords = FXCollections.observableArrayList();
            try{
                UsuarioDAO uDao = new UsuarioDAO();
                List<Usuario> coords = uDao.listarCoordSemestreLetivo(logado.getAno(), logado.getAnoSemestre());
                for (Usuario coord: coords){
                    opcoesCoords.add(coord.getNome());
                }
                cbCoordenadorCurso.setItems(opcoesCoords);
            } catch (SQLException e){
                e.printStackTrace();
            }
            anoAntes = logado.getAno();
            anoSemestreAntes = logado.getAnoSemestre();
            System.out.println("ANO DEPOIS: "+anoAntes);
            System.out.println("SEMESTRE_ANO DEPOIS: "+anoSemestreAntes);
        }
    }



    @FXML
    public void handleSalvarCurso() {
        String nomeCurso = tfCursoNome.getText();

    }

    // ═════════════════════════════════════════════════════════════════════════
    // SEÇÃO DE HORÁRIOS (Painel Direito)
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleAplicarTemplate() {
        // TODO: Preencher a tabela com uma estrutura de horários padrão com base no turno do curso
    }

    @FXML
    public void handlePropagarTurno() {
        // TODO: Replicar a grade de horários atual do curso para todos os outros cursos do mesmo turno
    }

    @FXML
    public void handleAdicionarLinhaHorario() {
        // TODO: Inserir uma nova linha vazia ou editável na TableView de horários
    }

    @FXML
    public void handleSalvarHorarios() {
        // TODO: Validar o encadeamento cronológico das linhas e salvar as alterações na tabela de horários
    }
}
