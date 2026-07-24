create table guest_magic_link_token (
    id uuid primary key,
    token text not null unique,
    invitation_id uuid not null references invitation(id) on delete cascade,
    guest_id uuid not null references guest(id),
    creation_date timestamp not null,
    expires_at timestamp not null,
    used_at timestamp null
);

create unique index ux_guest_magic_link_token_guest_unused
    on guest_magic_link_token (guest_id)
    where used_at is null;



