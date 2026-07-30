--liquibase formatted sql
--changeset elgregos:11
create table guest_rsvp (
    id uuid primary key,
    guest_id uuid not null unique references guest(id),
    version bigint not null,
    creation_date timestamp not null,
    update_date timestamp not null,
    attendance text not null
);
--rollback drop table guest_rsvp;

