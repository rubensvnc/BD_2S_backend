package main.java.org.example.demo3.validacao;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class ValidadorDeDatas {

    private Set<LocalDate> feriados;
    private Set<LocalDate> datasInstitucionais;
    private Set<LocalDate> datasSemestre;

    public ValidadorDeDatas(Set<LocalDate> feriados,
                            Set<LocalDate> datasInstitucionais,
                            Set<LocalDate> datasSemestre) {
        this.feriados = feriados;
        this.datasInstitucionais = datasInstitucionais;
        this.datasSemestre = datasSemestre;
    }

    public boolean podeMarcarAvaliacao(LocalDate data, ProfessorAvaliacao professor) {

        if (data.getDayOfWeek() == DayOfWeek.SATURDAY ||
                data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        if (feriados.contains(data)) {
            return false;
        }

        if (datasInstitucionais.contains(data)) {
            return false;
        }

        if (datasSemestre.contains(data)) {
            return false;
        }

        if (professor.getDatasIndisponiveis().contains(data)) {
            return false;
        }

        return true;
    }
}
