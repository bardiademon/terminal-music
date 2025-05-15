create database `terminal_music` character set 'utf8mb4';

use `terminal_music`;

create table if not exists `music`
(
  `id`           integer auto_increment not null,
  `path`         varchar(200)  character set 'utf8mb4' not null,
  `last_play_at` datetime,
  `last_play`    boolean,
  `favorite`     boolean,
  `created_at`   datetime not null default now(),
  constraint `pk_music_id`   primary key (`id`),
  constraint `un_music_path` unique (`path`)
) character set 'utf8mb4';

create table if not exists `play_list`
(
   `id`            integer auto_increment not null,
   `name`          varchar(50)  character set 'utf8mb4' not null ,
   `created_at`    datetime      not null default now(),
   constraint `pk_play_list_id`   primary key (`id`),
   constraint `un_play_list_name`  unique (`name`)
) character set 'utf8mb4';

create table if not exists `play_list_music`
(
   `id`            integer auto_increment not null,
   `music_id`      integer       not null,
   `play_list_id`  integer       not null,
   `created_at`    datetime      not null default now(),
   constraint `pk_play_list_music_id`   primary key (`id`),
   constraint `fk_play_list_music_music` foreign key (`music_id`) references `music`(`id`),
   constraint `fk_play_list_music`       foreign key (`play_list_id`) references `play_list`(`id`),
   constraint `un_play_list_music`  unique (`music_id`, `play_list_id`)
) character set 'utf8mb4';