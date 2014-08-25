create table if not exists info(
	version varchar(45)
);

create table if not exists users(
	id int not null primary key auto_increment,
	client_id varchar(45) not null,
	provider varchar(45) not null,
	username varchar(45) not null default '',
	unique(client_id, provider)
);

create table if not exists projects(
	id int not null primary key auto_increment,
	owner_id int not null,
	name varchar(45) not null default '',
	args varchar(45) not null default '',
	run_configuration ENUM ('java', 'js', 'canvas', 'jquery'),
	link varchar(150) unique,
	foreign key (owner_id) references users(id)
);

create table if not exists files(
	id int not null primary key auto_increment,
	project_id int not null,
	name varchar(45) not null,
	content longtext,
	foreign key (project_id) references projects(id)
);