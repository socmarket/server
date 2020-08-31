create table auth_code_req (
  id           serial primary key,
  msisdn       bigint not null,
  captcha      varchar,
  ip           varchar,
  user_agent   varchar,
  fingerprint  varchar,
  requested_at timestamp without time zone default (now() at time zone 'utc')
);

create index auth_code_req_msisdn_idx on auth_code_req(msisdn);

create table auth_code (
  id           serial primary key,
  msisdn       bigint not null,
  code         varchar,
  provider     varchar,
  handle       varchar,
  status       integer default 0,
  sent_at      timestamp without time zone default (now() at time zone 'utc'),
  delivered_at timestamp without time zone,
  verified_at  timestamp without time zone,
  failed_at    timestamp without time zone
);

create index auth_code_msisdn_idx on auth_code(msisdn, code);

create table auth_token (
  id           serial primary key,
  msisdn       bigint not null,
  token        varchar,
  is_valid     boolean default true,
  given_at     timestamp without time zone default (now() at time zone 'utc'),
  last_used_at timestamp without time zone default (now() at time zone 'utc')
);

create index auth_token_token_msisdn on auth_token(token, msisdn);

create table account (
  id serial  primary key,
  msisdn     bigint,
  created_at timestamp without time zone default (now() at time zone 'utc'),
  unique (msisdn)
);