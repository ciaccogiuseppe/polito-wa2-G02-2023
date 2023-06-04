create table if not exists products
(
    product_id varchar(255) not null
        primary key,
    brand      varchar(255) not null,
    name       varchar(255) not null
);

alter table products
    owner to postgres;

