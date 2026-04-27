import java.util.List;
import java.time.LocalDate;


public class CronogramaDao(int idCronograma, List<Tema> temas, LocalDate dataInicio, int idProfessor) {
    LocalDate dataAtual = dataInicio;
    int indexTema = 0;

    while (indexTema < temas.size()) {
        // Se a data atual for um feriado ou restrição do professor...
        if (isDataRestrita(dataAtual, idProfessor)) {
            // Opcional: Registrar no banco que esse dia não teve aula
            salvarItemCronograma(idCronograma, dataAtual, "PERÍODO RESTRITO / SEM AULA", null);

            // Apenas pula para o próximo dia de aula sem consumir o tema
            dataAtual = calcularProximoDiaDeAula(dataAtual);
            continue;
        }

        // Se a data for válida, atribui o tema
        Tema tema = temas.get(indexTema);
        salvarItemCronograma(idCronograma, dataAtual, tema.getNome(), tema.getId());

        // Move para o próximo tema e próxima data
        indexTema++;
        dataAtual = calcularProximoDiaDeAula(dataAtual);
    }
}
