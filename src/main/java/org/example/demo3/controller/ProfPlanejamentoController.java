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
import org.example.demo3.UsuarioAtual;
import org.example.demo3.SlotPlanejamento;
import org.example.demo3.dao.*;
import org.example.demo3.entity.*;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class ProfPlanejamentoController {

    @FXML private Label lblTotalTemas;
    @FXML private Label lblAulasGeradas;
    @FXML private Label lblAulasMinistradas;
    @FXML private Label lblAulasPendentes;
    @FXML private Label lblAulasCanceladas;
    @FXML private Label lblCargaHorariaMinima;
    @FXML private Label lblAvisoCargaInsuficiente;
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

    private Integer idSemestreLetivo;
    private Integer contadorCargaH = 0;
    private Boolean falhouCumprirQtdMinAulas = false;
    private HashMap<Tema, HashMap<LocalDate, List<HorarioCurso>>> hMapTemaDataHorarios = new HashMap<>();

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

        logado.idDisciplinaProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("O ID da disciplina mudou de: " + oldValue + " para: " + newValue);

            if (newValue != null) {
                atualizarDadosPlanejamento();
            }
        });
    }

    public void setMainShellController(MainShellController mainShellController) {
        this.mainShellController = mainShellController;
    }

    public LinkedHashMap<Tema, List<Tema>> mapearTemaPrioEDependenciasOrd() {
        LinkedHashMap<Tema, List<Tema>> hmapTemaPrioDepend = new LinkedHashMap<>();
        SemestreLetivoDAO slDao = new SemestreLetivoDAO();
        TemaDAO tDao = new TemaDAO();
        DependenciaTemaDAO dtDao = new DependenciaTemaDAO();

        try {
            idSemestreLetivo = slDao.getIdSemestreLetivo(
                    logado.getAno(), logado.getAnoSemestre());

            // Já vem ordenada por prioridade (1 = maior)
            List<Tema> temasOrdPrioridade = tDao.listarTemasPorDisciplinaESemestre(
                    logado.getIdDisciplina(),
                    idSemestreLetivo
            );

            for (Tema tDepois : temasOrdPrioridade) {
                List<Tema> temasOrdDependencia = new ArrayList<>();
                List<DependenciaTema> dependencias = dtDao.listarDependenciasTema(tDepois.getId_tema());

                for (DependenciaTema dt : dependencias) {
                    // CORRIGIDO: busca o Tema completo em vez de criar objeto parcial
                    Tema tVemAntes = tDao.buscarPorId(dt.getTema_dependencia_id());
                    temasOrdDependencia.add(tVemAntes);
                }

                hmapTemaPrioDepend.put(tDepois, temasOrdDependencia);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hmapTemaPrioDepend;
    }

    public HashMap<Integer, List<HorarioCurso>> mapearDiasSemanaHorarios() {
        HashMap<Integer, List<HorarioCurso>> hmapDiaSemanaHorarios = new HashMap<>();
        AtribuicaoProfessorDAO apDao = new AtribuicaoProfessorDAO();
        HorarioCursoDAO hcDao = new HorarioCursoDAO();
        AtribuicaoHorarioDAO ahDao = new AtribuicaoHorarioDAO();

        try {
            AtribuicaoProfessor ap = apDao.buscarPorDisciplinaESemestre(
                    logado.getIdDisciplina(), idSemestreLetivo);
            Integer idAtribuicao = ap.getId_atribuicao_professor();

            List<AtribuicaoHorario> listaAh = ahDao.listarPorAtribuicao(idAtribuicao);
            for (AtribuicaoHorario ah : listaAh) {
                Integer diaSemana = ah.getDia_semana();
                List<HorarioCurso> listaHorariosDiaSemana = hcDao.listarHorariosPorAtribuicaoDSemana(
                        idAtribuicao,
                        diaSemana
                );
                hmapDiaSemanaHorarios.put(diaSemana, listaHorariosDiaSemana);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hmapDiaSemanaHorarios;
    }

    private boolean isDatBloqueada(LocalDate data, List<LocalDate> datasBloqueadas,
                                   List<CancelamentoAdm> cancelamentosAdm) {
        if (datasBloqueadas.contains(data)) return true;

        for (CancelamentoAdm ca : cancelamentosAdm) {
            if (ca.getData().equals(data) && ca.getDia_inteiro()) return true;
        }

        return false;
    }

    private List<HorarioCurso> filtrarHorariosCancelados(
            LocalDate data,
            List<HorarioCurso> horariosDoDia,
            List<CancelamentoAdm> cancelamentosAdm,
            Map<Integer, List<Integer>> cancelamentosAhmPorCaId) {

        Set<Integer> idsCancelados = new HashSet<>();

        for (CancelamentoAdm ca : cancelamentosAdm) {
            if (ca.getData().equals(data) && !ca.getDia_inteiro()) {
                List<Integer> ids = cancelamentosAhmPorCaId.get(ca.getId_cancelamento_adm());
                if (ids != null) idsCancelados.addAll(ids);
            }
        }

        if (idsCancelados.isEmpty()) return horariosDoDia;

        List<HorarioCurso> filtrados = new ArrayList<>();
        for (HorarioCurso h : horariosDoDia) {
            if (!idsCancelados.contains(h.getId_horario_curso())) {
                filtrados.add(h);
            }
        }
        return filtrados;
    }

    private boolean isSlotOcupado(LocalDate data, HorarioCurso horario) {
        for (HashMap<LocalDate, List<HorarioCurso>> porData : hMapTemaDataHorarios.values()) {
            List<HorarioCurso> horariosNaData = porData.get(data);
            if (horariosNaData != null) {
                for (HorarioCurso h : horariosNaData) {
                    if (h.getId_horario_curso().equals(horario.getId_horario_curso())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isDataBloqueadaParaAvaliacao(LocalDate data, List<Sprint> sprints,
                                                 SemestreLetivo semestre) {
        // Regra 3: jamais em sábado ou domingo
        DayOfWeek dia = data.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) return true;

        // Regra 4: dia da feira de soluções
        if (data.equals(semestre.getData_feira())) return true;

        // Regra 5: semana de apresentação de TG (data_tg até data_tg + 4 dias)
        LocalDate inicioTg = semestre.getData_tg();
        LocalDate fimTg = inicioTg.plusDays(4);
        if (!data.isBefore(inicioTg) && !data.isAfter(fimTg)) return true;

        // Regras 1 e 2: verificadas por sprint
        for (Sprint sprint : sprints) {
            LocalDate dataReview = sprint.getData_review();

            // Regra 2: semana de sprint review (data_review até data_review + 4 dias)
            LocalDate fimReview = dataReview.plusDays(4);
            if (!data.isBefore(dataReview) && !data.isAfter(fimReview)) return true;

            // Regra 1: 3ª semana da sprint (data_review - 7 até data_review - 3)
            LocalDate inicioTerceiraSemana = dataReview.minusDays(7);
            LocalDate fimTerceiraSemana = dataReview.minusDays(3);
            if (!data.isBefore(inicioTerceiraSemana) && !data.isAfter(fimTerceiraSemana)) return true;
        }

        return false;
    }

    public LocalDate popularTemaNosDiasSemana(
            LocalDate inicio, LocalDate fim,
            HashMap<Integer, List<HorarioCurso>> hmapa,
            Tema tema, int limiteCargaH,
            List<LocalDate> datasBloqueadas,
            List<CancelamentoAdm> cancelamentosAdm,
            Map<Integer, List<Integer>> cancelamentosAhmPorCaId,
            List<Sprint> sprints,
            SemestreLetivo semestre)  {

        return alocarTema(inicio, fim, hmapa, tema, limiteCargaH,
                datasBloqueadas, cancelamentosAdm, cancelamentosAhmPorCaId,
                sprints, semestre, false);
    }

    public LocalDate popularTemaNosSabados(
            LocalDate inicio, LocalDate fim,
            HashMap<Integer, List<HorarioCurso>> hmapa,
            Tema tema, int limiteCargaH,
            List<LocalDate> datasBloqueadas,
            List<CancelamentoAdm> cancelamentosAdm,
            Map<Integer, List<Integer>> cancelamentosAhmPorCaId,
            List<Sprint> sprints,
            SemestreLetivo semestre) {

        return alocarTema(inicio, fim, hmapa, tema, limiteCargaH,
                datasBloqueadas, cancelamentosAdm, cancelamentosAhmPorCaId,
                sprints, semestre, true);
    }

    private LocalDate alocarTema(
            LocalDate inicio, LocalDate fim,
            HashMap<Integer, List<HorarioCurso>> hmapa,
            Tema tema, int limiteCargaH,
            List<LocalDate> datasBloqueadas,
            List<CancelamentoAdm> cancelamentosAdm,
            Map<Integer, List<Integer>> cancelamentosAhmPorCaId,
            List<Sprint> sprints,
            SemestreLetivo semestre,
            boolean usarSabadosRegressivo) {

        int aulasDesteTema = 0;
        LocalDate ultimaDataOcupada = inicio;

        List<LocalDate> datasPossiveis = new ArrayList<>();
        for (Integer diaSemana : hmapa.keySet()) {
            if (usarSabadosRegressivo && diaSemana != 6) continue;
            if (!usarSabadosRegressivo && diaSemana == 6) continue;

            LocalDate proximaData = inicio.with(
                    TemporalAdjusters.nextOrSame(DayOfWeek.of(diaSemana)));

            while (!proximaData.isAfter(fim)) {
                if (!isDatBloqueada(proximaData, datasBloqueadas, cancelamentosAdm)) {
                    datasPossiveis.add(proximaData);
                }
                proximaData = proximaData.plusWeeks(1);
            }
        }

        if (usarSabadosRegressivo) {
            datasPossiveis.sort(Collections.reverseOrder());
        } else {
            Collections.sort(datasPossiveis);
        }

        for (LocalDate data : datasPossiveis) {
            if (aulasDesteTema >= tema.getQtd_min_aulas() || contadorCargaH >= limiteCargaH) break;

            if (tema.getEh_avaliacao() == 1 && isDataBloqueadaParaAvaliacao(data, sprints, semestre)) {
                continue;
            }

            List<HorarioCurso> horariosDoDia = hmapa.get(data.getDayOfWeek().getValue());
            horariosDoDia = filtrarHorariosCancelados(data, horariosDoDia,
                    cancelamentosAdm, cancelamentosAhmPorCaId);

            for (HorarioCurso h : horariosDoDia) {
                if (aulasDesteTema >= tema.getQtd_min_aulas() || contadorCargaH >= limiteCargaH) break;

                if (h.getTipo().equalsIgnoreCase("aula") && !isSlotOcupado(data, h)) { // <-- adicionado
                    hMapTemaDataHorarios
                            .computeIfAbsent(tema, k -> new HashMap<>())
                            .computeIfAbsent(data, k -> new ArrayList<>())
                            .add(h);

                    aulasDesteTema++;
                    contadorCargaH++;
                    ultimaDataOcupada = data;
                }
            }
        }

        if (aulasDesteTema < tema.getQtd_min_aulas()) {
            this.falhouCumprirQtdMinAulas = true;
            if (contadorCargaH >= limiteCargaH) {
                System.out.println("AVISO: Limite de carga horária atingido antes de completar o tema: "
                        + tema.getNome());
            } else {
                System.out.println("ERRO: Datas insuficientes para o tema: " + tema.getNome());
            }
        }

        return ultimaDataOcupada;
    }

    private int contarAulasTema(Tema tema) {
        Map<LocalDate, List<HorarioCurso>> porData = hMapTemaDataHorarios.get(tema);
        if (porData == null) return 0;

        int total = 0;
        for (List<HorarioCurso> lista : porData.values()) {
            total += lista.size();
        }
        return total;
    }

    private boolean temaJaVisto(List<Tema> temasJaVistos, Tema tema) {
        return temasJaVistos.stream()
                .anyMatch(t -> t.getId_tema().equals(tema.getId_tema()));
    }

    @FXML
    public void handleGerarPlanejamento() {
        LinkedHashMap<Tema, List<Tema>> hmapTemaPrioDepend = mapearTemaPrioEDependenciasOrd();
        HashMap<Integer, List<HorarioCurso>> hmapDiaSemanaHorarios = mapearDiasSemanaHorarios();

        DisciplinaDAO dDao = new DisciplinaDAO();
        SemestreLetivoDAO slDao = new SemestreLetivoDAO();
        DataBloqueadaDAO dbDao = new DataBloqueadaDAO();
        CancelamentoAdmDAO caDao = new CancelamentoAdmDAO();
        PlanejamentoDAO pDao = new PlanejamentoDAO();
        AtribuicaoProfessorDAO apDao = new AtribuicaoProfessorDAO();

        try {
            SemestreLetivo semestreSelecionado = slDao.listarSLPorId(idSemestreLetivo);
            Disciplina disciplinaEscolhida = dDao.recuperarDisciplinaPorId(logado.getIdDisciplina());
            Integer cargaHoraria = disciplinaEscolhida.getCarga_horaria_minima();

            List<LocalDate> datasBloqueadas = dbDao.listarDatasBloqueadasPorSemestre(idSemestreLetivo);
            List<CancelamentoAdm> cancelamentosAdm = caDao.listarPorSemestre(idSemestreLetivo);
            Map<Integer, List<Integer>> cancelamentosAhmPorCaId =
                    caDao.listarHorariosCanceladosPorCancelamento(idSemestreLetivo);

            SprintDAO sprintDao = new SprintDAO();
            List<Sprint> sprints = sprintDao.listarPorSemestre(idSemestreLetivo);

            LocalDate dataInicio = semestreSelecionado.getData_inicio();
            LocalDate dataFim;
            List<Tema> temasJaVistos = new ArrayList<>();

            // Reseta estado antes de gerar
            contadorCargaH = 0;
            falhouCumprirQtdMinAulas = false;
            hMapTemaDataHorarios.clear();

            List<Tema> temasObrigatorios = new ArrayList<>();
            List<Tema> temasOpcionais = new ArrayList<>();
            for (Tema t : hmapTemaPrioDepend.keySet()) {
                if (t.getEh_opcional() == 1) temasOpcionais.add(t);
                else temasObrigatorios.add(t);
            }

            // --- PASSAGEM 1: obrigatórios, dias úteis ---
            for (Tema chave : temasObrigatorios) {
                List<Tema> dependenciasOrd = hmapTemaPrioDepend.get(chave);
                for (Tema temaDepen : dependenciasOrd) {
                    if (!temaJaVisto(temasJaVistos, temaDepen)) {
                        dataFim = popularTemaNosDiasSemana(dataInicio, semestreSelecionado.getData_fim(),
                                hmapDiaSemanaHorarios, temaDepen, cargaHoraria,
                                datasBloqueadas, cancelamentosAdm, cancelamentosAhmPorCaId,
                                sprints, semestreSelecionado);
                        temasJaVistos.add(temaDepen);
                        if (!dataFim.equals(dataInicio)) dataInicio = dataFim;
                    }
                }
                if (!temaJaVisto(temasJaVistos, chave)) {
                    dataFim = popularTemaNosDiasSemana(dataInicio, semestreSelecionado.getData_fim(),
                            hmapDiaSemanaHorarios, chave, cargaHoraria,
                            datasBloqueadas, cancelamentosAdm, cancelamentosAhmPorCaId,
                            sprints, semestreSelecionado);
                    temasJaVistos.add(chave);
                    if (!dataFim.equals(dataInicio)) dataInicio = dataFim;
                }
            }

            // --- PASSAGEM 2: sábados regressivos, se necessário ---
            if (falhouCumprirQtdMinAulas && hmapDiaSemanaHorarios.containsKey(6)) {
                falhouCumprirQtdMinAulas = false;
                for (Tema chave : temasObrigatorios) {
                    boolean temaCompleto = hMapTemaDataHorarios.containsKey(chave)
                            && contarAulasTema(chave) >= chave.getQtd_min_aulas();
                    if (!temaCompleto) {
                        popularTemaNosSabados(semestreSelecionado.getData_inicio(),
                                semestreSelecionado.getData_fim(), hmapDiaSemanaHorarios,
                                chave, cargaHoraria, datasBloqueadas, cancelamentosAdm,
                                cancelamentosAhmPorCaId,
                                sprints, semestreSelecionado);
                    }
                }
            }

            // --- PASSAGEM 3: opcionais, se sobrar carga ---
            if (contadorCargaH < cargaHoraria) {
                for (Tema chave : temasOpcionais) {
                    if (contadorCargaH >= cargaHoraria) break;
                    List<Tema> dependenciasOrd = hmapTemaPrioDepend.get(chave);
                    for (Tema temaDepen : dependenciasOrd) {
                        if (!temaJaVisto(temasJaVistos, temaDepen)) {
                            dataFim = popularTemaNosDiasSemana(dataInicio, semestreSelecionado.getData_fim(),
                                    hmapDiaSemanaHorarios, temaDepen, cargaHoraria,
                                    datasBloqueadas, cancelamentosAdm, cancelamentosAhmPorCaId,
                                    sprints, semestreSelecionado);
                            temasJaVistos.add(temaDepen);
                            if (!dataFim.equals(dataInicio)) dataInicio = dataFim;
                        }
                    }
                    dataFim = popularTemaNosDiasSemana(dataInicio, semestreSelecionado.getData_fim(),
                            hmapDiaSemanaHorarios, chave, cargaHoraria,
                            datasBloqueadas, cancelamentosAdm, cancelamentosAhmPorCaId,
                            sprints, semestreSelecionado);
                    temasJaVistos.add(chave);
                    if (!dataFim.equals(dataInicio)) dataInicio = dataFim;
                }
            }

            // --- PERSISTÊNCIA ---
            // Recupera ou cria o registro de planejamento
            AtribuicaoProfessor ap = apDao.buscarPorDisciplinaESemestre(
                    logado.getIdDisciplina(), idSemestreLetivo);
            Integer idAtribuicao = ap.getId_atribuicao_professor();

            Integer idPlanejamento = pDao.buscarOuCriarPlanejamento(idAtribuicao);

            // Apaga os slots anteriores antes de reinserir
            slotDAO.deletarPorPlanejamento(idPlanejamento);

            // Monta e persiste cada slot gerado
            List<SlotPlanejamento> slotsParaSalvar = new ArrayList<>();
            for (Map.Entry<Tema, HashMap<LocalDate, List<HorarioCurso>>> entryTema
                    : hMapTemaDataHorarios.entrySet()) {

                Tema tema = entryTema.getKey();
                for (Map.Entry<LocalDate, List<HorarioCurso>> entryData
                        : entryTema.getValue().entrySet()) {

                    LocalDate data = entryData.getKey();
                    for (HorarioCurso horario : entryData.getValue()) {
                        SlotPlanejamento slot = new SlotPlanejamento();
                        slot.setPlanejamento_id(idPlanejamento);
                        slot.setData(data);
                        slot.setHorario_curso_id(horario.getId_horario_curso());
                        slot.setTema_id(tema.getId_tema());
                        slot.setStatus("nao_ministrada");
                        slotsParaSalvar.add(slot);
                    }
                }
            }

            slotDAO.inserirEmLote(slotsParaSalvar);

            // Recarrega a tela com os dados recém-persistidos
            atualizarDadosPlanejamento();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizarDadosPlanejamento(){
        // CORREÇÃO: Se a disciplina ou filtros estiverem nulos, limpa tudo visualmente da árvore e encerra
        if (mainShellController == null ||
                logado.getAno() == null ||
                logado.getAnoSemestre() == null ||
                logado.getIdCurso() == null ||
                logado.getIdDisciplina() == null) {

            limparCamposEstatisticas();
            treePlanejamento.setRoot(null);
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

                boolean insuficiente = chMinima > 0 && totalAulas < chMinima;
                lblAvisoCargaInsuficiente.setVisible(insuficiente);
                lblAvisoCargaInsuficiente.setManaged(insuficiente);

                this.aulasGeradas.set(totalAulas);
                this.aulasMinistradas.set(ministradas);
                this.aulasPendentes.set(pendentes);
                this.aulasCanceladas.set(canceladas);
                this.cargaMinima.set(chMinima);
                this.totalTemas.set(totalTemas);

                double progresso = (chMinima > 0) ? (double) ministradas / chMinima : 0.0;
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

        lblAvisoCargaInsuficiente.setVisible(false);
        lblAvisoCargaInsuficiente.setManaged(false);
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
        atualizarDadosPlanejamento();
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
            atualizarDadosPlanejamento();
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