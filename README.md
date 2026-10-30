# FinControl — back-end

API do FinControl (Tópicos II, UNITINS): Quarkus 3.38 com Hibernate/Panache, PostgreSQL e Keycloak 26.4.

## Requisitos

| O quê | Versão |
|---|---|
| JDK | **25** (o `pom.xml` usa `maven.compiler.release=25`; o `JAVA_HOME` precisa apontar para ele) |
| PostgreSQL | banco `topicos2db`, usuário `postgres` |
| Docker Desktop | para o Keycloak 26.4 (`docker-compose.yml`) |

## Como subir

1. **PostgreSQL** rodando, com o banco criado (`create database topicos2db;`).
   A senha padrão está em `application.properties` (`123654`). Se a do seu PostgreSQL for outra, defina a
   variável de ambiente `QUARKUS_DATASOURCE_PASSWORD` (ela não vai para o repositório).
2. **Keycloak:** `docker compose up -d` (console em <http://localhost:8180>). Detalhes em [KEYCLOAK.md](KEYCLOAK.md).
3. **Back:** `./mvnw quarkus:dev` (no Windows, `.\mvnw.cmd quarkus:dev`). API em <http://localhost:8080> e
   Swagger em <http://localhost:8080/q/swagger-ui>.
4. **Front:** veja o README do `fincontrol-frontend`.

## Dados de demonstração

O banco usa `drop-and-create`: **a cada subida do back ele é recriado** e carregado com:

- `referencia.sql`: os 27 estados, os 5.571 municípios da lista oficial do IBGE (gerada uma vez, ligada pelo
  código IBGE) e as faixas de CEP de cada UF;
- `import.sql`: usuários, planos, cupons, assinaturas, pagamentos de 2025 a out/2026, histórico de mudanças
  de assinatura, eventos de auditoria, base de CEPs de clientes, log de consultas de CEP e execuções da cobrança.

Para voltar ao estado inicial, basta reiniciar o back. Para zerar também o Keycloak (contas criadas nos testes):
`docker compose down` e `docker compose up -d`.

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

## Configurações

| Propriedade | Padrão | Para quê |
|---|---|---|
| `fincontrol.cep.api-externa.habilitada` | `true` | valor inicial da consulta ao ViaCEP (o admin liga e desliga na tela, sem reiniciar) |
| `fincontrol.cobranca.cron` | `0 0 3 * * ?` | horário da cobrança automática |

## Testes

```shell
./mvnw test
```

55 testes unitários (JUnit 5, sem banco): códigos dos enums, indicadores do painel, auditoria, regras de CEP,
cobrança, acesso (conta desativada e sessão encerrada) e exportação CSV.
