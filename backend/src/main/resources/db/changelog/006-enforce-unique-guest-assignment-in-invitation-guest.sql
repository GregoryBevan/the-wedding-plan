--liquibase formatted sql
--changeset elgregos:6

drop index if exists idx_invitation_guest_guest_id;

alter table invitation_guest
    add constraint uk_invitation_guest_guest_id unique (guest_id);

--rollback alter table invitation_guest drop constraint if exists uk_invitation_guest_guest_id; create index if not exists idx_invitation_guest_guest_id on invitation_guest (guest_id);

