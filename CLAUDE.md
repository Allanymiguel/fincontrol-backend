# FinControl — back-end

API REST do FinControl (Tópicos II, UNITINS): controle financeiro pessoal/MEI com área administrativa
(planos, assinaturas, cupons, cobrança simulada, auditoria). Quarkus 3.38, Java 25, Hibernate ORM/Panache,
PostgreSQL e Keycloak 26.4 (OIDC).

O front-end Angular é **outro repositório** (`fincontrol-frontend`, normalmente clonado ao lado deste, em
`../fincontrol-frontend`). Mudou um DTO, um endpoint ou o código de um enum? O front precisa da mudança
equivalente, num commit próprio no repositório dele.

Documentação para humanos (não repetir aqui, consultar quando precisar):
- `README.md`: como subir, usuários de demonstração, endpoints da área administrativa, regras de cupom e pagamento.
- `KEYCLOAK.md`: login com PKCE, realm, clients, usar sem Keycloak, produção e as regras RN12, RN13 e RN16.

## Comandos

```bash
cp .env.example .env               # obrigatório: sem .env o Quarkus e o docker compose não sobem
docker compose up -d               # Keycloak :8180, Mailpit :8025 e PostgreSQL :5432
./mvnw quarkus:dev                 # API :8080, Swagger em /q/swagger-ui
./mvnw test                        # testes unitários (JUnit 5, sem banco)
./mvnw test -Dtest=RegrasCupomTest # uma classe de teste
./mvnw package                     # build completo
FINCONTROL_AUTH=false ./mvnw quarkus:dev   # modo dev sem Keycloak (usa o João como usuário simulado)
```

- O `pom.xml` usa `maven.compiler.release=25`: o `JAVA_HOME` precisa apontar para um JDK 25, senão o build
  falha com `release version 25 not supported`.
- **`quarkus:dev` apaga e recria o banco** (`drop-and-create` + `referencia.sql` + `import.sql`). Não rode contra
  um banco com dados que importam.
- O Keycloak guarda as contas no container: para voltar ao realm de `keycloak/fincontrol-realm.json`,
  rode `docker compose down` e depois `docker compose up -d`.

## Arquitetura

Pacote `br.unitins.tp2`, em camadas:

| Pacote | Papel |
|---|---|
| `resource` | JAX-RS. `@RolesAllowed` na classe, delega ao service, converte para DTO com o mapper |
| `service` | Interface `XxxService` + `XxxServiceImpl` (`@ApplicationScoped`). Regras, `@Transactional`, auditoria |
| `repository` | `PanacheRepository<Entidade>`; consultas em JPQL dentro do próprio repository |
| `model` | Entidades (estendem `DefaultEntity`: id, `dataCadastro`, `dataAlteracao`) e enums |
| `dto` | `record`s. `XxxRequestDTO` (entrada, com Bean Validation) e `XxxResponseDTO` (saída) |
| `mapper` | Classes `final` com `static toResponse(...)`; sem MapStruct |
| `converter` | Um `XxxConverter extends EnumIdConverter<Xxx>` (`autoApply`) por enum |
| `exception` | Exceções de domínio e `ExceptionMapper`s que respondem `Problem` (RFC 9457) |
| `util` | Regras puras (`RegrasCupom`, `RegrasCobranca`, `RegrasCep`...), testáveis sem Quarkus |
| `security` | `ContaAtivaAugmentor`: recusa token de conta desativada ou de sessão encerrada |
| `client` | REST clients: Keycloak (token e Admin API) e ViaCEP |

O login acontece só no Keycloak; o back nunca emite nem assina token. `UsuarioLogadoService.atual()` resolve o
`Usuario` local pelo claim `sub` (`keycloakId`). Criar ou editar um usuário também altera o Keycloak pela Admin API
(`KeycloakAdminService`).

## Convenções

- **Idioma:** classes, métodos, campos e endpoints em português (`buscarPorId`, `/admin/cobrancas`).
  Comentários de código em português **sem acentos**; mensagens para o usuário em pt-BR **com acentos**.
- **Endpoints novos:** sempre com `@RolesAllowed("USER")` ou `@RolesAllowed("ADMIN")`. Rota pública só com
  `@PermitAll` explícito (cadastro, consulta de CEP). Como `quarkus.http.auth.proactive=false`, endpoint sem
  anotação fica aberto. Listagens recebem `page` e `pageSize` (padrão `0` e `10`) e devolvem
  `PageResponse.of(itens, page, pageSize, total, mapper)`. Os headers `X-Page`, `X-Page-Size` e `X-Total-Count`
  são adicionados pelo `PaginacaoHeadersFilter`; não os coloque à mão.
- **Validação:** anotações do Bean Validation no `RequestDTO`, com `message` em pt-BR, e `@Valid` no parâmetro
  da **interface do service** (não no resource). Regra de negócio violada: lance
  `ValidationException(mensagem, List.of(new Problem.FieldError("campo", mensagem)))`. O nome do `field` deve ser
  o do DTO, porque o front usa esse nome para mostrar o erro no campo do formulário.
- **Não encontrado:** `ResourceNotFoundException`. Pela **RN12**, transações, categorias e contatos pertencem a um
  usuário: filtre sempre pelo usuário logado, e o id de outro usuário responde 404, sem revelar que existe.
- **Enums:** implementam `EnumComId`, com um `Long` explícito e estável. Esse id é o que vai para o banco e o que a
  API recebe nos campos `idXxx`. Não reordene nem reaproveite ids; ao criar um enum, crie também o converter.
  O front espelha esses ids.
- **Auditoria:** toda ação do admin que altera dados chama `auditoria.registrar(AcaoAuditoria.X, TipoAlvoAuditoria.Y,
  idAlvo, descricaoAlvo, antes, depois)`, com `RetratoAuditoria.de(...)` antes e depois da mudança. A auditoria só recebe inserções.
- **Privacidade (regra do produto):** o admin nunca vê o conteúdo das finanças pessoais (transações, categorias,
  contatos): só quantidades. CPF/CNPJ e telefone não entram na exportação CSV, na auditoria (só "alterado") nem
  na linha do tempo.
- **Configuração:** propriedade nova vai no `application.properties` como `${VARIAVEL:padrao}`. Segredos e URLs
  de ambiente ficam sem valor padrão. Documente a variável no `.env.example`; o `.env` não vai para o git.
- **Testes:** JUnit 5 puro, sem `@QuarkusTest` e sem banco. Para testar uma regra de negócio, extraia a lógica
  para uma classe `util/RegrasXxx` e teste ali.

## Dados de demonstração

`import.sql` é o seed da apresentação; mantenha-o coerente ao mudar o modelo:
- Ids explícitos e ordem das FKs (o topo do arquivo lista a ordem e os ids dos usuários).
- `dataCadastro` explícita: o `@PrePersist` não roda em SQL puro, e o painel conta os cadastros por ela.
- Usuário novo no seed precisa existir também em `keycloak/fincontrol-realm.json`, com o mesmo `keycloakId`.
- Municípios são referenciados pelo código IBGE; estados, municípios e faixas de CEP ficam em `referencia.sql`.

## Git

- Commits no padrão Conventional Commits, em inglês: `feat(cupom): ...`, `fix(assinatura): ...`,
  `chore(seed): ...` (mudanças no `import.sql`), `docs(readme): ...`, `test: ...`.
- Nunca commite o `.env`. Antes de commitar, confira `git diff --cached`.
