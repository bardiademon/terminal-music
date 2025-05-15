package com.bardiademon.music.terminal.service;

import io.vertx.core.AsyncResult;
import io.vertx.ext.sql.ResultSet;

public final class Service {

    private Service() {
    }

    public static boolean isEmptyResult(AsyncResult<ResultSet> handler) {
        return handler == null || handler.result() == null || handler.result().getRows() == null || handler.result().getRows().isEmpty();
    }

    public static String createQueryForInClause(int paramSize) {
        return "?,".repeat(paramSize - 1) + "?";
    }

}
