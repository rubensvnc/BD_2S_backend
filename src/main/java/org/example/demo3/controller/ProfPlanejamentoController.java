package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import org.example.demo3.SlotPlanejamento;
import org.example.demo3.dao.SlotPlanejamentoDAO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ProfPlanejamentoController {

    @FXML private ProgressIndicator progressGeracao;
    @FXML private TreeView<Object> treePlanejamento;
    @FXML private HBox barraAcoesLote;
    @FXML private Label lblQtdSelecionados;

    private final SlotPlanejamentoDAO slotDAO = new SlotPlanejamentoDAO();
    private final List<CheckBoxTreeItem<Object>> itensSelecionadosAulas = new ArrayList<>();

    // Variáveis de filtro recebidas dinamicamente do MainShellController
    private int anoContexto;
    private int semestreContexto;
    private int idCursoContexto;
    private int idDisciplinaContexto;

    @FXML
    public void initialize() {
        treePlanejamento.setCellFactory(CheckBoxTreeCell.forTreeView());
    }

    /**
     * Método público para o MainShellController injetar os parâmetros selecionados
     */
    public void setContextoFiltros(int ano, int semestre, int idCurso, int idDisciplina) {
        this.anoContexto = ano;
        this.semestreContexto = semestre;
        this.idCursoContexto = idCurso;
        this.idDisciplinaContexto = idDisciplina;

        // Dispara a busca real assim que recebe os dados de filtro do Shell
        handleGerarPlanejamento();
    }

    @FXML
    public void handleGerarPlanejamento() {
        // Se nenhum filtro foi passado pelo Shell ainda, não faz nada
        if (anoContexto == 0 || idDisciplinaContexto == 0) {
            return;
        }

        progressGeracao.setManaged(true);
        progressGeracao.setVisible(true);
        itensSelecionadosAulas.clear();
        atualizarBarraLote();

        // 1. Busca os dados reais utilizando as propriedades enviadas pelo Shell
        List<Map<String, Object>> dadosBrutos = slotDAO.buscarDadosMixados(
                anoContexto, semestreContexto, idCursoContexto, idDisciplinaContexto
        );

        // 2. Converte localmente para a classe interna SlotVisual
        List<SlotVisual> listaVisuais = dadosBrutos.stream().map(map -> new SlotVisual(
                (SlotPlanejamento) map.get("entidade"),
                (String) map.get("hora_inicio"),
                (String) map.get("nome_tema")
        )).collect(Collectors.toList());

        // 3. Agrupa por data baseando-se na entidade
        Map<LocalDate, List<SlotVisual>> agrupadosPorData = listaVisuais.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getSlot().getData(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

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
                    if (novo) {
                        itensSelecionadosAulas.add(slotNode);
                    } else {
                        itensSelecionadosAulas.remove(slotNode);
                    }
                    atualizarBarraLote();
                });

                diaNode.getChildren().add(slotNode);
            }
            rootNode.getChildren().add(diaNode);
        }

        treePlanejamento.setRoot(rootNode);
        progressGeracao.setVisible(false);
        progressGeracao.setManaged(false);
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

        List<Integer> ids = itensSelecionadosAulas.stream()
                .map(item -> ((SlotVisual) item.getValue()).getSlot().getId_slot_planejamento())
                .collect(Collectors.toList());

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

            List<Integer> ids = itensSelecionadosAulas.stream()
                    .map(item -> ((SlotVisual) item.getValue()).getSlot().getId_slot_planejamento())
                    .collect(Collectors.toList());

            slotDAO.atualizarStatusEmLote(ids, "cancelada_professor", motivo);
            handleGerarPlanejamento();
        });
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

        public SlotPlanejamento getSlot() {
            return slot;
        }

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
            String horaCortada = (this.horaInicio != null && this.horaInicio.length() >= 5)
                    ? this.horaInicio.substring(0, 5)
                    : "00:00";

            return String.format("%s - %s   %s", horaCortada, conteudo, statusFormatado);
        }
    }
}