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
