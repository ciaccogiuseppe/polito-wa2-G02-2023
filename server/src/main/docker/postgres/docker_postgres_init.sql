create table if not exists categories
(
    id   bigserial
        primary key,
    name varchar(255) not null
);

alter table categories
    owner to postgres;

create table if not exists products
(
    product_id  varchar(255) not null
        primary key,
    brand       varchar(255) not null,
    name        varchar(255) not null,
    category_id bigint       not null
        constraint fkog2rp4qthbtt2lfyhfo32lsw9
            references categories
);

alter table products
    owner to postgres;

create table if not exists profiles
(
    id           bigserial
        primary key,
    address      varchar(255),
    city         varchar(255),
    country      varchar(255),
    email        varchar(255) not null
        constraint uk_lnk8iosvsrn5614xw3lgnybgk
            unique,
    name         varchar(255) not null,
    phone_number varchar(255),
    region       varchar(255),
    role         varchar(255) not null,
    surname      varchar(255) not null
);

alter table profiles
    owner to postgres;

create table if not exists category_assigned
(
    expert_id   bigint not null
        constraint fkhaj7tcvv98any2j4kjnrwnc3s
            references profiles,
    category_id bigint not null
        constraint fkh2e1cphdejxr5wx7q9hllsjyx
            references categories,
    primary key (expert_id, category_id)
);

alter table category_assigned
    owner to postgres;

create table if not exists tickets
(
    id                bigserial
        primary key,
    created_timestamp timestamp(6) not null,
    description       varchar(255) not null,
    priority          integer      not null,
    status            varchar(255) not null,
    title             varchar(255) not null,
    customer_id       bigint       not null
        constraint fkwsg96xnnr1cobwin0fj5xtqe
            references profiles,
    expert_id         bigint
        constraint fk8ojtqms4badovjb5mj7w4se56
            references profiles,
    product_id        varchar(255) not null
        constraint fkavo2av2fyyehcvlec0vowwu1j
            references products
);

alter table tickets
    owner to postgres;

create table if not exists tickets_history
(
    id                bigserial
        primary key,
    new_state         varchar(255) not null,
    old_state         varchar(255) not null,
    updated_timestamp timestamp(6) not null,
    current_expert_id bigint
        constraint fk17muy6m28qespxgus32o6rtj
            references profiles,
    ticket_id         bigint       not null
        constraint fkc7awrpdvaei350mm2hlabffra
            references tickets,
    user_id           bigint       not null
        constraint fks5x4fttcy87h5hqqheeriml2p
            references profiles
);

alter table tickets_history
    owner to postgres;

create table if not exists messages
(
    id             bigserial
        primary key,
    sent_timestamp timestamp(6) not null,
    text           varchar(255) not null,
    sender_id      bigint       not null
        constraint fk79kgt6oyju1ma9ly4qmax5933
            references profiles,
    ticket_id      bigint       not null
        constraint fk6iv985o3ybdk63srj731en4ba
            references tickets
);

alter table messages
    owner to postgres;

create table if not exists attachments
(
    id         bigserial
        primary key,
    attachment bytea        not null,
    name       varchar(255) not null,
    message_id bigint       not null
        constraint fkcf4ta8qdkixetfy7wnqfv3vkv
            references messages
);

alter table attachments
    owner to postgres;

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

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'SMARTPHONE');
        insert into products(product_id, name, brand, category_id) values ('5432154321321', 'iPhone', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('1234567890123', 'smartphone', 'Samsung', id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'TV');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'PC');
        insert into products(product_id, name, brand, category_id) values ('1234512345123', 'PC', 'HP', id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'SOFTWARE');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'STORAGE_DEVICE');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'OTHER');

    END
$$;
COMMIT;
