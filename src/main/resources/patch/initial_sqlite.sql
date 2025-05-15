create table if not exists "music"
(
  "id"           integer primary key autoincrement not null,
  "path"         nvarchar(200),
  "last_play_at" datetime,
  "last_play"    boolean,
  "favorite"     boolean,
  "created_at"   datetime not null default current_timestamp,
  constraint "un_music_path"  unique ("path")
);

create table if not exists "play_list"
(
   "id"            integer primary key autoincrement not null,
   "name"          nvarchar(50)  not null,
   "created_at"    datetime      not null default current_timestamp,
   constraint "un_play_list_name"  unique ("name")
);

create table if not exists "play_list_music"
(
   "id"            integer primary key autoincrement not null,
   "music_id"      integer       not null,
   "play_list_id"  integer       not null,
   "created_at"    datetime      not null default current_timestamp,
   constraint "fk_play_list_music_music" foreign key ("music_id") references "music"("id"),
   constraint "fk_play_list_music"       foreign key ("play_list_id") references "play_list"("id"),
   constraint "un_play_list_music"  unique ("music_id", "play_list_id")
);