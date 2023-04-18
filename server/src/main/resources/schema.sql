create table if not exists profiles(
    profile_id serial primary key,
    email varchar(255) unique not null,
    name varchar (255) not null,
    surname varchar(255) not null
);

create table if not exists products(
    product_id varchar(15) primary key,
    name varchar (255) not null,
    brand varchar(255) not null
);