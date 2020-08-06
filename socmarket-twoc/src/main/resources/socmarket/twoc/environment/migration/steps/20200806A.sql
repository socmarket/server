create table auth_code_req(
  id           serial primary key,
  msisdn       bigint not null,
  ip           varchar,
  user_agent   varchar,
  captcha      varchar,
  requested_at timestamp without time zone default (now() at time zone 'utc'),
  sent_at      timestamp without time zone,
  status       int default 0,
  updated_at   timestamp without time zone default (now() at time zone 'utc')
);