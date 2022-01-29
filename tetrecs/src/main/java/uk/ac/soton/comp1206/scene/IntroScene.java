package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.MultiMedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * @author Royce_Lyu
 * @date 2021-04-11 18:15
 */
public class IntroScene extends BaseScene{
    Logger logger = LogManager.getLogger(IntroScene.class);
    private SequentialTransition sequence;
    private SequentialTransition sequence2;
    /**
     * Media player
     */
    private MediaPlayer introPlayer;
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public IntroScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {

        String music = MultiMedia.class.getResource("/sounds/intro.mp3").toExternalForm();
        Media play = new Media(music);

        introPlayer = new MediaPlayer(play);
        introPlayer.setVolume(0.5D);
        introPlayer.play();
        scene.setOnKeyPressed(keyEvent -> {
            skipIntro();
        });
        scene.setOnMouseClicked(mouseEvent -> {
            skipIntro();
        });
    }

    /**
     * Skip the intro animation
     */
    public void skipIntro() {
        introPlayer.stop();
        if (sequence != null) {
            sequence.stop();
        }
        if (sequence2 != null) {
            sequence2.stop();
        }
        gameWindow.startMenu();
    }

    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building " + getClass());
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        StackPane introPane = new StackPane();
        introPane.setMaxHeight(gameWindow.getHeight());
        introPane.setMaxWidth(gameWindow.getWidth());
        introPane.getStyleClass().add("intro");
        root.getChildren().add(introPane);

        String imagePath = getClass().getResource("/images/ECSGames.png").toExternalForm();
        Image ecsgameLogo = new Image(imagePath);
        ImageView logo = new ImageView(ecsgameLogo);
        logo.setPreserveRatio(true);
        logo.setOpacity(0);
        logo.setFitWidth(gameWindow.getWidth()/2.5);
        introPane.getChildren().add(logo);
        FadeTransition fadeIn = new FadeTransition(new Duration(2000), logo);
        fadeIn.setToValue(1);
        PauseTransition pause = new PauseTransition(new Duration(1500));
        FadeTransition fadeOut = new FadeTransition(new Duration(500), logo);
        fadeOut.setToValue(0);
        sequence = new SequentialTransition(new Animation[]{fadeIn, pause, fadeOut});
        sequence.play();
        sequence.setOnFinished((e) -> {
            introPane.getChildren().clear();
            String imagePath2 = getClass().getResource("/images/mylogo.png").toExternalForm();
            Image mylogo = new Image(imagePath2);
            ImageView logo2 = new ImageView(mylogo);
            logo2.setOpacity(0);
            introPane.getChildren().add(logo2);
            FadeTransition fadeIn2 = new FadeTransition(new Duration(2000), logo2);
            fadeIn2.setToValue(1);
            PauseTransition pause2 = new PauseTransition(new Duration(1500));
            FadeTransition fadeOut2 = new FadeTransition(new Duration(500), logo2);
            fadeOut2.setToValue(0);
            sequence2 = new SequentialTransition(new Animation[]{fadeIn2, pause2, fadeOut2});
            sequence2.play();
            sequence2.setOnFinished((e2) -> {
                gameWindow.startMenu();
            });
        });
    }
}
