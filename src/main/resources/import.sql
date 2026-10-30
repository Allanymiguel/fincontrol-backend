-- Dados de desenvolvimento (recriados a cada subida do back pelo drop-and-create do Hibernate).
-- Ordem respeita as FKs: estado -> municipio -> usuario -> plano/cupom -> categoria/transacao/contato -> assinatura -> pagamento -> mudanca.
-- Datas pensadas para a demonstracao de 30/10/2026: nenhuma cobranca vence antes de 01/11/2026.

-- =========================================================================
-- estados (municipios sao criados sob demanda pela consulta de CEP)
-- =========================================================================
insert into estado (nome, sigla) values('Acre', 'AC');
insert into estado (nome, sigla) values('Alagoas', 'AL');
insert into estado (nome, sigla) values('Amapá', 'AP');
insert into estado (nome, sigla) values('Amazonas', 'AM');
insert into estado (nome, sigla) values('Bahia', 'BA');
insert into estado (nome, sigla) values('Ceará', 'CE');
insert into estado (nome, sigla) values('Distrito Federal', 'DF');
insert into estado (nome, sigla) values('Espírito Santo', 'ES');
insert into estado (nome, sigla) values('Goiás', 'GO');
insert into estado (nome, sigla) values('Maranhão', 'MA');
insert into estado (nome, sigla) values('Mato Grosso', 'MT');
insert into estado (nome, sigla) values('Mato Grosso do Sul', 'MS');
insert into estado (nome, sigla) values('Minas Gerais', 'MG');
insert into estado (nome, sigla) values('Pará', 'PA');
insert into estado (nome, sigla) values('Paraíba', 'PB');
insert into estado (nome, sigla) values('Paraná', 'PR');
insert into estado (nome, sigla) values('Pernambuco', 'PE');
insert into estado (nome, sigla) values('Piauí', 'PI');
insert into estado (nome, sigla) values('Rio de Janeiro', 'RJ');
insert into estado (nome, sigla) values('Rio Grande do Norte', 'RN');
insert into estado (nome, sigla) values('Rio Grande do Sul', 'RS');
insert into estado (nome, sigla) values('Rondônia', 'RO');
insert into estado (nome, sigla) values('Roraima', 'RR');
insert into estado (nome, sigla) values('Santa Catarina', 'SC');
insert into estado (nome, sigla) values('São Paulo', 'SP');
insert into estado (nome, sigla) values('Sergipe', 'SE');
insert into estado (nome, sigla) values('Tocantins', 'TO');

insert into municipio (nome, codigo_ibge, id_estado) values('Palmas', '1721000', 27);
insert into municipio (nome, codigo_ibge, id_estado) values('Araguaína', '1702109', 27);

-- =========================================================================
-- usuarios (o login fica no Keycloak; aqui so o perfil, por isso senhaHash fica nulo).
-- keycloakId = id do mesmo usuario em keycloak/fincontrol-realm.json (senha = primeiro nome + 12345).
-- dataCadastro vai explicita: o @PrePersist nao roda em SQL puro e o painel conta os cadastros por ela.
-- Cadastro completo = documento, perfil fiscal e endereco (CEPs reais, conferidos no ViaCEP).
-- Admin nao tem registros operacionais: para usar o sistema de controle financeiro,
-- crie uma conta USER pela tela "Criar conta".
-- ids: 1 admin, 2 Joao, 3 Ana, 4 Carla, 5 Bruno, 6 Fernanda, 7 Diego, 8 Patricia, 9 Rafael, 10 Luana, 11 Marcos,
-- 12 Beatriz (segunda administradora, cadastro simples)
-- =========================================================================
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', '11144477735', '63999991111', 1, null, 2, true, '77006014', 'Quadra ACNE 1 Avenida Juscelino Kubitschek', '100', 'Sala 3', 'Plano Diretor Norte', (select id from municipio where codigo_ibge = '1721000'), '2025-02-01 09:00:00');
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000002', 'João Pereira', 'joao@email.com', '11222333000181', '63988887777', 2, 2, 1, true, '77001002', 'Praça Girassóis', '200', null, 'Plano Diretor Norte', (select id from municipio where codigo_ibge = '1721000'), '2025-08-01 10:15:00');
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000003', 'Ana Lima', 'ana@email.com', '12345678909', null, 1, null, 1, false, '77020524', 'Quadra ARSE 22 Alameda 8', '15', null, 'Plano Diretor Sul', (select id from municipio where codigo_ibge = '1721000'), '2025-03-01 14:20:00');
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000004', 'Carla Mendes', 'carla@email.com', '39053344705', '63991234567', 1, null, 1, true, '77015012', 'Quadra ACSO 1 Avenida Juscelino Kubitschek', '45', 'Apto 302', 'Plano Diretor Sul', (select id from municipio where codigo_ibge = '1721000'), '2026-03-02 08:40:00');
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000005', 'Bruno Costa', 'bruno@email.com', '45321764000117', '63992345678', 2, 1, 1, true, '77803010', 'Rua Dom Orione', '512', null, 'Setor Central', (select id from municipio where codigo_ibge = '1702109'), '2026-05-15 11:05:00');
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000006', 'Fernanda Rocha', 'fernanda@email.com', '71460238001', '63993456789', 1, null, 1, true, '77021090', 'Quadra AE SE 34 Avenida NS 10', '8', null, 'Plano Diretor Sul', (select id from municipio where codigo_ibge = '1721000'), '2026-04-03 16:30:00');
insert into usuario (keycloakId, nome, email, papel, ativo, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000007', 'Diego Alves', 'diego@email.com', 1, true, '2026-10-08 19:45:00');
insert into usuario (keycloakId, nome, email, papel, ativo, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000008', 'Patrícia Gomes', 'patricia@email.com', 1, true, '2026-09-21 13:10:00');
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000009', 'Rafael Souza', 'rafael@email.com', '16899535009', '63994567890', 1, null, 1, false, '77064540', 'Rua 1', '230', null, 'Centro (Taquaralto)', (select id from municipio where codigo_ibge = '1721000'), '2026-05-28 09:25:00');
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000010', 'Luana Martins', 'luana@email.com', '05382419000', '63995678901', 1, null, 1, true, '77818530', 'Rua José de Brito Soares', '77', null, 'Setor Anhangüera', (select id from municipio where codigo_ibge = '1702109'), '2026-08-12 10:00:00');
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000011', 'Marcos Teixeira', 'marcos@email.com', '27184936000176', '6332151234', 3, null, 1, true, '77006014', 'Quadra ACNE 1 Avenida Juscelino Kubitschek', '1500', 'Loja 2', 'Plano Diretor Norte', (select id from municipio where codigo_ibge = '1721000'), '2026-06-20 15:50:00');
insert into usuario (keycloakId, nome, email, papel, ativo, dataCadastro) values('0f1c2a3b-0000-4000-8000-000000000012', 'Beatriz Nunes', 'beatriz@fincontrol.com', 2, true, '2026-01-15 09:00:00');

-- =========================================================================
-- planos e cupons (admin)
-- =========================================================================
insert into plano (nome, precoMensal, precoAnual, limiteTransacoes, limiteMetas, liberaRecorrencias, liberaFiscal, liberaExportacao, ativo) values('Essencial', 19.90, 199.00, 1000, 10, true, false, true, true);
insert into plano (nome, precoMensal, precoAnual, limiteTransacoes, limiteMetas, liberaRecorrencias, liberaFiscal, liberaExportacao, ativo) values('Empreendedor', 39.90, 399.00, 10000, 50, true, true, true, true);
insert into plano (nome, precoMensal, precoAnual, limiteTransacoes, limiteMetas, liberaRecorrencias, liberaFiscal, liberaExportacao, ativo) values('Gratuito', 0.00, 0.00, 50, 1, false, false, false, true);

insert into cupom (codigo, percentualDesconto, validade, ativo) values('BEMVINDO10', 10.00, '2026-12-31', true);
insert into cupom (codigo, percentualDesconto, validade, ativo) values('BLACKFRIDAY', 30.00, '2026-11-30', true);
insert into cupom (codigo, percentualDesconto, validade, ativo) values('LANCAMENTO', 50.00, '2026-03-31', false);

-- =========================================================================
-- dados operacionais do Joao (usuario_id = 2): categorias, transacoes, contatos.
-- Os demais usuarios nao tem registros aqui.
-- =========================================================================
insert into categoria (usuario_id, nome, tipo, cor, ativa) values(2, 'Salário', 1, '#4F46E5', true);
insert into categoria (usuario_id, nome, tipo, cor, ativa) values(2, 'Alimentação', 2, '#EF4444', true);
insert into categoria (usuario_id, nome, tipo, cor, ativa) values(2, 'Transporte', 2, '#F59E0B', true);
insert into categoria (usuario_id, nome, tipo, cor, ativa) values(2, 'Serviços', 1, '#10B981', true);

insert into transacao (usuario_id, descricao, valor, data, tipo, escopo, categoria_id) values(2, 'Salário mensal', 5000.00, '2026-09-05', 1, 1, 1);
insert into transacao (usuario_id, descricao, valor, data, tipo, escopo, categoria_id) values(2, 'Almoço', 45.90, '2026-09-08', 2, 1, 2);
insert into transacao (usuario_id, descricao, valor, data, tipo, escopo, categoria_id) values(2, 'Combustível', 200.00, '2026-09-09', 2, 2, 3);
insert into transacao (usuario_id, descricao, valor, data, tipo, escopo, categoria_id) values(2, 'Consultoria', 1500.00, '2026-09-10', 1, 2, 4);

insert into contato (usuario_id, nome, tipo, documento, email, telefone, cep, logradouro, numero, complemento, bairro, id_municipio) values(2, 'Maria Souza', 1, '52998224725', 'maria@email.com', '63999990000', '77001002', 'Praça Girassóis', 's/n', null, 'Plano Diretor Norte', 1);
insert into contato (usuario_id, nome, tipo, documento, email, telefone, cep, logradouro, numero, complemento, bairro, id_municipio) values(2, 'Papelaria Central LTDA', 2, '11222333000181', 'contato@papelaria.com', '6332150000', '77001002', 'Praça Girassóis', '100', 'Sala 2', 'Plano Diretor Norte', 1);

-- =========================================================================
-- assinaturas: uma por usuario (RN16); o historico do que foi cobrado fica em pagamento.
-- Planos: 1 = Essencial, 2 = Empreendedor, 3 = Gratuito; cupons: 1 = BEMVINDO10, 2 = BLACKFRIDAY.
-- Periodicidade 1 = mensal, 2 = anual; forma 1 = Pix, 2 = cartao, 3 = boleto, 4 = nenhuma. O admin nao tem assinatura.
-- ids: 1 Joao, 2 Ana, 3 Carla, 4 Bruno, 5 Fernanda, 6 Diego, 7 Patricia, 8 Rafael, 9 Luana, 10 Marcos
-- - Joao: Essencial mensal de ago/2025 a jan/2026, depois Empreendedor anual com BEMVINDO10 (399,00 - 10% = 359,10).
-- - Ana: Essencial mensal por seis meses, voltou ao Gratuito e depois teve a conta desativada.
-- - Carla: Essencial mensal desde mar/2026.
-- - Bruno: Empreendedor mensal desde mai/2026, com downgrade para o Essencial agendado para 15/11/2026.
-- - Fernanda: Essencial anual desde abr/2026, com cancelamento (volta ao Gratuito) agendado para 05/04/2027.
-- - Diego e Patricia: cadastro simples, no Gratuito.
-- - Rafael: Empreendedor mensal desde jun/2026; conta desativada em out/2026 (volta ao Gratuito em 01/11).
-- - Luana: Gratuito desde ago/2026, assinou o Essencial em out/2026 com BLACKFRIDAY (19,90 - 30% = 13,93).
-- - Marcos: Essencial em jul e ago/2026, cancelou e voltou ao Gratuito em 01/09/2026.
-- =========================================================================
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao, dataCadastro) values(2, 2, 1, 2, 1, 2, '2025-08-01', 359.10, '2026-02-01', '2027-02-01', 359.10, null, '2025-08-01 10:15:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao, dataCadastro) values(3, 3, null, 1, 4, 2, '2025-03-01', 0.00, '2025-09-01', null, 0.00, 'Voltou ao Gratuito em setembro/2025', '2025-03-01 14:20:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao, dataCadastro) values(4, 1, null, 1, 1, 2, '2026-03-02', 19.90, '2026-10-10', '2026-11-10', 19.90, null, '2026-03-02 08:40:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, proximo_plano_id, proximaPeriodicidade, proximo_cupom_id, proximaFormaPagamento, observacao, dataCadastro) values(5, 2, null, 1, 2, 2, '2026-05-15', 39.90, '2026-10-15', '2026-11-15', 39.90, 1, 1, null, 2, 'Pediu para reduzir o plano a partir de novembro', '2026-05-15 11:05:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, proximo_plano_id, proximaPeriodicidade, proximo_cupom_id, proximaFormaPagamento, observacao, dataCadastro) values(6, 1, null, 2, 3, 2, '2026-04-03', 199.00, '2026-04-05', '2027-04-05', 199.00, 3, 1, null, 4, null, '2026-04-03 16:30:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao, dataCadastro) values(7, 3, null, 1, 4, 2, '2026-10-08', 0.00, '2026-10-08', null, 0.00, 'Plano gratuito aplicado no cadastro', '2026-10-08 19:45:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao, dataCadastro) values(8, 3, null, 1, 4, 2, '2026-09-21', 0.00, '2026-09-21', null, 0.00, 'Plano gratuito aplicado no cadastro', '2026-09-21 13:10:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao, dataCadastro) values(9, 2, null, 1, 1, 2, '2026-05-28', 39.90, '2026-10-01', '2026-11-01', 39.90, 'Conta desativada a pedido do titular', '2026-05-28 09:25:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao, dataCadastro) values(10, 1, 2, 1, 1, 2, '2026-08-12', 13.93, '2026-10-02', '2026-11-02', 13.93, null, '2026-08-12 10:00:00');
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao, dataCadastro) values(11, 3, null, 1, 4, 2, '2026-06-20', 0.00, '2026-09-01', null, 0.00, 'Voltou ao Gratuito em setembro/2026', '2026-06-20 15:50:00');

-- pagamentos (simulados, RN15): plano, periodicidade e cupom copiados do momento da cobranca
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 1, null, 19.90, 2, 2, '2025-08-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 1, null, 19.90, 2, 2, '2025-09-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 1, null, 19.90, 2, 2, '2025-10-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 1, null, 19.90, 2, 2, '2025-11-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 1, null, 19.90, 2, 2, '2025-12-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 1, null, 19.90, 2, 2, '2026-01-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 2, 2, 1, 359.10, 1, 2, '2026-02-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 1, null, 19.90, 3, 2, '2025-03-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 1, null, 19.90, 3, 2, '2025-04-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 1, null, 19.90, 3, 2, '2025-05-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 1, null, 19.90, 3, 2, '2025-06-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 1, null, 19.90, 3, 2, '2025-07-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 1, null, 19.90, 3, 2, '2025-08-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(3, 1, 1, null, 19.90, 1, 2, '2026-03-10');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(3, 1, 1, null, 19.90, 1, 2, '2026-04-10');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(3, 1, 1, null, 19.90, 1, 2, '2026-05-10');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(3, 1, 1, null, 19.90, 1, 2, '2026-06-10');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(3, 1, 1, null, 19.90, 1, 2, '2026-07-10');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(3, 1, 1, null, 19.90, 1, 2, '2026-08-10');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(3, 1, 1, null, 19.90, 1, 2, '2026-09-10');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(3, 1, 1, null, 19.90, 1, 2, '2026-10-10');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(4, 2, 1, null, 39.90, 2, 2, '2026-05-15');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(4, 2, 1, null, 39.90, 2, 2, '2026-06-15');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(4, 2, 1, null, 39.90, 2, 2, '2026-07-15');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(4, 2, 1, null, 39.90, 2, 2, '2026-08-15');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(4, 2, 1, null, 39.90, 2, 2, '2026-09-15');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(4, 2, 1, null, 39.90, 2, 2, '2026-10-15');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(5, 1, 2, null, 199.00, 3, 2, '2026-04-05');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(8, 2, 1, null, 39.90, 1, 2, '2026-06-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(8, 2, 1, null, 39.90, 1, 2, '2026-07-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(8, 2, 1, null, 39.90, 1, 2, '2026-08-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(8, 2, 1, null, 39.90, 1, 2, '2026-09-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(8, 2, 1, null, 39.90, 1, 2, '2026-10-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(9, 1, 1, 2, 13.93, 1, 2, '2026-10-02');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(10, 1, 1, null, 19.90, 2, 2, '2026-07-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(10, 1, 1, null, 19.90, 2, 2, '2026-08-01');

-- historico das assinaturas (so de insercao). tipo: 1 cadastro, 2 upgrade, 4 downgrade agendado,
-- 5 cancelamento agendado, 8 cancelamento aplicado; origem: 1 cadastro, 2 admin, 3 cobranca.
-- Valores mensais equivalentes (anual / 12), ja com o cupom.
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(1, 1, 1, '2025-08-01', null, null, null, 3, 1, 0.00, null, '2025-08-01 10:15:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(1, 2, 2, '2025-08-01', 3, 1, 0.00, 1, 1, 19.90, null, '2025-08-01 10:30:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(1, 2, 2, '2026-02-01', 1, 1, 19.90, 2, 2, 29.93, null, '2026-02-01 09:00:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(2, 1, 1, '2025-03-01', null, null, null, 3, 1, 0.00, null, '2025-03-01 14:20:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(2, 2, 2, '2025-03-01', 3, 1, 0.00, 1, 1, 19.90, null, '2025-03-01 14:40:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(2, 5, 2, '2025-08-10', 1, 1, 19.90, 3, 1, 0.00, 'Vale a partir de 01/09/2025.', '2025-08-10 16:00:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(2, 8, 3, '2025-09-01', 1, 1, 19.90, 3, 1, 0.00, null, '2025-09-01 03:00:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(3, 1, 1, '2026-03-02', null, null, null, 3, 1, 0.00, null, '2026-03-02 08:40:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(3, 2, 2, '2026-03-10', 3, 1, 0.00, 1, 1, 19.90, null, '2026-03-10 10:20:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(4, 1, 1, '2026-05-15', null, null, null, 3, 1, 0.00, null, '2026-05-15 11:05:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(4, 2, 2, '2026-05-15', 3, 1, 0.00, 2, 1, 39.90, null, '2026-05-15 11:30:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(4, 4, 2, '2026-10-20', 2, 1, 39.90, 1, 1, 19.90, 'Vale a partir de 15/11/2026.', '2026-10-20 14:10:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(5, 1, 1, '2026-04-03', null, null, null, 3, 1, 0.00, null, '2026-04-03 16:30:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(5, 2, 2, '2026-04-05', 3, 1, 0.00, 1, 2, 16.58, null, '2026-04-05 09:15:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(5, 5, 2, '2026-10-12', 1, 2, 16.58, 3, 1, 0.00, 'Vale a partir de 05/04/2027.', '2026-10-12 17:45:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(6, 1, 1, '2026-10-08', null, null, null, 3, 1, 0.00, null, '2026-10-08 19:45:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(7, 1, 1, '2026-09-21', null, null, null, 3, 1, 0.00, null, '2026-09-21 13:10:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(8, 1, 1, '2026-05-28', null, null, null, 3, 1, 0.00, null, '2026-05-28 09:25:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(8, 2, 2, '2026-06-01', 3, 1, 0.00, 2, 1, 39.90, null, '2026-06-01 08:50:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(9, 1, 1, '2026-08-12', null, null, null, 3, 1, 0.00, null, '2026-08-12 10:00:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(9, 2, 2, '2026-10-02', 3, 1, 0.00, 1, 1, 13.93, null, '2026-10-02 11:35:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(10, 1, 1, '2026-06-20', null, null, null, 3, 1, 0.00, null, '2026-06-20 15:50:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(10, 2, 2, '2026-07-01', 3, 1, 0.00, 1, 1, 19.90, null, '2026-07-01 10:05:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(10, 5, 2, '2026-08-15', 1, 1, 19.90, 3, 1, 0.00, 'Vale a partir de 01/09/2026.', '2026-08-15 12:00:00');
insert into MudancaAssinatura (assinatura_id, tipo, origem, data, plano_anterior_id, periodicidadeAnterior, valorMensalAnterior, plano_novo_id, periodicidadeNova, valorMensalNovo, detalhe, dataCadastro) values(10, 8, 3, '2026-09-01', 1, 1, 19.90, 3, 1, 0.00, null, '2026-09-01 03:00:00');

-- =========================================================================
-- auditoria das acoes dos admins (so de insercao). Coerente com os dados acima.
-- acao: 4 desativacao, 5 troca de plano, 7 cancelamento, 8 observacao, 10 alteracao de plano,
-- 12 criacao de cupom, 13 alteracao de cupom, 2 edicao de usuario, 15/16 parametro fiscal, 18 faixa do IRPF.
-- tipoAlvo: 1 usuario, 2 assinatura (id do alvo = id do usuario), 3 plano, 4 cupom, 5 parametro, 6 faixa.
-- Documento e telefone aparecem so como "oculto" (sem valores).
-- =========================================================================
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2025-01-02 09:10:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 15, 5, 2, 'SALARIO_MINIMO (desde 01/01/2025)', '[{"campo":"Chave","antes":null,"depois":"SALARIO_MINIMO","oculto":false},{"campo":"Valor","antes":null,"depois":"1518,00","oculto":false},{"campo":"Início da vigência","antes":null,"depois":"01/01/2025","oculto":false}]', '2025-01-02 09:10:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2025-01-02 09:12:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 16, 5, 1, 'SALARIO_MINIMO (desde 01/01/2024)', '[{"campo":"Fim da vigência","antes":null,"depois":"31/12/2024","oculto":false}]', '2025-01-02 09:12:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2025-05-02 10:00:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 18, 6, 6, 'Tabela de 01/05/2025, a partir de R$ 0,00', '[{"campo":"Vigência","antes":null,"depois":"01/05/2025","oculto":false},{"campo":"Limite inferior","antes":null,"depois":"0,00","oculto":false},{"campo":"Limite superior","antes":null,"depois":"2428,80","oculto":false},{"campo":"Alíquota (%)","antes":null,"depois":"0,00","oculto":false},{"campo":"Parcela a deduzir","antes":null,"depois":"0,00","oculto":false}]', '2025-05-02 10:00:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2025-08-10 16:00:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 7, 2, 3, 'Ana Lima (ana@email.com)', '[{"campo":"Troca agendada","antes":null,"depois":"Gratuito (Mensal)","oculto":false}]', '2025-08-10 16:00:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2025-10-05 11:20:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 4, 1, 3, 'Ana Lima (ana@email.com)', '[{"campo":"Ativo","antes":"Sim","depois":"Não","oculto":false}]', '2025-10-05 11:20:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-03-10 10:20:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 5, 2, 4, 'Carla Mendes (carla@email.com)', '[{"campo":"Plano","antes":"Gratuito","depois":"Essencial","oculto":false},{"campo":"Forma de pagamento","antes":"Nenhuma","depois":"Pix","oculto":false},{"campo":"Valor da cobrança","antes":"0,00","depois":"19,90","oculto":false},{"campo":"Próxima cobrança","antes":null,"depois":"10/04/2026","oculto":false}]', '2026-03-10 10:20:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-04-01 08:30:00', '0f1c2a3b-0000-4000-8000-000000000012', 'Beatriz Nunes', 'beatriz@fincontrol.com', 13, 4, 3, 'LANCAMENTO', '[{"campo":"Ativo","antes":"Sim","depois":"Não","oculto":false}]', '2026-04-01 08:30:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-07-01 10:05:00', '0f1c2a3b-0000-4000-8000-000000000012', 'Beatriz Nunes', 'beatriz@fincontrol.com', 5, 2, 11, 'Marcos Teixeira (marcos@email.com)', '[{"campo":"Plano","antes":"Gratuito","depois":"Essencial","oculto":false},{"campo":"Forma de pagamento","antes":"Nenhuma","depois":"Cartão","oculto":false},{"campo":"Valor da cobrança","antes":"0,00","depois":"19,90","oculto":false},{"campo":"Próxima cobrança","antes":null,"depois":"01/08/2026","oculto":false}]', '2026-07-01 10:05:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-08-15 12:00:00', '0f1c2a3b-0000-4000-8000-000000000012', 'Beatriz Nunes', 'beatriz@fincontrol.com', 7, 2, 11, 'Marcos Teixeira (marcos@email.com)', '[{"campo":"Troca agendada","antes":null,"depois":"Gratuito (Mensal)","oculto":false}]', '2026-08-15 12:00:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-09-28 15:40:00', '0f1c2a3b-0000-4000-8000-000000000012', 'Beatriz Nunes', 'beatriz@fincontrol.com', 2, 1, 4, 'Carla Mendes (carla@email.com)', '[{"campo":"Telefone","antes":null,"depois":null,"oculto":true},{"campo":"Complemento","antes":"Apto 301","depois":"Apto 302","oculto":false}]', '2026-09-28 15:40:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-10-01 09:00:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 12, 4, 2, 'BLACKFRIDAY', '[{"campo":"Código","antes":null,"depois":"BLACKFRIDAY","oculto":false},{"campo":"Desconto (%)","antes":null,"depois":"30,00","oculto":false},{"campo":"Validade","antes":null,"depois":"30/11/2026","oculto":false},{"campo":"Ativo","antes":null,"depois":"Sim","oculto":false}]', '2026-10-01 09:00:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-10-02 11:35:00', '0f1c2a3b-0000-4000-8000-000000000012', 'Beatriz Nunes', 'beatriz@fincontrol.com', 5, 2, 10, 'Luana Martins (luana@email.com)', '[{"campo":"Plano","antes":"Gratuito","depois":"Essencial","oculto":false},{"campo":"Cupom","antes":null,"depois":"BLACKFRIDAY","oculto":false},{"campo":"Forma de pagamento","antes":"Nenhuma","depois":"Pix","oculto":false},{"campo":"Valor da cobrança","antes":"0,00","depois":"13,93","oculto":false},{"campo":"Próxima cobrança","antes":null,"depois":"02/11/2026","oculto":false}]', '2026-10-02 11:35:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-10-12 17:45:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 7, 2, 6, 'Fernanda Rocha (fernanda@email.com)', '[{"campo":"Troca agendada","antes":null,"depois":"Gratuito (Mensal)","oculto":false}]', '2026-10-12 17:45:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-10-18 14:00:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 4, 1, 9, 'Rafael Souza (rafael@email.com)', '[{"campo":"Ativo","antes":"Sim","depois":"Não","oculto":false}]', '2026-10-18 14:00:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-10-20 14:10:00', '0f1c2a3b-0000-4000-8000-000000000012', 'Beatriz Nunes', 'beatriz@fincontrol.com', 5, 2, 5, 'Bruno Costa (bruno@email.com)', '[{"campo":"Troca agendada","antes":null,"depois":"Essencial (Mensal)","oculto":false}]', '2026-10-20 14:10:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-10-20 14:12:00', '0f1c2a3b-0000-4000-8000-000000000012', 'Beatriz Nunes', 'beatriz@fincontrol.com', 8, 2, 5, 'Bruno Costa (bruno@email.com)', '[{"campo":"Observação","antes":null,"depois":"Pediu para reduzir o plano a partir de novembro","oculto":false}]', '2026-10-20 14:12:00');
insert into EventoAuditoria (dataHora, adminKeycloakId, adminNome, adminEmail, acao, tipoAlvo, idAlvo, descricaoAlvo, alteracoes, dataCadastro) values('2026-10-22 10:30:00', '0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', 10, 3, 2, 'Empreendedor', '[{"campo":"Limite de metas","antes":"30","depois":"50","oculto":false}]', '2026-10-22 10:30:00');

-- =========================================================================
-- catalogos globais (admin): categorias padrao, parametros fiscais, faixas do IRPF.
-- Categorias padrao nao arquivadas sao copiadas para cada conta nova (RN13).
-- =========================================================================
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Salário', 1, '#4F46E5', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Vendas', 1, '#10B981', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Alimentação', 2, '#EF4444', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Transporte', 2, '#F59E0B', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Moradia', 2, '#8B5CF6', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Telefonia', 2, '#6B7280', true);

-- valores de exemplo para desenvolvimento: confira os oficiais antes de usar
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('SALARIO_MINIMO', 1412.00, '2024-01-01', '2024-12-31');
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('SALARIO_MINIMO', 1518.00, '2025-01-01', null);
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('TETO_MEI', 81000.00, '2018-01-01', null);
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('DAS_MEI_PERCENTUAL_INSS', 5.00, '2018-01-01', null);
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('DAS_MEI_ICMS_COMERCIO', 1.00, '2018-01-01', null);
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('DAS_MEI_ISS_SERVICO', 5.00, '2018-01-01', null);

-- faixas da tabela progressiva mensal do IRPF (confira a tabela oficial da Receita)
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(0.00, 2259.20, 0.00, 0.00, '2024-02-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(2259.21, 2826.65, 7.50, 169.44, '2024-02-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(2826.66, 3751.05, 15.00, 381.44, '2024-02-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(3751.06, 4664.68, 22.50, 662.77, '2024-02-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(4664.69, null, 27.50, 896.00, '2024-02-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(0.00, 2428.80, 0.00, 0.00, '2025-05-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(2428.81, 2826.65, 7.50, 182.16, '2025-05-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(2826.66, 3751.05, 15.00, 394.16, '2025-05-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(3751.06, 4664.68, 22.50, 675.49, '2025-05-01');
insert into FaixaIrpf (limiteInferior, limiteSuperior, aliquota, parcelaDeduzir, vigenciaInicio) values(4664.69, null, 27.50, 908.73, '2025-05-01');
