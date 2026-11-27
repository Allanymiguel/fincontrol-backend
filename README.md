# FinControl — back-end

API do FinControl (Tópicos II, UNITINS): Quarkus 3.38 com Hibernate/Panache, PostgreSQL e Keycloak 26.4.

## Requisitos

| O quê | Versão |
|---|---|
| JDK | **25** (o `pom.xml` usa `maven.compiler.release=25`; o `JAVA_HOME` precisa apontar para ele) |
| PostgreSQL | banco `topicos2db`, usuário `postgres` |
| Docker Desktop | para o Keycloak 26.4, o Mailpit e o SeaweedFS (`docker-compose.yml`), e para os testes (Dev Services) |

## Como subir

0. **Variáveis de ambiente:** copie o modelo para `.env` na raiz do projeto
   (`cp .env.example .env`; no Windows, `copy .env.example .env`) e troque `DB_PASSWORD` pela senha do seu
   PostgreSQL. O `.env` está no `.gitignore` e é lido pelo Quarkus e pelo `docker compose`; sem ele nenhum
   dos dois sobe. Cada variável está comentada no `.env.example`. Quem já tinha um `.env` precisa copiar dele `FRONTEND_URL` e `AUTH_COOKIE_SECURE` (no
   dev há valores padrão) e o bloco **S3** (`S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`).
1. **PostgreSQL** rodando, com o banco criado (`create database topicos2db;`).
2. **Keycloak, Mailpit e SeaweedFS:** `docker compose up -d`. Keycloak em <http://localhost:8180> (com o tema
   do FinControl), Mailpit em <http://localhost:8025> (caixa dos e-mails de "Esqueci minha senha") e
   SeaweedFS com a API S3 em `127.0.0.1:8333` e os arquivos em <http://127.0.0.1:8888/buckets/fincontrol/>.
   Detalhes em [KEYCLOAK.md](KEYCLOAK.md). Quem já tinha o Keycloak rodando de uma versão anterior precisa
   recriá-lo uma vez (`docker compose down` e `docker compose up -d`) para importar o realm novo.
3. **Back:** `./mvnw quarkus:dev` (no Windows, `.\mvnw.cmd quarkus:dev`). API em <http://localhost:8080> e
   Swagger em <http://localhost:8080/q/swagger-ui> (para chamar rotas protegidas, cole em **Authorize** o token de
   `POST /auth/login`).
4. **Front:** veja o README do `fincontrol-frontend`.

## Dados de demonstração

O banco usa `drop-and-create`: **a cada subida do back ele é recriado** e carregado com:

- `referencia.sql`: os 27 estados, os 5.571 municípios da lista oficial do IBGE (gerada uma vez, ligada pelo
  código IBGE) e as faixas de CEP de cada UF;
- `import.sql`: usuários, planos, cupons, assinaturas, pagamentos de 2025 a out/2026, histórico de mudanças
  de assinatura, eventos de auditoria, base de CEPs de clientes, log de consultas de CEP e execuções da cobrança.

Para voltar ao estado inicial, basta reiniciar o back. Para zerar também o Keycloak (contas criadas nos testes):
`docker compose down` e `docker compose up -d`.

O login é feito pela API (`POST /auth/login`), que consulta o Keycloak: o front não fala com ele e a senha não é
gravada no banco. Detalhes em [KEYCLOAK.md](KEYCLOAK.md).

Usuários (senha = primeiro nome em minúsculas + `12345`, ex.: `carla12345`):

| E-mail | Papel | Situação |
|---|---|---|
| `admin@fincontrol.com` (`admin12345`) | ADMIN | ativo |
| `beatriz@fincontrol.com` | ADMIN | ativo |
| `joao@email.com` | USER | Empreendedor anual (MEI) |
| `carla@email.com` | USER | Essencial mensal (pagamento de 10/10 estornado) |
| `bruno@email.com` | USER | Empreendedor, downgrade agendado |
| `fernanda@email.com` | USER | Essencial anual, cancelamento agendado |
| `luana@email.com` | USER | Essencial com cupom BLACKFRIDAY |
| `marcos@email.com` | USER | Empreendedor **pendente**: 2 de 3 cobranças recusadas (simulação ligada) |
| `diego@email.com`, `patricia@email.com` | USER | Gratuito, cadastro simples (incompleto) |
| `ana@email.com`, `rafael@email.com` | USER | **desativados** (Rafael ainda com plano pago) |

## Área administrativa

| Função | Endpoints |
|---|---|
| Painel (MRR, receita, receita em risco, assinantes, conversão) | `GET /admin/dashboard` |
| Auditoria (somente leitura) | `GET /admin/auditoria`, `/filtros`, `/{id}` |
| Usuários: filtros, CSV, linha do tempo, encerrar sessões | `GET /admin/usuarios`, `/exportacao`, `/{id}/linha-do-tempo`, `POST /{id}/sessoes/encerramento` |
| Cobrança: disparo manual ("cobrar até" uma data) e histórico | `POST /admin/cobrancas/processar?data=`, `GET /execucoes`, `/resumo`, `/dias-sem-execucao` |
| Pagamentos: lista com filtros, totais e estorno | `GET /admin/pagamentos`, `/resumo`, `POST /{id}/estorno` |
| Simulação de recusa da cobrança (sem gateway) | `PATCH /admin/usuarios/{id}/assinatura/simulacao-recusa` |
| Cupons: limite de usos, planos e métricas | `GET/POST/PUT /cupons` (`limiteUsos`, `idPlanos`, `usos`, `descontoConcedido`) |
| Imagem do plano: enviar/trocar e remover | `PUT /planos/{id}/imagem` (multipart, parte `arquivo`), `DELETE /planos/{id}/imagem` |
| Imagem do plano: exibir e baixar (público) | `GET /planos/{id}/imagem?v={versao}` (`&download=true` para baixar) |
| Catálogo de planos (USER e ADMIN) | `GET /planos/catalogo` |
| Base de CEPs de clientes | `GET/PUT /admin/ceps`, `/verificacao`, `/revalidacao`, `/importacao`, `/configuracao`, `/consultas` |
| Endereço do cadastro de cliente (público) | `GET /enderecos-cliente/cep/{cep}`, `/municipios`, `/sugestoes` |

Pagamento simulado (RN15): com a simulação de recusa ligada, a cobrança grava o pagamento como recusado, a
assinatura fica **pendente** com o plano atual e o período parado, e a cobrança seguinte tenta de novo; na 3ª
recusa seguida volta ao Gratuito. Cada tentativa vai para o histórico da assinatura e para a linha do tempo.
O **estorno** só mexe no registro financeiro: a receita do mês do estorno diminui, a assinatura não muda.
Cancelar uma assinatura pendente volta ao Gratuito na hora; a troca de plano fica bloqueada até a cobrança passar.

**Cupons**: `limiteUsos` nulo = sem limite e nenhum plano marcado = todos (os cupons antigos continuam assim).
Uso = adesão: a assinatura com o cupom vigente, agendado ou já pago com ele; desfazer um agendamento libera o
uso. As regras valem na troca de plano e na simulação. O desconto de cada cobrança fica gravado no pagamento
(`valorDesconto`), e as métricas somam só os pagamentos aprovados.

Toda listagem é paginada: o corpo traz o `PageResponse` e a resposta repete a paginação nos headers
`X-Page`, `X-Page-Size` e `X-Total-Count`. As fórmulas dos indicadores estão no Swagger.

### Privacidade

- O admin **nunca** vê o conteúdo das finanças pessoais (transações, categorias e contatos), nem valores,
  nomes, somas ou médias: só quantidades.
- Assinatura, pagamento, plano e cupom (a relação do usuário com o FinControl) são visíveis ao admin.
- CPF/CNPJ e telefone não aparecem na exportação, na auditoria (só "alterado", sem valores) nem na linha do tempo.

### Endereço dos clientes

O cadastro completo de cliente (auto-registro e formulário do admin) usa uma **base própria de CEPs**:
base local → ViaCEP (opcional, timeout curto) → modo manual (UF pela faixa do CEP, município da lista do IBGE).
O back valida o endereço antes de criar a conta no Keycloak. Os **contatos** continuam com `GET /cep/{cep}`
e nunca entram na base.

## Imagens dos planos (Object Storage S3)

Trabalho 5: upload, armazenamento, exibição, download e exclusão de arquivos num Object Storage compatível com
a API S3. No FinControl, os "produtos" são os **planos**: cada plano tem **uma imagem**, enviada pelo admin e
exibida no **catálogo de planos** do usuário (front-end).

**Por que SeaweedFS:** é open source (Apache 2.0), roda num único container (`weed mini`: master, volume, filer e
S3 num processo), tem um navegador de arquivos (filer) para mostrar o que foi gravado e é compatível com a API S3,
então o back usa o cliente S3 padrão da AWS (`quarkus-amazon-s3`) e poderia trocar para a AWS só mudando o `.env`.

**Regras:**

- **Validação:** JPEG, PNG ou WebP, até **2 MB** (2 097 152 bytes). O tipo vem do **conteúdo** do arquivo
  (magic bytes), nunca da extensão nem do `Content-Type`: um `.txt` renomeado para `.png` é recusado, e um JPEG
  enviado como `.png` é gravado como `.jpg`. Erros respondem 400 no campo `arquivo`; corpo acima de 3 MB
  recebe 413.
- **Chave no bucket:** `planos/{id}/{uuid}.{ext}`. Cada envio gera uma chave nova; a chave nunca sai da API (o
  front recebe só a `versao`, que é o uuid).
- **Troca:** o objeto novo é enviado, os metadados são gravados (com lock no plano) e só **depois do commit** o
  objeto antigo é apagado. Se a gravação falhar, o objeto novo é apagado.
- **Remoção da imagem:** apaga os metadados e, depois do commit, o objeto. Plano sem imagem: 204, nada muda.
- **Exclusão do plano:** segue as regras de sempre (plano com assinaturas, pagamentos ou cupons não sai); quando
  sai, a imagem é apagada do bucket. Exclusão barrada não apaga nada.
- **Desativar o plano** mantém a imagem; ele só sai do catálogo. A imagem de plano inativo continua pública
  (é conteúdo do catálogo, não dado pessoal).
- **Auditoria:** "Envio de imagem do plano" e "Remoção de imagem do plano", com o antes e depois (nome, tamanho
  e data de envio).
- **Armazenamento fora do ar:** o back sobe mesmo assim (só avisa no log); enviar ou ler uma imagem responde 503
  até o SeaweedFS voltar, sem mudar o banco.

**Leitura, cache e download:** `GET /planos/{id}/imagem` é público (o `<img>` do front não leva token) e o back
lê o objeto no S3. Com `?v=` da versão atual a resposta fica em cache por 1 ano (`immutable`); sem ela, o
navegador revalida com `If-None-Match` e recebe 304 (respondido só com o banco, sem ler o S3). `download=true`
responde com `Content-Disposition: attachment`.

**Arquivos órfãos:** o banco é recriado a cada subida do back (`drop-and-create`), mas o SeaweedFS guarda os
arquivos num volume. Depois de reiniciar o back, os planos voltam sem imagem e os objetos antigos ficam no bucket
sem uso (as chaves são uuids, então nunca colidem). Para zerar só o armazenamento:
`docker compose rm -sf seaweedfs && docker volume rm fincontrol-backend_seaweedfs-data && docker compose up -d`.

**Credenciais e portas:** as credenciais do `.env` só valem na **primeira** subida do SeaweedFS (ficam no volume);
para trocá-las, zere o volume como acima. As portas 8333 e 8888 ficam só em `127.0.0.1`, porque o filer não tem
autenticação.

## Configurações

| Propriedade | Padrão | Para quê |
|---|---|---|
| `quarkus.s3.endpoint-override` | `S3_ENDPOINT` | endereço da API S3 (SeaweedFS: `http://localhost:8333`) |
| `fincontrol.storage.bucket` | `S3_BUCKET` | bucket das imagens (criado na subida se faltar) |
| `quarkus.s3.aws.credentials.static-provider.*` | `S3_ACCESS_KEY`, `S3_SECRET_KEY` | credenciais do S3 |
| `quarkus.http.limits.max-body-size` | `3M` | limite do corpo da requisição (a imagem tem até 2 MB) |
| `fincontrol.cep.api-externa.habilitada` | `true` | valor inicial da consulta ao ViaCEP (o admin liga e desliga na tela, sem reiniciar) |
| `fincontrol.cobranca.cron` | `0 0 3 * * ?` | horário da cobrança automática |

## Testes

```shell
./mvnw test
```

215 testes (JUnit 5). Os unitários (sem banco) cobrem códigos dos enums, indicadores do painel, auditoria,
regras de CEP, cobrança, acesso, exportação CSV e as regras das imagens. Os de integração (`@QuarkusTest`) cobrem
login, controle de acesso (401 e 403), as imagens dos planos e o catálogo com um **PostgreSQL descartável do Dev Services** (Testcontainers) e o S3
simulado (`@InjectMock ArmazenamentoService`): **o Docker precisa estar rodando** para o `./mvnw test`.
