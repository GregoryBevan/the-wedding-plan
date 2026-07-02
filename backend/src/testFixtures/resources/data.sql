truncate table invitation_guest, invitation, guest cascade;

insert into guest (id, version, creation_date, update_date, first_name, last_name, email) values ('019e82e0-0cc1-727e-a483-947fea529ef3', 1, '2026-06-13 10:00:00', '2026-06-13 10:00:00', 'John', 'Doe', 'john.doe@example.com');
insert into guest (id, version, creation_date, update_date, first_name, last_name, email) values ('019e8807-5321-75ed-8a40-99a73529e50f', 1, '2026-06-13 10:00:00', '2026-06-13 10:00:00', 'Jane', 'Doe', 'jane.doe@example.com');
insert into guest (id, version, creation_date, update_date, first_name, last_name, email) values ('019f2287-efe1-748b-a7f6-e20962d062cf', 1, '2026-06-13 10:00:00', '2026-06-13 10:00:00', 'Mickael', 'Kael', 'mickael.kael@example.com');

insert into invitation (id, version, creation_date, update_date, label) values ('019f2282-6db9-72a3-8d9d-2f6dc80cb89d', 1, '2026-07-01 10:45:28', '2026-07-01 10:45:28', 'Bridesmaid');
insert into invitation_guest (invitation_id, guest_id) values ('019f2282-6db9-72a3-8d9d-2f6dc80cb89d', '019e8807-5321-75ed-8a40-99a73529e50f');
insert into invitation (id, version, creation_date, update_date, label) values ('019f2282-71a1-7276-87f9-f80375e2570e', 1, '2026-07-01 10:45:28', '2026-07-01 10:45:28', 'Bestman');
insert into invitation_guest (invitation_id, guest_id) values ('019f2282-71a1-7276-87f9-f80375e2570e', '019f2287-efe1-748b-a7f6-e20962d062cf');

