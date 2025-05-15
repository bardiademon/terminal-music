package com.bardiademon.music.terminal.controller;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.time.format.DateTimeFormatter;

import static com.bardiademon.music.terminal.utils.Paths.*;

public final class DatabaseConnection {

    public static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final int POOL_SIZE = 20;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "7348";

    private static SQLClient jdbcClient;

    private DatabaseConnection() {
    }

    public static Future<SQLConnection> connect(Vertx vertx) {
        Promise<SQLConnection> promise = Promise.promise();

        try {
            JsonObject config = new JsonObject()
                    .put("url", CONNECTION_URL)
                    .put("driver_class", DRIVER_CLASS)
                    .put("max_pool_size", POOL_SIZE)
                    .put("user", USERNAME)
                    .put("password", PASSWORD);

            jdbcClient = JDBCClient.createShared(vertx, config);

            jdbcClient.getConnection(resultHandler -> {
                if (resultHandler.succeeded()) {
                    resultHandler.result().close();
                    promise.complete();
                } else {
                    resultHandler.cause().printStackTrace(System.err);
                    promise.fail(resultHandler.cause());
                }
            });
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        return promise.future();
    }

    public static void close() {
        jdbcClient.close();
    }

    public static SQLClient getConnection() {
        return jdbcClient;
    }
}
