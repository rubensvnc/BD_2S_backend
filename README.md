# SwiftPlan — Sistema de Gestão Acadêmica (Planejamento de Sprints, Horários e TCC)

Aplicação desktop em **Java + JavaFX** para gestão de semestres letivos, horários de cursos, atribuições de professores, planejamento de sprints (estilo ágil) e temas de TCC, com diferentes painéis de acesso por perfil de usuário (Administrador, Coordenador e Professor).

## 🎯 Sobre o projeto

O sistema permite que administradores configurem o calendário do semestre (datas de início/fim, feira, TCC, sprints e bloqueios/feriados), coordenadores gerenciem cursos e horários, e professores organizem seu planejamento e temas de trabalho. Toda a lógica de persistência é feita via **JDBC puro** contra um banco **MySQL**, sem uso de frameworks de ORM — decisão que forçou um entendimento mais profundo de SQL e do ciclo de vida de conexões.

## 🛠️ Tecnologias utilizadas

- **Java** (uso de recursos modernos: *text blocks*, `var`, *Streams API*)
- **JavaFX** — interface gráfica desktop (FXML + Controllers)
- **MySQL** — banco de dados relacional
- **JDBC** — acesso a dados via SQL puro (sem ORM)
- **CSS** — estilização customizada da interface JavaFX
- **Maven/estrutura de packages** (`controller`, `dao`, `entity`) — organização em camadas

## 🧠 Técnicas e conceitos aplicados

- **Programação Orientada a Objetos (POO)**: encapsulamento (getters/setters), composição entre entidades, separação clara de responsabilidades entre `Controller`, `DAO` e `Entity`.
- **Padrão DAO (Data Access Object)**: uma classe DAO por entidade (`UsuarioDAO`, `CursoDAO`, `SprintDAO`, etc.), isolando toda a lógica SQL da camada de interface.
- **Padrão MVC (adaptado ao JavaFX)**: FXML como View, Controllers como camada de controle, Entities como Model.
- **Singleton**: classe `UsuarioAtual` implementada como singleton para manter o contexto do usuário logado (sessão) acessível globalmente.
- **JDBC avançado**:
  - `PreparedStatement` para evitar SQL Injection;
  - `try-with-resources` para fechamento automático de `Connection`/`ResultSet`;
  - **Transações manuais** (`setAutoCommit(false)`, `commit()`, `rollback()`) para garantir atomicidade em operações de substituição de dados (ex.: deletar e reinserir horários);
  - **Batch updates** (`addBatch()` / `executeBatch()`) para inserir múltiplos registros de forma performática;
  - Recuperação de chaves geradas automaticamente (`Statement.RETURN_GENERATED_KEYS`);
  - Consultas com **JOIN** (relacionamentos entre tabelas como atribuição × professor × curso) e agregações (`GROUP BY`, `COUNT`).
- **Stream API e programação funcional**: uso extensivo de `.stream()`, `.filter()`, `.map()`, `Collectors.toSet()/toList()`, expressões lambda e `Comparator` para manipular coleções de dados (ex.: cruzamento de horários bloqueados, filtragem de cancelamentos por data).
- **Collections avançadas**: `Map`, `LinkedHashMap`, `Set` e `ObservableList` (JavaFX) para sincronizar dados entre o modelo e a interface.
- **Data Binding e Observer Pattern**: uso de `Property` (`ObjectProperty`, `StringProperty`) e `addListener()` do JavaFX para reagir a mudanças de estado (ex.: atualizar o calendário automaticamente quando o ano/semestre selecionado muda).
- **Manipulação de datas**: `java.time` (`LocalDate`, `LocalTime`, `YearMonth`, `DateTimeFormatter`) para cálculos de calendário, sprints e feriados.
- **Enums** para modelar estados internos de forma segura (ex.: tipo de cancelamento: feriado, dia inteiro ou horário).
- **Configuração externa**: leitura de credenciais de banco via arquivo `.properties` (com fallback entre arquivo externo e recurso interno), evitando hardcode de credenciais no código.
- **Tratamento de exceções** consistente (`SQLException`, `IOException`) com feedback ao usuário via `Alert` do JavaFX.

## 📚 O que eu aprendi

- Como estruturar uma aplicação desktop em camadas (Controller/DAO/Entity), aproximando a lógica do padrão usado em APIs back-end reais.
- Como trabalhar com **JDBC sem ORM**, entendendo na prática o que frameworks como Hibernate/JPA abstraem (conexões, transações, mapeamento manual de `ResultSet` para objetos).
- Como garantir **integridade de dados** usando transações manuais em operações compostas (deletar + inserir).
- Como aplicar o **Singleton** para gerenciar estado de sessão sem acoplar essa responsabilidade aos controllers.
- Como usar **Streams e Collections** para resolver problemas de cruzamento de dados (ex.: identificar conflitos de horário) sem laços `for` aninhados.
- Como conectar a camada de dados ao **Data Binding** do JavaFX, fazendo a UI reagir automaticamente a mudanças de contexto (ano/semestre selecionado).
- A importância de separar configuração (credenciais de banco) do código-fonte.
