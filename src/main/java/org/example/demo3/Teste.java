package org.example.demo3;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.sql.*;

public class Teste {
    @FXML private MFXLegacyTableView<PlanejamentoDia> mfxTableView;
    @FXML private TableColumn<PlanejamentoDia, String> dataCol, disciCol, temaCol, obsCol, diaCol;
    @FXML private TableColumn<PlanejamentoDia, Integer> aulasCol;

    @FXML private MFXLegacyComboBox<Integer> comboSemestre;
    @FXML private MFXLegacyComboBox<String> comboCurso;
    @FXML private MFXButton editRow, deleteRow;

    private Integer curso_atual;
    private final String DB_URL = "jdbc:mysql://localhost:3306/teste";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";

    public void initialize() {
        // 1. CONFIGURAÇÃO DAS COLUNAS
        dataCol.setCellValueFactory(new PropertyValueFactory<>("data"));
        disciCol.setCellValueFactory(new PropertyValueFactory<>("disciplina"));
        temaCol.setCellValueFactory(new PropertyValueFactory<>("tema"));
        obsCol.setCellValueFactory(new PropertyValueFactory<>("obs"));
        aulasCol.setCellValueFactory(new PropertyValueFactory<>("aulas"));
        diaCol.setCellValueFactory(new PropertyValueFactory<>("dia_semana"));

        // 2. LÓGICA DE SELEÇÃO E BOTÕES (O "Cérebro" da UI)
        mfxTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean temSelecao = (newVal != null);
            editRow.setDisable(!temSelecao);
            deleteRow.setDisable(!temSelecao);
            if (temSelecao) System.out.println("Selecionado ID: " + newVal.getId());
        });

        // Limpa seleção ao perder foco ou clicar no vazio da tabela
        mfxTableView.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                // Aguarda um "pulo" de frame para verificar qual componente ganhou o foco
                javafx.application.Platform.runLater(() -> {
                    var novoFoco = mfxTableView.getScene().getFocusOwner();

                    // Se o novo foco NÃO for o botão de editar nem o de excluir, limpamos
                    if (novoFoco != editRow && novoFoco != deleteRow) {
                        mfxTableView.getSelectionModel().clearSelection();
                    }
                });
            }
        });

        mfxTableView.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof StackPane) mfxTableView.getSelectionModel().clearSelection();
        });

        // 3. EVENTOS DOS COMBOBOXES
        comboCurso.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, nomeCurso) -> {
            if (nomeCurso != null) {
                curso_atual = getIdCurso(nomeCurso);
                comboSemestre.getSelectionModel().clearSelection();
                comboSemestre.setItems(getSemestres(2, curso_atual));
                mfxTableView.getItems().clear();
            }
        });

        comboSemestre.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, semestre) -> {
            if (semestre != null && curso_atual != null) {
                mfxTableView.setItems(getCronogramaItens(2, semestre, curso_atual));
            }
        });

        editRow.setOnAction(event -> {
            PlanejamentoDia selecionado = mfxTableView.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                System.out.println("O botão EDITAR foi selecionado para a linha ID: " + selecionado.getId());
            }
        });

        deleteRow.setOnAction(event -> {
            PlanejamentoDia selecionado = mfxTableView.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                System.out.println("O botão EXCLUIR foi selecionado para a linha ID: " + selecionado.getId());
            }
        });

        // 4. CARGA INICIAL
        comboCurso.setItems(getCursos(2));
    }

    // --- MÉTODOS DE BANCO DE DADOS (Usando PreparedStatement para segurança) ---

    public ObservableList<PlanejamentoDia> getCronogramaItens(Integer profId, Integer semestre, Integer cursoId) {
        ObservableList<PlanejamentoDia> lista = FXCollections.observableArrayList();
        String sql = """
            SELECT ci.id_cronograma_i AS id, ci.data_evento AS data, d.nome AS disciplina, 
                   t.nome_tema AS tema, ci.observacao AS obs, ci.qtd_aulas AS aulas, ci.dia_semana 
            FROM Cronograma_Itens ci 
            JOIN Temas t ON ci.tema_id = t.id_tema 
            JOIN Disciplinas d ON t.disciplina_id = d.id_disciplina 
            JOIN Cronogramas c ON ci.cronograma_id = c.id_cronograma 
            WHERE c.professor_id = ? AND c.semestre = ? AND d.curso_id = ?
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, profId);
            pstmt.setInt(2, semestre);
            pstmt.setInt(3, cursoId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.add(new PlanejamentoDia(
                        rs.getInt("id"), rs.getString("data"), rs.getString("disciplina"),
                        rs.getString("tema"), rs.getString("obs"), rs.getInt("aulas"), rs.getString("dia_semana")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public ObservableList<String> getCursos(Integer profId) {
        ObservableList<String> cursos = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT c.nome_curso FROM Cursos c " +
                "JOIN Disciplinas d ON c.id_curso = d.curso_id WHERE d.professor_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, profId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) cursos.add(rs.getString("nome_curso"));
        } catch (SQLException e) { e.printStackTrace(); }
        return cursos;
    }

    public ObservableList<Integer> getSemestres(Integer profId, Integer cursoId) {
        ObservableList<Integer> semestres = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT semestre_num FROM Disciplinas WHERE professor_id = ? AND curso_id = ? ORDER BY semestre_num";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, profId);
            pstmt.setInt(2, cursoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) semestres.add(rs.getInt("semestre_num"));
        } catch (SQLException e) { e.printStackTrace(); }
        return semestres;
    }

    public Integer getIdCurso(String nome) {
        String sql = "SELECT id_curso FROM Cursos WHERE nome_curso = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id_curso");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}