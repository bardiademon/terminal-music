package com.bardiademon.music.terminal.service;

import com.bardiademon.music.terminal.data.entity.MusicEntity;
import com.bardiademon.music.terminal.data.mapper.MusicMapper;
import com.bardiademon.music.terminal.data.repository.MusicRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLConnection;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.bardiademon.music.terminal.controller.DatabaseConnection.getConnection;

public final class MusicService implements MusicRepository {

    private static MusicRepository musicRepository;

    public static MusicRepository repository() {
        if (musicRepository == null) {
            musicRepository = new MusicService();
        }
        return musicRepository;
    }

    @Override
    public Future<Void> addMusic(String path) {
        Promise<Void> promise = Promise.promise();

        String query = """
                insert into `music` ("path") values (?)
                """;

        JsonArray params = new JsonArray()
                .add(path);

        getConnection().updateWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete();
        });

        return promise.future();
    }

    @Override
    public Future<List<Integer>> addMusic(List<String> path) {
        Promise<List<Integer>> promise = Promise.promise();

        getConnection().getConnection(sqlConnectionResult -> {

            if (sqlConnectionResult.failed()) {
                promise.fail(sqlConnectionResult.cause());
                return;
            }

            String query = """
                    insert ignore into `music` (`path`) values (?)
                    """;

            List<JsonArray> params = path.stream().map(item -> new JsonArray().add(item)).toList();

            try (SQLConnection sqlConnection = sqlConnectionResult.result()) {
                sqlConnection.batchWithParams(query, params, resultHandler -> {
                    if (resultHandler.failed()) {
                        promise.fail(resultHandler.cause());
                        return;
                    }

                    fetchMusicIdsPaths(path).onSuccess(promise::complete).onFailure(failedFetch -> {
                        failedFetch.printStackTrace(System.out);
                        promise.fail(failedFetch);
                    });

                });
            }

        });

        return promise.future();
    }

    @Override
    public Future<Void> setFavorite(int musicId, boolean favorite) {
        Promise<Void> promise = Promise.promise();

        String query = """
                update `music` set `favorite` = ? where `id` = ?
                """;

        JsonArray params = new JsonArray()
                .add(favorite)
                .add(musicId);

        getConnection().updateWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete();
        });

        return promise.future();
    }

    @Override
    public Future<Void> setLatPlay(int musicId) {
        Promise<Void> promise = Promise.promise();

        setFalseLatPlay().onSuccess(success -> {

            String query = """
                    update `music` set `last_play` = true , `last_play_at` = now() where `id` = ?
                    """;

            JsonArray params = new JsonArray()
                    .add(musicId);

            getConnection().updateWithParams(query, params, resultHandler -> {
                if (resultHandler.failed()) {
                    promise.fail(resultHandler.cause());
                    return;
                }
                promise.complete();
            });

        }).onFailure(failedSetFalse -> {
            failedSetFalse.printStackTrace(System.out);
            promise.fail(failedSetFalse);
        });

        return promise.future();
    }

    private Future<Void> setFalseLatPlay() {
        Promise<Void> promise = Promise.promise();

        String query = """
                update `music` set `last_play` = false
                """;

        JsonArray params = new JsonArray();

        getConnection().updateWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete();
        });

        return promise.future();
    }

    @Override
    public Future<List<MusicEntity>> fetchAllMusic() {
        Promise<List<MusicEntity>> promise = Promise.promise();

        String query = """
                select
                    "id",
                    "path",
                    "favorite",
                    "last_play",
                    strftime('%Y/%m/%d %H:%M:%S', "created_at") as "last_play_at",
                    strftime('%Y/%m/%d %H:%M:%S', "created_at") as "created_at"
                from "music"
                """;

        JsonArray params = new JsonArray();

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete(MusicMapper.toMusics(resultHandler.result().getRows()));
        });

        return promise.future();
    }

    @Override
    public Future<MusicEntity> fetchMusicById(int musicId) {
        Promise<MusicEntity> promise = Promise.promise();

        String query = """
                select
                    `id`,
                    `path`,
                    `favorite`,
                    `last_play`,
                     date_format(`created_at`, '%d/%m/%Y %H:%i:%s') as created_at,
                     date_format(`last_play_at`, '%d/%m/%Y %H:%i:%s') as last_play_at
                from `music`
                     where `id` = ?
                """;

        JsonArray params = new JsonArray()
                .add(musicId);

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }

            if (Service.isEmptyResult(resultHandler)) {
                promise.complete(null);
                return;
            }

            promise.complete(MusicMapper.toMusic(resultHandler.result().getRows().getFirst()));
        });

        return promise.future();
    }

    @Override
    public Future<MusicEntity> fetchLastPlayMusic() {
        Promise<MusicEntity> promise = Promise.promise();

        String query = """
                select
                    `id`,
                    `path`,
                    `favorite`,
                    `last_play`,
                    date_format(`created_at`, '%d/%m/%Y %H:%i:%s') as created_at,
                    date_format(`last_play_at`, '%d/%m/%Y %H:%i:%s') as last_play_at
                from `music`
                     where `last_play` = true
                """;

        JsonArray params = new JsonArray();

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }

            if (Service.isEmptyResult(resultHandler)) {
                promise.complete(null);
                return;
            }

            promise.complete(MusicMapper.toMusic(resultHandler.result().getRows().getFirst()));
        });

        return promise.future();
    }

    @Override
    public Future<List<Integer>> fetchMusicIdsPaths(List<String> musicId) {
        Promise<List<Integer>> promise = Promise.promise();

        String query = """
                select `id` from `music` where `path` in (::QUESTION::)
                """
                .replace("::QUESTION::", Service.createQueryForInClause(musicId.size()));

        JsonArray params = new JsonArray()
                .addAll(new JsonArray(musicId));

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }

            if (Service.isEmptyResult(resultHandler)) {
                promise.complete(Collections.emptyList());
                return;
            }

            promise.complete(resultHandler.result().getRows().stream().map(item -> item.getInteger("id")).toList());
        });

        return promise.future();
    }

    @Override
    public Future<List<MusicEntity>> searchMusic(String path, int limit, int offset) {
        Promise<List<MusicEntity>> promise = Promise.promise();

        String query = """
                select
                    `id`,
                    `path`,
                    `favorite`,
                    `last_play`,
                    date_format(`created_at`, '%d/%m/%Y %H:%i:%s') as created_at,
                    date_format(`last_play_at`, '%d/%m/%Y %H:%i:%s') as last_play_at
                from `music`
                    where `path` is not null
                      and substr(`path`, length(`path`) - instr(reverse(`path`), '::SEP::') + length('::SEP::') + 1) like ?
                order by `id` asc
                    limit ? offset ?
                """
                .replace("::SEP::", (File.separator.equals("\\") ? File.separator + "\\" : File.separator));

        JsonArray params = new JsonArray()
                .add(String.format("%%%s%%", path))
                .add(limit).add(offset);

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete(MusicMapper.toMusics(resultHandler.result().getRows()));
        });

        return promise.future();
    }

    @Override
    public Future<Integer> totalSearchMusic(String path) {
        Promise<Integer> promise = Promise.promise();

        String query = """
                select
                    count(`id`) as count
                from `music`
                    where `path` is not null
                       and substr(`path`, length(`path`) - instr(reverse(`path`), '::SEP::') + length('::SEP::') + 1) like ?
                """
                .replace("::SEP::", (File.separator.equals("\\") ? File.separator + "\\" : File.separator));

        JsonArray params = new JsonArray()
                .add(String.format("%%%s%%", path));

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            try {
                promise.complete(resultHandler.result().getRows().getFirst().getInteger("count", 0));
            } catch (Exception e) {
                promise.complete(0);
            }
        });

        return promise.future();
    }

    @Override
    public Future<List<MusicEntity>> fetchMusic(int limit, int offset) {
        Promise<List<MusicEntity>> promise = Promise.promise();

        String query = """
                select
                    `id`,
                    `path`,
                    `favorite`,
                    `last_play`,
                    date_format(`created_at`, '%d/%m/%Y %H:%i:%s') as created_at,
                    date_format(`last_play_at`, '%d/%m/%Y %H:%i:%s') as last_play_at
                from `music`
                    order by `id` asc
                limit ? offset ?
                """;

        JsonArray params = new JsonArray()
                .add(limit).add(offset);

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete(MusicMapper.toMusics(resultHandler.result().getRows()));
        });

        return promise.future();
    }

    @Override
    public Future<Integer> fetchTotalMusic() {
        Promise<Integer> promise = Promise.promise();

        String query = """
                select count(`id`) as count from `music`
                """;

        JsonArray params = new JsonArray();

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            try {
                promise.complete(resultHandler.result().getRows().getFirst().getInteger("count"));
            } catch (Exception e) {
                promise.complete(0);
            }
        });

        return promise.future();
    }

    @Override
    public Future<List<MusicEntity>> fetchMusicByPlayList(int playListId, int limit, int offset) {
        Promise<List<MusicEntity>> promise = Promise.promise();

        String query = """
                select
                    `m`.`id`,
                    `m`.`path`,
                    `m`.`favorite`,
                    `m`.`last_play`,
                    date_format(`m`.`created_at`, '%d/%m/%Y %H:%i:%s') as created_at,
                    date_format(`m`.`last_play_at`, '%d/%m/%Y %H:%i:%s') as last_play_at
                from `play_list_music` plm , `music` m
                      where `plm`.`play_list_id` = ?
                            and `m`.`id` = `plm`.`music_id`
                    order by `m`.`id` asc
                limit ? offset ?
                """;

        JsonArray params = new JsonArray()
                .add(playListId).add(limit).add(offset);

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete(MusicMapper.toMusics(resultHandler.result().getRows()));
        });

        return promise.future();
    }

    @Override
    public Future<Integer> fetchTotalMusicByPlayList(int playListId) {
        Promise<Integer> promise = Promise.promise();

        String query = """
                select count(`m`.`id`) as count
                    from `play_list_music` plm , `music` m
                      where `plm`.`play_list_id` = ?
                            and `m`.`id` = `plm`.`music_id`
                """;

        JsonArray params = new JsonArray()
                .add(playListId);

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            try {
                promise.complete(resultHandler.result().getRows().getFirst().getInteger("count"));
            } catch (Exception e) {
                promise.complete(0);
            }
        });

        return promise.future();
    }

    @Override
    public Future<List<MusicEntity>> fetchFavoriteMusic(int limit, int offset) {
        Promise<List<MusicEntity>> promise = Promise.promise();

        String query = """
                select
                    `id`,
                    `path`,
                    `favorite`,
                    `last_play`,
                    date_format(`created_at`, '%d/%m/%Y %H:%i:%s') as created_at,
                    date_format(`last_play_at`, '%d/%m/%Y %H:%i:%s') as last_play_at
                from `music`
                         where `favorite` = true
                    order by `id` asc
                limit ? offset ?
                """;

        JsonArray params = new JsonArray()
                .add(limit).add(offset);

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete(MusicMapper.toMusics(resultHandler.result().getRows()));
        });

        return promise.future();
    }

    @Override
    public Future<Integer> fetchTotalFavoriteMusic() {
        Promise<Integer> promise = Promise.promise();

        String query = """
                select count(`id`) as count from `music` where `favorite` = true
                """;

        JsonArray params = new JsonArray();

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            try {
                promise.complete(resultHandler.result().getRows().getFirst().getInteger("count"));
            } catch (Exception e) {
                promise.complete(0);
            }
        });

        return promise.future();
    }

    @Override
    public Future<Void> deleteMusic(int musicId) {
        Promise<Void> promise = Promise.promise();

        String query = """
                delete from `music` where `id` = ?
                """;

        JsonArray params = new JsonArray()
                .add(musicId);

        getConnection().updateWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete();
        });

        return promise.future();
    }
}
