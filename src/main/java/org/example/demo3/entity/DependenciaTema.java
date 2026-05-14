package org.example.demo3.entity;


public class DependenciaTema{

    private Integer id_dependencia_tema;
    private Integer tema_id;
    private Integer tema_dependencia_id;
    private Integer ordem;

    public DependenciaTema(){};

    public DependenciaTema(Integer id_dependencia_tema, Integer tema_id,
                           Integer tema_dependencia_id, Integer ordem) {
        this.id_dependencia_tema = id_dependencia_tema;
        this.tema_id = tema_id;
        this.tema_dependencia_id = tema_dependencia_id;
        this.ordem = ordem;
    }

    public Integer getId_dependencia_tema() {
        return id_dependencia_tema;
    }

    public void setId_dependencia_tema(Integer id_dependencia_tema) {
        this.id_dependencia_tema = id_dependencia_tema;
    }

    public Integer getTema_id() {
        return tema_id;
    }

    public void setTema_id(Integer tema_id) {
        this.tema_id = tema_id;
    }

    public Integer getTema_dependencia_id() {
        return tema_dependencia_id;
    }

    public void setTema_dependencia_id(Integer tema_dependencia_id) {
        this.tema_dependencia_id = tema_dependencia_id;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}