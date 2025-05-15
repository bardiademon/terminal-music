package com.bardiademon.music.terminal.controller;

import com.bardiademon.music.terminal.data.entity.MusicEntity;
import com.bardiademon.music.terminal.data.entity.PlayListEntity;
import com.bardiademon.music.terminal.data.model.MenuModel;
import com.bardiademon.music.terminal.data.model.MenuTitleModel;
import com.bardiademon.music.terminal.exception.UniqueException;
import com.bardiademon.music.terminal.service.MusicService;
import com.bardiademon.music.terminal.service.PlayListService;
import com.bardiademon.music.terminal.utils.DurationUtil;
import com.bardiademon.music.terminal.utils.Pagination;
import com.bardiademon.music.terminal.view.MenuView;
import io.vertx.core.Future;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TerminalMusicController {

    private static final List<String> AUDIO_EXTENSIONS = List.of("mp3", "wav", "ogg", "flac", "aac", "wma", "m4a", "opus", "amr", "aiff");

    private static final String KEY_CD_PATH = "!c", KEY_SELECT_PATH = "!s", KEY_BACK_MAIN_MENU = "!b";

    private static final int LIMIT_FETCH_MUSIC = 20;

    private static final PlayerController player = new PlayerController();
    private static Function<Void, Void> nextMusic = null;
    private static Function<Void, Void> preMusic = null;
    private static MusicEntity playedMusic = null;
    private static boolean showPlayMusicTime = false;
    private static final List<Integer> playingMusicIndex = new ArrayList<>();
    private static boolean shuffle = false;
    private static final Random random = new Random();

    static {
        player.setPlayerListener(new PlayerListener() {
            @Override
            public void onFinished() {
                if (nextMusic != null) {
                    nextMusic.apply(null);
                }
            }

            @Override
            public void onTime(long time) {
                if (showPlayMusicTime) {
                    System.out.printf("\r%s / %s / %s",
                            DurationUtil.formatDuration(Duration.ofMillis(player.getTime())),
                            DurationUtil.formatDuration(Duration.ofMillis(player.getLength())),
                            FilenameUtils.getName(playedMusic.getPath())
                    );
                }
            }
        });
    }

    private MenuModel<Integer> getMainMenu() {
        List<MenuTitleModel<Void>> titles = new ArrayList<>();

        titles.add(MenuTitleModel.createVoid("Play last music", unused -> {
            TerminalMusicController.this.playLastMusic();
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Add musics", unused -> {
            TerminalMusicController.this.addMusics();
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Play List", unused -> {
            TerminalMusicController.this.playList();
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("List Musics", unused -> {
            TerminalMusicController.this.listMusic(0);
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("List Favorite Musics", unused -> {
            TerminalMusicController.this.listFavoriteMusic(0);
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Search Musics", unused -> {
            TerminalMusicController.this.searchMusic();
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Create Play list", unused -> {
            TerminalMusicController.this.createPlayList(playList -> {
                selectPlayList(playList);
                return null;
            });
            return null;
        }));

        if (player.isPlay()) {
            titles.add(MenuTitleModel.createVoid("Music control", unused -> {
                playMusic(playedMusic, false, false, nextMusic, preMusic);
                return null;
            }));
        }

        return MenuModel.numberInput("Main Menu", titles);
    }

    public TerminalMusicController() {
        mainMenu();
    }

    private void searchMusic() {
        MenuTitleModel<String> searchMusicTitle = MenuTitleModel.createString("Enter music name", name -> {

            if (name != null && name.isEmpty()) {
                searchMusic();
                return null;
            }
            searchMusic(name, LIMIT_FETCH_MUSIC, 0);
            return null;
        });
        MenuView.showMenu(MenuModel.inputMessage("Search music", searchMusicTitle));
    }

    private void searchMusic(String name, int limit, int offset) {

        Future<List<MusicEntity>> searchMusic = MusicService.repository().searchMusic(name, limit, offset);
        Future<Integer> totalSearchMusic = MusicService.repository().totalSearchMusic(name);

        Future.join(totalSearchMusic, searchMusic).onSuccess(fetchMusicResult -> {

            List<MusicEntity> music = searchMusic.result();
            int total = totalSearchMusic.result();

            if (total == 0 || music == null || music.isEmpty()) {
                System.out.println("Not found");
                mainMenu();
                return;
            }

            showResultListMusic(offset, limit, total, music, newOffset -> {
                searchMusic(name, limit, newOffset);
                return null;
            }, unused -> {
                MusicService.repository().searchMusic(name, total, 0).onSuccess(this::addToPlayList).onFailure(failedFetchMusics -> {
                    failedFetchMusics.printStackTrace(System.out);
                    searchMusic();
                });
                return null;
            });

        }).onFailure(failedSearchMusic -> {
            failedSearchMusic.printStackTrace(System.out);
            mainMenu();
        });
    }

    private void mainMenu() {
        MenuView.showMenu(getMainMenu());
    }

    private void addMusics() {
        getInputPath(null, selectedPath -> {

            System.out.println("Getting musics...");
            try (Stream<Path> stream = Files.walk(new File(selectedPath).toPath())) {

                AtomicInteger count = new AtomicInteger(0);
                List<String> musics = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> AUDIO_EXTENSIONS.contains(FilenameUtils.getExtension(p.getFileName().toString()).toLowerCase()))
                        .map(item -> {
                            System.out.print("\rFounded: " + count.incrementAndGet());
                            return item.toString();
                        })
                        .toList();

                if (musics.isEmpty()) {
                    System.out.print("Not found musics");
                    mainMenu();
                    return null;
                }

                System.out.println("\nAdding to db...");
                MusicService.repository().addMusic(musics).onSuccess(ids -> {
                    System.out.println("\rSuccess added musics, Size: " + musics.size());
                    MenuView.showMenu(MenuModel.inputMessage("Save to play list", MenuTitleModel.createString("Save to play list? Y/n", isYes -> {
                        if (isYes.equalsIgnoreCase("y")) {
                            addToPlayListByMusicIds(ids);
                        } else {
                            mainMenu();
                        }
                        return null;
                    })));
                }).onFailure(failedAddMusic -> {
                    System.out.println();
                    failedAddMusic.printStackTrace(System.out);
                    mainMenu();
                });

            } catch (IOException e) {
                e.printStackTrace(System.out);
                mainMenu();
            }

            return null;
        });
    }

    private void getInputPath(String inputPath, Function<String, Void> selectedPathResult) {
        if (inputPath != null) {
            File file = new File(inputPath);
            if (isValidDir(file)) {
                File[] files = file.listFiles();
                if (files != null) {
                    Set<String> ext = new HashSet<>();
                    int numberOfFiles = 0;
                    for (File item : files) {
                        if (item.isDirectory() && !item.isFile()) {
                            System.out.println(item.getName());
                        } else {
                            ext.add(FilenameUtils.getExtension(item.getName()));
                            numberOfFiles++;
                        }
                    }
                    System.out.printf("Number of files: %s , Extension: %s\n", numberOfFiles, ext);
                }
            } else {
                System.out.println("Invalid file " + file.getAbsolutePath());
            }
        }

        String inputMessageName = "Select dir: %s , Back: %s , Select: %s , Back main menu: %s"
                .formatted(inputPath != null ? String.format("[%s]", inputPath) : "", KEY_CD_PATH, KEY_SELECT_PATH, KEY_BACK_MAIN_MENU);
        MenuModel<String> inputMessage = MenuModel.inputMessage(inputMessageName, MenuTitleModel.createString("Enter path", path -> {

            if (path.equals(KEY_BACK_MAIN_MENU)) {
                mainMenu();
                return null;
            }

            File file;
            if (path.equals(KEY_CD_PATH) || path.equals(KEY_SELECT_PATH)) {
                if (inputPath == null) {
                    getInputPath(null, selectedPathResult);
                    return null;
                } else {
                    file = new File(inputPath);
                }
            } else {
                file = new File(inputPath, path);
            }
            File parent = file.getParentFile();

            if (!isValidDir(file)) {
                System.out.println("Invalid path");
                getInputPath(parent != null ? parent.getAbsolutePath() : null, selectedPathResult);
                return null;
            }

            if (path.equals(KEY_SELECT_PATH)) {
                selectedPathResult.apply(file.getAbsolutePath());
                return null;
            }

            if (path.equals(KEY_CD_PATH)) {
                if (parent != null) {
                    file = file.getParentFile();
                }
            }

            getInputPath(file.getAbsolutePath(), selectedPathResult);

            return null;
        }));

        MenuView.showMenu(inputMessage, false);
    }

    private boolean isValidDir(File dir) {
        return dir.exists() && !dir.isFile() && dir.isDirectory() && dir.canRead();
    }

    private void listMusic(int offset) {

        Future<List<MusicEntity>> fetchMusic = MusicService.repository().fetchMusic(LIMIT_FETCH_MUSIC, offset);
        Future<Integer> fetchTotalMusic = MusicService.repository().fetchTotalMusic();

        Future.join(fetchMusic, fetchTotalMusic).onSuccess(fetchMusicHandler -> {

            List<MusicEntity> musics = fetchMusic.result();
            int total = fetchTotalMusic.result();

            showResultListMusic(offset, LIMIT_FETCH_MUSIC, total, musics, newOffset -> {
                listMusic(newOffset);
                return null;
            }, unused -> {
                MusicService.repository().fetchMusic(total, 0).onSuccess(this::addToPlayList).onFailure(failedFetchMusics -> {
                    failedFetchMusics.printStackTrace(System.out);
                    searchMusic();
                });
                return null;
            });

        }).onFailure(failedFetchPlayList -> {
            failedFetchPlayList.printStackTrace(System.out);
            mainMenu();
        });
    }

    private void listFavoriteMusic(int offset) {

        Future<List<MusicEntity>> fetchMusic = MusicService.repository().fetchFavoriteMusic(LIMIT_FETCH_MUSIC, offset);
        Future<Integer> fetchTotalMusic = MusicService.repository().fetchTotalFavoriteMusic();

        Future.join(fetchMusic, fetchTotalMusic).onSuccess(fetchMusicHandler -> {

            List<MusicEntity> musics = fetchMusic.result();
            int total = fetchTotalMusic.result();

            showResultListMusic(offset, LIMIT_FETCH_MUSIC, total, musics, newOffset -> {
                listFavoriteMusic(newOffset);
                return null;
            }, unused -> {
                MusicService.repository().fetchFavoriteMusic(total, 0).onSuccess(this::addToPlayList).onFailure(failedFetchMusics -> {
                    failedFetchMusics.printStackTrace(System.out);
                    searchMusic();
                });
                return null;
            });

        }).onFailure(failedFetchPlayList -> {
            failedFetchPlayList.printStackTrace(System.out);
            mainMenu();
        });
    }

    private void playListMusics(int playListId, int offset) {

        Future<List<MusicEntity>> fetchMusic = MusicService.repository().fetchMusicByPlayList(playListId, LIMIT_FETCH_MUSIC, offset);
        Future<Integer> fetchTotalMusic = MusicService.repository().fetchTotalMusicByPlayList(playListId);

        Future.join(fetchMusic, fetchTotalMusic).onSuccess(fetchMusicHandler -> {

            List<MusicEntity> musics = fetchMusic.result();
            int total = fetchTotalMusic.result();

            showResultListMusic(offset, LIMIT_FETCH_MUSIC, total, musics, newOffset -> {
                playListMusics(playListId, newOffset);
                return null;
            }, null);

        }).onFailure(failedFetchPlayList -> {
            failedFetchPlayList.printStackTrace(System.out);
            mainMenu();
        });
    }

    private void showResultListMusic(int offset, int limit, int total, List<MusicEntity> musics, Function<Integer, Void> back, Function<Void, Void> addAllToPlayList) {
        System.out.printf("List music, Offset: %d , Total: %d\n", offset, total);
        List<MenuTitleModel<Void>> titles = new ArrayList<>();
        if (total > 0) {
            for (MusicEntity music : musics) {
                titles.add(MenuTitleModel.createVoid(FilenameUtils.getName(music.getPath()), unused -> {
                    selectMusic(music);
                    return null;
                }));
            }

            int newOffsetNext = Pagination.next(offset, limit, total);
            if (offset != newOffsetNext) {
                titles.add(MenuTitleModel.createVoid(" -->", unused -> {
                    back.apply(newOffsetNext);
                    return null;
                }));
            }

            if (offset > 0) {
                titles.add(MenuTitleModel.createVoid(" <--", unused -> {
                    back.apply(Pagination.pre(offset, limit, total));
                    return null;
                }));
            }
            if (addAllToPlayList != null) {
                titles.add(MenuTitleModel.createVoid("-- Add all to play list", unused -> {
                    addAllToPlayList.apply(null);
                    return null;
                }));
            }
        }
        titles.add(getMenuTitleBackToMenu());
        MenuView.showMenu(MenuModel.numberInput("Musics", titles));
    }

    private MenuTitleModel<Void> getMenuTitleBackToMenu() {
        return MenuTitleModel.createVoid(" Main Menu", unused -> {
            mainMenu();
            return null;
        });
    }

    private void playList() {
        PlayListService.repository().fetchPlayList().onSuccess(playLists -> {

            List<MenuTitleModel<Void>> titles = new ArrayList<>();
            for (PlayListEntity playList : playLists) {
                titles.add(MenuTitleModel.createVoid(playList.getName(), unused -> {
                    selectPlayList(playList);
                    return null;
                }));
            }
            titles.add(getMenuTitleBackToMenu());
            MenuView.showMenu(MenuModel.numberInput("Play list", titles));
        }).onFailure(failedFetchPlayList -> {
            failedFetchPlayList.printStackTrace(System.out);
            mainMenu();
        });
    }

    private void selectMusic(MusicEntity music) {
        List<MenuTitleModel<Void>> titles = new ArrayList<>();

        titles.add(MenuTitleModel.createVoid("Play", unused -> {
            playMusic(music, true, true, unused1 -> {
                selectMusic(music);
                return null;
            }, null);
            return null;
        }));
        titles.add(MenuTitleModel.createVoid((music.isFavorite() ? "Delete " : "Add ") + "Favorite", unused -> {
            MusicService.repository().setFavorite(music.getId(), !music.isFavorite())
                    .compose(v -> MusicService.repository().fetchMusicById(music.getId()))
                    .onSuccess(musicEntity -> {
                        System.out.println("Success delete music, Music: " + music.getPath());
                        selectMusic(musicEntity);
                    })
                    .onFailure(failedDelete -> {
                        failedDelete.printStackTrace(System.out);
                        selectMusic(music);
                    });
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Delete", unused -> {
            PlayListService.repository().deleteMusic(music.getId())
                    .compose(v -> MusicService.repository().deleteMusic(music.getId()))
                    .onSuccess(deletedMusic -> {
                        System.out.println("Success delete music, Music: " + music.getPath());
                        listMusic(0);
                    })
                    .onFailure(failedDelete -> {
                        failedDelete.printStackTrace(System.out);
                        selectMusic(music);
                    });

            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Add to play list", unused -> {
            addToPlayList(Collections.singletonList(music));
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Music List", unused -> {
            listMusic(0);
            return null;
        }));
        titles.add(getMenuTitleBackToMenu());
        MenuView.showMenu(MenuModel.numberInput("Music " + FilenameUtils.getName(music.getPath()), titles));
    }

    private void addToPlayList(List<MusicEntity> musics) {
        addToPlayListByMusicIds(musics.stream().map(MusicEntity::getId).toList());
    }

    private void addToPlayListByMusicIds(List<Integer> musics) {
        PlayListService.repository().fetchPlayList().onSuccess(playLists -> {

            List<MenuTitleModel<Void>> titles = new ArrayList<>();
            for (PlayListEntity playList : playLists) {
                titles.add(MenuTitleModel.createVoid(playList.getName(), unused -> {
                    addToPlayList(playList, musics);
                    return null;
                }));
            }
            titles.add(getMenuTitleBackToMenu());
            MenuView.showMenu(MenuModel.numberInput("Play list", titles));
        }).onFailure(failedFetchPlayList -> {
            failedFetchPlayList.printStackTrace(System.out);
            mainMenu();
        });
    }

    private void addToPlayList(PlayListEntity playList, List<Integer> musics) {
        PlayListService.repository().addMusic(playList.getId(), musics).onSuccess(unused -> {
            System.out.println("Success add music");
            selectPlayList(playList);
        }).onFailure(failedAddPlayList -> {
            failedAddPlayList.printStackTrace(System.out);
            mainMenu();
        });
    }

    private void createPlayList(Function<PlayListEntity, Void> result) {
        MenuTitleModel<String> playListName = MenuTitleModel.createString("Play list name, Main menu: %s".formatted(KEY_BACK_MAIN_MENU), name -> {

            if (name.equals(KEY_BACK_MAIN_MENU)) {
                mainMenu();
                return null;
            }

            PlayListService.repository().addPlayList(name).onSuccess(unused -> PlayListService.repository().fetchPlayList(name).onSuccess(playList -> {
                System.out.println("Success added play list, Name: " + name);
                result.apply(playList);
            }).onFailure(failedFetchPlayList -> {
                failedFetchPlayList.printStackTrace(System.out);
                mainMenu();
            })).onFailure(failedAddPlayList -> {
                if (failedAddPlayList instanceof UniqueException) {
                    System.out.println(failedAddPlayList.getMessage());
                    createPlayList(result);
                } else {
                    failedAddPlayList.printStackTrace(System.out);
                    mainMenu();
                }
            });

            return null;
        });
        MenuView.showMenu(MenuModel.inputMessage("Create play list", playListName));
    }

    private void selectPlayList(PlayListEntity playList) {
        List<MenuTitleModel<Void>> titles = new ArrayList<>();

        titles.add(MenuTitleModel.createVoid("Play", unused -> {
            playPlayList(playList, 0, false);
            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Delete", unused -> {
            PlayListService.repository().deleteMusicPlayList(playList.getId())
                    .compose(v -> PlayListService.repository().deletePlayList(playList.getId()))
                    .onSuccess(deletedMusic -> {
                        System.out.println("Success delete music, PlayList: " + playList.getName());
                        playList();
                    })
                    .onFailure(failedDelete -> {
                        failedDelete.printStackTrace(System.out);
                        selectPlayList(playList);
                    });

            return null;
        }));
        titles.add(MenuTitleModel.createVoid("Music List", unused -> {
            playListMusics(playList.getId(), 0);
            return null;
        }));
        titles.add(getMenuTitleBackToMenu());
        MenuView.showMenu(MenuModel.numberInput("Play list " + FilenameUtils.getName(playList.getName()), titles));
    }

    private void playLastMusic() {
        MusicService.repository().fetchLastPlayMusic().onSuccess(lastPlayMusic -> playMusic(lastPlayMusic, true, true, unused -> {
            mainMenu();
            return null;
        }, null)).onFailure(failedFetchLastPlay -> {
            failedFetchLastPlay.printStackTrace(System.out);
            mainMenu();
        });
    }

    private void playPlayList(PlayListEntity playList, int offset, boolean pre) {
        Future<List<MusicEntity>> fetchMusicByPlayList = MusicService.repository().fetchMusicByPlayList(playList.getId(), LIMIT_FETCH_MUSIC, 0);
        Future<Integer> fetchTotalMusicByPlayList = MusicService.repository().fetchTotalMusicByPlayList(playList.getId());

        Future.join(fetchTotalMusicByPlayList, fetchMusicByPlayList).onSuccess(successFetch -> {

            List<MusicEntity> musics = fetchMusicByPlayList.result();
            int total = fetchTotalMusicByPlayList.result();
            if (total == 0) {
                mainMenu();
                return;
            }
            if (musics == null || musics.isEmpty()) {
                selectPlayList(playList);
                return;
            }
            fillPlayMusicIndex(musics.size());
            doPlayPlayList(playList, fetchMusicByPlayList.result(), pre ? musics.size() - 1 : 0, LIMIT_FETCH_MUSIC, offset, fetchTotalMusicByPlayList.result(), pre);
        }).onFailure(failed -> {
            failed.printStackTrace(System.out);
            mainMenu();
        });

    }

    private void fillPlayMusicIndex(int size) {
        playingMusicIndex.clear();
        IntStream.range(0, size - 1).forEach(playingMusicIndex::add);
        Collections.shuffle(playingMusicIndex);
    }

    private void doPlayPlayList(PlayListEntity playList, List<MusicEntity> musics, int index, int limit, int offset, int total, boolean pre) {
        if (index < 0 || index >= musics.size()) {
            if (shuffle && pre) {
                index = 0;
            } else {
                if (index < 0) {
                    playPlayList(playList, Pagination.pre(offset, limit, total), true);
                } else {
                    int newOffset = Pagination.next(offset, limit, total);
                    playPlayList(playList, newOffset == offset ? 0 : newOffset, false);
                }
                return;
            }
        }

        if (!pre && shuffle && playingMusicIndex.isEmpty()) {
            int newOffset = Pagination.next(offset, limit, total);
            playPlayList(playList, newOffset == offset ? 0 : newOffset, false);
            return;
        }

        int newIndex = shuffle && !pre ? newPlayListIndex(index) : index;
        playMusic(musics.get(newIndex), false, true, unused -> {
            doPlayPlayList(playList, musics, newIndex + 1, limit, offset, total, false);
            return null;
        }, unused -> {
            doPlayPlayList(playList, musics, newIndex - 1, limit, offset, total, true);
            return null;
        });

    }

    private int newPlayListIndex(int index) {
        if (shuffle) {
            System.out.println("playingMusicIndex = " + playingMusicIndex);
            Collections.shuffle(playingMusicIndex);
            return playingMusicIndex.remove(random.nextInt(playingMusicIndex.size()));
        }
        return index;
    }

    private void playMusic(MusicEntity music, boolean start, boolean quickStart, Function<Void, Void> next, Function<Void, Void> pre) {
        nextMusic = next;
        preMusic = pre;
        playedMusic = music;

        MusicService.repository().setLatPlay(music.getId());

        if (quickStart) {
            player.play(playedMusic.getPath());
        }

        if (MenuView.isReadingLine()) {
            return;
        }

        List<MenuTitleModel<Void>> menuPlayMusicTitles = new ArrayList<>();

        if (start && !quickStart) {
            menuPlayMusicTitles.add(MenuTitleModel.createVoid("Start", unused -> {
                player.play(playedMusic.getPath());
                playMusic(playedMusic, false, false, nextMusic, preMusic);
                return null;
            }));
        } else {
            menuPlayMusicTitles.add(MenuTitleModel.createVoid(player.isPause() ? "Resume" : "Pause", unused -> {
                player.pause();
                playMusic(playedMusic, false, false, nextMusic, preMusic);
                return null;
            }));
        }
        menuPlayMusicTitles.add(MenuTitleModel.createVoid("Stop", unused -> {
            player.stop();
            playMusic(playedMusic, true, false, nextMusic, preMusic);
            return null;
        }));
        menuPlayMusicTitles.add(MenuTitleModel.createVoid(player.isRepeat() ? "UnRepeat" : "Repeat", unused -> {
            player.setRepeat();
            playMusic(playedMusic, false, false, nextMusic, preMusic);
            return null;
        }));
        menuPlayMusicTitles.add(MenuTitleModel.createVoid("Show info", unused -> {
            playMusic(playedMusic, false, false, nextMusic, preMusic);
            return null;
        }));
        menuPlayMusicTitles.add(MenuTitleModel.createVoid("Process", unused -> {
            if (player.isPlay()) {
                showPlayMusicTime = true;
                MenuView.readLine();
                showPlayMusicTime = false;
            }
            playMusic(playedMusic, false, false, nextMusic, preMusic);
            return null;
        }));
        menuPlayMusicTitles.add(MenuTitleModel.createVoid("Shuffle", unused -> {
            shuffle = !shuffle;
            playMusic(playedMusic, false, false, nextMusic, preMusic);
            return null;
        }));
        if (nextMusic != null) {
            menuPlayMusicTitles.add(MenuTitleModel.createVoid("Next", unused -> {
                nextMusic.apply(null);
                return null;
            }));
        }
        if (preMusic != null) {
            menuPlayMusicTitles.add(MenuTitleModel.createVoid("Pre", unused -> {
                preMusic.apply(null);
                return null;
            }));
        }
        menuPlayMusicTitles.add(getMenuTitleBackToMenu());

        MenuView.showMenu(MenuModel.numberInput("Music " + FilenameUtils.getName(music.getPath()), menuPlayMusicTitles));
    }

}
