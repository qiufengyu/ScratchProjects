create table webs.news
(
	id int auto_increment
		primary key,
	title varchar(255) null,
	content blob null,
	keywords varchar(127) null,
	summary varchar(511) null,
	time int null,
	url varchar(255) null,
	constraint news_url_uindex
		unique (url)
)
collate=utf8mb4_bin;

