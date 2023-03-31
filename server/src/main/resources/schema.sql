create table if not exists profiles(
    profile_id serial primary key,
    email varchar(255) unique,
    name varchar (255),
    surname varchar(255)
);

create table if not exists products(
    product_id varchar(15) primary key,
    name varchar (255),
    brand varchar(255)
);