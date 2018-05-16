create database highways;

use highways;

create table incidents
(
  id              int auto_increment
    primary key,
  positive_agent  varchar(127) default 'NA'        null,
  passive_agent   varchar(127) default 'NA'        null,
  vehicle         varchar(127) default 'NA'        null,
  time            datetime                         null,
  location        varchar(127)                     null,
  road_name       varchar(127)                     null,
  road_type       varchar(127)                     null,
  road_condition  varchar(127) default 'normal'    null,
  speed_limit     int default '0'                  null,
  weather         varchar(127) default 'sunny'     null,
  temperature     int                              null,
  light_condition varchar(127) default 'normal'    null,
  planned         varchar(127) default 'unplanned' null,
  type            varchar(127)                     null,
  severity        varchar(127) default 'minor'     null,
  description     varchar(511)                     null,
  constraint incidents_id_uindex
  unique (id)
)
  engine = InnoDB;



INSERT INTO highways.incidents (id, positive_agent, passive_agent, vehicle, time, location, road_name, road_type, road_condition, speed_limit, weather, temperature, light_condition, planned, type, severity, description) VALUES (1, 'NA', 'NA', 'NA', '2018-05-06 00:00:00', '', null, 'A', 'normal', 0, 'sunny', null, 'normal', 'unplanned', null, 'minor', '');
INSERT INTO highways.incidents (id, positive_agent, passive_agent, vehicle, time, location, road_name, road_type, road_condition, speed_limit, weather, temperature, light_condition, planned, type, severity, description) VALUES (4, 'NA', 'NA', 'NA', '2018-05-01 00:00:00', '', null, 'Other', 'normal', 0, 'sunny', 15, 'normal', 'traffic control', null, 'minor', '');
INSERT INTO highways.incidents (id, positive_agent, passive_agent, vehicle, time, location, road_name, road_type, road_condition, speed_limit, weather, temperature, light_condition, planned, type, severity, description) VALUES (5, 'person', 'person', 'car', '2018-05-06 00:00:00', 'Main Street', null, 'A', 'wet', 60, 'rainy', 16, 'dim', 'unplanned', 'death', 'severe', 'A car bumped into a pedestrian');
INSERT INTO highways.incidents (id, positive_agent, passive_agent, vehicle, time, location, road_name, road_type, road_condition, speed_limit, weather, temperature, light_condition, planned, type, severity, description) VALUES (6, 'animal', 'animal', 'NA', '2018-05-16 00:00:00', '', null, 'B', 'wet', 20, 'cloudy', null, 'dim', 'unplanned', 'other', 'moderate', 'A cat and a dog fought on the wet road.');