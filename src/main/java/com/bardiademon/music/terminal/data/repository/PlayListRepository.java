package com.bardiademon.music.terminal.data.repository;

import com.bardiademon.music.terminal.data.entity.PlayListEntity;
import io.vertx.core.Future;

import java.util.List;

public interface PlayListRepository {

    Future<Void> addPlayList(String name);

    Future<Void> addMusic(int playListId, int musicId);

    Future<Void> addMusic(int playListId, List<Integer> musicId);

    Future<List<PlayListEntity>> fetchPlayList();

    Future<PlayListEntity> fetchPlayList(String name);

    Future<Void> deletePlayList(int playListId);

    Future<Void> deleteMusicPlayList(int playListId, int musicId);

    Future<Void> deleteMusic(int musicId);

    Future<Void> deleteMusicPlayList(int playListId);
}
