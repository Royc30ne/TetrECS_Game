package uk.ac.soton.comp1206.Utility;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.MenuScene;

/**
 * @author Royce_Lyu
 * @date 2021-03-23 16:27
 */
public class MultiMedia {

    private static final Logger logger = LogManager.getLogger(Media.class);

    /**
     * Media player to play audio file
     */
    private static MediaPlayer audioPlayer;

    /**
     * Media player to play background file
     */
    private static MediaPlayer backgroundMusicPlayer;

    /**
     * Switch to enable or disable music
     */
    public static BooleanProperty enableMusic = new SimpleBooleanProperty(true);

    public static BooleanProperty mutProperty = new SimpleBooleanProperty(true);
    /**
     *Playing appointed background music file
     * @param musicTitle The appoint background music file's name
     * @param loop parameter to set looping
     */
    public static void playBackgroundMusic(String musicTitle, boolean loop) {

        if (!enableMusic.get()) return;

        if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop();

        try {
            String music = MultiMedia.class.getResource("/music/" + musicTitle).toExternalForm();
            Media play = new Media(music);

            backgroundMusicPlayer = new MediaPlayer(play);
            backgroundMusicPlayer.muteProperty().bind(mutProperty);
            backgroundMusicPlayer.setVolume(0.5D);
            backgroundMusicPlayer.play();
            if(loop) {
                backgroundMusicPlayer.setCycleCount(-1);
            } } catch (Exception e) {
            enableMusic.set(false);
            e.printStackTrace();
            logger.info("Unable to play audio file");
        }
    }

    /**
     * Playing appointed music file
     * @param title The appointed music file's name
     */
    public static void playAudio(String title) {
        if(enableMusic.get()) {
            String audioPath = MultiMedia.class.getResource("/sounds/" + title).toExternalForm();
            logger.info("Playing file: {}", audioPath);

            try{
                Media play = new Media(audioPath);
                audioPlayer = new MediaPlayer(play);
                audioPlayer.muteProperty().bind(mutProperty);
                audioPlayer.play();
            } catch (Exception e) {
                enableMusic.set(false);
                e.printStackTrace();
                logger.error("Unable to play audio. ~~~ Audio is disabled");
            }
        }
    }

    /**
     * Stop playing music
     */
    public static void stopMusic() {
        if (audioPlayer != null) {
            audioPlayer.stop();
        }

        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }
}
