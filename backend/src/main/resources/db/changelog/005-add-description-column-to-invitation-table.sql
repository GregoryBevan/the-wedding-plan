--liquibase formatted sql
--changeset elgregos:5
alter table invitation add column if not exists description text not null;

--rollback alter table invitation drop column if exists description;

