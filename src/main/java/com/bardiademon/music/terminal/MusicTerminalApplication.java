package com.bardiademon.music.terminal;

import com.bardiademon.music.terminal.controller.DatabaseConnection;
import com.bardiademon.music.terminal.controller.TerminalMusicController;
import com.bardiademon.music.terminal.utils.Paths;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicTerminalApplication extends AbstractVerticle {

    private static MusicTerminalApplication app;

    private static Vertx vertx;

    public static void main(String[] args) {
        Logger.getLogger("com.mchange.v2").setLevel(Level.OFF);

        System.out.println("bardiademon");
        app = new MusicTerminalApplication();
        addShutdownHook("Close app", app::closeApp);

        VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(40)
                .setMaxEventLoopExecuteTime(10000000L)
                .setBlockedThreadCheckInterval(10000000000L);
        ;

        Vertx.vertx(vertxOptions).deployVerticle(app);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        MusicTerminalApplication.vertx = super.vertx;

        File dbDataPath = new File(Paths.DATA_PATH);
        if (!dbDataPath.exists() && !dbDataPath.mkdirs()) {
            throw new FileNotFoundException(dbDataPath.getAbsolutePath());
        }

        DatabaseConnection.connect(vertx).onSuccess(successConnection -> {
            new TerminalMusicController();
            startPromise.complete();
        }).onFailure(failedConnection -> {
            startPromise.fail(failedConnection);
            closeApp();
        });

    }

    public static InputStream getResource(String path) {
        return MusicTerminalApplication.class.getResourceAsStream(path);
    }

    private void closeApp() {
        try {
            DatabaseConnection.close();
            if (vertx != null) {
                vertx.close();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static MusicTerminalApplication getApp() {
        return app;
    }

    public static void addShutdownHook(String name, Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Starting shutdown -> " + name);
            runnable.run();
            System.out.println("Successfully shutdown -> " + name);
        }));
    }

    public static Vertx getAppVertx() {
        return vertx;
    }
}
