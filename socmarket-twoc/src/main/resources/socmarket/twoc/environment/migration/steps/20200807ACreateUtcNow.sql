create or replace function utcnow() returns timestamp
language plpgsql
as $$
begin
  return now() at time zone 'UTC';
end
$$