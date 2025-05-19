package com.bardiademon.music.terminal.controller;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.Meta;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;

public class PlayerController implements MediaPlayerEventListener {

    private EmbeddedMediaPlayer mediaPlayer;

    private PlayerListener playerListener;

    private boolean isPlay;
    private boolean isPause = false;
    private boolean isFinished = false;

    public PlayerController() {
    }

    private void initial() {

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.events().addMediaPlayerEventListener(this);

        isPlay = false;
        isPause = false;
        isFinished = false;
    }

    public void setPlayerListener(PlayerListener playerListener) {
        this.playerListener = playerListener;
    }

    public void play(String path) {
        initial();
        mediaPlayer.media().play(new File(path).toPath().toUri().toString());
        isPlay = true;
        isPause = false;
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
                            🎵 [Title] -------- %s
                            👤 [Artist] ------- %s
                            💿 [Album] -------- %s
                            🎼 [Genre] -------- %s
                            📅 [Date] --------- %s
                            📝 [Description] -- %s
                            ~*~*~*~*~*~*~*~*~*~*~*~*~""",
                    safeFormat(title), safeFormat(artist), safeFormat(album), safeFormat(genre), safeFormat(date), safeFormat(description));
        } catch (Exception e) {
            return "🎵 Title:\n👤 Artist:\n💿 Album:\n🎼 Genre:\n📅 Data:\n📝 Description:";
        }
    }

    private String safeFormat(String value) {
        return value == null ? "" : value;
    }

    public String generateSeek(int value, int percent, int seekTotal, String filledChar, String lastFill, String emptyChar, String color) {
        if (mediaPlayer == null) {
            return "";
        }
        int filledLength = Math.round((value / ((float) percent)) * seekTotal);
        int emptyLength = Math.max((seekTotal - (filledLength + lastFill.length())), 0);
        String filled = (color == null || color.isEmpty() ? "" : color) + filledChar.repeat(filledLength) + (color == null || color.isEmpty() ? "" : "\u001B[0m");
        filled += lastFill;
        String empty = emptyChar.repeat(emptyLength);
        return filled + empty;
    }

    public String generateVolumeSeek() {
        if (mediaPlayer == null) {
            return "";
        }
        return generateSeek(getVolume(), 200, 8, "━", "⬤", "─", "\u001B[38;2;0;255;0m");
    }

    public String generatePositionSeek() {
        return generateSeek(getPosition(), 100, 30, "━", "⬤", "─", "\u001B[38;2;0;122;204m");
    }

    public void printImage() {
        if (mediaPlayer == null) return;

        try {
            String path = mediaPlayer.media().meta().get(Meta.ARTWORK_URL);
            BufferedImage original = ImageIO.read(Path.of(URI.create(path)).toFile());

            int targetWidth = 50;
            int targetHeight = 50;

            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < targetHeight; y++) {
                for (int x = 0; x < targetWidth; x++) {
                    int srcX = x * original.getWidth() / targetWidth;
                    int srcY = y * original.getHeight() / targetHeight;
                    resized.setRGB(x, y, original.getRGB(srcX, srcY));
                }
            }

            for (int y = 0; y < targetHeight; y += 2) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < targetWidth; x++) {
                    int rgbTop = resized.getRGB(x, y);
                    int rgbBottom = (y + 1 < targetHeight) ? resized.getRGB(x, y + 1) : rgbTop;

                    int r1 = (rgbTop >> 16) & 0xFF;
                    int g1 = (rgbTop >> 8) & 0xFF;
                    int b1 = rgbTop & 0xFF;

                    int r2 = (rgbBottom >> 16) & 0xFF;
                    int g2 = (rgbBottom >> 8) & 0xFF;
                    int b2 = rgbBottom & 0xFF;
                    row.append(String.format("\u001B[38;2;%d;%d;%dm\u001B[48;2;%d;%d;%dm▀",
                            r1, g1, b1, r2, g2, b2));
                }
                row.append("\u001B[0m");
                System.out.println(row);
            }

        } catch (Exception ignored) {
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public boolean isMute() {
        return mediaPlayer != null && mediaPlayer.audio() != null && mediaPlayer.audio().isMute();
    }

    public int getVolume() {
        if (mediaPlayer != null && mediaPlayer.audio() != null) {
            return mediaPlayer.audio().volume();
        }
        return -1;
    }

    public void setPosition(int position) {
        if (position < 0 || position > 100) {
            return;
        }
        if (mediaPlayer != null && mediaPlayer.controls() != null) {
            mediaPlayer.controls().setPosition(position / 100F);
        }
    }

    public void setVolume(int volume) {
        if (mediaPlayer != null && mediaPlayer.audio() != null) {
            mediaPlayer.audio().setVolume(volume);
        }
    }

    public void setMute() {
        if (mediaPlayer != null && mediaPlayer.audio() != null) {
            mediaPlayer.audio().setMute(!mediaPlayer.audio().isMute());
        }
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
        if (mediaPlayer != null && mediaPlayer.status().isPlaying()) {
            isPlay = false;
            isPause = false;
            mediaPlayer.controls().stop();
        }
    }

    public long getTime() {
        return mediaPlayer.status().time();
    }

    public long getLength() {
        return mediaPlayer.status().length();
    }

    public int getPosition() {
        int round = Math.round(mediaPlayer.status().position() * 100);
        if (round < 0) {
            return 0;
        }
        return Math.min(round, 100);
    }

    public void setRepeat() {
        if (mediaPlayer != null && mediaPlayer.controls() != null) {
            mediaPlayer.controls().setRepeat(!mediaPlayer.controls().getRepeat());
        }
    }

    public boolean isRepeat() {
        return mediaPlayer != null && mediaPlayer.controls() != null && mediaPlayer.controls().getRepeat();
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
