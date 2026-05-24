package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.CursoDAO;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.dao.UsuarioTipoDAO;
import org.example.demo3.entity.Curso;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdmCursosHorariosController {

    @FXML private TextField tfCursoNome;
    @FXML private ToggleButton tbManha;
    @FXML private ToggleButton tbNoite;
    @FXML private Spinner<Integer> spQtdSemestres;
    @FXML private ComboBox<String> cbProfessorCurso;
    @FXML private TitledPane painelFormCurso;
    @FXML private CheckBox checkUsarProfessor;

    private UsuarioAtual logado = UsuarioAtual.getInstancia();

    private Integer anoAntes = 0;
    private Integer anoSemestreAntes = 0;

    private Boolean profsCarregados = false;
    private Map<String, Integer> mapaProfessores = new HashMap<>();
    private Integer idProfessorSelecionado;

    @FXML
    public void initialize(){
        logado.usuarioAdm();
        cbProfessorCurso.setDisable(true);
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
    public void usarProfessor(){
        if (checkUsarProfessor.isSelected()){
            cbProfessorCurso.setDisable(false);
            fillComboboxProfessor();
        } else {
            cbProfessorCurso.setDisable(true);
        }
    }

    public void fillComboboxProfessor() {
        if (!profsCarregados || !anoAntes.equals(logado.getAno()) || !anoSemestreAntes.equals(logado.getAnoSemestre())) {
            carregarProfs();
        }
    }

    private void carregarProfs() {
        try {
            UsuarioDAO uDao = new UsuarioDAO();
            List<Usuario> profs = uDao.listarProfSemestreLetivo(logado.getAno(), logado.getAnoSemestre());

            mapaProfessores.clear();
            ObservableList<String> opcoesProfs = FXCollections.observableArrayList();
            for (Usuario prof : profs) {
                opcoesProfs.add(prof.getEmail());
                mapaProfessores.put(prof.getEmail(), prof.getId_usuario());
            }
            cbProfessorCurso.setItems(opcoesProfs);

            // Atualiza controle
            profsCarregados = true;
            anoAntes = logado.getAno();
            anoSemestreAntes = logado.getAnoSemestre();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSelecaoProfessor(){
        if (checkUsarProfessor.isSelected()){
            String emailSelecionado = cbProfessorCurso.getValue();
            if (emailSelecionado != null) {
                this.idProfessorSelecionado = mapaProfessores.get(emailSelecionado);
            }
        } else {
            this.idProfessorSelecionado = null;
        }
    }

    @FXML
    public void handleSalvarCurso() {
        String nomeCurso = tfCursoNome.getText();
        String turno;
        Integer qtd_semestres = spQtdSemestres.getValue();


        if (tbManha.isSelected()){
            turno = "manha";
        } else {
            turno = "noite";
        }

        try {
            CursoDAO cDao = new CursoDAO();
            if (checkUsarProfessor.isSelected()){
                UsuarioTipoDAO utDao = new UsuarioTipoDAO();

                utDao.inserirUsuarioTipo(new UsuarioTipo(idProfessorSelecionado, "COORD"));
                cDao.inserirCurso(idProfessorSelecionado, nomeCurso, turno, qtd_semestres);

            } else {
                cDao.inserirCurso(nomeCurso, turno, qtd_semestres);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
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
