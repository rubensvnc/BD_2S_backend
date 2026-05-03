package org.example.demo3.entity;

import org.example.demo3.dao.*;
import org.example.demo3.entity.*;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeradorCronograma {

    private AtribuicaoDAO atribuicaoDAO = new AtribuicaoDAO();
    private CronogramaDAO cronogramaDAO = new CronogramaDAO();
    private CronogramaItemDAO cronogramaItemDAO = new CronogramaItemDAO();
    private SemestreDAO semestreDAO = new SemestreDAO();
    private TemaDAO temaDAO = new TemaDAO();
    private DataRestritaTodosDAO dataRestritaDAO = new DataRestritaTodosDAO();
    private DataCanceladaDAO dataCanceladaDAO = new DataCanceladaDAO();
    private SprintDAO sprintDAO = new SprintDAO();
    private GeradorDAO geradorDAO = new GeradorDAO();

    public void gerarCronograma(int professorId, int cursoId, int semestreId) throws SQLException {
        Cronograma cronograma = cronogramaDAO.buscarCronograma(professorId, cursoId, semestreId);
        int cronogramaId;

        if (cronograma == null) {
            Cronograma novo = new Cronograma();
            novo.setUsuario_id(professorId);
            novo.setCurso_id(cursoId);
            novo.setSemestre_id(semestreId);
            novo.setGrade_semestre(1);
            cronogramaId = cronogramaDAO.inserirCronograma(novo);
        } else {
            cronogramaId = cronograma.getId_cronograma();
            List<CronogramaItem> itensExistentes = geradorDAO.listarItensPorCronograma(cronogramaId);
            if (!itensExistentes.isEmpty()) {
                return;
            }
        }

        Semestre semestre = semestreDAO.buscarPorId(semestreId);
        List<Sprint> sprints = sprintDAO.listarPorSemestre(semestreId);
        List<DataRestritaTodos> restritas = dataRestritaDAO.listarTodas();
        List<DataCancelada> canceladas = dataCanceladaDAO.listarCanceladas(professorId);
        List<Atribuicao> atribuicoes = atribuicaoDAO.listarPorProfessorCurso(professorId, cursoId, semestreId);

        for (Atribuicao at : atribuicoes) {
            Disciplina disciplina = geradorDAO.buscarDisciplinaPorId(at.getDisciplina_id());
            List<Tema> temas = temaDAO.listarPorDisciplina(disciplina.getId_disciplina().intValue());
            DayOfWeek diaDaSemana = converterDiaSemana(at.getDia_semana());

            List<LocalDate> diasDisponiveis = obterDiasDisponiveis(
                    semestre.getDataInicio(), semestre.getDataFim(),
                    diaDaSemana, restritas, canceladas);

            int aulasAlocadas = 0;
            int indexDia = 0;

            for (Tema tema : temas) {
                boolean alocado = false;

                while (!alocado && indexDia < diasDisponiveis.size()) {
                    LocalDate dataAtual = diasDisponiveis.get(indexDia);

                    if (tema.isEhAvaliacao() && dataNaTerceiraSemanaSprint(dataAtual, sprints)) {
                        indexDia++;
                        continue;
                    }

                    CronogramaItem item = new CronogramaItem();
                    item.setCronograma_id(cronogramaId);
                    item.setTema_id(tema.getId());
                    item.setData_prevista(dataAtual);
                    item.setQtd_aulas(tema.getQtdMinAulas());
                    item.setStatus_aula("PENDENTE");

                    cronogramaItemDAO.inserirItem(item);
                    aulasAlocadas += tema.getQtdMinAulas();
                    alocado = true;
                    indexDia++;
                }
            }

            if (aulasAlocadas < disciplina.getCargaHoraria() && !temas.isEmpty()) {
                List<LocalDate> sabados = obterDiasDisponiveis(
                        semestre.getDataInicio(), semestre.getDataFim(),
                        DayOfWeek.SATURDAY, restritas, canceladas);

                Collections.reverse(sabados);
                int indexSabado = 0;
                Tema ultimoTema = temas.get(temas.size() - 1);

                while (aulasAlocadas < disciplina.getCargaHoraria() && indexSabado < sabados.size()) {
                    LocalDate dataAtual = sabados.get(indexSabado);

                    if (ultimoTema.isEhAvaliacao() && dataNaTerceiraSemanaSprint(dataAtual, sprints)) {
                        indexSabado++;
                        continue;
                    }

                    int diferenca = disciplina.getCargaHoraria() - aulasAlocadas;
                    int aulasNesteSabado = diferenca > 4 ? 4 : diferenca;

                    CronogramaItem item = new CronogramaItem();
                    item.setCronograma_id(cronogramaId);
                    item.setTema_id(ultimoTema.getId());
                    item.setData_prevista(dataAtual);
                    item.setQtd_aulas(aulasNesteSabado);
                    item.setStatus_aula("PENDENTE");

                    cronogramaItemDAO.inserirItem(item);
                    aulasAlocadas += aulasNesteSabado;
                    indexSabado++;
                }

                if (aulasAlocadas < disciplina.getCargaHoraria()) {
                    System.out.println("Impossivel cumprir a carga horaria da disciplina: " + disciplina.getNome());
                }
            }
        }
    }

    private List<LocalDate> obterDiasDisponiveis(LocalDate inicio, LocalDate fim, DayOfWeek diaAlvo,
                                                 List<DataRestritaTodos> restritas, List<DataCancelada> canceladas) {
        List<LocalDate> dias = new ArrayList<>();
        LocalDate atual = inicio;

        while (!atual.isAfter(fim)) {
            if (atual.getDayOfWeek() == diaAlvo) {
                if (!diaBloqueado(atual, restritas) && !diaCancelado(atual, canceladas)) {
                    dias.add(atual);
                }
            }
            atual = atual.plusDays(1);
        }
        return dias;
    }

    private boolean diaBloqueado(LocalDate data, List<DataRestritaTodos> restritas) {
        for (DataRestritaTodos r : restritas) {
            if (r.getData_bloqueio().equals(data)) {
                return true;
            }
        }
        return false;
    }

    private boolean diaCancelado(LocalDate data, List<DataCancelada> canceladas) {
        for (DataCancelada c : canceladas) {
            if (c.getData_bloqueio().equals(data)) {
                return true;
            }
        }
        return false;
    }

    private boolean dataNaTerceiraSemanaSprint(LocalDate data, List<Sprint> sprints) {
        for (Sprint s : sprints) {
            LocalDate inicioSprint = s.getData_inicio();
            LocalDate inicioTerceiraSem = inicioSprint.plusDays(14);
            LocalDate fimTerceiraSem = inicioSprint.plusDays(20);

            if (!data.isBefore(inicioTerceiraSem) && !data.isAfter(fimTerceiraSem)) {
                return true;
            }
        }
        return false;
    }

    private DayOfWeek converterDiaSemana(String dia) {
        switch (dia.toUpperCase()) {
            case "SEGUNDA": return DayOfWeek.MONDAY;
            case "TERCA": return DayOfWeek.TUESDAY;
            case "QUARTA": return DayOfWeek.WEDNESDAY;
            case "QUINTA": return DayOfWeek.THURSDAY;
            case "SEXTA": return DayOfWeek.FRIDAY;
            case "SABADO": return DayOfWeek.SATURDAY;
            default: return DayOfWeek.SUNDAY;
        }
    }
}