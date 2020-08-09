create table auth_code_req(
  id           serial primary key,
  msisdn       bigint not null,
  captcha      varchar,
  ip           varchar,
  user_agent   varchar,
  fingerprint  varchar,
  requested_at timestamp without time zone default (now() at time zone 'utc')
);

create index auth_code_req_msisdn_idx on auth_code_req(msisdn);

create table auth_code(
  id           serial primary key,
  msisdn       bigint not null,
  code         varchar,
  provider     varchar,
  handle       varchar,
  status       int default 0,
  sent_at      timestamp without time zone default (now() at time zone 'utc'),
  finished_at  timestamp without time zone
);

create index auth_code_msisdn_idx on auth_code(msisdn);
