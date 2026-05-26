package org.example.demo3.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import org.example.demo3.DatabaseConnection;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.SlotPlanejamento;
import org.example.demo3.dao.PlanejamentoDAO;
import org.example.demo3.dao.SlotPlanejamentoDAO;
import org.example.demo3.entity.Planejamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ProfPlanejamentoController {

    @FXML private Label lblTotalTemas;
    @FXML private Label lblAulasGeradas;
    @FXML private Label lblAulasMinistradas;
    @FXML private Label lblAulasPendentes;
    @FXML private Label lblAulasCanceladas;
    @FXML private Label lblCargaHorariaMinima;
    @FXML private ProgressBar progressConclusao;
    @FXML private Label lblPercentual;
    @FXML private PieChart chartStatusAulas;

    @FXML private ProgressIndicator progressGeracao;
    @FXML private TreeView<Object> treePlanejamento;
    @FXML private HBox barraAcoesLote;
    @FXML private Label lblQtdSelecionados;

    private final IntegerProperty totalTemas = new SimpleIntegerProperty(0);
    private final IntegerProperty aulasGeradas = new SimpleIntegerProperty(0);
    private final IntegerProperty aulasMinistradas = new SimpleIntegerProperty(0);
    private final IntegerProperty aulasPendentes = new SimpleIntegerProperty(0);
    private final IntegerProperty aulasCanceladas = new SimpleIntegerProperty(0);
    private final IntegerProperty cargaMinima = new SimpleIntegerProperty(0);
    private final DoubleProperty percentualConclusao = new SimpleDoubleProperty(0.0);

    private final SlotPlanejamentoDAO slotDAO = new SlotPlanejamentoDAO();
    private final List<CheckBoxTreeItem<Object>> itensSelecionadosAulas = new ArrayList<>();

    private MainShellController mainShellController;

    private UsuarioAtual logado = UsuarioAtual.getInstancia();

    @FXML
    public void initialize() {
        lblTotalTemas.textProperty().bind(totalTemas.asString());
        lblAulasGeradas.textProperty().bind(aulasGeradas.asString());
        lblAulasMinistradas.textProperty().bind(aulasMinistradas.asString());
        lblAulasPendentes.textProperty().bind(aulasPendentes.asString());
        lblAulasCanceladas.textProperty().bind(aulasCanceladas.asString());
        lblCargaHorariaMinima.textProperty().bind(cargaMinima.asString());

        progressConclusao.progressProperty().bind(percentualConclusao);
        lblPercentual.textProperty().bind(
                Bindings.concat(Bindings.format("%.1f", percentualConclusao.multiply(100)), "%")
        );

        treePlanejamento.setCellFactory(CheckBoxTreeCell.forTreeView());
    }

    public void setMainShellController(MainShellController mainShellController) {
        this.mainShellController = mainShellController;
    }

    @FXML
    public void handleGerarPlanejamento() {
        if (mainShellController == null ||
                logado.getAno() == null ||
                logado.getIdCurso() == null ||
                logado.getIdDisciplina() == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Filtros insuficientes");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecione Ano, Semestre, Curso e Disciplina nos menus do topo antes de gerar.");
            alert.showAndWait();
            return;
        }

        progressGeracao.setManaged(true);
        progressGeracao.setVisible(true);
        itensSelecionadosAulas.clear();
        atualizarBarraLote();

        try {
            List<Map<String, Object>> dadosBrutos = slotDAO.buscarDadosMixados(logado.getAno(), logado.getAnoSemestre(), logado.getIdCurso(), logado.getIdDisciplina());

            List<SlotVisual> listaVisuais = dadosBrutos.stream().map(map -> new SlotVisual(
                    (SlotPlanejamento) map.get("entidade"),
                    (String) map.get("hora_inicio"),
                    (String) map.get("nome_tema")
            )).collect(Collectors.toList());

            Map<LocalDate, List<SlotVisual>> agrupadosPorData = listaVisuais.stream()
                    .collect(Collectors.groupingBy(v -> v.getSlot().getData(), LinkedHashMap::new, Collectors.toList()));

            CheckBoxTreeItem<Object> rootNode = new CheckBoxTreeItem<>("Raiz");
            rootNode.setExpanded(true);

            String[] diasDaSemana = {"", "Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado"};

            for (Map.Entry<LocalDate, List<SlotVisual>> entry : agrupadosPorData.entrySet()) {
                LocalDate data = entry.getKey();
                int diaNum = data.getDayOfWeek().getValue() == 7 ? 1 : data.getDayOfWeek().getValue() + 1;

                String labelDia = String.format("%s (%s)", data.toString(), diasDaSemana[diaNum]);
                CheckBoxTreeItem<Object> diaNode = new CheckBoxTreeItem<>(labelDia);
                diaNode.setExpanded(true);

                for (SlotVisual visual : entry.getValue()) {
                    CheckBoxTreeItem<Object> slotNode = new CheckBoxTreeItem<>(visual);

                    if ("cancelada_adm".equals(visual.getSlot().getStatus())) {
                        slotNode.setIndependent(true);
                    }

                    slotNode.selectedProperty().addListener((obs, antigo, novo) -> {
                        if ("cancelada_adm".equals(visual.getSlot().getStatus())) {
                            slotNode.setSelected(false);
                            return;
                        }
                        if (novo) itensSelecionadosAulas.add(slotNode);
                        else itensSelecionadosAulas.remove(slotNode);
                        atualizarBarraLote();
                    });
                    diaNode.getChildren().add(slotNode);
                }
                rootNode.getChildren().add(diaNode);
            }

            treePlanejamento.setRoot(rootNode);
            carregarEstatisticasContexto(logado.getAno(), logado.getAnoSemestre(), logado.getIdCurso(), logado.getIdDisciplina());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            progressGeracao.setVisible(false);
            progressGeracao.setManaged(false);
        }
    }

    public void carregarEstatisticasContexto(int ano, int semestreAno, Integer id_curso, Integer id_disciplina) {
        Integer idProfessor = UsuarioAtual.getInstancia().getId_usuario();
        if (idProfessor == null) return;

        try {
            PlanejamentoDAO pDao = new PlanejamentoDAO();
            Map<String, Object> metricas = pDao.obterEstatisticasGlobais(ano, semestreAno, id_curso, id_disciplina, idProfessor);

            if (metricas != null && !metricas.isEmpty()) {
                int totalAulas = (int) metricas.getOrDefault("totalAulas", 0);
                int ministradas = (int) metricas.getOrDefault("ministradas", 0);
                int pendentes = (int) metricas.getOrDefault("pendentes", 0);
                int canceladas = (int) metricas.getOrDefault("canceladas", 0);
                int chMinima = (int) metricas.getOrDefault("chMinima", 0);
                int totalTemas = (int) metricas.getOrDefault("totalTemas", 0);

                this.aulasGeradas.set(totalAulas);
                this.aulasMinistradas.set(ministradas);
                this.aulasPendentes.set(pendentes);
                this.aulasCanceladas.set(canceladas);
                this.cargaMinima.set(chMinima);
                this.totalTemas.set(totalTemas);

                double progresso = (totalAulas > 0) ? (double) ministradas / totalAulas : 0.0;
                this.percentualConclusao.set(progresso);

                ObservableList<PieChart.Data> dadosGrafico = FXCollections.observableArrayList(
                        new PieChart.Data("Ministradas (" + ministradas + ")", ministradas),
                        new PieChart.Data("Pendentes (" + pendentes + ")", pendentes),
                        new PieChart.Data("Canceladas (" + canceladas + ")", canceladas)
                );
                chartStatusAulas.setData(dadosGrafico);
            } else {
                limparCamposEstatisticas();
            }
        } catch (Exception e) {
            e.printStackTrace();
            limparCamposEstatisticas();
        }
    }

    private void limparCamposEstatisticas() {
        this.aulasGeradas.set(0);
        this.aulasMinistradas.set(0);
        this.aulasPendentes.set(0);
        this.aulasCanceladas.set(0);
        this.cargaMinima.set(0);
        this.totalTemas.set(0);
        this.percentualConclusao.set(0.0);
        chartStatusAulas.getData().clear();
    }

    private void atualizarBarraLote() {
        int qtd = itensSelecionadosAulas.size();
        if (qtd >= 1) {
            lblQtdSelecionados.setText(qtd + " item(ns) selecionado(s)");
            barraAcoesLote.setManaged(true);
            barraAcoesLote.setVisible(true);
        } else {
            barraAcoesLote.setVisible(false);
            barraAcoesLote.setManaged(false);
        }
    }

    @FXML
    public void handleMarcarMinistrada() {
        if (itensSelecionadosAulas.isEmpty()) return;
        List<Integer> ids = itensSelecionadosAulas.stream().map(item -> ((SlotVisual) item.getValue()).getSlot().getId_slot_planejamento()).collect(Collectors.toList());
        slotDAO.atualizarStatusEmLote(ids, "ministrada", null);
        handleGerarPlanejamento();
    }

    @FXML
    public void handleCancelarSelecionados() {
        if (itensSelecionadosAulas.isEmpty()) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mudar Status — Cancelamento pelo Professor");
        dialog.setHeaderText("Cancelamento em Lote (" + itensSelecionadosAulas.size() + " aulas)");
        dialog.setContentText("Informe o motivo do cancelamento:");

        dialog.showAndWait().ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) return;
            List<Integer> ids = itensSelecionadosAulas.stream().map(item -> ((SlotVisual) item.getValue()).getSlot().getId_slot_planejamento()).collect(Collectors.toList());
            slotDAO.atualizarStatusEmLote(ids, "cancelada_professor", motivo);
            handleGerarPlanejamento();
        });
    }

    private int descobrirIdCursoPorNome(String nomeCurso) {
        String sql = "SELECT id_curso FROM curso WHERE nome = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeCurso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_curso");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int descobrirIdDisciplinaPorNome(String nomeDisciplina) {
        String sql = "SELECT id_disciplina FROM disciplina WHERE nome = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_disciplina");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static class SlotVisual {
        private final SlotPlanejamento slot;
        private final String horaInicio;
        private final String nomeTema;

        public SlotVisual(SlotPlanejamento slot, String horaInicio, String nomeTema) {
            this.slot = slot;
            this.horaInicio = horaInicio;
            this.nomeTema = nomeTema;
        }

        public SlotPlanejamento getSlot() { return slot; }

        @Override
        public String toString() {
            String statusFormatado = switch (slot.getStatus()) {
                case "ministrada" -> "[MINISTRADA]";
                case "nao_ministrada" -> "[NAO_MINISTRADA]";
                case "cancelada_professor" -> "[CANCELADA PELO PROFESSOR]";
                case "cancelada_adm" -> "[CANCELADO PELA SECRETARIA]";
                default -> "[" + slot.getStatus().toUpperCase() + "]";
            };
            String conteudo = (this.nomeTema != null) ? this.nomeTema : "Aula sem tema definido";
            String horaCortada = (this.horaInicio != null && this.horaInicio.length() >= 5) ? this.horaInicio.substring(0, 5) : "00:00";
            return String.format("%s - %s   %s", horaCortada, conteudo, statusFormatado);
        }
    }
}