--liquibase formatted sql
--changeset elgregos:1
create table if not exists guest (
    id uuid primary key,
    version bigint not null,
    creation_date timestamp not null,
    update_date timestamp not null,
    first_name text not null,
    last_name text not null,
    email text not null
);
--rollback drop table guest;
