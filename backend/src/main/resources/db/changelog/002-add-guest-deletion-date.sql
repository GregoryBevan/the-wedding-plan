--liquibase formatted sql
--changeset elgregos:2
alter table guest add column if not exists deletion_date timestamp null;
--rollback alter table guest drop column if exists deletion_date;

