create table if not exists brands
(
    id   bigserial
        primary key,
    name varchar(255) not null
        constraint uk_oce3937d2f4mpfqrycbr0l93m
            unique
);

alter table brands
    owner to postgres;


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
    name        varchar(255) not null,
    brand_id    bigint       not null
        constraint fka3a4mpsfdf4d2y6r8ra3sc8mv
            references brands,
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
    email        varchar(255) not null
        constraint uk_lnk8iosvsrn5614xw3lgnybgk
            unique,
    name         varchar(255) not null,
    phone_number varchar(255),
    role         varchar(255) not null,
    surname      varchar(255) not null
);

alter table profiles
    owner to postgres;

create table if not exists items
(
    serial_num           bigint       not null,
    duration_months      integer,
    uuid                 uuid,
    valid_from_timestamp timestamp(6),
    product_id           varchar(255) not null
        constraint fkmtk37pxnx7d5ck7fkq2xcna4i
            references products,
    client_id            bigint
        constraint fk4lcgine4ykbqt0ee7sh7hukob
            references profiles,
    primary key (product_id, serial_num)
);

alter table items
    owner to postgres;

create table if not exists addresses
(
    id        bigserial
        primary key,
    address   varchar(255),
    city      varchar(255),
    country   varchar(255),
    region    varchar(255),
    client_id bigint not null
        constraint fkdois8l22rrsfm4w5l13wuaamy
            references profiles
);

alter table addresses
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
    product_id        varchar(255) not null,
    serial_num        bigint       not null,
    constraint fkmywnw3u4uhm5x7mcxsmnr6p37
        foreign key (product_id, serial_num) references items
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
    message_id bigint
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
        select nextval('profiles_id_seq') into id;
        insert into profiles(id, email, name, surname, role) values (id, 'vendor@polito.it', 'Vendor', 'PoliTo', 'VENDOR');


        insert into brands(name) values ('Apple');
        insert into brands(name) values ('Samsung');
        insert into brands(name) values ('LG');
        insert into brands(name) values ('Microsoft');
        insert into brands(name) values ('Google');
        insert into brands(name) values ('Amazon');
        insert into brands(name) values ('Dell');
        insert into brands(name) values ('HP');
        insert into brands(name) values ('Lenovo');
        insert into brands(name) values ('ASUS');
        insert into brands(name) values ('Acer');
        insert into brands(name) values ('Toshiba');
        insert into brands(name) values ('IBM');
        insert into brands(name) values ('Intel');
        insert into brands(name) values ('AMD');
        insert into brands(name) values ('NVIDIA');
        insert into brands(name) values ('Qualcomm');
        insert into brands(name) values ('Cisco');
        insert into brands(name) values ('Netgear');
        insert into brands(name) values ('Linksys');
        insert into brands(name) values ('Logitech');
        insert into brands(name) values ('Razer');
        insert into brands(name) values ('Corsair');
        insert into brands(name) values ('Seagate');
        insert into brands(name) values ('Western Digital');
        insert into brands(name) values ('Kingston');
        insert into brands(name) values ('Sony');
        insert into brands(name) values ('Panasonic');
        insert into brands(name) values ('Sharp');
        insert into brands(name) values ('TCL');
        insert into brands(name) values ('Nokia');
        insert into brands(name) values ('Motorola');
        insert into brands(name) values ('OnePlus');
        insert into brands(name) values ('Xiaomi');
        insert into brands(name) values ('Huawei');
        insert into brands(name) values ('HTC');
        insert into brands(name) values ('Adobe');
        insert into brands(name) values ('Oracle');
        insert into brands(name) values ('Salesforce');
        insert into brands(name) values ('SAP');
        insert into brands(name) values ('VMware');
        insert into brands(name) values ('Symantec');
        insert into brands(name) values ('McAfee');
        insert into brands(name) values ('Kaspersky');
        insert into brands(name) values ('Avast');
        insert into brands(name) values ('Bitdefender');
        insert into brands(name) values ('Juniper');
        insert into brands(name) values ('Fortinet');
        insert into brands(name) values ('Check Point');
        insert into brands(name) values ('F5 Networks');
        insert into brands(name) values ('Citrix');
        insert into brands(name) values ('Red Hat');
        insert into brands(name) values ('Ubuntu');
        insert into brands(name) values ('CentOS');
        insert into brands(name) values ('Amazon Web Services');
        insert into brands(name) values ('Microsoft Azure');
        insert into brands(name) values ('Google Cloud Platform');
        insert into brands(name) values ('ServiceNow');
        insert into brands(name) values ('Zendesk');
        insert into brands(name) values ('Atlassian');
        insert into brands(name) values ('Slack');
        insert into brands(name) values ('Zoom');
        insert into brands(name) values ('Cisco Webex');
        insert into brands(name) values ('Microsoft Teams');
        insert into brands(name) values ('Adobe Creative Cloud');
        insert into brands(name) values ('Autodesk');
        insert into brands(name) values ('Unity');
        insert into brands(name) values ('Epic Games');
        insert into brands(name) values ('Nintendo');
        insert into brands(name) values ('PlayStation');
        insert into brands(name) values ('Xbox');
        insert into brands(name) values ('Amazon Echo');
        insert into brands(name) values ('Google Home');
        insert into brands(name) values ('Apple HomePod');
        insert into brands(name) values ('Samsung SmartThings');
        insert into brands(name) values ('Philips Hue');
        insert into brands(name) values ('Nest');
        insert into brands(name) values ('Ring');
        insert into brands(name) values ('GoPro');
        insert into brands(name) values ('DJI');
        insert into brands(name) values ('Fitbit');
        insert into brands(name) values ('Garmin');
        insert into brands(name) values ('Bose');
        insert into brands(name) values ('Sonos');
        insert into brands(name) values ('JBL');
        insert into brands(name) values ('Beats');
        insert into brands(name) values ('SteelSeries');
        insert into brands(name) values ('HyperX');


        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'SMARTPHONE');
        insert into products(product_id, name, brand_id, category_id) values ('0000000000001', 'iPhone 13 Pro', (select brands.id from brands where name = 'Apple'), id);
        insert into products(product_id, name, brand_id, category_id) values ('0000000000002', 'Galaxy S10', (select brands.id from brands where name = 'Samsung'), id);
        insert into products(product_id, name, brand_id, category_id) values ('0000000000004', 'Galaxy S21 Ultra', (select brands.id from brands where name = 'Samsung'), id);
        insert into products(product_id, name, brand_id, category_id) values ('0000000000005', 'Galaxy Note 20', (select brands.id from brands where name = 'Samsung'), id);
        insert into products(product_id, name, brand_id, category_id) values ('0000000000007', 'LG G8 ThinQ', (select brands.id from brands where name = 'LG'), id);
        insert into products(product_id, name, brand_id, category_id) values ('0000000000008', 'LG V60 ThinQ', (select brands.id from brands where name = 'LG'), id);
        insert into products(product_id, name, brand_id, category_id) values ('0000000000009', 'LG Gram', (select brands.id from brands where name = 'LG'), id);


        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'TV');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'PC');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'SOFTWARE');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'STORAGE_DEVICE');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'OTHER');

    END
$$;
COMMIT;

