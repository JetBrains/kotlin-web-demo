drop table if exists dbinfo;
drop table if exists files;
drop table if exists projects;
drop table if exists users;

create table if not exists dbinfo(
	version varchar(45)
);

create table if not exists users(
	id int not null primary key auto_increment,
	client_id varchar(45) not null,
	provider ENUM ('google', 'twitter', 'facebook') not null,
	username varchar(45) not null default '',
	constraint client_id unique(client_id, provider)
);

create table if not exists projects(
	id int not null primary key auto_increment,
	owner_id int not null,
	name varchar(45) not null default '',
	args varchar(45) not null default '',
	run_configuration ENUM ('java', 'js', 'canvas', 'junit') not null default 'java',
	link varchar(150) unique,
  origin varchar(100),
	constraint project_name unique (owner_id, name),
	foreign key (owner_id) references users(id) on delete cascade
);

create table if not exists files(
	id int not null primary key auto_increment,
	project_id int not null,
	name varchar(45) not null,
	content longtext,
	constraint file_name unique (project_id, name),
	foreign key (project_id) references projects(id) on delete cascade
);