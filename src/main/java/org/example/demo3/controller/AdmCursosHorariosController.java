package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class AdmCursosHorariosController {

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
    public void handleSalvarCurso() {
        // TODO: Validar as entradas e persistir as alterações do curso no banco de dados
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
