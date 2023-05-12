BEGIN;
DO
$$
DECLARE
    id bigint;
BEGIN
select nextval('profiles_id_seq') into id;
insert into profiles(profile_id, email, name, surname, role) values (id, 'mario.rossi@polito.it', 'Mario', 'Rossi', 'EXPERT');
select nextval('profiles_id_seq') into id;
insert into profiles(profile_id, email, name, surname, role) values (id, 'john.doe@polito.it', 'John', 'Doe', 'EXPERT');
select nextval('profiles_id_seq') into id;
insert into profiles(profile_id, email, name, surname, role) values (id, 'jane.doe@polito.it', 'Jane', 'Doe', 'ADMIN');

insert into products(product_id, name, brand) values ('1234512345123', 'PC', 'HP');
insert into products(product_id, name, brand) values ('5432154321321', 'iPhone', 'Apple');
insert into products(product_id, name, brand) values ('1234567890123', 'smartphone', 'Samsung');
END;
$$;
COMMIT;