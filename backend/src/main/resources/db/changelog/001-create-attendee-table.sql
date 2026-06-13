--liquibase formatted sql
--changeset elgregos:1
create table if not exists attendee (
    id uuid primary key,
    first_name varchar(255) not null,
    last_name varchar(255) not null
);
--rollback drop table attendee;
