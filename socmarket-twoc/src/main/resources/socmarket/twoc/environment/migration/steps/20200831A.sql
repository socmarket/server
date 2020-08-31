create table unit (
  account_id integer,
  unit_id integer,
  title varchar,
  notation varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, unit_id)
);

create table currency (
  account_id integer,
  currency_id integer,
  title varchar,
  notation varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, currency_id)
);

create table client (
  account_id integer,
  client_id integer,
  name varchar,
  contacts varchar,
  notes varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, client_id)
);

create table supplier (
  account_id integer,
  supplier_id integer,
  name varchar,
  contacts varchar,
  notes varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, supplier_id)
);

create table category (
  account_id integer,
  category_id integer,
  parent_id integer,
  title varchar,
  notes varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, category_id)
);

create table settings (
  account_id integer,
  key varchar,
  value varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, key)
);

create table barcode (
  account_id integer,
  code integer,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, code)
);

create table product (
  account_id integer,
  product_id integer,
  barcode varchar,
  code varchar,
  title varchar,
  notes varchar,
  unit_id int,
  category_id integer,
  brand varchar,
  model varchar,
  engine varchar,
  oemno varchar,
  serial varchar,
  coord varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, product_id)
);

create table price (
  account_id integer,
  product_id integer,
  currency_id integer,
  price bigint,
  set_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc')
);

create table salecheck (
  account_id integer,
  sale_check_id integer,
  client_id integer,
  cash bigint,
  change bigint,
  closed boolean,
  sold_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, sale_check_id)
);

create table salecheckitem (
  account_id integer,
  sale_check_item_id integer,
  sale_check_id integer,
  product_id integer,
  unit_id integer,
  currency_id integer,
  quantity bigint,
  original_price bigint,
  price bigint,
  discount bigint,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc')
);

create table consignment (
  account_id integer,
  consignment_id integer,
  supplier_id integer,
  closed boolean,
  accepted_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, consignment_id)
);

create table consignmentitem (
  account_id integer,
  consignment_item_id integer,
  consignment_id integer,
  product_id integer,
  unit_id integer,
  currency_id integer,
  quantity bigint,
  price bigint,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc')
);

create table salecheckret (
  account_id integer,
  sale_check_item_id integer,
  quantity bigint,
  notes varchar,
  returned_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc')
);

create table consignmentret (
  account_id integer,
  consignment_item_id integer,
  quantity bigint,
  notes varchar,
  returned_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc')
);
