# Autenticação com Keycloak

O front fala **só com a API**. A API Quarkus conversa com o **Keycloak**, que confere a senha e emite o JWT.
A API nunca assina token nem guarda senha.

| Papel do Keycloak | Onde fica no código | O que faz |
|---|---|---|
| **Servidor de recursos** | `quarkus-oidc` + `application.properties` | Valida o `Authorization: Bearer` e transforma `realm_access.roles` em roles do `@RolesAllowed` |
| **Sessão** | `AuthResource`, `AuthServiceImpl`, `KeycloakTokenService`, `CookieSessao` | Login, renovação e logout no endpoint de token do realm (OpenID Connect) |
| **Administração** | `KeycloakAdminClient`, `KeycloakAdminService` | Cria a conta no cadastro, atribui a role, ativa/desativa usuário, encerra sessões, envia o e-mail de nova senha |

As chamadas de administração usam a **service account** do client `fincontrol-backend`, que só tem permissão de gerenciar usuários do realm (nenhuma senha de admin fica na configuração).

## Subir o Keycloak (desenvolvimento)

Com o Docker rodando, na pasta `fincontrol-backend`:

```bash
docker compose up -d
```

- Console de administração: <http://localhost:8180> (usuário e senha em `KC_ADMIN_USER`/`KC_ADMIN_PASSWORD` do `.env`; no modelo, `admin`/`admin`). O `docker compose` exige o `.env` (veja o README).
- Na primeira subida ele importa `keycloak/fincontrol-realm.json`: o realm `fincontrol` (em pt-BR, com o tema `fincontrol`), as roles `USER` e `ADMIN`, o client e os usuários de teste.
- **Mailpit** (<http://localhost:8025>): servidor de e-mail falso que recebe o "Esqueci minha senha" do Keycloak.

| Client | Tipo | Para quê |
|---|---|---|
| `fincontrol-backend` | confidencial (secret de desenvolvimento `fincontrol-dev-secret`) | login por senha (*direct access grants*), service account da Admin API e destino do link de nova senha (`redirectUris`) |

O realm também liga a **proteção contra força bruta**: depois de 10 senhas erradas a conta é bloqueada temporariamente.

Quem já tinha o Keycloak rodando de uma versão anterior precisa recriá-lo uma vez (veja *Voltar ao estado inicial*).

Depois suba o back (`./mvnw quarkus:dev`) e o front (`ng serve`) normalmente.

## Como funciona a sessão

1. A tela de login do Angular envia e-mail e senha a `POST /auth/login`; a API pede o token ao Keycloak (*password grant*, escopo `openid`).
2. O **access token** volta no corpo da resposta (o front o guarda em memória). O **refresh token** vai num cookie
   `fincontrol_refresh` `HttpOnly`, `SameSite=Strict`, restrito a `/auth` e `Secure` fora do desenvolvimento.
3. Ao abrir o app, ou quando o access token está vencendo, o front chama `POST /auth/refresh`; o cookie faz o resto.
   Se o Keycloak recusar (401), o cookie é apagado e o app volta ao login. Com o Keycloak fora do ar (503), a sessão é mantida.
4. **Sair** chama `POST /auth/logout`: a API encerra a sessão no Keycloak, apaga o cookie e revoga o `sid`, e o access
   token já emitido passa a receber 401 na hora.
5. **Esqueci minha senha** chama `POST /auth/esqueci-senha`; o Keycloak envia o link (no desenvolvimento, para o
   Mailpit). A nova senha é criada numa página do Keycloak com o visual do FinControl, aberta pelo link. A resposta é
   a mesma exista a conta ou não.

`refresh` e `logout` exigem o header `X-Requested-With` (proteção contra CSRF; sem ele, 403).

O *password grant* é considerado legado nas recomendações atuais do OAuth (o fluxo indicado é o Authorization Code).
Ele foi adotado porque o login é feito na interface Angular e intermediado pelo back-end.

O tema fica em `keycloak/themes/fincontrol/login` (layout em `template.ftl`, CSS em `resources/css/fincontrol.css`
e textos em `messages/messages_pt_BR.properties`). Com `start-dev` o Keycloak não guarda cache do tema: basta salvar
e recarregar a página.

### Usuários de teste

| E-mail | Senha | Roles | Situação |
|---|---|---|---|
| `admin@fincontrol.com` | `admin12345` | ADMIN | ativo (só a área administrativa) |
| `beatriz@fincontrol.com` | `beatriz12345` | ADMIN | ativo (segunda administradora, para o filtro da auditoria) |
| `joao@email.com` | `joao12345` | USER | ativo (MEI) |
| `carla@`, `bruno@`, `fernanda@`, `diego@`, `patricia@`, `luana@`, `marcos@email.com` | primeiro nome + `12345` | USER | ativos |
| `ana@email.com`, `rafael@email.com` | primeiro nome + `12345` | USER | **desativados** (o login é recusado) |

### Desativar e encerrar sessões

O back valida o JWT sozinho, mas a cada requisição também confere o perfil local (`ContaAtivaAugmentor`):

- **Conta desativada** perde o acesso na hora (401 "Conta desativada"), sem esperar o token expirar.
- **Encerrar sessões** (resumo do usuário) chama `POST /users/{id}/logout` no Keycloak e guarda os ids das
  sessões encerradas (claim `sid`); o logout do próprio usuário faz o mesmo com a sessão dele. Um token dessas sessões recebe 401; um login novo funciona normalmente.
  A comparação é por id, e não por horário, porque o relógio do container pode ficar alguns segundos à frente.

Os ids desses usuários no realm são os mesmos de `Usuario.keycloakId` no `import.sql`, então o perfil local e a conta do Keycloak já nascem ligados. Contas novas são criadas pela tela **Criar conta** (`POST /auth/registro/simples` ou `/completo`), sempre com a role `USER`, ou pelo admin em **Usuários** (`POST /admin/usuarios/simples` ou `/completo`), com a role `USER` ou `ADMIN`.

**Os papéis são separados:** o admin só tem `ADMIN` e não acessa Transações, Categorias e Contatos (403), que são dados pessoais de cada usuário. Para usar o controle financeiro, use uma conta `USER` (por exemplo `joao@email.com` ou uma criada em **Criar conta**).

### Voltar ao estado inicial

O Postgres é recriado a cada subida do back (`drop-and-create`), mas o Keycloak guarda os dados dentro do container. Para os dois voltarem a bater (por exemplo, depois de cadastrar contas de teste):

```bash
docker compose down
docker compose up -d
```

## Usar o app sem o Keycloak

Para trabalhar sem o Docker, desligue o login nos dois lados:

- **Back:** `FINCONTROL_AUTH=false ./mvnw quarkus:dev` (no PowerShell: `$env:FINCONTROL_AUTH='false'; ./mvnw quarkus:dev`). Só vale no modo dev; fora dele a autenticação é sempre exigida.
- **Front:** `AUTH_HABILITADA = false` em `src/app/auth/auth-config.ts`.

Todas as telas ficam acessíveis sem login. Sem token, as telas operacionais usam o João como usuário simulado (`fincontrol.auth.dev-fallback-email`). Com o login ligado, o app também abre com o Keycloak fora do ar (sem sessão); o login, o cadastro e a nova senha respondem 503.

## Endpoints de autenticação

| Método e caminho | Acesso | Descrição |
|---|---|---|
| `POST /auth/login` | público | `{email, senha}` → `{accessToken, expiresIn}` + cookie |
| `POST /auth/refresh` | cookie + `X-Requested-With` | novo access token e novo cookie |
| `POST /auth/logout` | cookie + `X-Requested-With` | encerra a sessão e apaga o cookie (204) |
| `POST /auth/esqueci-senha` | público | `{email}` → 204; envia o link de nova senha se a conta existir |
| `POST /auth/registro/simples` | público | cria a conta (Keycloak + perfil) com role `USER`, só com nome, e-mail e senha |
| `POST /auth/registro/completo` | público | idem, com CPF/CNPJ, perfil fiscal, telefone e endereço |
| `GET /auth/me` | logado | perfil do dono do token |

Erros seguem o mesmo formato `Problem` (RFC 7807) do resto da API: 401 para credenciais erradas, token ausente ou inválido, conta desativada ou sessão encerrada; 403 para quem não tem a role; 503 se o Keycloak estiver fora do ar ou o client estiver mal configurado (o motivo real vai para o log).

## Testar pelo Swagger

1. No Swagger (`http://localhost:8080/q/swagger-ui`), execute `POST /auth/login` com `admin@fincontrol.com` / `admin12345`.
2. Copie o `accessToken` da resposta, clique em **Authorize** e cole.

O token vale uns 30 minutos; quando a API voltar a responder 401, repita os passos.

## Produção

- Passe a configuração por variáveis de ambiente (os nomes estão no `.env.example`), no mínimo `KEYCLOAK_URL`, `KEYCLOAK_CLIENT_SECRET`, `PROBLEM_BASE_URL`, `CORS_ORIGINS` e as `DB_*`; use `DB_GENERATION=validate`, `DB_LOAD_SCRIPT=no-file` e `LOG_SQL=false`.
- Gere um secret novo para o client e troque a senha do admin do Keycloak; os valores do realm de desenvolvimento são públicos.
- Exija HTTPS no realm (`sslRequired`) e rode o Keycloak com `start` em vez de `start-dev`.
- Aponte `FRONTEND_URL` para o front de produção e cadastre `<FRONTEND_URL>/login` nos `redirectUris` do client `fincontrol-backend`.
- Mantenha `AUTH_COOKIE_SECURE=true` (HTTPS) e publique o front e a API no mesmo *site*, porque o cookie é `SameSite=Strict`.
- O "Esqueci minha senha" não limita a quantidade de envios: limite no proxy.
- Troque o Mailpit por um servidor SMTP real (`smtpServer` do realm).

## Regras implementadas

- **RN12 (isolamento por usuário):** `Transacao`, `Categoria` e `Contato` têm o campo `usuario`. Cada `USER` vê somente seus próprios registros; tentar acessar o id de outro usuário responde 404 (sem vazar existência). `UsuarioLogadoService` resolve o usuário atual a partir do `sub` do JWT.
- **RN13 (defaults no cadastro):** todo cadastro com papel `USER` (público ou pelo admin, simples ou completo) copia as `CategoriaPadrao` não arquivadas para o novo usuário e cria a `Assinatura` do usuário no plano **Gratuito** (precisa existir no banco; o seed já traz). A cópia é feita **uma única vez**, no momento do cadastro.
  - Alterações posteriores em `CategoriaPadrao` (adicionar, editar, arquivar) **só afetam cadastros feitos a partir daquele momento**. Usuários existentes mantêm as próprias categorias — o controle delas pertence a cada usuário.
- **RN16 (assinatura única e troca de plano):** cada usuário tem **uma** `Assinatura` (o admin não tem). Trocar de plano altera essa mesma assinatura; o histórico do que foi cobrado fica em `Pagamento`. As trocas seguem a lógica da Netflix e são feitas pelo admin em `/admin/usuarios/{id}/assinatura`:
  - **Plano mais caro** (preço por dia maior, já com o cupom): vale **na hora**, sem cobrança. O saldo não usado do período atual vira dias do plano novo, então a próxima cobrança fica mais cedo. Saindo do Gratuito não há saldo: o primeiro ciclo é cobrado na hora.
  - **Plano mais barato:** fica **agendado** para a próxima cobrança; os recursos atuais continuam até lá. Uma nova troca antes da cobrança substitui a agendada, e escolher de novo o plano vigente desfaz o agendamento.
  - **Cancelar o plano pago** = voltar ao Gratuito na próxima cobrança (ninguém fica sem assinatura).
  - **Cobrança:** um agendador diário (`fincontrol.cobranca.cron`) aplica as trocas agendadas e registra um `Pagamento` APROVADO (pagamento simulado, RN15) para cada cobrança vencida. Conta desativada não é cobrada: volta ao Gratuito na data da cobrança.
- **Cadastro simples e completo:** o **simples** tem só o acesso (nome, e-mail e senha); o **completo** inclui CPF/CNPJ (PF usa CPF; MEI e ME usam CNPJ), perfil fiscal, atividade do MEI, telefone e endereço (o município vem da consulta do CEP, que é pública). Cliente e admin podem ter qualquer uma das formas. O `ADMIN` não recebe categorias nem assinatura.
- **Contato simples e completo:** o **simples** (`POST /contatos/simples`) tem nome, tipo e telefone (e-mail opcional); o **completo** (`POST /contatos/completo`) também CPF/CNPJ e endereço com CEP. `PUT /contatos/{id}/completo` completa um contato simples; um completo não volta a ser simples. O CPF/CNPJ é único **por usuário**: dois usuários podem cadastrar o mesmo fornecedor.
- **Edição pelo admin:** `PUT /admin/usuarios/{id}/simples` (nome e e-mail, só para cadastro simples) e `PUT /admin/usuarios/{id}/completo` (completa ou atualiza tudo; um cadastro completo não volta a ser simples). Papel e senha não mudam. Nome e e-mail também mudam no Keycloak, e o e-mail novo passa a ser o login; para isso o realm tem `editUsernameAllowed: true`. Quem já tinha o Keycloak rodando precisa recriá-lo (veja *Voltar ao estado inicial*) para pegar essa configuração.
