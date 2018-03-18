create database grades;
create table `2014`
(
  department varchar(255) not null
    primary key,
  totalCount int          null,
  average    double       null,
  gradeA     double       null,
  gradeB     double       null,
  gradeC     double       null,
  gradeD     double       null,
  gradeF     double       null,
  excellent  double       null,
  qualified  double       null,
  failed     double       null,
  constraint `2014_department_uindex`
  unique (department)
)
  engine = InnoDB
  charset = utf8;

create table `2015`
(
  department varchar(255) not null
    primary key,
  totalCount int          null,
  average    double       null,
  gradeA     double       null,
  gradeB     double       null,
  gradeC     double       null,
  gradeD     double       null,
  gradeF     double       null,
  excellent  double       null,
  qualified  double       null,
  failed     double       null,
  constraint `2015_department_uindex`
  unique (department)
)
  engine = InnoDB
  charset = utf8;

create table `2016`
(
  department varchar(255) not null
    primary key,
  totalCount int          null,
  average    double       null,
  gradeA     double       null,
  gradeB     double       null,
  gradeC     double       null,
  gradeD     double       null,
  gradeF     double       null,
  excellent  double       null,
  qualified  double       null,
  failed     double       null,
  constraint `2016_department_uindex`
  unique (department)
)
  engine = InnoDB
  charset = utf8;


create table `2017`
(
  department varchar(255) not null
    primary key,
  totalCount int          null,
  average    double       null,
  gradeA     double       null,
  gradeB     double       null,
  gradeC     double       null,
  gradeD     double       null,
  gradeF     double       null,
  excellent  double       null,
  qualified  double       null,
  failed     double       null,
  constraint `2017_department_uindex`
  unique (department)
)
  engine = InnoDB
  charset = utf8;





