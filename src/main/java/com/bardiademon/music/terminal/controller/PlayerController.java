package com.bardiademon.music.terminal.controller;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.Meta;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;

public class PlayerController implements MediaPlayerEventListener {

    private MediaPlayer mediaPlayer;

    private PlayerListener playerListener;

    private boolean isPlay;
    private boolean isPause = false;
    private boolean isStop = false;
    private boolean isFinished = false;

    private static final int PROGRESS_TOTAL_LEN = 20;

    public PlayerController() {
    }

    private void initial() {
        if (mediaPlayer != null && !isFinished) {
            mediaPlayer.controls().stop();
        }
        mediaPlayer = new MediaPlayerFactory().mediaPlayers().newMediaPlayer();
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
        mediaPlayer.media().start(new File(path).toPath().toUri().toString());
        isPlay = true;
        isPause = false;
        isStop = false;
        isFinished = false;
    }

    public String getMeta() {
        try {
            if (mediaPlayer == null) {
                throw new NullPointerException();
            }
            String title = mediaPlayer.media().meta().get(Meta.TITLE);
            String artist = mediaPlayer.media().meta().get(Meta.ARTIST);
            String album = mediaPlayer.media().meta().get(Meta.ALBUM);
            String genre = mediaPlayer.media().meta().get(Meta.GENRE);
            String date = mediaPlayer.media().meta().get(Meta.DATE);
            String description = mediaPlayer.media().meta().get(Meta.DESCRIPTION);
            return String.format("""
                            ~*~*~*~*~*~*~*~*~*~*~*~*~
                            [Title] -------  %s
                            [Artist] ------  %s
                            [Album] -------  %s
                            [Genre] -------  %s
                            [Date] --------  %s
                            [Description] -- %s
                            ~*~*~*~*~*~*~*~*~*~*~*~*~""",
                    safeFormat(title), safeFormat(artist), safeFormat(album), safeFormat(genre), safeFormat(date), safeFormat(description));
        } catch (Exception e) {
            return "Title:\nArtist:\nAlbum:\nGenre:\nData:\nDescription:";
        }
    }

    private String safeFormat(String value) {
        return value == null ? "-" : value;
    }

    public String generateProgress() {
        if (mediaPlayer == null) {
            return "";
        }
        int filledLength = Math.round(mediaPlayer.status().position() * PROGRESS_TOTAL_LEN);
        int emptyLength = PROGRESS_TOTAL_LEN - filledLength;
        String filled = "▓".repeat(filledLength);
        String empty = "░".repeat(emptyLength);
        return filled + empty;
    }

    public void printImage() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            String path = mediaPlayer.media().meta().get(Meta.ARTWORK_URL);
            BufferedImage image = ImageIO.read(Path.of(URI.create(path)).toFile());
            int width = 80;
            int height = image.getHeight() * width / image.getWidth() / 2;  // حفظ نسبت ابعاد
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = resized.createGraphics();
            g.drawImage(scaledImage, 0, 0, null);
            g.dispose();
            String shades = "@#&$%*o!;:. ";
            for (int y = 0; y < height; y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    int color = resized.getRGB(x, y) & 0xFF;
                    int index = (color * (shades.length() - 1)) / 255;
                    row.append(shades.charAt(index));
                }
                System.out.println(row);
            }
        } catch (Exception ignored) {
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.status() != null && mediaPlayer.status().isPlaying();
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
