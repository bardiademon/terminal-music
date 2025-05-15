package com.bardiademon.music.terminal.data.repository;

import com.bardiademon.music.terminal.data.entity.MusicEntity;
import io.vertx.core.Future;

import java.util.List;

public interface MusicRepository {

    Future<Void> addMusic(String path);

    Future<List<Integer>> addMusic(List<String> path);

    Future<List<Integer>> fetchMusicIdsPaths(List<String> musicId);

    Future<Void> setFavorite(int musicId, boolean favorite);

    Future<Void> setLatPlay(int musicId);

    Future<List<MusicEntity>> fetchAllMusic();

    Future<MusicEntity> fetchMusicById(int musicId);

    Future<MusicEntity> fetchLastPlayMusic();

    Future<List<MusicEntity>> searchMusic(String path, int limit, int offset);

    Future<Integer> totalSearchMusic(String path);

    Future<List<MusicEntity>> fetchMusicByPlayList(int playListId, int limit, int offset);

    Future<Integer> fetchTotalMusicByPlayList(int playListId);

    Future<List<MusicEntity>> fetchMusic(int limit, int offset);

    Future<Integer> fetchTotalMusic();

    Future<List<MusicEntity>> fetchFavoriteMusic(int limit, int offset);

    Future<Integer> fetchTotalFavoriteMusic();

    Future<Void> deleteMusic(int musicId);
}
