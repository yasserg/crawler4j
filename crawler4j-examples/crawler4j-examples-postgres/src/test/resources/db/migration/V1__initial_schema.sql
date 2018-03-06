CREATE SEQUENCE id_master_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 6
  CACHE 1;

CREATE TABLE webpage(
  id bigint NOT NULL,
  html TEXT,
  text TEXT,
  url varchar(4096),
  seen timestamp without time zone NOT NULL,
  primary key (id)
);
