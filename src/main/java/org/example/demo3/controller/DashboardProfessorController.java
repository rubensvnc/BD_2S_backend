package org.example.demo3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.demo3.dao.*;
import org.example.demo3.dto.CronogramaExibicaoDTO;
import org.example.demo3.entity.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class DashboardProfessorController {

    // --- COMPONENTES FXML ---
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
    private final Integer MOCK_ID_PROFESSOR = 2;
    private Integer idCursoAtual = null;
    private Integer idSemestreAtual = null;

    @FXML
    public void initialize() {
        configurarColunasCronograma();
        inserirCursosCBox();
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
        preencherCronograma();
    }
}