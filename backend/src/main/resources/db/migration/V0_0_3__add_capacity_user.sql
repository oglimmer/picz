
alter table user add column capacity bigint not null default 10485760;
alter table user add column used_capacity bigint not null default 0;
