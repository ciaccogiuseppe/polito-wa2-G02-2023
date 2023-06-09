BEGIN;
DO
$$
DECLARE
    id bigint;
BEGIN
select nextval('profiles_id_seq') into id;
insert into profiles(id, email, name, surname, role) values (id, 'client1@polito.it', 'ClientA', 'PoliTo', 'CLIENT');
select nextval('profiles_id_seq') into id;
insert into profiles(id, email, name, surname, role) values (id, 'client2@polito.it', 'ClientB', 'PoliTo', 'CLIENT');
select nextval('profiles_id_seq') into id;
insert into profiles(id, email, name, surname, role) values (id, 'expert1@polito.it', 'ExpertA', 'PoliTo', 'EXPERT');
select nextval('profiles_id_seq') into id;
insert into profiles(id, email, name, surname, role) values (id, 'expert2@polito.it', 'ExpertB', 'PoliTo', 'EXPERT');
select nextval('profiles_id_seq') into id;
insert into profiles(id, email, name, surname, role) values (id, 'manager@polito.it', 'Manager', 'PoliTo', 'MANAGER');

insert into products(product_id, name, brand) values ('1234512345123', 'PC', 'HP');
insert into products(product_id, name, brand) values ('5432154321321', 'iPhone', 'Apple');
insert into products(product_id, name, brand) values ('1234567890123', 'smartphone', 'Samsung');
END;
$$;
COMMIT;