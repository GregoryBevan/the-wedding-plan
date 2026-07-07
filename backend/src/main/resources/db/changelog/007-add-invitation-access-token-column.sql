--liquibase formatted sql
--changeset elgregos:7
alter table invitation
    add column if not exists access_token text not null default gen_random_uuid()::text,
    add constraint uk_invitation_access_token unique (access_token);

--rollback alter table invitation drop constraint if exists uk_invitation_access_token; alter table invitation alter column access_token drop default; alter table invitation drop column if exists access_token;

