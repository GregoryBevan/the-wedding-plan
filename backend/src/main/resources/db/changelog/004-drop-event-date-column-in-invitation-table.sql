--liquibase formatted sql
--changeset elgregos:4
alter table invitation drop column if exists event_date;

--rollback alter table invitation add column if not exists event_date date;

