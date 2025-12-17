create extension if not exists pgcrypto;

create table orders (
  order_id uuid primary key,
  email varchar(255) not null,
  first_name varchar(100) not null,
  last_name varchar(100) not null,
  product_id varchar(100) not null,
  created_at timestamp not null default now(),
  constraint uk_orders_email_product unique (email, product_id)
);

