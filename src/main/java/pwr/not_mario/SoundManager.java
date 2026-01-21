package pwr.not_mario;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {

    private static MediaPlayer musicPlayer;
    private static boolean isMusicEnabled = true;
    private static String currentTrackName = "";

    // --- EFEKTY -------------------------------------------------------------------------------------------------
    public static void playSound(String soundName) {
        try {
            String path = "/assets/sounds/" + soundName + ".wav";
            URL url = SoundManager.class.getResource(path);
            if (url != null) {
                AudioClip clip = new AudioClip(url.toExternalForm());
                clip.setVolume(0.2);
                clip.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- MUZYKA ----------------------------------------------------------------------------------------------
    public static void playMusic(String musicName) {
        if (currentTrackName.equals(musicName)) {
            return;
        }

        try {
            if (musicPlayer != null) {
                musicPlayer.stop();
            }

            String path = "/assets/sounds/" + musicName + ".mp3";
            URL url = SoundManager.class.getResource(path);

            if (url != null) {
                Media media = new Media(url.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                musicPlayer.setVolume(0.1);

                currentTrackName = musicName;

                if (isMusicEnabled) {
                    musicPlayer.play();
                }
            } else {
                System.out.println("Brak pliku muzyki: " + musicName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-----------------Opcje-------------------------------------------------------------------------
    public static void setMusicEnabled(boolean enabled) {
        isMusicEnabled = enabled;
        if (musicPlayer == null) return;

        if (enabled) {
            musicPlayer.play();
        } else {
            musicPlayer.pause();
        }
    }
    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        currentTrackName = "";
    }

    public static void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    public static void resumeMusic() {
        if (musicPlayer != null && isMusicEnabled) {
            musicPlayer.play();
        }
    }
}