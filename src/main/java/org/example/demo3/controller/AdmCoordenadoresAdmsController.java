package org.example.demo3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.example.demo3.DatabaseConnection;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.CursoDAO;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.dao.UsuarioTipoDAO;
import org.example.demo3.entity.Curso;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
    @FXML private Button           btnSalvarCoord;   // fx:id no FXML

    // ─── Administradores ──────────────────────────────────────────────────────
    @FXML private TableView<Usuario>           tabelaAdms;
    @FXML private TableColumn<Usuario, String> colAdmNome;
    @FXML private TableColumn<Usuario, String> colAdmEmail;
    @FXML private TableColumn<Usuario, Void>   colAdmAcoes;

    @FXML private Label         lblTituloFormAdm;
    @FXML private TextField     tfAdmNome;
    @FXML private TextField     tfAdmEmail;
    @FXML private PasswordField pfAdmSenha;
    @FXML private Button        btnSalvarAdm;       // fx:id no FXML

    // ─── DAOs ─────────────────────────────────────────────────────────────────
    private final UsuarioDAO     usuarioDAO     = new UsuarioDAO();
    private final UsuarioTipoDAO usuarioTipoDAO = new UsuarioTipoDAO();
    private final CursoDAO       cursoDAO       = new CursoDAO();

    // ─── Estado ───────────────────────────────────────────────────────────────
    private final UsuarioAtual logado = UsuarioAtual.getInstancia();

    private final ObservableList<Usuario> listaCoordenadoresFX = FXCollections.observableArrayList();
    private final ObservableList<Usuario> listaAdmsFX          = FXCollections.observableArrayList();


    private final Map<Integer, String> mapaCoordParaCurso  = new HashMap<>();


    private final Map<String, Integer> mapaNomeCursoParaId = new HashMap<>();

    private Usuario coordenadorSelecionado;
    private Usuario admSelecionado;

    // Senha mínima de 6 caracteres
    private static final int SENHA_MIN = 6;

    // =========================================================================
    //  INICIALIZAÇÃO
    // =========================================================================

    @FXML
    public void initialize() {

        // ── Colunas de coordenadores ──────────────────────────────────────────
        colCoordNome .setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCoordEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        if (colCoordCurso != null) {
            colCoordCurso.setCellValueFactory(cellData -> {
                Integer idCoord   = cellData.getValue().getId_usuario();
                String  nomeCurso = mapaCoordParaCurso.getOrDefault(idCoord, "—");
                return new SimpleStringProperty(nomeCurso);
            });
        }

        configurarColunaAcoesCoord();

        // ── Colunas de administradores ────────────────────────────────────────
        colAdmNome .setCellValueFactory(new PropertyValueFactory<>("nome"));
        colAdmEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        if (colAdmAcoes != null) {
            configurarColunaAcoesAdm();
        }

        tabelaCoordenadores.setItems(listaCoordenadoresFX);
        tabelaAdms         .setItems(listaAdmsFX);

        // ── Validação de senha em tempo real ──────────────────────────────────
        configurarValidacaoSenha();

        atualizarTabelas();
        recarregarComboBoxCurso(null);
    }

    // =========================================================================
    //  VALIDAÇÃO DE SENHA — botões desabilitados enquanto senha < 6 dígitos
    //  Na edição (senha em branco = "não alterar") o botão permanece habilitado.
    // =========================================================================

    private void configurarValidacaoSenha() {
        // Coordenador
        pfCoordSenha.textProperty().addListener((obs, antigo, novo) ->
                atualizarEstadoBtnCoord()
        );

        // Administrador
        pfAdmSenha.textProperty().addListener((obs, antigo, novo) ->
                atualizarEstadoBtnAdm()
        );
    }


    private void atualizarEstadoBtnCoord() {
        if (btnSalvarCoord == null) return;
        String senha = pfCoordSenha.getText();
        boolean modoEdicao = coordenadorSelecionado != null;
        boolean senhaOk    = senha.isEmpty() && modoEdicao   // edição sem trocar senha
                || senha.length() >= SENHA_MIN;    // nova senha válida
        btnSalvarCoord.setDisable(!senhaOk);
    }


    private void atualizarEstadoBtnAdm() {
        if (btnSalvarAdm == null) return;
        String senha = pfAdmSenha.getText();
        boolean modoEdicao = admSelecionado != null;
        boolean senhaOk    = senha.isEmpty() && modoEdicao
                || senha.length() >= SENHA_MIN;
        btnSalvarAdm.setDisable(!senhaOk);
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
    //  COLUNA DE AÇÕES — ADMINISTRADORES
    //  O botão NÃO aparece para o ADM de id = 1 (superusuário protegido).
    // =========================================================================

    private void configurarColunaAcoesAdm() {
        colAdmAcoes.setCellFactory(col -> new TableCell<>() {

            private final Button btnDeletar = new Button("Excluir");

            {
                btnDeletar.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;"          +
                                "-fx-cursor: hand;"
                );
                btnDeletar.setOnAction(e -> {
                    Usuario adm = getTableView().getItems().get(getIndex());
                    excluirAdm(adm);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Usuario adm = getTableView().getItems().get(getIndex());
                // Oculta o botão para o superusuário protegido (id = 1)
                boolean protegido = adm != null && adm.getId_usuario() == 1;
                setGraphic(protegido ? null : btnDeletar);
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

            // ── 1. Mapa coordenador_id → nome do curso ────────────────────────
            // getCoordenador_id() retorna 0 quando o campo é NULL no banco
            // (comportamento padrão do ResultSet.getInt para NULL).
            for (Curso c : cursoDAO.listarCursos()) {
                if (c.getCoordenador_id() != 0) {
                    mapaCoordParaCurso.put(c.getCoordenador_id(), c.getNome());
                }
            }

            // ── 2. Carrega coordenadores e adms separadamente por tipo ─────────
            // Isso evita o problema de sobrescrita no mapa quando um usuário
            // possui múltiplos registros em usuario_tipo (ex: COORD + PROF).
            List<Usuario> coordenadores = listarUsuariosPorTipo("COORD");
            List<Usuario> adms          = listarUsuariosPorTipo("ADM");

            // Ordena por id_usuario crescente (menor → maior, de cima para baixo)
            coordenadores.stream()
                    .sorted(Comparator.comparingInt(Usuario::getId_usuario))
                    .forEach(listaCoordenadoresFX::add);

            adms.stream()
                    .sorted(Comparator.comparingInt(Usuario::getId_usuario))
                    .forEach(listaAdmsFX::add);

            // Força redesenho da coluna Curso (o mapa foi reconstruído)
            tabelaCoordenadores.refresh();

        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao carregar dados: " + e.getMessage());
        }
    }


    private List<Usuario> listarUsuariosPorTipo(String tipo) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = """
                SELECT u.id_usuario, u.nome, u.email, u.senha_hash
                FROM usuario u
                INNER JOIN usuario_tipo ut ON ut.usuario_id = u.id_usuario
                WHERE ut.tipo = ?
                  AND u.deletado_em IS NULL
                ORDER BY u.id_usuario ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId_usuario(rs.getInt("id_usuario"));
                    u.setNome(rs.getString("nome"));
                    u.setEmail(rs.getString("email"));
                    u.setSenha_hash(rs.getString("senha_hash"));
                    u.setTipo(tipo);
                    lista.add(u);
                }
            }
        }
        return lista;
    }


    private void recarregarComboBoxCurso(String cursoAtualDoCoord) {
        try {
            mapaNomeCursoParaId.clear();
            ObservableList<String> nomes = FXCollections.observableArrayList();

            for (Curso c : cursoDAO.listarCursos()) {
                // getCoordenador_id() == 0 significa NULL no banco
                boolean semCoordenador = (c.getCoordenador_id() == 0);
                boolean eCursoAtual    = c.getNome().equals(cursoAtualDoCoord);

                if (semCoordenador || eCursoAtual) {
                    nomes.add(c.getNome());
                    mapaNomeCursoParaId.put(c.getNome(), c.getId_curso());
                }
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

        String cursoAtual = mapaCoordParaCurso.get(coordenadorSelecionado.getId_usuario());
        recarregarComboBoxCurso(cursoAtual);
        cbCoordCurso.setValue(cursoAtual != null ? cursoAtual : null);

        // Modo edição: senha vazia é permitida (não altera), então habilita o botão
        atualizarEstadoBtnCoord();
    }

    // =========================================================================
    //  COORDENADORES — FORMULÁRIO
    // =========================================================================

    @FXML
    public void handleNovoCoordenador() {
        coordenadorSelecionado = null;
        tabelaCoordenadores.getSelectionModel().clearSelection();
        lblTituloFormCoord.setText("Novo Coordenador");
        recarregarComboBoxCurso(null);
        handleLimparFormCoord();
        // Novo cadastro exige senha: desabilita o botão até senha válida
        atualizarEstadoBtnCoord();
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

        // Validação de senha (redundante com o listener, mas garante consistência)
        boolean modoEdicao = coordenadorSelecionado != null;
        if (!senha.isEmpty() && senha.length() < SENHA_MIN) {
            exibirAlerta("Erro", "A senha deve ter no mínimo " + SENHA_MIN + " caracteres.");
            return;
        }
        if (!modoEdicao && senha.isEmpty()) {
            exibirAlerta("Erro", "Informe uma senha para o novo coordenador.");
            return;
        }

        Integer idCursoSelecionado = (cursoNome != null)
                ? mapaNomeCursoParaId.get(cursoNome)
                : null;

        try {
            if (!modoEdicao) {
                // ── INSERÇÃO ──────────────────────────────────────────────────
                if (usuarioDAO.emailJaExiste(email)) {
                    exibirAlerta("Erro", "Este e-mail já está cadastrado.");
                    return;
                }
                if (idCursoSelecionado != null && cursoJaPossuiCoordenador(idCursoSelecionado)) {
                    exibirAlerta("Erro", "O curso selecionado já possui um coordenador.");
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
    //  COORDENADORES — EXCLUSÃO
    // =========================================================================

    private void excluirCoordenador(Usuario coord) {
        if (coord == null) return;

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja excluir o coordenador \"" + coord.getNome() + "\"?");

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

        // Modo edição: senha vazia é permitida
        atualizarEstadoBtnAdm();
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
        // Novo cadastro exige senha: desabilita o botão até senha válida
        atualizarEstadoBtnAdm();
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

        boolean modoEdicao = admSelecionado != null;

        // Validação de senha (redundante com o listener, mas garante consistência)
        if (!senha.isEmpty() && senha.length() < SENHA_MIN) {
            exibirAlerta("Erro", "A senha deve ter no mínimo " + SENHA_MIN + " caracteres.");
            return;
        }
        if (!modoEdicao && senha.isEmpty()) {
            exibirAlerta("Erro", "Informe uma senha para o novo administrador.");
            return;
        }

        try {
            if (!modoEdicao) {
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
        excluirAdm(admSelecionado);
    }

    // =========================================================================
    //  ADMINISTRADORES — EXCLUSÃO
    //  O ADM de id = 1 é o superusuário protegido e nunca pode ser removido.
    // =========================================================================

    private void excluirAdm(Usuario adm) {
        if (adm == null) return;

        if (adm.getId_usuario() == 1) {
            exibirAlerta("Aviso", "Este administrador não pode ser removido.");
            return;
        }
        if (adm.getId_usuario().equals(logado.getId_usuario())) {
            exibirAlerta("Aviso", "Você não pode remover sua própria conta.");
            return;
        }

        try {
            usuarioTipoDAO.excluirUsuarioTipo(adm.getId_usuario(), "ADM");
            usuarioDAO.excluirUsuario(adm.getId_usuario());
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


    private boolean cursoJaPossuiCoordenador(int idCurso) {
        try {
            for (Curso c : cursoDAO.listarCursos()) {
                if (c.getId_curso() == idCurso && c.getCoordenador_id() != 0) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }


    private void vincularCoordAoCurso(int coordId, int idCurso) throws Exception {
        String sql = "UPDATE curso SET coordenador_id = ? WHERE id_curso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
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