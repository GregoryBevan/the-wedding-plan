--liquibase formatted sql
--changeset elgregos:10
-- Store magic-link tokens hashed at rest (SHA-256). The application now writes and
-- looks up the token digest, so a database leak never exposes usable bearer tokens.
-- Any in-flight plaintext tokens stop matching after this change; given the short TTL
-- (default 15 min) affected guests simply request a new link.
alter table guest_magic_link_token rename column token to token_hash;
--rollback alter table guest_magic_link_token rename column token_hash to token;

