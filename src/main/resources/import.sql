-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

insert into categoria (nome, tipo, cor, ativa) values('Salário', 'RECEITA', '#4F46E5', true);
insert into categoria (nome, tipo, cor, ativa) values('Alimentação', 'DESPESA', '#EF4444', true);
insert into categoria (nome, tipo, cor, ativa) values('Transporte', 'DESPESA', '#F59E0B', true);
insert into categoria (nome, tipo, cor, ativa) values('Serviços', 'RECEITA', '#10B981', true);

insert into transacao (descricao, valor, data, tipo, escopo, categoria_id) values('Salário mensal', 5000.00, '2026-09-05', 'RECEITA', 'PESSOAL', 1);
insert into transacao (descricao, valor, data, tipo, escopo, categoria_id) values('Almoço', 45.90, '2026-09-08', 'DESPESA', 'PESSOAL', 2);
insert into transacao (descricao, valor, data, tipo, escopo, categoria_id) values('Combustível', 200.00, '2026-09-09', 'DESPESA', 'EMPRESA', 3);
insert into transacao (descricao, valor, data, tipo, escopo, categoria_id) values('Consultoria', 1500.00, '2026-09-10', 'RECEITA', 'EMPRESA', 4);

-- estados (municipios sao criados sob demanda pela consulta de CEP, usando o codigo IBGE)
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

insert into contato (nome, tipo, documento, email, telefone, cep, logradouro, numero, complemento, bairro, id_municipio) values('Maria Souza', 'CLIENTE', '52998224725', 'maria@email.com', '63999990000', '77001002', 'Praça Girassóis', 's/n', null, 'Plano Diretor Norte', 1);
insert into contato (nome, tipo, documento, email, telefone, cep, logradouro, numero, complemento, bairro, id_municipio) values('Papelaria Central LTDA', 'FORNECEDOR', '11222333000181', 'contato@papelaria.com', '6332150000', '77001002', 'Praça Girassóis', '100', 'Sala 2', 'Plano Diretor Norte', 1);

-- usuarios (o login fica no Keycloak; aqui so o perfil, por isso senhaHash fica nulo)
insert into usuario (nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo) values('Administrador', 'admin@fincontrol.com', '11144477735', '63999991111', 'PF', null, 'ADMIN', true);
insert into usuario (nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo, cep, logradouro, numero, complemento, bairro, id_municipio) values('João Pereira', 'joao@email.com', '11222333000181', '63988887777', 'MEI', 'SERVICO', 'USER', true, '77001002', 'Praça Girassóis', '200', null, 'Plano Diretor Norte', 1);
insert into usuario (nome, email, documento, telefone, perfilFiscal, atividadeMei, papel, ativo) values('Ana Lima', 'ana@email.com', '12345678909', null, 'PF', null, 'USER', false);

-- planos de assinatura
insert into plano (nome, precoMensal, precoAnual, limiteTransacoes, limiteMetas, liberaRecorrencias, liberaFiscal, liberaExportacao, ativo) values('Essencial', 19.90, 199.00, 1000, 10, true, false, true, true);
insert into plano (nome, precoMensal, precoAnual, limiteTransacoes, limiteMetas, liberaRecorrencias, liberaFiscal, liberaExportacao, ativo) values('Empreendedor', 39.90, 399.00, 10000, 50, true, true, true, true);
insert into plano (nome, precoMensal, precoAnual, limiteTransacoes, limiteMetas, liberaRecorrencias, liberaFiscal, liberaExportacao, ativo) values('Legado 2025', 14.90, 149.00, 500, 5, false, false, false, false);

-- cupons de desconto
insert into cupom (codigo, percentualDesconto, validade, ativo) values('BEMVINDO10', 10.00, '2026-12-31', true);
insert into cupom (codigo, percentualDesconto, validade, ativo) values('BLACKFRIDAY', 30.00, '2026-11-30', true);
insert into cupom (codigo, percentualDesconto, validade, ativo) values('LANCAMENTO', 50.00, '2026-03-31', false);

-- categorias padrao (copiadas para cada conta nova; arquivadas nao sao copiadas)
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Salário', 'RECEITA', '#4F46E5', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Vendas', 'RECEITA', '#10B981', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Alimentação', 'DESPESA', '#EF4444', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Transporte', 'DESPESA', '#F59E0B', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Moradia', 'DESPESA', '#8B5CF6', false);
insert into CategoriaPadrao (nome, tipo, cor, arquivada) values('Telefonia', 'DESPESA', '#6B7280', true);

-- parametros fiscais (valores de exemplo para desenvolvimento: confira os oficiais antes de usar)
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('SALARIO_MINIMO', 1412.00, '2024-01-01', '2024-12-31');
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('SALARIO_MINIMO', 1518.00, '2025-01-01', null);
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('TETO_MEI', 81000.00, '2018-01-01', null);
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('DAS_MEI_PERCENTUAL_INSS', 5.00, '2018-01-01', null);
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('DAS_MEI_ICMS_COMERCIO', 1.00, '2018-01-01', null);
insert into ParametroFiscal (chave, valor, vigenciaInicio, vigenciaFim) values('DAS_MEI_ISS_SERVICO', 5.00, '2018-01-01', null);

-- faixas da tabela progressiva mensal do IRPF (valores de exemplo: confira a tabela oficial da Receita)
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
