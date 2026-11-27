# FinControl — back-end

API REST do FinControl (Tópicos II, UNITINS): controle financeiro pessoal/MEI com área administrativa
(planos, assinaturas, cupons, cobrança simulada, auditoria). Quarkus 3.38, Java 25, Hibernate ORM/Panache,
PostgreSQL e Keycloak 26.4 (OIDC).

O front-end Angular é **outro repositório** (`fincontrol-frontend`, normalmente clonado ao lado deste, em
`../fincontrol-frontend`). Mudou um DTO, um endpoint ou o código de um enum? O front precisa da mudança
equivalente, num commit próprio no repositório dele.

Documentação para humanos (não repetir aqui, consultar quando precisar):
- `README.md`: como subir, usuários de demonstração, endpoints da área administrativa, regras de cupom e pagamento.
- `KEYCLOAK.md`: sessão pela API, realm, client, usar sem Keycloak, produção e as regras RN12, RN13 e RN16.

## Comandos

```bash
cp .env.example .env               # obrigatório: sem .env o Quarkus e o docker compose não sobem
docker compose up -d               # Keycloak :8180, Mailpit :8025, PostgreSQL :5432 e SeaweedFS (S3 :8333, arquivos :8888)
./mvnw quarkus:dev                 # API :8080, Swagger em /q/swagger-ui
./mvnw test                        # unitários + @QuarkusTest (exige o Docker: PostgreSQL do Dev Services)
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
- O SeaweedFS guarda as imagens num volume que **não** é recriado com o banco: depois de reiniciar o back, os
  planos voltam sem imagem e os objetos antigos ficam órfãos no bucket (esperado em dev).

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
| `security` | `ContaAtivaAugmentor`: recusa token de conta desativada ou de sessão encerrada. `CookieSessao`: cookie do refresh token |
| `client` | REST clients: Keycloak (token e Admin API) e ViaCEP |
| `service/ArmazenamentoService` | Object Storage S3 (`S3Client` do `quarkus-amazon-s3`): enviar, baixar e apagar objetos. `VerificacaoBucket` cria o bucket na subida |

O front fala só com o back, que faz login, renovação e logout no Keycloak (`AuthResource`, `KeycloakTokenService`);
o back nunca assina token nem guarda senha. `UsuarioLogadoService.atual()` resolve o
`Usuario` local pelo claim `sub` (`keycloakId`). Criar ou editar um usuário também altera o Keycloak pela Admin API
(`KeycloakAdminService`).

## Convenções

- **Idioma:** classes, métodos, campos e endpoints em português (`buscarPorId`, `/admin/cobrancas`).
  Comentários de código em português **sem acentos**; mensagens para o usuário em pt-BR **com acentos**.
- **Endpoints novos:** sempre com `@RolesAllowed("USER")` ou `@RolesAllowed("ADMIN")`. Rota pública só com
  `@PermitAll` explícito (login e sessão, cadastro, consulta de CEP). Endpoint que age só com o cookie exige o header
  `X-Requested-With`. Como `quarkus.http.auth.proactive=false`, endpoint sem
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
- **Enum novo ou constante nova** num enum com converter (ex.: `AcaoAuditoria`): inclua também no
  `EnumIdConverterTest`, que trava os códigos; senão o teste `todasAsConstantesTemCodigoTravadoNoTeste` falha.
- **Arquivos (S3):**
  - Valide pelo **conteúdo** (`RegrasImagem.detectar`, magic bytes), nunca pela extensão nem pelo `Content-Type`;
    o tipo, a extensão da chave e o `Content-Type` gravado vêm do tipo detectado.
  - O S3 fica **fora da transação** (método do service sem `@Transactional`): envia o objeto novo, grava os
    metadados em `QuarkusTransaction.requiringNew()` com lock (`findByIdParaAlterar`), e só depois do commit
    apaga o antigo com `excluirSemFalhar`. Se a gravação falhar, apaga o objeto novo. Leia a chave antiga
    **depois** do lock.
  - A chave do bucket nunca sai da API: o DTO expõe só a `versao` (uuid da chave).
  - S3 fora do ar vira `ServicoExternoException` (503); objeto inexistente, `ResourceNotFoundException` (404).
- **Ids na rota:** use `{id: \\d+}` quando houver rotas literais irmãs (ex.: `planos/catalogo` e `planos/{id}`).
- **Testes:**
  - Regra de negócio pura: extraia para `util/RegrasXxx` e teste com JUnit 5, sem Quarkus.
  - Integração: `@QuarkusTest` com o PostgreSQL do Dev Services (perfil `%test` do `application.properties`; o
    seed é carregado). O usuário logado vem de `@TestSecurity` + `@OidcSecurity(claims = @Claim(key = "sub",
    ...))` com os ids de `testes/Usuarios`. O S3 é simulado com `@InjectMock ArmazenamentoService`.
  - O banco de teste é compartilhado entre as classes: crie os próprios planos em vez de alterar os do seed (e,
    se alterar, restaure no `finally`).

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
