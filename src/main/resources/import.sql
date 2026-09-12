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
