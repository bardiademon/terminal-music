package com.bardiademon.music.terminal;

import com.bardiademon.music.terminal.controller.DatabaseConnection;
import com.bardiademon.music.terminal.controller.TerminalMusicController;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicTerminalApplication extends AbstractVerticle {

    private static MusicTerminalApplication app;

    private static Vertx vertx;

    public static void main(String[] args) {
        initialTerminal();

        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Logger.getLogger("com").setLevel(Level.OFF);

        app = new MusicTerminalApplication();
        addShutdownHook("Close app", app::closeApp);

        VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(40)
                .setMaxEventLoopExecuteTime(10000000L)
                .setBlockedThreadCheckInterval(10000000000L);

        Vertx.vertx(vertxOptions).deployVerticle(app);
    }

    @Override
    public void start(Promise<Void> startPromise) {
        MusicTerminalApplication.vertx = super.vertx;
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

    private static void initialTerminal() {
        System.out.print("Initialing terminal");
        String osName = System.getProperty("os.name").trim().toLowerCase();
        System.out.printf(": %s\n", osName);
        if (osName.contains("windows")) {
            initialWindowsTerminal();
        } else if (osName.contains("linux")) {
            initialLinuxTerminal();
        } else {
            System.out.println("Unknown your os");
        }
    }

    private static void initialWindowsTerminal() {
        try {
            new ProcessBuilder("powershell.exe", "-Command", "$OutputEncoding = [Console]::OutputEncoding = [Text.UTF8Encoding]::new()").inheritIO().start().waitFor();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initialLinuxTerminal() {
        try {
            new ProcessBuilder("bash", "-c", "export LC_ALL=en_US.UTF-8 && export LANG=en_US.UTF-8").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Vertx getAppVertx() {
        return vertx;
    }
}
