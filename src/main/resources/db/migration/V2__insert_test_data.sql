insert into orders (order_id, email, first_name, last_name, product_id)
values
  (gen_random_uuid(), 'janet.weaver@reqres.in', 'Weaver', 'Janet', 'TV-10'),
  (gen_random_uuid(), 'emma.wong@reqres.in', 'Wong', 'Emma', 'TV-11'),
  (gen_random_uuid(), 'emma.wong@reqres.in', 'Wong', 'Emma', 'iPhone 17'),
  (gen_random_uuid(), 'janet.weaver@reqres.in', 'Weaver', 'Janet', 'macbook pro');
