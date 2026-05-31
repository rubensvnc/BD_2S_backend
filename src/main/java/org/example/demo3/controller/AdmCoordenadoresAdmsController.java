package org.example.demo3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.CursoDAO;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.dao.UsuarioTipoDAO;
import org.example.demo3.entity.Curso;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdmCoordenadoresAdmsController {

    // ─── Coordenadores ────────────────────────────────────────────────────────
    @FXML private TableView<Usuario>           tabelaCoordenadores;
    @FXML private TableColumn<Usuario, String> colCoordNome;
    @FXML private TableColumn<Usuario, String> colCoordEmail;
    @FXML private TableColumn<Usuario, String> colCoordCurso;
    @FXML private TableColumn<Usuario, Void>   colCoordAcoes;

    @FXML private Label            lblTituloFormCoord;
    @FXML private TextField        tfCoordNome;
    @FXML private TextField        tfCoordEmail;
    @FXML private PasswordField    pfCoordSenha;
    @FXML private ComboBox<String> cbCoordCurso;

    // ─── Administradores ──────────────────────────────────────────────────────
    @FXML private TableView<Usuario>           tabelaAdms;
    @FXML private TableColumn<Usuario, String> colAdmNome;
    @FXML private TableColumn<Usuario, String> colAdmEmail;

    @FXML private Label         lblTituloFormAdm;
    @FXML private TextField     tfAdmNome;
    @FXML private TextField     tfAdmEmail;
    @FXML private PasswordField pfAdmSenha;

    // ─── DAOs ─────────────────────────────────────────────────────────────────
    private final UsuarioDAO     usuarioDAO     = new UsuarioDAO();
    private final UsuarioTipoDAO usuarioTipoDAO = new UsuarioTipoDAO();
    private final CursoDAO       cursoDAO       = new CursoDAO();

    // ─── Estado ───────────────────────────────────────────────────────────────
    private final UsuarioAtual logado = UsuarioAtual.getInstancia();

    private final ObservableList<Usuario> listaCoordenadoresFX = FXCollections.observableArrayList();
    private final ObservableList<Usuario> listaAdmsFX          = FXCollections.observableArrayList();

    /**
     * id do coordenador → nome do curso.
     * Alimenta a coluna "Curso" sem precisar alterar a entidade Usuario.
     */
    private final Map<Integer, String> mapaCoordParaCurso  = new HashMap<>();

    /**
     * nome do curso → id_curso.
     * Usado ao salvar para recuperar o ID a partir do nome escolhido no ComboBox.
     */
    private final Map<String, Integer> mapaNomeCursoParaId = new HashMap<>();

    private Usuario coordenadorSelecionado;
    private Usuario admSelecionado;

    // =========================================================================
    //  INICIALIZAÇÃO
    // =========================================================================

    @FXML
    public void initialize() {

        // ── Colunas de coordenadores ──────────────────────────────────────────
        colCoordNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCoordEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        if (colCoordCurso != null) {
            colCoordCurso.setCellValueFactory(cellData -> {
                Integer idCoord  = cellData.getValue().getId_usuario();
                String  nomeCurso = mapaCoordParaCurso.getOrDefault(idCoord, "—");
                return new SimpleStringProperty(nomeCurso);
            });
        }

        configurarColunaAcoesCoord();

        // ── Colunas de administradores ────────────────────────────────────────
        colAdmNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colAdmEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tabelaCoordenadores.setItems(listaCoordenadoresFX);
        tabelaAdms.setItems(listaAdmsFX);

        atualizarTabelas();
        recarregarComboBoxCurso();
    }

    // =========================================================================
    //  COLUNA DE AÇÕES — COORDENADORES
    // =========================================================================

    private void configurarColunaAcoesCoord() {
        colCoordAcoes.setCellFactory(col -> new TableCell<>() {

            private final Button btnDeletar = new Button("Excluir");

            {
                btnDeletar.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;"          +
                                "-fx-cursor: hand;"
                );
                btnDeletar.setOnAction(e -> {
                    Usuario coord = getTableView().getItems().get(getIndex());
                    excluirCoordenador(coord);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDeletar);
            }
        });
    }

    // =========================================================================
    //  CARREGAMENTO DE DADOS
    // =========================================================================

    private void atualizarTabelas() {
        try {
            listaCoordenadoresFX.clear();
            listaAdmsFX.clear();
            mapaCoordParaCurso.clear();

            List<Usuario>     todosUsuarios = usuarioDAO.listarUsuarios();
            List<UsuarioTipo> todosTipos    = usuarioTipoDAO.listarUsuariosTipo();
            List<Curso>       todosCursos   = cursoDAO.listarCursos();

            // Monta mapa coordenador_id → nome do curso
            for (Curso c : todosCursos) {
                if (c.getCoordenador_id() != 0) {
                    mapaCoordParaCurso.put(c.getCoordenador_id(), c.getNome());
                }
            }

            // Monta mapa usuario_id → tipo
            Map<Integer, String> mapaTipos = new HashMap<>();
            for (UsuarioTipo ut : todosTipos) {
                mapaTipos.put(ut.getUsuario_id(), ut.getTipo());
            }

            for (Usuario user : todosUsuarios) {
                String tipo = mapaTipos.get(user.getId_usuario());
                if (tipo == null) continue;

                user.setTipo(tipo);

                if ("COORD".equalsIgnoreCase(tipo)) {
                    listaCoordenadoresFX.add(user);
                } else if ("ADM".equalsIgnoreCase(tipo)) {
                    listaAdmsFX.add(user);
                }
            }

            // Força redesenho da coluna Curso (o mapa foi reconstruído)
            tabelaCoordenadores.refresh();

        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao carregar dados: " + e.getMessage());
        }
    }

    /** Popula o ComboBox com TODOS os cursos cadastrados. */
    private void recarregarComboBoxCurso() {
        try {
            mapaNomeCursoParaId.clear();
            ObservableList<String> nomes = FXCollections.observableArrayList();

            for (Curso c : cursoDAO.listarCursos()) {
                nomes.add(c.getNome());
                mapaNomeCursoParaId.put(c.getNome(), c.getId_curso());
            }

            cbCoordCurso.setItems(nomes);

        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao carregar cursos: " + e.getMessage());
        }
    }

    // =========================================================================
    //  COORDENADORES — SELEÇÃO NA TABELA
    // =========================================================================

    @FXML
    public void handleSelecionarCoordenador(MouseEvent event) {
        Usuario selecionado = tabelaCoordenadores.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        coordenadorSelecionado = selecionado;
        lblTituloFormCoord.setText("Editar Coordenador");
        tfCoordNome.setText(coordenadorSelecionado.getNome());
        tfCoordEmail.setText(coordenadorSelecionado.getEmail());
        pfCoordSenha.clear();

        // Pré-seleciona o curso atual do coordenador no ComboBox
        String cursoAtual = mapaCoordParaCurso.get(coordenadorSelecionado.getId_usuario());
        cbCoordCurso.setValue(cursoAtual != null ? cursoAtual : null);
    }

    // =========================================================================
    //  COORDENADORES — FORMULÁRIO
    // =========================================================================

    @FXML
    public void handleNovoCoordenador() {
        coordenadorSelecionado = null;
        tabelaCoordenadores.getSelectionModel().clearSelection();
        lblTituloFormCoord.setText("Novo Coordenador");
        handleLimparFormCoord();
    }

    @FXML
    public void handleLimparFormCoord() {
        tfCoordNome.clear();
        tfCoordEmail.clear();
        pfCoordSenha.clear();
        if (cbCoordCurso != null) cbCoordCurso.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleSalvarCoordenador() {
        String nome      = tfCoordNome.getText().trim();
        String email     = tfCoordEmail.getText().trim();
        String senha     = pfCoordSenha.getText();
        String cursoNome = cbCoordCurso.getValue();

        if (nome.isEmpty() || email.isEmpty()) {
            exibirAlerta("Erro", "Nome e E-mail são obrigatórios.");
            return;
        }

        Integer idCursoSelecionado = (cursoNome != null)
                ? mapaNomeCursoParaId.get(cursoNome)
                : null;

        try {
            if (coordenadorSelecionado == null) {
                // ── INSERÇÃO ──────────────────────────────────────────────────
                if (usuarioDAO.emailJaExiste(email)) {
                    exibirAlerta("Erro", "Este e-mail já está cadastrado.");
                    return;
                }

                Usuario novo = new Usuario();
                novo.setNome(nome);
                novo.setEmail(email);
                novo.setSenha_hash(senha);
                novo.setCriado_em(LocalDate.now());

                int novoId = usuarioDAO.inserirUsuarioRetornandoId(novo);
                if (novoId == -1) {
                    exibirAlerta("Erro", "Falha ao gerar ID do usuário.");
                    return;
                }

                UsuarioTipo ut = new UsuarioTipo();
                ut.setUsuario_id(novoId);
                ut.setTipo("COORD");
                usuarioTipoDAO.inserirUsuarioTipo(ut);

                if (idCursoSelecionado != null) {
                    vincularCoordAoCurso(novoId, idCursoSelecionado);
                }

                exibirAlerta("Sucesso", "Coordenador cadastrado com sucesso!");

            } else {
                // ── EDIÇÃO ────────────────────────────────────────────────────
                coordenadorSelecionado.setNome(nome);
                coordenadorSelecionado.setEmail(email);
                if (!senha.isEmpty()) {
                    coordenadorSelecionado.setSenha_hash(senha);
                }

                usuarioDAO.editarUsuario(coordenadorSelecionado);

                // Remove vínculos antigos e aplica o novo
                for (Curso c : cursoDAO.buscarCursoCoordenador(coordenadorSelecionado.getId_usuario())) {
                    cursoDAO.removerCoordenadorDeCurso(c.getId_curso());
                }
                if (idCursoSelecionado != null) {
                    vincularCoordAoCurso(coordenadorSelecionado.getId_usuario(), idCursoSelecionado);
                }

                exibirAlerta("Sucesso", "Coordenador atualizado com sucesso!");
            }

            atualizarTabelas();
            handleNovoCoordenador();

        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar coordenador: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeletarCoordenador() {
        if (coordenadorSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um coordenador na tabela para excluir.");
            return;
        }
        excluirCoordenador(coordenadorSelecionado);
    }

    // =========================================================================
    //  COORDENADORES — EXCLUSÃO (compartilhada pelo botão inline e pelo handler)
    // =========================================================================

    private void excluirCoordenador(Usuario coord) {
        if (coord == null) return;

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText(
                "Deseja excluir o coordenador \"" + coord.getNome() + "\"?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta != ButtonType.OK) return;
            try {
                for (Curso c : cursoDAO.buscarCursoCoordenador(coord.getId_usuario())) {
                    cursoDAO.removerCoordenadorDeCurso(c.getId_curso());
                }
                usuarioTipoDAO.excluirUsuarioTipo(coord.getId_usuario(), "COORD");
                usuarioDAO.excluirUsuario(coord.getId_usuario());

                exibirAlerta("Sucesso", "Coordenador \"" + coord.getNome() + "\" removido.");
                atualizarTabelas();
                handleNovoCoordenador();

            } catch (Exception e) {
                exibirAlerta("Erro", "Erro ao excluir coordenador: " + e.getMessage());
            }
        });
    }

    // =========================================================================
    //  ADMINISTRADORES — SELEÇÃO NA TABELA
    // =========================================================================

    @FXML
    public void handleSelecionarAdm(MouseEvent event) {
        Usuario selecionado = tabelaAdms.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        admSelecionado = selecionado;
        lblTituloFormAdm.setText("Editar Administrador");
        tfAdmNome.setText(admSelecionado.getNome());
        tfAdmEmail.setText(admSelecionado.getEmail());
        pfAdmSenha.clear();
    }

    // =========================================================================
    //  ADMINISTRADORES — FORMULÁRIO
    // =========================================================================

    @FXML
    public void handleNovoAdm() {
        admSelecionado = null;
        tabelaAdms.getSelectionModel().clearSelection();
        lblTituloFormAdm.setText("Novo Administrador");
        handleLimparFormAdm();
    }

    @FXML
    public void handleLimparFormAdm() {
        tfAdmNome.clear();
        tfAdmEmail.clear();
        pfAdmSenha.clear();
    }

    @FXML
    public void handleSalvarAdm() {
        String nome  = tfAdmNome.getText().trim();
        String email = tfAdmEmail.getText().trim();
        String senha = pfAdmSenha.getText();

        if (nome.isEmpty() || email.isEmpty()) {
            exibirAlerta("Erro", "Campos obrigatórios vazios.");
            return;
        }

        try {
            if (admSelecionado == null) {
                if (usuarioDAO.emailJaExiste(email)) {
                    exibirAlerta("Erro", "Este e-mail já está cadastrado.");
                    return;
                }

                Usuario novoAdm = new Usuario();
                novoAdm.setNome(nome);
                novoAdm.setEmail(email);
                novoAdm.setSenha_hash(senha);
                novoAdm.setCriado_em(LocalDate.now());

                int novoId = usuarioDAO.inserirUsuarioRetornandoId(novoAdm);
                if (novoId != -1) {
                    UsuarioTipo ut = new UsuarioTipo();
                    ut.setUsuario_id(novoId);
                    ut.setTipo("ADM");
                    usuarioTipoDAO.inserirUsuarioTipo(ut);
                    exibirAlerta("Sucesso", "Novo Administrador cadastrado!");
                } else {
                    exibirAlerta("Erro", "Falha ao gerar ID do Administrador.");
                }
            } else {
                admSelecionado.setNome(nome);
                admSelecionado.setEmail(email);
                if (!senha.isEmpty()) admSelecionado.setSenha_hash(senha);

                usuarioDAO.editarUsuario(admSelecionado);
                exibirAlerta("Sucesso", "Administrador atualizado!");
            }

            atualizarTabelas();
            handleNovoAdm();

        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao processar Administrador: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeletarAdm() {
        if (admSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um administrador para excluir.");
            return;
        }

        // Impede que o ADM logado remova a própria conta
        if (admSelecionado.getId_usuario().equals(logado.getId_usuario())) {
            exibirAlerta("Aviso", "Você não pode remover sua própria conta.");
            return;
        }

        try {
            usuarioTipoDAO.excluirUsuarioTipo(admSelecionado.getId_usuario(), "ADM");
            usuarioDAO.excluirUsuario(admSelecionado.getId_usuario());
            exibirAlerta("Sucesso", "Administrador removido.");
            atualizarTabelas();
            handleNovoAdm();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao deletar administrador: " + e.getMessage());
        }
    }

    // =========================================================================
    //  UTILITÁRIOS PRIVADOS
    // =========================================================================

    /** Vincula um coordenador a um curso via UPDATE direto no banco. */
    private void vincularCoordAoCurso(int coordId, int idCurso) throws Exception {
        String sql = "UPDATE curso SET coordenador_id = ? WHERE id_curso = ?";
        try (Connection conn = org.example.demo3.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coordId);
            ps.setInt(2, idCurso);
            ps.executeUpdate();
        }
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}