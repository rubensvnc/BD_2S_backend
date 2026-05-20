package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class CoordPainelController {

    // ═════════════════════════════════════════════════════════════════════════
    // ABA 1 — DISCIPLINAS
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleNovaDisciplina() {
        // TODO: Lógica para preparar o formulário para uma nova disciplina
    }

    @FXML
    public void handleSelecionarDisciplina(MouseEvent event) {
        // TODO: Lógica para carregar os dados da linha clicada no formulário lateral
    }

    @FXML
    public void handleLimparDisc() {
        // TODO: Lógica para limpar os campos do formulário de disciplinas
    }

    @FXML
    public void handleSalvarDisciplina() {
        // TODO: Lógica para validar e salvar/atualizar a disciplina no banco
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ABA 2 — PROFESSORES
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleNovoProfessor() {
        // TODO: Lógica para preparar o formulário para um novo professor
    }

    @FXML
    public void handleSelecionarProfessor(MouseEvent event) {
        // TODO: Lógica para carregar os dados do professor selecionado no formulário
    }

    @FXML
    public void handleAtribuirMesmo() {
        // TODO: Lógica para puxar os dados do coordinator logado para o formulário
    }

    @FXML
    public void handleVerificarEmailProf(KeyEvent event) {
        // TODO: Lógica executada a cada tecla solta para checar duplicidade de e-mail
    }

    @FXML
    public void handleLimparProf() {
        // TODO: Lógica para limpar os campos do formulário de professores
    }

    @FXML
    public void handleSalvarProfessor() {
        // TODO: Lógica para salvar o novo professor ou atualizar o existente
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ABA 3 — ATRIBUIÇÕES
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleAtribContextChange() {
        // TODO: Lógica acionada ao mudar o ComboBox de Professor ou Disciplina
        // Usada para recarregar a grade semanal baseada no novo contexto selecionado
    }

    @FXML
    public void handleLimparGrade() {
        // TODO: Lógica para desmarcar todos os CheckBoxes da grade semanal
    }

    @FXML
    public void handleSalvarAtribuicao() {
        // TODO: Lógica para salvar a relação do professor, disciplina e horários marcados
    }
}
