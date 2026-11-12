# Autenticação com Keycloak

O FinControl não gera nem assina token. O **Keycloak** faz o login e emite o JWT; o back-end só valida.
A senha é digitada **só na tela do Keycloak** (com o visual do FinControl): nem o front nem a API a recebem.

| Papel do Keycloak | Onde fica no código | O que faz |
|---|---|---|
| **Servidor de recursos** | `quarkus-oidc` + `application.properties` | Valida o `Authorization: Bearer` e transforma `realm_access.roles` em roles do `@RolesAllowed` |
| **Login** | front (`keycloak-js` no `AuthService`) e tema `keycloak/themes/fincontrol` | Authorization Code com PKCE: o front redireciona para a tela do Keycloak e recebe os tokens; renovação e logout também vão direto ao Keycloak |
| **Administração** | `KeycloakAdminClient`, `KeycloakAdminService`, `KeycloakTokenService` | Cria a conta no cadastro, atribui a role, ativa/desativa usuário, encerra sessões |

As chamadas de administração usam a **service account** do client `fincontrol-backend`, que só tem permissão de gerenciar usuários do realm (nenhuma senha de admin fica na configuração).

## Subir o Keycloak (desenvolvimento)

Com o Docker rodando, na pasta `fincontrol-backend`:

```bash
docker compose up -d
```

- Console de administração: <http://localhost:8180> (usuário `admin`, senha `admin`).
- Na primeira subida ele importa `keycloak/fincontrol-realm.json`: o realm `fincontrol` (em pt-BR, com o tema de login `fincontrol`), as roles `USER` e `ADMIN`, os clients e os usuários de teste.
- **Mailpit** (<http://localhost:8025>): servidor de e-mail falso que recebe o "Esqueci minha senha" do Keycloak.

| Client | Tipo | Para quê |
|---|---|---|
| `fincontrol-frontend` | público, PKCE (S256) obrigatório | login do front (volta para `http://localhost:4200/*`) e o **Authorize** do Swagger |
| `fincontrol-backend` | confidencial (secret de desenvolvimento `fincontrol-dev-secret`), só service account | Admin API; o login por senha (*direct access grants*) está desligado |

Quem já tinha o Keycloak rodando de uma versão anterior precisa recriá-lo uma vez (veja *Voltar ao estado inicial*).

Depois suba o back (`./mvnw quarkus:dev`) e o front (`ng serve`) normalmente.

## Como funciona o login

1. Ao abrir, o front pergunta ao Keycloak, num iframe invisível, se já existe sessão (`check-sso`). Se existir, entra sem pedir senha.
2. Sem sessão, `/login` (ou qualquer tela protegida) redireciona para a tela de login do Keycloak, com o tema do FinControl.
3. Depois do login, o Keycloak devolve o usuário à tela pedida com um código, que o front troca pelos tokens usando o PKCE.
   Os tokens ficam só em memória; ao recarregar a página, o passo 1 os recupera.
4. O token é renovado direto no Keycloak. Se a renovação falhar (sessão expirada ou encerrada pelo admin, conta desativada), o front volta para o login.
5. **Sair** encerra a sessão no Keycloak e volta para o início do app.

A tela de login tem **Esqueci minha senha** (o e-mail chega no Mailpit) e **Criar conta**, que leva para a tela de cadastro do app.
O tema fica em `keycloak/themes/fincontrol/login` (layout em `template.ftl`, telas `login.ftl` e `login-reset-password.ftl`,
CSS em `resources/css/fincontrol.css` e textos em `messages/messages_pt_BR.properties`). Com `start-dev` o Keycloak não guarda
cache do tema: basta salvar e recarregar a página.

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
  sessões encerradas (claim `sid`). Um token dessas sessões recebe 401; um login novo funciona normalmente.
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

Todas as telas ficam acessíveis sem login. Sem token, as telas operacionais usam o João como usuário simulado (`fincontrol.auth.dev-fallback-email`). Com o login ligado, o app também abre com o Keycloak fora do ar (sem sessão); só o login e o cadastro ficam indisponíveis.

## Endpoints de autenticação

| Método e caminho | Acesso | Descrição |
|---|---|---|
| `POST /auth/registro/simples` | público | cria a conta (Keycloak + perfil) com role `USER`, só com nome, e-mail e senha |
| `POST /auth/registro/completo` | público | idem, com CPF/CNPJ, perfil fiscal, telefone e endereço |
| `GET /auth/me` | logado | perfil do dono do token |

Login, renovação e logout não têm endpoint na API: são feitos direto no Keycloak.

Erros seguem o mesmo formato `Problem` (RFC 7807) do resto da API: 401 para token ausente ou inválido, conta desativada ou sessão encerrada; 403 para quem não tem a role; 503 se o Keycloak estiver fora do ar ou o client estiver mal configurado no cadastro (o motivo real vai para o log).

## Testar pelo Swagger

O Swagger (`http://localhost:8080/q/swagger-ui`) usa o esquema OpenID Connect do realm e faz o login na tela do Keycloak:

1. Clique em **Authorize**. No bloco **authorization_code with PKCE**, o `client_id` já vem preenchido (`fincontrol-frontend`) e o escopo `openid` marcado.
2. Clique em **Authorize** desse bloco: abre uma janela com a tela de login do Keycloak. Entre com `admin@fincontrol.com` / `admin12345`.
3. A janela fecha sozinha e as chamadas passam a levar o token.

O token vale uns 30 minutos; quando a API voltar a responder 401, faça **Logout** e **Authorize** de novo.

## Produção

- Passe o secret por variável de ambiente: `KEYCLOAK_CLIENT_SECRET` (e `KEYCLOAK_URL`, se o Keycloak não estiver em `localhost:8180`).
- Gere um secret novo para o client e troque a senha do admin do Keycloak; os valores do realm de desenvolvimento são públicos.
- Exija HTTPS no realm (`sslRequired`) e rode o Keycloak com `start` em vez de `start-dev`.
- Cadastre os endereços de produção do front em `redirectUris`, `webOrigins` e `post.logout.redirect.uris` do client `fincontrol-frontend`, e ajuste `KEYCLOAK_CONFIG` em `src/app/auth/auth-config.ts` do front.
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
