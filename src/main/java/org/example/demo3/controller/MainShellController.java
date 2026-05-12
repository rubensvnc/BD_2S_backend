package org.example.demo3.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainShellController {

    // ── Toolbar ──────────────────────────────────────────────────────────────
    @FXML private ComboBox<?> cbAno;
    @FXML private ToggleButton tbSem1;
    @FXML private ToggleButton tbSem2;
    @FXML private ComboBox<?> cbCurso;
    @FXML private ComboBox<?> cbSemestreCurso;
    @FXML private Label lblNomeUsuario;
    @FXML private Label lblPerfilUsuario;
    @FXML private Label bannerReadOnly;

    // ── Menu lateral ─────────────────────────────────────────────────────────
    @FXML private VBox menuLateral;
    @FXML private VBox secaoAdm;
    @FXML private VBox secaoCoordenador;
    @FXML private VBox secaoProfessor;

    // ── Área de conteúdo central ──────────────────────────────────────────────
    @FXML private StackPane areaConteudo;

    @FXML
    public void initialize() {
        // Vincula os dois ToggleButtons num ToggleGroup para exclusividade
        ToggleGroup grupoSemestre = new ToggleGroup();
        tbSem1.setToggleGroup(grupoSemestre);
        tbSem2.setToggleGroup(grupoSemestre);
    }

    // ── Navegação ─────────────────────────────────────────────────────────────

    /** Carrega prof_temas.fxml no centro */
    @FXML
    private void navTemas(ActionEvent event) {
        Parent view = carregarFXML("/prof_temas.fxml");
        if (view != null) areaConteudo.getChildren().setAll(view);
    }

    /** Carrega prof_planejamento.fxml no centro */
    @FXML
    private void navPlanejamento(ActionEvent event) {
        Parent view = carregarFXML("/prof_planejamento.fxml");
        if (view != null) areaConteudo.getChildren().setAll(view);
    }

    // Stubs dos demais itens de menu (implemente conforme necessário)
    @FXML private void navCalendario(ActionEvent event)       { /* TODO */ }
    @FXML private void navCursosHorarios(ActionEvent event)   { /* TODO */ }
    @FXML private void navCoordenaodresAdms(ActionEvent event) { /* TODO */ }
    @FXML private void navCoordPainel(ActionEvent event)      { /* TODO */ }

    // ── Toolbar handlers ──────────────────────────────────────────────────────
    @FXML private void handleContextChange(ActionEvent event) { /* TODO */ }
    @FXML private void handleSemestreToggle(ActionEvent event) { /* TODO */ }
    @FXML private void handleLogout(ActionEvent event)        { /* TODO */ }

    // ── Utilitário ────────────────────────────────────────────────────────────

    /**
     * Carrega um FXML da raiz de resources e devolve o nó raiz.
     * Se precisar acessar o controller filho, use FXMLLoader diretamente
     * (veja o exemplo comentado abaixo).
     */
    private Parent carregarFXML(String caminho) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminho));
            return loader.load();

            // ── Para acessar o controller filho e passar contexto: ──
            // Parent view = loader.load();
            // ProfTemasController ctrl = loader.getController();
            // ctrl.setContexto(...);
            // return view;

        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML: " + caminho);
            e.printStackTrace();
            return null;
        }
    }
}