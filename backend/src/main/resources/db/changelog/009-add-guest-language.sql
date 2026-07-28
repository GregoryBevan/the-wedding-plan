--liquibase formatted sql
--changeset elgregos:9
alter table guest add column if not exists language text not null default 'FR';
--rollback alter table guest drop column if exists language;

