--liquibase formatted sql
--changeset elgregos:12
alter table guest_rsvp
    add column if not exists answers jsonb;
--rollback alter table guest_rsvp drop column if exists answers;

