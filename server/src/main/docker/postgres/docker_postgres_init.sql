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
    surname      varchar(255) not null,
    valid        bool not null
);

alter table profiles
    owner to postgres;

create table if not exists password_reset
(
    uuid    uuid primary key,
    created timestamp,
    user_id bigint
        constraint password_reset___fk
            references profiles
);

alter table password_reset
    owner to postgres;

create table if not exists email_verification
(
    id        bigserial
        primary key,
    uuid    uuid,
    created timestamp,
    user_id bigint unique
        constraint email_verification___fk
            references profiles
);

alter table email_verification
    owner to postgres;

create table if not exists items
(
    serial_num           bigint       not null,
    duration_months      bigint,
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
    address   varchar(255) not null,
    city      varchar(255) not null,
    country   varchar(255) not null,
    region    varchar(255) not null,
    client_id bigint       not null
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
    description       varchar      not null,
    priority          integer      not null,
    status            varchar(255) not null,
    title             varchar(255) not null,
    client_id         bigint       not null
        constraint fk87o5gt7m6d4fo8lg32o8raliw
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
        id    bigint;
        id2   bigint;
        id_e1 bigint;
        id_e2 bigint;
    BEGIN
        select nextval('profiles_id_seq') into id;
        insert into profiles(id, email, name, surname, role, valid, phone_number)
        values (id, 'client1@polito.it', 'ClientA', 'PoliTo', 'CLIENT', true, '+39 333 1234567');
        select nextval('addresses_id_seq') into id2;
        insert into addresses(id, address, city, country, region, client_id)
        values (id2, 'Corso Duca degli Abruzzi, 24', 'Turin', 'Italy', 'Piedmont', id);
        select nextval('profiles_id_seq') into id;
        insert into profiles(id, email, name, surname, role, valid, phone_number)
        values (id, 'client2@polito.it', 'ClientB', 'PoliTo', 'CLIENT', true, '+39 333 1122333');
        select nextval('addresses_id_seq') into id2;
        insert into addresses(id, address, city, country, region, client_id)
        values (id2, 'Piazza Leonardo da Vinci, 32', 'Milan', 'Italy', 'Lombardy', id);
        select nextval('profiles_id_seq') into id_e1;
        insert into profiles(id, email, name, surname, role, valid)
        values (id_e1, 'expert1@polito.it', 'ExpertA', 'PoliTo', 'EXPERT', true);
        select nextval('profiles_id_seq') into id_e2;
        insert into profiles(id, email, name, surname, role, valid)
        values (id_e2, 'expert2@polito.it', 'ExpertB', 'PoliTo', 'EXPERT', true);
        select nextval('profiles_id_seq') into id;
        insert into profiles(id, email, name, surname, role, valid)
        values (id, 'manager@polito.it', 'Manager', 'PoliTo', 'MANAGER', true);
        select nextval('profiles_id_seq') into id;
        insert into profiles(id, email, name, surname, role, valid)
        values (id, 'vendor@polito.it', 'Vendor', 'PoliTo', 'VENDOR', true);


        insert into brands(name) values ('Apple');
        insert into brands(name) values ('Samsung');
        insert into brands(name) values ('LG');
        insert into brands(name) values ('Microsoft');
        insert into brands(name) values ('Google');
        insert into brands(name) values ('Amazon');
        insert into brands(name) values ('Dell');
        insert into brands(name) values ('HP');
        insert into brands(name) values ('Lenovo');
        insert into brands(name) values ('MSI');
        insert into brands(name) values ('ASUS');
        insert into brands(name) values ('Acer');
        insert into brands(name) values ('Toshiba');
        insert into brands(name) values ('IBM');
        insert into brands(name) values ('Intel');
        insert into brands(name) values ('AMD');
        insert into brands(name) values ('NVIDIA');
        insert into brands(name) values ('Qualcomm');
        insert into brands(name) values ('Cisco');
        insert into brands(name) values ('NETGEAR');
        insert into brands(name) values ('SanDisk');
        insert into brands(name) values ('Linksys');
        insert into brands(name) values ('Logitech');
        insert into brands(name) values ('Insta360');
        insert into brands(name) values ('Razer');
        insert into brands(name) values ('Corsair');
        insert into brands(name) values ('Seagate');
        insert into brands(name) values ('Hisense');
        insert into brands(name) values ('Western Digital');
        insert into brands(name) values ('Kingston');
        insert into brands(name) values ('Sony');
        insert into brands(name) values ('Panasonic');
        insert into brands(name) values ('Sharp');
        insert into brands(name) values ('Oculus');
        insert into brands(name) values ('Valve');
        insert into brands(name) values ('TCL');
        insert into brands(name) values ('Nokia');
        insert into brands(name) values ('RAVPower');
        insert into brands(name) values ('Motorola');
        insert into brands(name) values ('AUKEY');
        insert into brands(name) values ('Anker');
        insert into brands(name) values ('OnePlus');
        insert into brands(name) values ('Xiaomi');
        insert into brands(name) values ('Huawei');
        insert into brands(name) values ('HTC');
        insert into brands(name) values ('Adobe');
        insert into brands(name) values ('Oracle');
        insert into brands(name) values ('Salesforce');
        insert into brands(name) values ('Crucial');
        insert into brands(name) values ('SAP');
        insert into brands(name) values ('VMware');
        insert into brands(name) values ('Symantec');
        insert into brands(name) values ('McAfee');
        insert into brands(name) values ('TP-Link');
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
        insert into brands(name) values ('Sennheiser');
        insert into brands(name) values ('Canon');
        insert into brands(name) values ('Brother');
        insert into brands(name) values ('Epson');
        insert into brands(name) values ('Harman Kardon');
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
        insert into brands(name) values ('Vizio');
        insert into brands(name) values ('Philips');
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
        insert into products(product_id, name, brand_id, category_id)
        values ('0000000000001', 'iPhone 13 Pro', (select brands.id from brands where name = 'Apple'), id);
        insert into products(product_id, name, brand_id, category_id)
        values ('0000000000002', 'Galaxy S10', (select brands.id from brands where name = 'Samsung'), id);
        insert into products(product_id, name, brand_id, category_id)
        values ('0000000000004', 'Galaxy S21 Ultra', (select brands.id from brands where name = 'Samsung'), id);
        insert into products(product_id, name, brand_id, category_id)
        values ('0000000000005', 'Galaxy Note 20', (select brands.id from brands where name = 'Samsung'), id);
        insert into products(product_id, name, brand_id, category_id)
        values ('0000000000007', 'LG G8 ThinQ', (select brands.id from brands where name = 'LG'), id);
        insert into products(product_id, name, brand_id, category_id)
        values ('0000000000008', 'LG V60 ThinQ', (select brands.id from brands where name = 'LG'), id);
        insert into products(product_id, name, brand_id, category_id)
        values ('0000000000009', 'LG Gram', (select brands.id from brands where name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000015', 'iPhone 12', (SELECT brands.id FROM brands WHERE name = 'Apple'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000016', 'iPhone SE', (SELECT brands.id FROM brands WHERE name = 'Apple'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000017', 'iPhone XR', (SELECT brands.id FROM brands WHERE name = 'Apple'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000018', 'iPhone 11 Pro Max', (SELECT brands.id FROM brands WHERE name = 'Apple'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000020', 'Galaxy S20', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000021', 'Galaxy Note 10', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000022', 'Galaxy A52', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000023', 'Galaxy Fold 3', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000025', 'LG Velvet', (SELECT brands.id FROM brands WHERE name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000026', 'LG Wing', (SELECT brands.id FROM brands WHERE name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000027', 'LG K92', (SELECT brands.id FROM brands WHERE name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000028', 'LG Stylo 6', (SELECT brands.id FROM brands WHERE name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000030', 'Google Pixel 5', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000031', 'Google Pixel 4a', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000032', 'Google Pixel 3 XL', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000033', 'Google Pixel 2', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000040', 'OnePlus 9 Pro', (SELECT brands.id FROM brands WHERE name = 'OnePlus'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000041', 'OnePlus 8T', (SELECT brands.id FROM brands WHERE name = 'OnePlus'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000042', 'OnePlus Nord', (SELECT brands.id FROM brands WHERE name = 'OnePlus'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000043', 'OnePlus 7 Pro', (SELECT brands.id FROM brands WHERE name = 'OnePlus'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000050', 'Xiaomi Mi 11', (SELECT brands.id FROM brands WHERE name = 'Xiaomi'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000051', 'Xiaomi Redmi Note 10', (SELECT brands.id FROM brands WHERE name = 'Xiaomi'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000052', 'Xiaomi Poco X3', (SELECT brands.id FROM brands WHERE name = 'Xiaomi'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000053', 'Xiaomi Mi 9T', (SELECT brands.id FROM brands WHERE name = 'Xiaomi'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000060', 'Huawei P40 Pro', (SELECT brands.id FROM brands WHERE name = 'Huawei'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000061', 'Huawei Mate 40 Pro', (SELECT brands.id FROM brands WHERE name = 'Huawei'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000062', 'Huawei Nova 7', (SELECT brands.id FROM brands WHERE name = 'Huawei'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000063', 'Huawei P30 Lite', (SELECT brands.id FROM brands WHERE name = 'Huawei'), id);

        insert into category_assigned(expert_id, category_id) values (id_e1, id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'TV');


        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000101', 'Samsung QLED Q90T', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000102', 'Samsung Neo QLED QN90A', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000103', 'Samsung Crystal UHD 7 Series', (SELECT brands.id FROM brands WHERE name = 'Samsung'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000104', 'Samsung The Frame', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000105', 'LG OLED C1', (SELECT brands.id FROM brands WHERE name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000106', 'LG NanoCell 90', (SELECT brands.id FROM brands WHERE name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000107', 'LG UHD 70 Series', (SELECT brands.id FROM brands WHERE name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000108', 'LG OLED BX', (SELECT brands.id FROM brands WHERE name = 'LG'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000109', 'Sony Bravia XR A90J', (SELECT brands.id FROM brands WHERE name = 'Sony'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000110', 'Sony Bravia X90J', (SELECT brands.id FROM brands WHERE name = 'Sony'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000111', 'Sony Bravia X80J', (SELECT brands.id FROM brands WHERE name = 'Sony'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000112', 'Sony Bravia X70J', (SELECT brands.id FROM brands WHERE name = 'Sony'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000113', 'TCL 6-Series', (SELECT brands.id FROM brands WHERE name = 'TCL'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000114', 'TCL 5-Series', (SELECT brands.id FROM brands WHERE name = 'TCL'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000115', 'TCL 4-Series', (SELECT brands.id FROM brands WHERE name = 'TCL'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000116', 'TCL 3-Series', (SELECT brands.id FROM brands WHERE name = 'TCL'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000117', 'Panasonic JX850', (SELECT brands.id FROM brands WHERE name = 'Panasonic'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000118', 'Panasonic JX700', (SELECT brands.id FROM brands WHERE name = 'Panasonic'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000119', 'Panasonic HX600', (SELECT brands.id FROM brands WHERE name = 'Panasonic'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000120', 'Panasonic GX800', (SELECT brands.id FROM brands WHERE name = 'Panasonic'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000121', 'Hisense U8G', (SELECT brands.id FROM brands WHERE name = 'Hisense'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000122', 'Hisense U6G', (SELECT brands.id FROM brands WHERE name = 'Hisense'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000123', 'Hisense A6G', (SELECT brands.id FROM brands WHERE name = 'Hisense'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000124', 'Hisense R6 Series', (SELECT brands.id FROM brands WHERE name = 'Hisense'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000125', 'Philips OLED 806', (SELECT brands.id FROM brands WHERE name = 'Philips'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000126', 'Philips 9000 Series', (SELECT brands.id FROM brands WHERE name = 'Philips'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000127', 'Philips 8500 Series', (SELECT brands.id FROM brands WHERE name = 'Philips'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000128', 'Philips 7300 Series', (SELECT brands.id FROM brands WHERE name = 'Philips'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000129', 'Vizio P-Series Quantum', (SELECT brands.id FROM brands WHERE name = 'Vizio'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000130', 'Vizio M-Series Quantum', (SELECT brands.id FROM brands WHERE name = 'Vizio'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000131', 'Vizio V-Series', (SELECT brands.id FROM brands WHERE name = 'Vizio'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000132', 'Vizio D-Series', (SELECT brands.id FROM brands WHERE name = 'Vizio'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000133', 'Sharp Aquos R2', (SELECT brands.id FROM brands WHERE name = 'Sharp'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000134', 'Sharp Aquos C10', (SELECT brands.id FROM brands WHERE name = 'Sharp'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000135', 'Sharp Aquos B10', (SELECT brands.id FROM brands WHERE name = 'Sharp'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000136', 'Sharp Aquos S3', (SELECT brands.id FROM brands WHERE name = 'Sharp'), id);

        insert into category_assigned(expert_id, category_id) values (id_e1, id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'PC');

        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000137', 'Dell XPS 15', (SELECT brands.id FROM brands WHERE name = 'Dell'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000138', 'Dell Inspiron 5000', (SELECT brands.id FROM brands WHERE name = 'Dell'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000139', 'Dell Alienware Aurora R12', (SELECT brands.id FROM brands WHERE name = 'Dell'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000140', 'Dell Precision 7750', (SELECT brands.id FROM brands WHERE name = 'Dell'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000141', 'HP Spectre x360', (SELECT brands.id FROM brands WHERE name = 'HP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000142', 'HP Pavilion Gaming Desktop', (SELECT brands.id FROM brands WHERE name = 'HP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000143', 'HP EliteBook 840 G8', (SELECT brands.id FROM brands WHERE name = 'HP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000144', 'HP Omen 30L', (SELECT brands.id FROM brands WHERE name = 'HP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000145', 'Lenovo ThinkPad X1 Carbon', (SELECT brands.id FROM brands WHERE name = 'Lenovo'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000146', 'Lenovo IdeaPad 5', (SELECT brands.id FROM brands WHERE name = 'Lenovo'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000147', 'Lenovo Legion 5', (SELECT brands.id FROM brands WHERE name = 'Lenovo'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000148', 'Lenovo Yoga C740', (SELECT brands.id FROM brands WHERE name = 'Lenovo'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000149', 'ASUS ZenBook Pro Duo', (SELECT brands.id FROM brands WHERE name = 'ASUS'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000150', 'ASUS ROG Strix G15', (SELECT brands.id FROM brands WHERE name = 'ASUS'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000151', 'ASUS VivoBook 15', (SELECT brands.id FROM brands WHERE name = 'ASUS'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000152', 'ASUS TUF Gaming A15', (SELECT brands.id FROM brands WHERE name = 'ASUS'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000153', 'Acer Predator Helios 300', (SELECT brands.id FROM brands WHERE name = 'Acer'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000154', 'Acer Aspire 5', (SELECT brands.id FROM brands WHERE name = 'Acer'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000155', 'Acer Swift 3', (SELECT brands.id FROM brands WHERE name = 'Acer'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000156', 'Acer Nitro 5', (SELECT brands.id FROM brands WHERE name = 'Acer'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000157', 'MSI GS66 Stealth', (SELECT brands.id FROM brands WHERE name = 'MSI'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000158', 'MSI Trident X', (SELECT brands.id FROM brands WHERE name = 'MSI'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000159', 'MSI Creator 17', (SELECT brands.id FROM brands WHERE name = 'MSI'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000160', 'MSI Optix MAG341CQ', (SELECT brands.id FROM brands WHERE name = 'MSI'), id);

        insert into category_assigned(expert_id, category_id) values (id_e1, id);
        insert into category_assigned(expert_id, category_id) values (id_e2, id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'SOFTWARE');

        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000177', 'Microsoft Office 365', (SELECT brands.id FROM brands WHERE name = 'Microsoft'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000178', 'Windows 10 Pro', (SELECT brands.id FROM brands WHERE name = 'Microsoft'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000179', 'Visual Studio', (SELECT brands.id FROM brands WHERE name = 'Microsoft'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000180', 'Azure Cloud Services', (SELECT brands.id FROM brands WHERE name = 'Microsoft'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000181', 'Adobe Photoshop', (SELECT brands.id FROM brands WHERE name = 'Adobe'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000182', 'Adobe Illustrator', (SELECT brands.id FROM brands WHERE name = 'Adobe'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000183', 'Adobe Premiere Pro', (SELECT brands.id FROM brands WHERE name = 'Adobe'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000184', 'Adobe Acrobat Pro', (SELECT brands.id FROM brands WHERE name = 'Adobe'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000185', 'Autodesk AutoCAD', (SELECT brands.id FROM brands WHERE name = 'Autodesk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000186', 'Autodesk 3ds Max', (SELECT brands.id FROM brands WHERE name = 'Autodesk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000187', 'Autodesk Maya', (SELECT brands.id FROM brands WHERE name = 'Autodesk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000188', 'Autodesk Revit', (SELECT brands.id FROM brands WHERE name = 'Autodesk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000189', 'Oracle Database', (SELECT brands.id FROM brands WHERE name = 'Oracle'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000190', 'Oracle E-Business Suite', (SELECT brands.id FROM brands WHERE name = 'Oracle'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000191', 'Oracle WebLogic Server', (SELECT brands.id FROM brands WHERE name = 'Oracle'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000192', 'Oracle Fusion Middleware', (SELECT brands.id FROM brands WHERE name = 'Oracle'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000193', 'IBM Watson', (SELECT brands.id FROM brands WHERE name = 'IBM'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000194', 'IBM Db2', (SELECT brands.id FROM brands WHERE name = 'IBM'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000195', 'IBM Cognos Analytics', (SELECT brands.id FROM brands WHERE name = 'IBM'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000196', 'IBM WebSphere Application Server', (SELECT brands.id FROM brands WHERE name = 'IBM'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000197', 'SAP S/4HANA', (SELECT brands.id FROM brands WHERE name = 'SAP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000198', 'SAP SuccessFactors', (SELECT brands.id FROM brands WHERE name = 'SAP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000199', 'SAP BusinessObjects', (SELECT brands.id FROM brands WHERE name = 'SAP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000200', 'SAP Hybris Commerce', (SELECT brands.id FROM brands WHERE name = 'SAP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000201', 'Google Workspace', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000202', 'Google Cloud Platform', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000203', 'Google Analytics', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000204', 'Google Chrome', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000205', 'Salesforce Sales Cloud', (SELECT brands.id FROM brands WHERE name = 'Salesforce'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000206', 'Salesforce Service Cloud', (SELECT brands.id FROM brands WHERE name = 'Salesforce'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000207', 'Salesforce Marketing Cloud', (SELECT brands.id FROM brands WHERE name = 'Salesforce'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000208', 'Salesforce Commerce Cloud', (SELECT brands.id FROM brands WHERE name = 'Salesforce'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000209', 'VMware vSphere', (SELECT brands.id FROM brands WHERE name = 'VMware'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000210', 'VMware ESXi', (SELECT brands.id FROM brands WHERE name = 'VMware'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000211', 'VMware Fusion', (SELECT brands.id FROM brands WHERE name = 'VMware'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000212', 'VMware Workstation', (SELECT brands.id FROM brands WHERE name = 'VMware'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000213', 'Autodesk AutoCAD', (SELECT brands.id FROM brands WHERE name = 'Autodesk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000214', 'Autodesk Revit', (SELECT brands.id FROM brands WHERE name = 'Autodesk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000215', 'Autodesk Maya', (SELECT brands.id FROM brands WHERE name = 'Autodesk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000216', 'Autodesk Inventor', (SELECT brands.id FROM brands WHERE name = 'Autodesk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000217', 'Cisco Catalyst Switch', (SELECT brands.id FROM brands WHERE name = 'Cisco'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000218', 'Cisco ISR Router', (SELECT brands.id FROM brands WHERE name = 'Cisco'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000219', 'Cisco ASA Firewall', (SELECT brands.id FROM brands WHERE name = 'Cisco'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000220', 'Cisco UCS Server', (SELECT brands.id FROM brands WHERE name = 'Cisco'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000221', 'Symantec Endpoint Protection', (SELECT brands.id FROM brands WHERE name = 'Symantec'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000222', 'Symantec Backup Exec', (SELECT brands.id FROM brands WHERE name = 'Symantec'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000223', 'Symantec Norton Security', (SELECT brands.id FROM brands WHERE name = 'Symantec'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000224', 'Symantec Ghost Solution Suite',
                (SELECT brands.id FROM brands WHERE name = 'Symantec'), id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'STORAGE_DEVICE');


        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000225', 'Seagate Barracuda HDD', (SELECT brands.id FROM brands WHERE name = 'Seagate'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000226', 'Seagate IronWolf NAS HDD', (SELECT brands.id FROM brands WHERE name = 'Seagate'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000227', 'Seagate Backup Plus Portable HDD',
                (SELECT brands.id FROM brands WHERE name = 'Seagate'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000228', 'Seagate FireCuda Gaming SSD', (SELECT brands.id FROM brands WHERE name = 'Seagate'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000229', 'Western Digital Blue SSD',
                (SELECT brands.id FROM brands WHERE name = 'Western Digital'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000230', 'Western Digital Red NAS HDD',
                (SELECT brands.id FROM brands WHERE name = 'Western Digital'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000231', 'Western Digital My Passport Portable HDD',
                (SELECT brands.id FROM brands WHERE name = 'Western Digital'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000232', 'Western Digital Black SN850 NVMe SSD',
                (SELECT brands.id FROM brands WHERE name = 'Western Digital'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000233', 'Samsung 860 EVO SSD', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000234', 'Samsung T5 Portable SSD', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000235', 'Samsung 970 PRO NVMe SSD', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000236', 'Samsung X5 Thunderbolt 3 SSD', (SELECT brands.id FROM brands WHERE name = 'Samsung'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000237', 'Crucial MX500 SSD', (SELECT brands.id FROM brands WHERE name = 'Crucial'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000238', 'Crucial P2 NVMe SSD', (SELECT brands.id FROM brands WHERE name = 'Crucial'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000239', 'Crucial BX500 SSD', (SELECT brands.id FROM brands WHERE name = 'Crucial'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000240', 'Crucial X8 Portable SSD', (SELECT brands.id FROM brands WHERE name = 'Crucial'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000241', 'Kingston A2000 NVMe PCIe SSD', (SELECT brands.id FROM brands WHERE name = 'Kingston'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000242', 'Kingston UV500 SSD', (SELECT brands.id FROM brands WHERE name = 'Kingston'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000243', 'Kingston DataTraveler USB Flash Drive',
                (SELECT brands.id FROM brands WHERE name = 'Kingston'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000244', 'Kingston KC600 SATA SSD', (SELECT brands.id FROM brands WHERE name = 'Kingston'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000245', 'SanDisk Ultra microSD Card', (SELECT brands.id FROM brands WHERE name = 'SanDisk'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000246', 'SanDisk SSD PLUS', (SELECT brands.id FROM brands WHERE name = 'SanDisk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000247', 'SanDisk Extreme Portable SSD', (SELECT brands.id FROM brands WHERE name = 'SanDisk'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000248', 'SanDisk Cruzer USB Flash Drive',
                (SELECT brands.id FROM brands WHERE name = 'SanDisk'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000249', 'Toshiba P300 HDD', (SELECT brands.id FROM brands WHERE name = 'Toshiba'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000250', 'Toshiba X300 HDD', (SELECT brands.id FROM brands WHERE name = 'Toshiba'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000251', 'Toshiba Canvio Basics Portable HDD',
                (SELECT brands.id FROM brands WHERE name = 'Toshiba'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000252', 'Toshiba TR200 SSD', (SELECT brands.id FROM brands WHERE name = 'Toshiba'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000253', 'Intel 660p NVMe SSD', (SELECT brands.id FROM brands WHERE name = 'Intel'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000254', 'Intel 545s SSD', (SELECT brands.id FROM brands WHERE name = 'Intel'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000255', 'Intel Optane Memory', (SELECT brands.id FROM brands WHERE name = 'Intel'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000256', 'Intel DC P4510 NVMe SSD', (SELECT brands.id FROM brands WHERE name = 'Intel'), id);

        insert into category_assigned(expert_id, category_id) values (id_e2, id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'OTHER');
        -- Monitors
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000257', 'Dell Ultrasharp U2719D Monitor', (SELECT brands.id FROM brands WHERE name = 'Dell'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000258', 'HP Pavilion 22cwa Monitor', (SELECT brands.id FROM brands WHERE name = 'HP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000259', 'ASUS VG245H Gaming Monitor', (SELECT brands.id FROM brands WHERE name = 'ASUS'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000260', 'LG 34WK650-W Ultrawide Monitor', (SELECT brands.id FROM brands WHERE name = 'LG'),
                id);

-- Keyboards
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000261', 'Logitech G915 Mechanical Gaming Keyboard',
                (SELECT brands.id FROM brands WHERE name = 'Logitech'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000262', 'Corsair K70 RGB Mechanical Keyboard',
                (SELECT brands.id FROM brands WHERE name = 'Corsair'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000263', 'Razer Huntsman Elite Gaming Keyboard',
                (SELECT brands.id FROM brands WHERE name = 'Razer'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000264', 'SteelSeries Apex Pro Mechanical Keyboard',
                (SELECT brands.id FROM brands WHERE name = 'SteelSeries'), id);

-- Mice
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000265', 'Logitech MX Master 3 Wireless Mouse',
                (SELECT brands.id FROM brands WHERE name = 'Logitech'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000266', 'Razer DeathAdder V2 Gaming Mouse',
                (SELECT brands.id FROM brands WHERE name = 'Razer'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000267', 'Corsair M65 RGB Elite Gaming Mouse',
                (SELECT brands.id FROM brands WHERE name = 'Corsair'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000268', 'SteelSeries Rival 600 Gaming Mouse',
                (SELECT brands.id FROM brands WHERE name = 'SteelSeries'), id);

-- Headphones
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000269', 'Sony WH-1000XM4 Wireless Headphones',
                (SELECT brands.id FROM brands WHERE name = 'Sony'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000270', 'Bose QuietComfort 35 II Wireless Headphones',
                (SELECT brands.id FROM brands WHERE name = 'Bose'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000271', 'Sennheiser HD 660 S Open-Back Headphones',
                (SELECT brands.id FROM brands WHERE name = 'Sennheiser'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000272', 'JBL Quantum 800 Gaming Headset', (SELECT brands.id FROM brands WHERE name = 'JBL'),
                id);

-- Speakers
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000273', 'Sonos One (Gen 2) Smart Speaker', (SELECT brands.id FROM brands WHERE name = 'Sonos'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000274', 'Bose SoundLink Revolve+ Portable Speaker',
                (SELECT brands.id FROM brands WHERE name = 'Bose'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000275', 'JBL Flip 5 Waterproof Bluetooth Speaker',
                (SELECT brands.id FROM brands WHERE name = 'JBL'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000276', 'Harman Kardon Aura Studio 3 Wireless Speaker',
                (SELECT brands.id FROM brands WHERE name = 'Harman Kardon'), id);

-- Printers
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000277', 'HP OfficeJet Pro 9015 All-in-One Printer',
                (SELECT brands.id FROM brands WHERE name = 'HP'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000278', 'Epson EcoTank ET-4760 Wireless Printer',
                (SELECT brands.id FROM brands WHERE name = 'Epson'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000279', 'Canon PIXMA TR8520 Wireless Printer',
                (SELECT brands.id FROM brands WHERE name = 'Canon'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000280', 'Brother HL-L2395DW Laser Printer',
                (SELECT brands.id FROM brands WHERE name = 'Brother'), id);

-- Routers
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000281', 'NETGEAR Nighthawk AX12 Wi-Fi Router',
                (SELECT brands.id FROM brands WHERE name = 'NETGEAR'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000282', 'TP-Link Archer AX6000 Wi-Fi Router',
                (SELECT brands.id FROM brands WHERE name = 'TP-Link'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000283', 'Linksys EA7500 Dual-Band Wi-Fi Router',
                (SELECT brands.id FROM brands WHERE name = 'Linksys'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000284', 'ASUS RT-AC68U AC1900 Wi-Fi Router',
                (SELECT brands.id FROM brands WHERE name = 'ASUS'), id);

-- Webcams
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000285', 'Logitech C920 HD Pro Webcam', (SELECT brands.id FROM brands WHERE name = 'Logitech'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000286', 'Microsoft LifeCam HD-3000 Webcam',
                (SELECT brands.id FROM brands WHERE name = 'Microsoft'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000287', 'Razer Kiyo Streaming Webcam', (SELECT brands.id FROM brands WHERE name = 'Razer'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000288', 'AUKEY FHD Webcam', (SELECT brands.id FROM brands WHERE name = 'AUKEY'), id);

-- Power Banks
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000289', 'Anker PowerCore 10000 Portable Charger',
                (SELECT brands.id FROM brands WHERE name = 'Anker'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000290', 'RAVPower 26800mAh Power Bank', (SELECT brands.id FROM brands WHERE name = 'RAVPower'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000291', 'AUKEY 20000mAh Power Bank', (SELECT brands.id FROM brands WHERE name = 'AUKEY'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000292', 'Samsung 10000mAh Wireless Power Bank',
                (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);

-- Smart Watches
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000293', 'Apple Watch Series 6', (SELECT brands.id FROM brands WHERE name = 'Apple'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000294', 'Samsung Galaxy Watch Active2', (SELECT brands.id FROM brands WHERE name = 'Samsung'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000295', 'Fitbit Versa 3', (SELECT brands.id FROM brands WHERE name = 'Fitbit'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000296', 'Garmin Forerunner 245 Music', (SELECT brands.id FROM brands WHERE name = 'Garmin'),
                id);

-- VR Headsets
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000297', 'Oculus Quest 2 VR Headset', (SELECT brands.id FROM brands WHERE name = 'Oculus'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000298', 'HTC Vive Cosmos Elite VR Headset', (SELECT brands.id FROM brands WHERE name = 'HTC'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000299', 'Sony PlayStation VR Headset', (SELECT brands.id FROM brands WHERE name = 'Sony'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000300', 'Valve Index VR Headset', (SELECT brands.id FROM brands WHERE name = 'Valve'), id);

-- Smart Home Hubs
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000301', 'Google Nest Hub', (SELECT brands.id FROM brands WHERE name = 'Google'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000302', 'Amazon Echo Show 10', (SELECT brands.id FROM brands WHERE name = 'Amazon'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000303', 'Apple HomePod Mini', (SELECT brands.id FROM brands WHERE name = 'Apple'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000304', 'Samsung SmartThings Hub', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);

-- Portable SSDs
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000305', 'Samsung T7 Portable SSD', (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000306', 'SanDisk Extreme Portable SSD', (SELECT brands.id FROM brands WHERE name = 'SanDisk'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000307', 'Western Digital My Passport SSD',
                (SELECT brands.id FROM brands WHERE name = 'Western Digital'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000308', 'Crucial X8 Portable SSD', (SELECT brands.id FROM brands WHERE name = 'Crucial'), id);

-- Fitness Trackers
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000309', 'Fitbit Charge 4 Fitness Tracker',
                (SELECT brands.id FROM brands WHERE name = 'Fitbit'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000310', 'Garmin Venu 2 Fitness Tracker', (SELECT brands.id FROM brands WHERE name = 'Garmin'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000311', 'Samsung Galaxy Fit2 Fitness Tracker',
                (SELECT brands.id FROM brands WHERE name = 'Samsung'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000312', 'Xiaomi Mi Band 6 Fitness Tracker',
                (SELECT brands.id FROM brands WHERE name = 'Xiaomi'), id);

-- Action Cameras
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000313', 'GoPro HERO9 Black Action Camera', (SELECT brands.id FROM brands WHERE name = 'GoPro'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000314', 'DJI Osmo Action Camera', (SELECT brands.id FROM brands WHERE name = 'DJI'), id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000315', 'Sony FDR-X3000 Action Camera', (SELECT brands.id FROM brands WHERE name = 'Sony'),
                id);
        INSERT INTO products(product_id, name, brand_id, category_id)
        VALUES ('0000000000316', 'Insta360 ONE R Action Camera', (SELECT brands.id FROM brands WHERE name = 'Insta360'),
                id);


        INSERT INTO items (serial_num, duration_months, uuid, valid_from_timestamp, product_id, client_id)
        VALUES (203241506, 24, '9fcebd14-fa40-4f11-a11f-84391df346da', '2023-06-21 19:10:54.961201', '0000000000025',
                1);
        INSERT INTO items (serial_num, duration_months, uuid, valid_from_timestamp, product_id, client_id)
        VALUES (633020516, 12, 'e559a864-d32d-49db-bc52-7182e38036bb', '2023-06-21 19:11:23.318321', '0000000000112',
                1);
        INSERT INTO items (serial_num, duration_months, uuid, valid_from_timestamp, product_id, client_id)
        VALUES (203040506, 12, '72895077-097e-4e84-9917-5a57d0b56196', '2023-06-21 19:09:27.839294', '0000000000023',
                1);
        INSERT INTO items (serial_num, duration_months, uuid, valid_from_timestamp, product_id, client_id)
        VALUES (213244506, 12, 'a78d44fa-6137-4b79-8d6c-afc0450267ed', '2021-06-21 19:12:05.853000', '0000000000189',
                1);

        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-21 19:47:14.726903', e'am reaching out to you regarding an issue I am experiencing with my LG Velvet smartphone\'s touch screen. I am encountering difficulties with the touch functionality and would greatly appreciate your assistance in resolving this problem.

Issue Description:
The touch screen on my LG Velvet smartphone has become unresponsive, making it impossible for me to interact with the device using touch gestures. I have tried various troubleshooting steps but have been unable to restore the touch screen functionality.',
                2, 'IN_PROGRESS', 'Touch screen not working', 1, 3, '0000000000025', 203241506);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-21 20:04:23.758173', e'I am writing to report an issue that I have encountered with my Sony Bravia X70J television. I am experiencing distorted audio output, which is significantly affecting my viewing experience. I kindly request your assistance in resolving this problem promptly.

Issue Description:
While watching various programs on my Sony Bravia X70J TV, I noticed that the audio output has become distorted and unclear. The sound appears muffled, with a noticeable loss of clarity and depth. This issue persists across all audio sources, including built-in TV apps, external devices connected via HDMI, and broadcast channels.

Troubleshooting Steps Taken:
In an attempt to resolve the audio distortion problem, I have performed the following troubleshooting steps without success:

    Adjusted audio settings: I have reviewed and adjusted the audio settings on my Sony Bravia X70J TV, ensuring that they are optimized for the best audio quality. However, the distortion issue remains unresolved.
    Tested different audio sources: I have tested the TV with various audio sources, including different channels and streaming services, to rule out the possibility of a specific source causing the distortion. Unfortunately, the problem persists across all sources.
    Checked external devices: I have disconnected all external devices connected to the TV, such as soundbars or AV receivers, to ensure that they are not causing the audio distortion. However, the issue still persists when using the TV\'s built-in speakers.',
                0, 'OPEN', 'Distorted Audio Output on Sony Bravia X70J TV', 1, null, '0000000000112', 633020516);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-20 20:04:23.758000', e'I am reaching out to you regarding a pressing concern with my Sony Bravia X70J television. Unfortunately, I have been experiencing consistent audio distortion, severely impacting my viewing enjoyment. I humbly request your prompt assistance in resolving this matter.

Issue Description:
I have noticed persistent audio distortion on my Sony Bravia X70J TV, causing the sound to become muffled and unclear. This problem persists regardless of the audio source, encompassing both built-in TV apps, HDMI-connected external devices, and broadcast channels.

Troubleshooting Steps Taken:
To address the audio distortion, I have undertaken the following troubleshooting steps, but have been unable to resolve the issue:

    Adjusted audio settings: I carefully reviewed and optimized the TV\'s audio settings, aiming to enhance the sound quality. Unfortunately, the distortion problem persists despite these adjustments.

    Tested different audio sources: I conducted tests using various audio sources to eliminate the possibility of a specific source causing the distortion. Regrettably, the issue persists across all sources, indicating an underlying concern.

    Checked external devices: As a precautionary measure, I disconnected all external devices connected to the TV, such as soundbars or AV receivers. However, even when relying solely on the TV\'s internal speakers, the audio distortion persists.

Given the urgency and adverse impact on my viewing experience, I sincerely seek your expertise and support in promptly resolving this matter. Your prompt attention would be greatly appreciated.

Thank you for your understanding and assistance.', 0, 'OPEN', 'Distorted Audio Output on Sony Bravia X70J TV', 1, null,
                '0000000000112', 633020516);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-20 20:04:23.758000', e'I am writing to report a critical issue with my Sony Bravia X70J television. The audio output is consistently distorted, negatively impacting my viewing experience. I kindly request your prompt assistance in resolving this problem.

Issue Description:
The audio output on my Sony Bravia X70J TV is experiencing distortion, resulting in muffled and unclear sound quality. This problem persists across all audio sources, including built-in TV apps, HDMI-connected external devices, and broadcast channels.

Troubleshooting Steps Taken:
I have attempted to address the audio distortion issue with the following unsuccessful troubleshooting steps:

    Adjusted audio settings: I reviewed and optimized the TV\'s audio settings for better quality, but the distortion remains unresolved.

    Tested different audio sources: I tested various audio sources to rule out any specific source causing the issue, but the distortion persists across all sources.

    Checked external devices: I disconnected all external devices to eliminate them as potential causes, yet the distortion persists when using the TV\'s built-in speakers.

I urgently seek your expertise and support in resolving this matter. Your prompt attention to this issue would be highly appreciated.

Thank you for your assistance.', 1, 'IN_PROGRESS', 'Distorted Audio Output on Sony Bravia X70J TV', 1, 3,
                '0000000000112', 633020516);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-20 20:04:23.758000', e'I am writing to seek immediate assistance regarding an audio distortion issue on my Sony Bravia X70J television. The sound output is consistently distorted, significantly affecting my viewing experience. I kindly request your prompt attention and resolution of this problem.

Issue Description:
I have encountered persistent audio distortion on my Sony Bravia X70J TV, resulting in muffled and unclear sound quality. This problem persists across all audio sources, including built-in TV apps, HDMI-connected external devices, and broadcast channels.

Troubleshooting Steps Taken:
Despite my efforts to troubleshoot the issue, the audio distortion problem remains unresolved. Here are the steps I have taken:

    Adjusted audio settings: I thoroughly reviewed and optimized the TV\'s audio settings to ensure the best sound quality. However, the distortion persists despite these adjustments.

    Tested different audio sources: I extensively tested various audio sources to identify any specific cause of the distortion. Unfortunately, the problem persists across all sources.

    Checked external devices: I disconnected all external devices connected to the TV, such as soundbars or AV receivers, to eliminate potential interference. However, the audio distortion persists when using the TV\'s built-in speakers.

Given the urgency and negative impact on my entertainment experience, I humbly request your expert assistance in promptly resolving this matter. Your prompt attention would be greatly appreciated.

Thank you for your understanding and support.', 2, 'RESOLVED', 'Distorted Audio Output on Sony Bravia X70J TV', 1, null,
                '0000000000112', 633020516);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-19 20:04:23.758000', e'I am writing to report a critical issue with my Sony Bravia X70J television. The audio output is consistently distorted, negatively impacting my viewing experience. I kindly request your prompt assistance in resolving this problem.

Issue Description:
The audio output on my Sony Bravia X70J TV is experiencing distortion, resulting in muffled and unclear sound quality. This problem persists across all audio sources, including built-in TV apps, HDMI-connected external devices, and broadcast channels.

Troubleshooting Steps Taken:
I have attempted to address the audio distortion issue with the following unsuccessful troubleshooting steps:

    Adjusted audio settings: I reviewed and optimized the TV\'s audio settings for better quality, but the distortion remains unresolved.

    Tested different audio sources: I tested various audio sources to rule out any specific source causing the issue, but the distortion persists across all sources.

    Checked external devices: I disconnected all external devices to eliminate them as potential causes, yet the distortion persists when using the TV\'s built-in speakers.

I urgently seek your expertise and support in resolving this matter. Your prompt attention to this issue would be highly appreciated.

Thank you for your assistance.', 1, 'CLOSED', 'Distorted Audio Output on Sony Bravia X70J TV', 1, null, '0000000000112',
                633020516);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-20 20:04:23.758000', e'I am writing to bring to your attention a pressing matter regarding my Sony Bravia X70J television. Regrettably, I have encountered a persistent issue with distorted audio output that significantly impairs my viewing experience. Therefore, I am reaching out to your esteemed team in the hope of a prompt resolution.

Description of the Issue:
During my regular usage of the Sony Bravia X70J TV, I have noticed a distressing audio distortion that affects the quality of the sound. The audio output exhibits a muffled and unclear nature, depriving me of the clarity and depth I expect from my viewing sessions. This frustrating problem persists across all audio sources, encompassing the TV\'s built-in apps, external devices connected via HDMI, and even the broadcast channels.

Steps Taken for Troubleshooting:
In my sincere efforts to address the audio distortion concern, I have undertaken the following troubleshooting measures, unfortunately without success:

    Adjusted audio settings: I diligently reviewed and optimized the audio settings on my Sony Bravia X70J TV, striving to achieve the finest audio quality possible. However, regrettably, these adjustments did not resolve the distortion issue.

    Tested different audio sources: To eliminate the possibility of a specific source causing the problem, I conscientiously tested the TV with various audio sources, including different channels and streaming services. Sadly, the audio distortion persisted across all sources, indicating a more intrinsic problem.

    Checked external devices: In an attempt to isolate the root cause, I meticulously disconnected all external devices connected to the TV, such as soundbars or AV receivers. Despite this precautionary measure, the audio distortion remains prevalent even when utilizing the TV\'s internal speakers.

Considering the urgency and impact of this issue on my entertainment experience, I kindly implore your expertise and support in resolving this matter at the earliest convenience. Your swift assistance would be greatly appreciated.

Thank you for your prompt attention to this matter, and I look forward to a positive resolution.', 0, 'OPEN',
                'Distorted Audio Output on Sony Bravia X70J TV', 1, null, '0000000000112', 633020516);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-20 20:04:23.758000', e'I am writing to seek immediate assistance regarding an audio distortion issue on my Sony Bravia X70J television. The sound output is consistently distorted, significantly affecting my viewing experience. I kindly request your prompt attention and resolution of this problem.

Issue Description:
I have encountered persistent audio distortion on my Sony Bravia X70J TV, resulting in muffled and unclear sound quality. This problem persists across all audio sources, including built-in TV apps, HDMI-connected external devices, and broadcast channels.

Troubleshooting Steps Taken:
Despite my efforts to troubleshoot the issue, the audio distortion problem remains unresolved. Here are the steps I have taken:

    Adjusted audio settings: I thoroughly reviewed and optimized the TV\'s audio settings to ensure the best sound quality. However, the distortion persists despite these adjustments.

    Tested different audio sources: I extensively tested various audio sources to identify any specific cause of the distortion. Unfortunately, the problem persists across all sources.

    Checked external devices: I disconnected all external devices connected to the TV, such as soundbars or AV receivers, to eliminate potential interference. However, the audio distortion persists when using the TV\'s built-in speakers.

Given the urgency and negative impact on my entertainment experience, I humbly request your expert assistance in promptly resolving this matter. Your prompt attention would be greatly appreciated.

Thank you for your understanding and support.', 2, 'RESOLVED', 'Distorted Audio Output on Sony Bravia X70J TV', 1, 3,
                '0000000000112', 633020516);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-20 20:04:23.758000', e'I am writing to report a critical issue with my Sony Bravia X70J television. The audio output is consistently distorted, negatively impacting my viewing experience. I kindly request your prompt assistance in resolving this problem.

Issue Description:
The audio output on my Sony Bravia X70J TV is experiencing distortion, resulting in muffled and unclear sound quality. This problem persists across all audio sources, including built-in TV apps, HDMI-connected external devices, and broadcast channels.

Troubleshooting Steps Taken:
I have attempted to address the audio distortion issue with the following unsuccessful troubleshooting steps:

    Adjusted audio settings: I reviewed and optimized the TV\'s audio settings for better quality, but the distortion remains unresolved.

    Tested different audio sources: I tested various audio sources to rule out any specific source causing the issue, but the distortion persists across all sources.

    Checked external devices: I disconnected all external devices to eliminate them as potential causes, yet the distortion persists when using the TV\'s built-in speakers.

I urgently seek your expertise and support in resolving this matter. Your prompt attention to this issue would be highly appreciated.

Thank you for your assistance.', 1, 'IN_PROGRESS', 'Distorted Audio Output on Sony Bravia X70J TV', 1, 3,
                '0000000000112', 633020516);
        INSERT INTO tickets (created_timestamp, description, priority, status, title, client_id, expert_id,
                             product_id, serial_num)
        VALUES ('2023-06-20 20:04:23.758000', e'I am writing to bring to your attention a pressing matter regarding my Sony Bravia X70J television. Regrettably, I have encountered a persistent issue with distorted audio output that significantly impairs my viewing experience. Therefore, I am reaching out to your esteemed team in the hope of a prompt resolution.

Description of the Issue:
During my regular usage of the Sony Bravia X70J TV, I have noticed a distressing audio distortion that affects the quality of the sound. The audio output exhibits a muffled and unclear nature, depriving me of the clarity and depth I expect from my viewing sessions. This frustrating problem persists across all audio sources, encompassing the TV\'s built-in apps, external devices connected via HDMI, and even the broadcast channels.

Steps Taken for Troubleshooting:
In my sincere efforts to address the audio distortion concern, I have undertaken the following troubleshooting measures, unfortunately without success:

    Adjusted audio settings: I diligently reviewed and optimized the audio settings on my Sony Bravia X70J TV, striving to achieve the finest audio quality possible. However, regrettably, these adjustments did not resolve the distortion issue.

    Tested different audio sources: To eliminate the possibility of a specific source causing the problem, I conscientiously tested the TV with various audio sources, including different channels and streaming services. Sadly, the audio distortion persisted across all sources, indicating a more intrinsic problem.

    Checked external devices: In an attempt to isolate the root cause, I meticulously disconnected all external devices connected to the TV, such as soundbars or AV receivers. Despite this precautionary measure, the audio distortion remains prevalent even when utilizing the TV\'s internal speakers.

Considering the urgency and impact of this issue on my entertainment experience, I kindly implore your expertise and support in resolving this matter at the earliest convenience. Your swift assistance would be greatly appreciated.

Thank you for your prompt attention to this matter, and I look forward to a positive resolution.', 0, 'IN_PROGRESS',
                'Distorted Audio Output on Sony Bravia X70J TV', 1, 3, '0000000000112', 633020516);

        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-19 20:04:23.758000', null, 6, 1);  --12
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-20 20:04:23.758000', null, 10, 1); --23
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-20 20:04:23.758000', null, 3, 1); --4
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-20 20:04:23.758000', null, 4, 1); --5
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-20 20:04:23.758000', null, 7, 1); --17
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-20 20:04:23.758000', null, 5, 1); --7
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-20 20:04:23.758000', null, 9, 1); --21
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-20 20:04:23.758000', null, 8, 1); --18
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('IN_PROGRESS', 'OPEN', '2023-06-21 10:10:49.726903', 3, 8, 5); --19
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('IN_PROGRESS', 'OPEN', '2023-06-21 10:15:49.726903', 3, 9, 5); --22
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('IN_PROGRESS', 'OPEN', '2023-06-21 10:16:43.726903', 3, 10, 5); --24
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-21 19:47:14.726903', null, 1, 1); --1
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('OPEN', 'OPEN', '2023-06-21 20:04:23.758173', null, 2, 1); --3
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('IN_PROGRESS', 'OPEN', '2023-06-22 13:45:49.726903', 3, 4, 5); --6
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('IN_PROGRESS', 'OPEN', '2023-06-22 13:45:49.726903', 3, 5, 5); --8
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('CLOSED', 'IN_PROGRESS', '2023-06-22 17:46:29.726903', 3, 5, 3); --9
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('IN_PROGRESS', 'OPEN', '2023-06-23 11:32:49.726903', 3, 1, 5); --2
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('RESOLVED', 'IN_PROGRESS', '2023-06-23 18:50:14.726903', 3, 8, 1); --20
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('REOPENED', 'CLOSED', '2023-06-23 21:15:49.726903', null, 5, 1); --10
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('IN_PROGRESS', 'OPEN', '2023-06-24 11:33:49.726903', 3, 6, 5); --13
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('RESOLVED', 'REOPENED', '2023-06-24 17:18:21.726903', null, 5, 1); --11
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('CLOSED', 'IN_PROGRESS', '2023-06-24 17:50:14.726903', 3, 6, 3); --14
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('REOPENED', 'CLOSED', '2023-06-25 19:33:23.758000', null, 6, 1); --15
        INSERT INTO tickets_history (new_state, old_state, updated_timestamp, current_expert_id, ticket_id, user_id)
        VALUES ('CLOSED', 'REOPENED', '2023-06-25 19:35:23.758000', null, 6, 5); --16

    END
$$;
COMMIT;

