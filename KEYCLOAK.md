# Autenticação com Keycloak

O FinControl não gera nem assina token. O **Keycloak** faz o login e emite o JWT; o back-end só valida.

| Papel do Keycloak | Onde fica no código | O que faz |
|---|---|---|
| **Servidor de recursos** | `quarkus-oidc` + `application.properties` | Valida o `Authorization: Bearer` e transforma `realm_access.roles` em roles do `@RolesAllowed` |
| **Login** | `KeycloakTokenClient`, `KeycloakTokenService`, `AuthResource` | `POST /auth/login` repassa e-mail/senha ao Keycloak e devolve o token que ele emitiu |
| **Administração** | `KeycloakAdminClient`, `KeycloakAdminService` | Cria a conta no cadastro, atribui a role, ativa/desativa usuário |

As chamadas de administração usam a **service account** do client `fincontrol-backend`, que só tem permissão de gerenciar usuários do realm (nenhuma senha de admin fica na configuração).

## Subir o Keycloak (desenvolvimento)

Com o Docker rodando, na pasta `fincontrol-backend`:

```bash
docker compose up -d
```

- Console de administração: <http://localhost:8180> (usuário `admin`, senha `admin`).
- Na primeira subida ele importa `keycloak/fincontrol-realm.json`: o realm `fincontrol`, as roles `USER` e `ADMIN`, o client `fincontrol-backend` (secret de desenvolvimento `fincontrol-dev-secret`) e os usuários de teste.

Depois suba o back (`./mvnw quarkus:dev`) e o front (`ng serve`) normalmente.

### Usuários de teste

| E-mail | Senha | Roles | Situação |
|---|---|---|---|
| `admin@fincontrol.com` | `admin12345` | USER, ADMIN | ativo |
| `joao@email.com` | `joao12345` | USER | ativo (MEI) |
| `ana@email.com` | `ana12345` | USER | **desativada** (o login é recusado) |

Os ids desses usuários no realm são os mesmos de `Usuario.keycloakId` no `import.sql`, então o perfil local e a conta do Keycloak já nascem ligados. Contas novas são criadas pela tela **Criar conta** (`POST /auth/registro`).

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

Todas as telas ficam acessíveis sem login. O app sobe mesmo com o Keycloak fora do ar; só o login responde 503.

## Endpoints de autenticação

| Método e caminho | Acesso | Descrição |
|---|---|---|
| `POST /auth/login` | público | `{email, senha}` → `{accessToken, expiresIn, refreshToken, refreshExpiresIn}` |
| `POST /auth/refresh` | público | `{refreshToken}` → tokens novos |
| `POST /auth/logout` | público | `{refreshToken}` → encerra a sessão no Keycloak |
| `POST /auth/registro` | público | cria a conta (Keycloak + perfil) com role `USER` |
| `GET /auth/me` | logado | perfil do dono do token |

Erros seguem o mesmo formato `Problem` (RFC 7807) do resto da API: 401 para credenciais erradas, conta desativada ou sessão expirada; 403 para quem não tem a role; 503 se o Keycloak estiver fora do ar ou o client estiver mal configurado (o motivo real vai para o log).

## Produção

- Passe o secret por variável de ambiente: `KEYCLOAK_CLIENT_SECRET` (e `KEYCLOAK_URL`, se o Keycloak não estiver em `localhost:8180`).
- Gere um secret novo para o client e troque a senha do admin do Keycloak; os valores do realm de desenvolvimento são públicos.
- Exija HTTPS no realm (`sslRequired`) e rode o Keycloak com `start` em vez de `start-dev`.

## Pendências

- **RN12 (isolamento):** `Transacao`, `Categoria` e `Contato` ainda não têm vínculo com `Usuario`, então qualquer `USER` logado vê todos os registros. Os resources têm um `TODO (RN12)`.
- **RN13:** no cadastro, copiar as categorias padrão e criar a assinatura no plano gratuito (TODO em `UsuarioServiceImpl.registrar`).
