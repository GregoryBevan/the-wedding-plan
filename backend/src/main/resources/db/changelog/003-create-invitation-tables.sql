--liquibase formatted sql
--changeset elgregos:3
create table if not exists invitation (
    id uuid primary key,
    creation_date timestamp without time zone not null,
    update_date timestamp without time zone not null,
    title text not null,
    event_date date
);

create table if not exists invitation_guest (
    invitation_id uuid not null,
    guest_id uuid not null,
    primary key (invitation_id, guest_id),
    constraint fk_invitation_guest_invitation
        foreign key (invitation_id)
        references invitation (id)
        on delete cascade,
    constraint fk_invitation_guest_guest
        foreign key (guest_id)
        references guest (id)
        on delete restrict
);

create index if not exists idx_invitation_guest_guest_id on invitation_guest (guest_id);

--rollback drop index if exists idx_invitation_guest_guest_id; drop table if exists invitation_guest; drop table if exists invitation;

