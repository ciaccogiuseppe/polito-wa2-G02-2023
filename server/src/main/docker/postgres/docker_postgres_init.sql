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
        insert into products(product_id, name, brand, category_id) values ('0000000000001', 'iPhone 13 Pro', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000002', 'Galaxy S10', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000003', 'Pixel 6', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000004', 'Xperia 1 II', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000005', 'Mi 11 Ultra', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000006', 'OnePlus 9 Pro', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000007', 'Mate 40 Pro', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000008', 'ZenFone 8', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000009', 'ROG Phone 5', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000010', 'Galaxy Note 20 Ultra', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000011', 'iPhone 12', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000012', 'Redmi Note 10 Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000013', 'Galaxy A52', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000014', 'Pixel 5', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000015', 'Xperia 5 II', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000016', 'Mi 10T Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000017', 'OnePlus 8 Pro', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000018', 'P40 Pro', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000019', 'ZenFone 7 Pro', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000020', 'ROG Phone 3', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000021', 'Galaxy S21 Ultra', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000022', 'iPhone SE', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000023', 'Redmi Note 9 Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000024', 'Galaxy A72', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000025', 'Pixel 4a', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000026', 'Xperia 10 II', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000027', 'Mi 10 Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000028', 'OnePlus 7T Pro', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000029', 'P30 Pro', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000030', 'ZenFone 6', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000031', 'Galaxy S20 Ultra', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000032', 'iPhone 11', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000033', 'Redmi Note 8 Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000034', 'Galaxy A51', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000035', 'Pixel 3a', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000036', 'Xperia 1', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000037', 'Mi 9T Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000038', 'OnePlus 7 Pro', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000039', 'P20 Pro', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000040', 'ZenFone 5Z', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000041', 'Galaxy S10+', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000042', 'iPhone XR', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000043', 'Redmi Note 7 Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000044', 'Galaxy A50', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000045', 'Pixel 3', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000046', 'Xperia 5', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000047', 'Mi 9', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000048', 'OnePlus 7T', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000049', 'P20', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000050', 'ZenFone 4', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000051', 'Galaxy Note 10+', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000052', 'iPhone XS', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000053', 'Redmi Note 6 Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000054', 'Galaxy A40', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000055', 'Pixel 2', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000056', 'Xperia 10', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000057', 'Mi 8 Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000058', 'OnePlus 6T', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000059', 'P10 Plus', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000060', 'ZenFone 3', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000061', 'Galaxy S9', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000062', 'iPhone X', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000063', 'Redmi Note 5 Pro', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000064', 'Galaxy A30', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000065', 'Pixel 2 XL', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000066', 'Xperia XZ3', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000067', 'Mi 8', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000068', 'OnePlus 6', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000069', 'P10', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000070', 'ZenFone 2', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000071', 'Galaxy Note 9', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000072', 'iPhone 8', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000073', 'Redmi Note 4', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000074', 'Galaxy A20e', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000075', 'Pixel XL', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000076', 'Xperia XZ2', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000077', 'Mi Mix 3', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000078', 'OnePlus 5T', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000079', 'P9 Plus', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000080', 'ZenFone', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000081', 'Galaxy S8', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000082', 'iPhone 7', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000083', 'Redmi Note 3', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000084', 'Galaxy A10', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000085', 'Pixel', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000086', 'Xperia XZ1', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000087', 'Mi 6', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000088', 'OnePlus 5', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000089', 'P9', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000090', 'ZenFone 3 Zoom', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000091', 'Galaxy Note 8', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000092', 'iPhone SE (2020)', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000093', 'Redmi Note 2', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000094', 'Galaxy A5', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000095', 'Pixel C', 'Google', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000096', 'Xperia XZ', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000097', 'Mi 5', 'Xiaomi', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000098', 'OnePlus 3T', 'OnePlus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000099', 'P8', 'Huawei', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000100', 'ZenFone 2 Laser', 'Asus', id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'TV');
        insert into products(product_id, name, brand, category_id) values ('0000000000201', 'LG OLED C1', 'LG', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000202', 'Samsung QLED Q90T', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000203', 'Sony Bravia X90J', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000204', 'TCL 6-Series', 'TCL', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000205', 'Vizio OLED', 'Vizio', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000206', 'Samsung Neo QLED QN900A', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000207', 'LG NanoCell NANO99', 'LG', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000208', 'Sony XBR A90J', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000209', 'TCL 5-Series', 'TCL', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000210', 'Vizio P-Series Quantum', 'Vizio', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000211', 'Samsung Crystal UHD 8 Series', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000212', 'LG OLED G1', 'LG', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000213', 'Sony Bravia A80J', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000214', 'TCL 4-Series', 'TCL', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000215', 'Vizio M-Series', 'Vizio', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000216', 'Samsung The Frame', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000217', 'LG OLED BX', 'LG', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000218', 'Sony XBR X95J', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000219', 'TCL 3-Series', 'TCL', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000220', 'Vizio V-Series', 'Vizio', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000221', 'Samsung Crystal UHD 7 Series', 'Samsung', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000222', 'LG NanoCell NANO90', 'LG', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000223', 'Sony Bravia X85J', 'Sony', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000224', 'TCL 2-Series', 'TCL', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000225', 'Vizio D-Series', 'Vizio', id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'PC');
        insert into products(product_id, name, brand, category_id) values ('0000000000101', 'MacBook Pro', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000102', 'Surface Pro 7', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000103', 'ThinkPad X1 Carbon', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000104', 'ROG Zephyrus G14', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000105', 'Dell XPS 15', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000106', 'HP Spectre x360', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000107', 'Surface Laptop 4', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000108', 'Inspiron 15 7000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000109', 'Legion 5', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000110', 'ROG Strix G15', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000111', 'MacBook Air', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000112', 'Surface Book 3', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000113', 'ThinkPad T14s', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000114', 'ROG Zephyrus G15', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000115', 'Dell G5 15', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000116', 'HP Envy x360', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000117', 'Surface Studio 2', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000118', 'Inspiron 14 5000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000119', 'Legion 7', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000120', 'ROG Strix Scar 15', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000121', 'iMac', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000122', 'Surface Go 2', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000123', 'ThinkCentre M75q', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000124', 'ROG Strix G17', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000125', 'Dell Precision 5550', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000126', 'HP Pavilion', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000127', 'Surface Pro X', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000128', 'Inspiron 13 7000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000129', 'ThinkStation P340', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000130', 'ROG Zephyrus G14', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000131', 'Mac mini', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000132', 'Surface Laptop Go', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000133', 'ThinkPad X1 Extreme', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000134', 'ROG Zephyrus S17', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000135', 'Dell Alienware Aurora', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000136', 'HP Omen', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000137', 'Surface Studio', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000138', 'Inspiron 27 7000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000139', 'ThinkCentre M720q', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000140', 'ROG Strix G15 Advantage Edition', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000141', 'iMac Pro', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000142', 'Surface Pro 6', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000143', 'ThinkPad T15', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000144', 'ROG Strix G15', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000145', 'Dell Inspiron 3671', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000146', 'HP EliteBook', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000147', 'Surface Book 2', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000148', 'Inspiron 15 3000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000149', 'ThinkStation P520', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000150', 'ROG Strix G17', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000151', 'Mac Pro', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000152', 'Surface Laptop 3', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000153', 'ThinkPad X1 Yoga', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000154', 'ROG Zephyrus M16', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000155', 'Dell Latitude', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000156', 'HP ProBook', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000157', 'Surface Go', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000158', 'Inspiron 13 5000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000159', 'ThinkCentre M920q', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000160', 'ROG Zephyrus S15', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000161', 'iMac 27-inch', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000162', 'Surface Pro 5', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000163', 'ThinkPad L14', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000164', 'ROG Strix Scar 17', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000165', 'Dell G7 15', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000166', 'HP ZBook', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000167', 'Surface Pro 4', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000168', 'Inspiron 14 3000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000169', 'ThinkStation P620', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000170', 'ROG Strix G15 Advantage Edition', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000171', 'Mac mini M1', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000172', 'Surface Laptop 2', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000173', 'ThinkPad X1 Tablet', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000174', 'ROG Strix G14', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000175', 'Dell Precision 7550', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000176', 'HP EliteDesk', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000177', 'Surface Laptop', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000178', 'Inspiron 24 5000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000179', 'ThinkCentre M630e', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000180', 'ROG Zephyrus S14', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000181', 'iMac 21.5-inch', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000182', 'Surface Pro 3', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000183', 'ThinkPad L15', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000184', 'ROG Zephyrus G15', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000185', 'Dell Inspiron 3470', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000186', 'HP EliteBook x360', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000187', 'Surface Book', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000188', 'Inspiron 11 3000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000189', 'ThinkStation P520c', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000190', 'ROG Strix Scar 15', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000191', 'MacBook', 'Apple', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000192', 'Surface Pro X', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000193', 'ThinkPad E14', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000194', 'ROG Zephyrus G14', 'Asus', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000195', 'Dell Vostro', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000196', 'HP Pavilion x360', 'HP', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000197', 'Surface Hub 2S', 'Microsoft', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000198', 'Inspiron 15 5000', 'Dell', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000199', 'ThinkCentre M75s', 'Lenovo', id);
        insert into products(product_id, name, brand, category_id) values ('0000000000200', 'ROG Strix Scar 17', 'Asus', id);

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'SOFTWARE');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'STORAGE_DEVICE');

        select nextval('categories_id_seq') into id;
        insert into categories(id, name) values (id, 'OTHER');

    END
$$;
COMMIT;
