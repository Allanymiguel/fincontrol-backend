-- Dados de desenvolvimento (recriados a cada subida do back pelo drop-and-create do Hibernate).
-- Ordem respeita as FKs: estado -> municipio -> usuario -> plano/cupom -> categoria/transacao/contato -> assinatura -> pagamento.

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
-- keycloakId = id do mesmo usuario em keycloak/fincontrol-realm.json.
-- Admin nao tem registros operacionais: para usar o sistema de controle financeiro,
-- crie uma conta USER pela tela "Criar conta".
-- =========================================================================
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo) values('0f1c2a3b-0000-4000-8000-000000000001', 'Administrador', 'admin@fincontrol.com', '11144477735', '63999991111', 'PF', null, 'ADMIN', true);
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio) values('0f1c2a3b-0000-4000-8000-000000000002', 'João Pereira', 'joao@email.com', '11222333000181', '63988887777', 'MEI', 'SERVICO', 'USER', true, '77001002', 'Praça Girassóis', '200', null, 'Plano Diretor Norte', 1);
insert into usuario (keycloakId, nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo) values('0f1c2a3b-0000-4000-8000-000000000003', 'Ana Lima', 'ana@email.com', '12345678909', null, 'PF', null, 'USER', false);

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
-- Admin (id 1) e Ana (id 3) nao tem registros aqui.
-- =========================================================================
insert into categoria (usuario_id, nome, tipo, cor, ativa) values(2, 'Salário', 'RECEITA', '#4F46E5', true);
insert into categoria (usuario_id, nome, tipo, cor, ativa) values(2, 'Alimentação', 'DESPESA', '#EF4444', true);
insert into categoria (usuario_id, nome, tipo, cor, ativa) values(2, 'Transporte', 'DESPESA', '#F59E0B', true);
insert into categoria (usuario_id, nome, tipo, cor, ativa) values(2, 'Serviços', 'RECEITA', '#10B981', true);

insert into transacao (usuario_id, descricao, valor, data, tipo, escopo, categoria_id) values(2, 'Salário mensal', 5000.00, '2026-09-05', 'RECEITA', 'PESSOAL', 1);
insert into transacao (usuario_id, descricao, valor, data, tipo, escopo, categoria_id) values(2, 'Almoço', 45.90, '2026-09-08', 'DESPESA', 'PESSOAL', 2);
insert into transacao (usuario_id, descricao, valor, data, tipo, escopo, categoria_id) values(2, 'Combustível', 200.00, '2026-09-09', 'DESPESA', 'EMPRESA', 3);
insert into transacao (usuario_id, descricao, valor, data, tipo, escopo, categoria_id) values(2, 'Consultoria', 1500.00, '2026-09-10', 'RECEITA', 'EMPRESA', 4);

insert into contato (usuario_id, nome, tipo, documento, email, telefone, cep, logradouro, numero, complemento, bairro, id_municipio) values(2, 'Maria Souza', 'CLIENTE', '52998224725', 'maria@email.com', '63999990000', '77001002', 'Praça Girassóis', 's/n', null, 'Plano Diretor Norte', 1);
insert into contato (usuario_id, nome, tipo, documento, email, telefone, cep, logradouro, numero, complemento, bairro, id_municipio) values(2, 'Papelaria Central LTDA', 'FORNECEDOR', '11222333000181', 'contato@papelaria.com', '6332150000', '77001002', 'Praça Girassóis', '100', 'Sala 2', 'Plano Diretor Norte', 1);

-- =========================================================================
-- assinaturas: uma por usuario (RN16); o historico do que foi cobrado fica em pagamento.
-- Planos: 1 = Essencial, 2 = Empreendedor, 3 = Gratuito; cupom 1 = BEMVINDO10. O admin nao tem assinatura.
-- Joao pagou o Essencial mensal de ago/2025 a jan/2026 e passou para o Empreendedor anual
-- com cupom (399,00 - 10% = 359,10), que vence em 01/02/2027.
-- Ana pagou o Essencial mensal por seis meses e voltou ao Gratuito antes de ter a conta desativada.
-- =========================================================================
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao) values(2, 2, 1, 'ANUAL', 'PIX', 'ATIVA', '2025-08-01', 359.10, '2026-02-01', '2027-02-01', 359.10, null);
insert into assinatura (usuario_id, plano_id, cupom_id, periodicidade, formaPagamento, status, dataInicio, valor, dataInicioPeriodo, dataProximaCobranca, valorPeriodo, observacao) values(3, 3, null, 'MENSAL', 'NENHUMA', 'ATIVA', '2025-03-01', 0.00, '2025-09-01', null, 0.00, 'Voltou ao Gratuito em setembro/2025');

insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 'MENSAL', null, 19.90, 'CARTAO', 'APROVADO', '2025-08-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 'MENSAL', null, 19.90, 'CARTAO', 'APROVADO', '2025-09-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 'MENSAL', null, 19.90, 'CARTAO', 'APROVADO', '2025-10-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 'MENSAL', null, 19.90, 'CARTAO', 'APROVADO', '2025-11-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 'MENSAL', null, 19.90, 'CARTAO', 'APROVADO', '2025-12-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 1, 'MENSAL', null, 19.90, 'CARTAO', 'APROVADO', '2026-01-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(1, 2, 'ANUAL', 1, 359.10, 'PIX', 'APROVADO', '2026-02-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 'MENSAL', null, 19.90, 'BOLETO', 'APROVADO', '2025-03-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 'MENSAL', null, 19.90, 'BOLETO', 'APROVADO', '2025-04-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 'MENSAL', null, 19.90, 'BOLETO', 'APROVADO', '2025-05-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 'MENSAL', null, 19.90, 'BOLETO', 'APROVADO', '2025-06-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 'MENSAL', null, 19.90, 'BOLETO', 'APROVADO', '2025-07-01');
insert into pagamento (assinatura_id, plano_id, periodicidade, cupom_id, valor, formaPagamento, status, dataPagamento) values(2, 1, 'MENSAL', null, 19.90, 'BOLETO', 'APROVADO', '2025-08-01');

-- =========================================================================
-- catalogos globais (admin): categorias padrao, parametros fiscais, faixas do IRPF.
-- Categorias padrao nao arquivadas sao copiadas para cada conta nova (RN13).
-- =========================================================================
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Salário', 'RECEITA', '#4F46E5', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Vendas', 'RECEITA', '#10B981', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Alimentação', 'DESPESA', '#EF4444', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Transporte', 'DESPESA', '#F59E0B', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Moradia', 'DESPESA', '#8B5CF6', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Telefonia', 'DESPESA', '#6B7280', true);

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
