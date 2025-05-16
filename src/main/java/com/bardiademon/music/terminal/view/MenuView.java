package com.bardiademon.music.terminal.view;

import com.bardiademon.music.terminal.MusicTerminalApplication;
import com.bardiademon.music.terminal.data.model.MenuModel;
import com.bardiademon.music.terminal.data.model.MenuTitleModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MenuView {

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private static boolean readingLine = false;

    private MenuView() {
    }

    public static <T> void showMenu(MenuModel<T> menu) {
        showMenu(menu, true);
    }

    public static <T> void showMenu(MenuModel<T> menu, boolean clear) {

        if (readingLine) {
            return;
        }

        MusicTerminalApplication.getAppVertx().executeBlocking(() -> {
            if (clear) {
                clearConsole();
                System.out.println("bardiademon");
            }

            System.out.printf("Menu %s, For exit app -> :exit\n", menu.name());

            if (menu.numberInputTitles() != null) {
                List<MenuTitleModel<Void>> numberInputTitles = menu.numberInputTitles();
                for (int i = 0; i < numberInputTitles.size(); i++) {
                    MenuTitleModel<Void> numberInputTitle = numberInputTitles.get(i);
                    System.out.printf("%d. %s\n", (i + 1), numberInputTitle.title());
                }
                while (true) {
                    System.out.print("Please enter an number: ");
                    try {
                        String line = readLine();
                        checkExit(line);

                        int number = Integer.parseInt(line);

                        if (number <= 0 || number > menu.numberInputTitles().size()) {
                            System.out.println("Invalid number");
                            continue;
                        }

                        MenuTitleModel<Void> voidMenuTitleModel = menu.numberInputTitles().get(number - 1);
                        String apply = voidMenuTitleModel.result().apply(null);
                        if (apply != null && !apply.isEmpty()) {
                            System.out.println(apply);
                        }
                        break;
                    } catch (Exception ignored) {
                    }
                }
            } else {
                System.out.printf("%s: ", menu.inputMessage().title());
                try {
                    String line = readLine();
                    checkExit(line);

                    String apply;
                    if (menu.inputMessage().map() != null) {
                        apply = menu.inputMessage().result().apply(menu.inputMessage().map().apply(line));
                    } else {
                        apply = menu.inputMessage().result().apply(null);
                    }

                    if (apply != null && !apply.isEmpty()) {
                        System.out.println(apply);
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }

            return null;
        });
    }

    private static void checkExit(String str) {
        if (str != null && str.equals(":exit")) {
            System.exit(0);
        }
    }

    public static void clearConsole() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception ignored) {
        }
    }

    public static String readLine() {
        try {
            readingLine = true;
            String line = reader.readLine();
            readingLine = false;
            return line;
        } catch (IOException e) {
            return "";
        }
    }

    public static boolean isReadingLine() {
        return readingLine;
    }
}
