package com.bardiademon.music.terminal.service;

import com.bardiademon.music.terminal.data.entity.PlayListEntity;
import com.bardiademon.music.terminal.data.mapper.PlayListMapper;
import com.bardiademon.music.terminal.data.repository.PlayListRepository;
import com.bardiademon.music.terminal.exception.UniqueException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;

import static com.bardiademon.music.terminal.controller.DatabaseConnection.getConnection;

public final class PlayListService implements PlayListRepository {

    private static final String CONSTRAINT_UN_PLAY_LIST_NAME = "un_play_list_name";

    private static PlayListRepository playListRepository;

    public static PlayListRepository repository() {
        if (playListRepository == null) {
            playListRepository = new PlayListService();
        }
        return playListRepository;
    }

    @Override
    public Future<Void> addPlayList(String name) {
        Promise<Void> promise = Promise.promise();

        String query = """
                insert into `play_list` (`name`) values (?)
                """;

        JsonArray params = new JsonArray()
                .add(name);

        getConnection().updateWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                String errorMessage = resultHandler.cause().getMessage();
                if (errorMessage.contains(CONSTRAINT_UN_PLAY_LIST_NAME)) {
                    promise.fail(new UniqueException(name));
                } else {
                    promise.fail(resultHandler.cause());
                }
                return;
            }
            promise.complete();
        });

        return promise.future();
    }

    @Override
    public Future<Void> addMusic(int playListId, int musicId) {
        Promise<Void> promise = Promise.promise();

        String query = """
                insert into `play_list_music` (`play_list_id`,`music_id`) values (?,?)
                """;

        JsonArray params = new JsonArray()
                .add(playListId)
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
    public Future<Void> addMusic(int playListId, List<Integer> musicIds) {
        Promise<Void> promise = Promise.promise();

        getConnection().getConnection(sqlConnectionResult -> {

            if (sqlConnectionResult.failed()) {
                promise.fail(sqlConnectionResult.cause());
                return;
            }

            try (SQLConnection sqlConnection = sqlConnectionResult.result()) {

                String query = """
                        insert ignore into `play_list_music` (`play_list_id`,`music_id`) values (?,?)
                        """;

                List<JsonArray> params = musicIds.stream().map(musicId -> new JsonArray().add(playListId).add(musicId)).toList();

                sqlConnection.batchWithParams(query, params, resultHandler -> {
                    if (resultHandler.failed()) {
                        promise.fail(resultHandler.cause());
                        return;
                    }
                    promise.complete();
                });
            }

        });


        return promise.future();
    }

    @Override
    public Future<List<PlayListEntity>> fetchPlayList() {
        Promise<List<PlayListEntity>> promise = Promise.promise();

        String query = """
                select
                    `id`,
                    `name`,
                    date_format(`created_at`, '%d/%m/%Y %H:%i:%s') as created_at
                from `play_list`
                """;

        JsonArray params = new JsonArray();

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }
            promise.complete(PlayListMapper.toPlayLists(resultHandler.result().getRows()));
        });

        return promise.future();
    }

    @Override
    public Future<PlayListEntity> fetchPlayList(String name) {

        Promise<PlayListEntity> promise = Promise.promise();

        String query = """
                select
                    `id`,
                    `name`,
                    date_format(`created_at`, '%d/%m/%Y %H:%i:%s') as created_at
                from `play_list`
                    where `name` = ?
                """;

        JsonArray params = new JsonArray()
                .add(name);

        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                promise.fail(resultHandler.cause());
                return;
            }

            if (Service.isEmptyResult(resultHandler)) {
                promise.complete(null);
                return;
            }

            promise.complete(PlayListMapper.toPlayList(resultHandler.result().getRows().getFirst()));

        });

        return promise.future();
    }

    @Override
    public Future<Void> deletePlayList(int playListId) {
        Promise<Void> promise = Promise.promise();

        String query = """
                delete from `play_list` where `id` = ?
                """;

        JsonArray params = new JsonArray()
                .add(playListId);

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
    public Future<Void> deleteMusicPlayList(int playListId, int musicId) {
        Promise<Void> promise = Promise.promise();

        String query = """
                delete from `play_list_music` where `play_list_id` = ? and `music_id` = ?
                """;

        JsonArray params = new JsonArray()
                .add(playListId)
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
    public Future<Void> deleteMusic(int musicId) {
        Promise<Void> promise = Promise.promise();

        String query = """
                delete from `play_list_music` where `music_id` = ?
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

    @Override
    public Future<Void> deleteMusicPlayList(int playListId) {
        Promise<Void> promise = Promise.promise();

        String query = """
                delete from `play_list_music` where `play_list_id` = ?
                """;

        JsonArray params = new JsonArray()
                .add(playListId);

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
