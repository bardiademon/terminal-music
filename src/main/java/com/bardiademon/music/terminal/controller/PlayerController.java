package com.bardiademon.music.terminal.controller;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class PlayerController implements MediaPlayerEventListener {

    private EmbeddedMediaPlayer mediaPlayer;

    private PlayerListener playerListener;

    private boolean isPlay;
    private boolean isPause = false;
    private boolean isStop = false;
    private boolean isFinished = false;

    public PlayerController() {
    }

    private void initial() {
        if (mediaPlayer != null && !isFinished) {
            mediaPlayer.controls().stop();
        }
        mediaPlayer = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.events().addMediaPlayerEventListener(this);

        isPlay = false;
        isPause = false;
        isStop = false;
        isFinished = false;
    }

    public void setPlayerListener(PlayerListener playerListener) {
        this.playerListener = playerListener;
    }

    public void play(String path) {
        initial();
        mediaPlayer.media().start(path);
        isPlay = true;
        isPause = false;
        isStop = false;
        isFinished = false;
    }

    public void pause() {
        isPlay = !isPause;
        isPause = !isPause;
        mediaPlayer.controls().pause();
    }

    public void stop() {
        isPlay = false;
        isPause = false;
        mediaPlayer.controls().stop();
    }

    public long getTime() {
        return mediaPlayer.status().time();
    }

    public long getLength() {
        return mediaPlayer.status().length();
    }

    public int getProgress() {
        return (int) (mediaPlayer.status().position() * 100);
    }

    public void setRepeat() {
        mediaPlayer.controls().setRepeat(!mediaPlayer.controls().getRepeat());
    }

    public boolean isRepeat() {
        return mediaPlayer.controls().getRepeat();
    }

    public boolean isPlay() {
        return isPlay;
    }

    public boolean isPause() {
        return isPause;
    }

    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void mediaChanged(MediaPlayer mediaPlayer, MediaRef mediaRef) {

    }

    @Override
    public void opening(MediaPlayer mediaPlayer) {

    }

    @Override
    public void buffering(MediaPlayer mediaPlayer, float v) {

    }

    @Override
    public void playing(MediaPlayer mediaPlayer) {

    }

    @Override
    public void paused(MediaPlayer mediaPlayer) {
        isPause = true;
        isPlay = false;
    }

    @Override
    public void stopped(MediaPlayer mediaPlayer) {
        isPause = false;
        isStop = true;
        isPlay = false;
    }

    @Override
    public void forward(MediaPlayer mediaPlayer) {

    }

    @Override
    public void backward(MediaPlayer mediaPlayer) {

    }

    @Override
    public void finished(MediaPlayer mediaPlayer) {
        isFinished = true;
        isPause = false;
        isPlay = false;
        playerListener.onFinished();
    }

    @Override
    public void timeChanged(MediaPlayer mediaPlayer, long time) {
        playerListener.onTime(time);
    }

    @Override
    public void positionChanged(MediaPlayer mediaPlayer, float v) {

    }

    @Override
    public void seekableChanged(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void pausableChanged(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void titleChanged(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void snapshotTaken(MediaPlayer mediaPlayer, String s) {

    }

    @Override
    public void lengthChanged(MediaPlayer mediaPlayer, long l) {

    }

    @Override
    public void videoOutput(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void scrambledChanged(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType trackType, int i) {

    }

    @Override
    public void elementaryStreamDeleted(MediaPlayer mediaPlayer, TrackType trackType, int i) {

    }

    @Override
    public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType trackType, int i) {

    }

    @Override
    public void corked(MediaPlayer mediaPlayer, boolean b) {

    }

    @Override
    public void muted(MediaPlayer mediaPlayer, boolean b) {

    }

    @Override
    public void volumeChanged(MediaPlayer mediaPlayer, float v) {

    }

    @Override
    public void audioDeviceChanged(MediaPlayer mediaPlayer, String s) {

    }

    @Override
    public void chapterChanged(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void error(MediaPlayer mediaPlayer) {

    }

    @Override
    public void mediaPlayerReady(MediaPlayer mediaPlayer) {

    }
}
