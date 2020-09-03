create table unit (
  account_id integer,
  id integer,
  title varchar,
  notation varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, id)
);

create table currency (
  account_id integer,
  id integer,
  title varchar,
  notation varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, id)
);

create table client (
  account_id integer,
  id integer,
  name varchar,
  contacts varchar,
  notes varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, id)
);

create table supplier (
  account_id integer,
  id integer,
  name varchar,
  contacts varchar,
  notes varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, id)
);

create table category (
  account_id integer,
  id integer,
  parent_id integer,
  title varchar,
  notes varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, id)
);

create table settings (
  account_id integer,
  id integer,
  key varchar,
  value varchar,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique(account_id, id),
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
  id integer,
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
  unique(account_id, id)
);

create table price (
  account_id integer,
  id         integer,
  product_id integer,
  currency_id integer,
  price bigint,
  set_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, id)
);

create table salecheck (
  account_id integer,
  id integer,
  client_id integer,
  cash bigint,
  change bigint,
  discount bigint,
  closed boolean,
  sold_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, id)
);

create table salecheckitem (
  account_id integer,
  id integer,
  sale_check_id integer,
  product_id integer,
  unit_id integer,
  currency_id integer,
  quantity bigint,
  original_price bigint,
  price bigint,
  discount bigint,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, id)
);

create table consignment (
  account_id integer,
  id integer,
  supplier_id integer,
  closed boolean,
  accepted_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, id)
);

create table consignmentitem (
  account_id integer,
  id integer,
  consignment_id integer,
  product_id integer,
  unit_id integer,
  currency_id integer,
  quantity bigint,
  price bigint,
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, id)
);

create table salecheckret (
  account_id integer,
  sale_check_item_id integer,
  quantity bigint,
  notes varchar,
  returned_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, sale_check_item_id)
);

create table consignmentret (
  account_id integer,
  consignment_item_id integer,
  quantity bigint,
  notes varchar,
  returned_at timestamp without time zone default (now() at time zone 'utc'),
  uploaded_at timestamp without time zone default (now() at time zone 'utc'),
  updated_at timestamp without time zone default (now() at time zone 'utc'),
  unique (account_id, consignment_item_id)
);