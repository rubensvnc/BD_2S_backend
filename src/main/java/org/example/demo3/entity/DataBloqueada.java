import java.time.LocalDate;

public class DataBloqueada {

    private Integer idDataBloqueada;
    private Integer semestreLetivoId;
    private LocalDate data;
    private String motivo;
    private Integer admId;
    private boolean recorrente;


    public DataBloqueada() {
    }


    public DataBloqueada(Integer idDataBloqueada, Integer semestreLetivoId, LocalDate data, String motivo, Integer admId, boolean recorrente) {
        this.idDataBloqueada = idDataBloqueada;
        this.semestreLetivoId = semestreLetivoId;
        this.data = data;
        this.motivo = motivo;
        this.admId = admId;
        this.recorrente = recorrente;
    }


    public Integer getIdDataBloqueada() {
        return idDataBloqueada;
    }

    public void setIdDataBloqueada(Integer idDataBloqueada) {
        this.idDataBloqueada = idDataBloqueada;
    }

    public Integer getSemestreLetivoId() {
        return semestreLetivoId;
    }

    public void setSemestreLetivoId(Integer semestreLetivoId) {
        this.semestreLetivoId = semestreLetivoId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Integer getAdmId() {
        return admId;
    }

    public void setAdmId(Integer admId) {
        this.admId = admId;
    }

    public boolean isRecorrente() {
        return recorrente;
    }

    public void setRecorrente(boolean recorrente) {
        this.recorrente = recorrente;
    }
}