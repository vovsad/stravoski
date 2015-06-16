# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table activity_model (
  id                        integer auto_increment not null,
  resource_state            integer,
  external_id               varchar(255),
  upload_id                 integer,
  name                      varchar(255),
  distance                  float,
  moving_time               integer,
  elapsed_time              integer,
  total_elevation_gain      float,
  type                      varchar(255),
  start_date                varchar(255),
  start_date_local          varchar(255),
  time_zone                 varchar(255),
  start_lat                 varchar(255),
  start_lng                 varchar(255),
  end_lat                   varchar(255),
  end_lng                   varchar(255),
  location_city             varchar(255),
  location_state            varchar(255),
  achievement_count         integer,
  kudos_count               integer,
  comment_count             integer,
  athlete_count             integer,
  photo_count               integer,
  trainer                   tinyint(1) default 0,
  commute                   tinyint(1) default 0,
  manual                    tinyint(1) default 0,
  private                   tinyint(1) default 0,
  flagged                   tinyint(1) default 0,
  gear_id                   varchar(255),
  average_speed             float,
  max_speed                 float,
  average_cadence           float,
  average_temp              integer,
  average_watts             float,
  kilojoules                float,
  average_heartrate         float,
  max_heartrate             float,
  calories                  float,
  truncated                 integer,
  has_kudoed                tinyint(1) default 0,
  constraint pk_activity_model primary key (id))
;

create table athlete_model (
  id                        integer auto_increment not null,
  resource_state            varchar(255),
  firstname                 varchar(255),
  lastname                  varchar(255),
  profile_medium            varchar(255),
  profile                   varchar(255),
  city                      varchar(255),
  state                     varchar(255),
  sex                       varchar(255),
  friend                    varchar(255),
  follower                  varchar(255),
  premium                   tinyint(1) default 0,
  created_at                varchar(255),
  updated_at                varchar(255),
  date_preference           varchar(255),
  measurement_preference    varchar(255),
  email                     varchar(255),
  constraint pk_athlete_model primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table activity_model;

drop table athlete_model;

SET FOREIGN_KEY_CHECKS=1;

