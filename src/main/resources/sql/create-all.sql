DROP SEQUENCE IF EXISTS hibernate_sequence;

CREATE SEQUENCE hibernate_sequence
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 146
  CACHE 1;

/*DROP TABLE IF EXISTS accounts_social_providers;*/
/*DROP TABLE IF EXISTS social_provider;*/
DROP TABLE IF EXISTS Test;
/*DROP TABLE IF EXISTS UserConnection;*/
DROP TABLE IF EXISTS persistent_logins;
DROP TABLE IF EXISTS password_reset_token;
DROP TABLE IF EXISTS verification_token;
DROP TABLE IF EXISTS accounts_roles;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS account;


CREATE TABLE account
(
  id bigint NOT NULL,
  email character varying(255) NOT NULL,
  password character varying(255) NOT NULL,
  enabled boolean NOT NULL DEFAULT FALSE,
  CONSTRAINT account_pkey PRIMARY KEY (id)
);

CREATE TABLE role
(
  id bigint NOT NULL,
  role character varying(50) NOT NULL,
  CONSTRAINT role_pkey PRIMARY KEY (id)
);

INSERT INTO account(id, email, password, enabled) VALUES (1, 'jose@jo.se', '$2a$10$kMVhUDUeDSdZOeCOboXzXOwy9f4VXvIp2yE0OEHIWA.BTlaX6.vpq', TRUE);
INSERT INTO account(id, email, password, enabled) VALUES (2, 'adrianq92@hotmail.com', '$2a$10$AK1rKs1jY0W0qjACmoDioO7gzCzJIxAfXDBgOi0gfyYaf.adw8m7y', TRUE);

INSERT INTO role(id,role) VALUES (1, 'ROLE_USER');
INSERT INTO role(id,role) VALUES (2, 'ROLE_ADMIN');

CREATE TABLE accounts_roles
(
	account_id bigint NOT NULL,
	role_id bigint NOT NULL,
	CONSTRAINT usersroles_accid_fkey FOREIGN KEY (account_id) REFERENCES account (id),
	CONSTRAINT usersroles_roleid_fkey FOREIGN KEY (role_id) REFERENCES role (id)
);

INSERT INTO accounts_roles(account_id, role_id) VALUES (1, 1);
INSERT INTO accounts_roles(account_id, role_id) VALUES (1, 2);
INSERT INTO accounts_roles(account_id, role_id) VALUES (2, 1);
INSERT INTO accounts_roles(account_id, role_id) VALUES (2, 2);

/*Email Verification Token*/
CREATE TABLE verification_token
(
  id bigint NOT NULL,
  token character varying(255) NOT NULL,
  expiry_date timestamp without time zone NOT NULL,
  account_id bigint NOT NULL,
  CONSTRAINT verification_token_pkey PRIMARY KEY (id),
  CONSTRAINT verification_token_fkey FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE TABLE password_reset_token
(
  id bigint NOT NULL,
  token character varying(255) NOT NULL,
  account_id bigint NOT NULL,
  expiry_date timestamp without time zone NOT NULL,
  CONSTRAINT password_reset_token_pkey PRIMARY KEY (id),
  CONSTRAINT password_reset_token_fkey FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE TABLE persistent_logins
(
  username VARCHAR(64) NOT NULL,
  series VARCHAR(64) NOT NULL,
  token VARCHAR(64) NOT NULL,
  last_used TIMESTAMP NOT NULL,
  PRIMARY KEY (series)
);

/*Social User connection*/
/*create table UserConnection (userId varchar(255) not null,
    providerId varchar(255) not null,
    providerUserId varchar(255),
    rank int not null,
    displayName varchar(255),
    profileUrl varchar(512),
    imageUrl varchar(512),
    accessToken varchar(255) not null,
    secret varchar(255),
    refreshToken varchar(255),
    expireTime bigint,
    primary key (userId, providerId, providerUserId));
create unique index UserConnectionRank on UserConnection(userId, providerId, rank);*/

create table Test (
  id bigint NOT NULL,
  account_id bigint NOT NULL,
  test_word varchar(100),
  CONSTRAINT test_pkey PRIMARY KEY (id),
  CONSTRAINT test_fkey FOREIGN KEY (account_id) REFERENCES account(id)
);

/*create table social_provider(
  provider_name varchar(50) NOT NULL,
  CONSTRAINT social_provider_pkey PRIMARY KEY (provider_name)
);*/

/*INSERT INTO social_provider(provider_name) VALUES ('FACEBOOK');
INSERT INTO social_provider(provider_name) VALUES ('LINKEDIN');
INSERT INTO social_provider(provider_name) VALUES ('GITHUB');
INSERT INTO social_provider(provider_name) VALUES ('GOOGLE');
INSERT INTO social_provider(provider_name) VALUES ('PIXELPIN');*/

/*
create table accounts_social_providers(
  account_id bigint NOT NULL,
  provider_name varchar(50) NOT NULL,
  CONSTRAINT accsocialproviders_accid_fkey FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT accsocialproviders_providername_fkey FOREIGN KEY (provider_name) REFERENCES social_provider(provider_name)
);*/
