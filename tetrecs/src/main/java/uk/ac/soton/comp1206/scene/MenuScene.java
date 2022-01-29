package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.Utility.FileOperation;
import uk.ac.soton.comp1206.Utility.MultiMedia;
import uk.ac.soton.comp1206.game.SerializePack;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.File;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    public static BooleanProperty muteProperty = new SimpleBooleanProperty(true);
    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        var title = new Text("  TETRECS");
        title.getStyleClass().add("menuTitle");
        mainPane.setTop(title);
        RotateTransition rotater = new RotateTransition(new Duration(3000.0D), title);
        rotater.setCycleCount(-1);
        rotater.setFromAngle(-5.0D);
        rotater.setToAngle(5.0D);
        rotater.setAutoReverse(true);
        rotater.play();
        BorderPane.setAlignment(title, Pos.CENTER_LEFT);
        BorderPane.setMargin(title, new Insets(40,0,0,0));

        //Creat a field to contain buttons
        var Vbox = new VBox();
        Vbox.setAlignment(Pos.CENTER_LEFT);
        Vbox.setSpacing(20);

        //Resume game
        var resumeButton = new Text("Resume Game");
        resumeButton.setVisible(false);
        File file = new File("game.ser");
        if (file.exists()) {
            resumeButton.setVisible(true);
        }
        applyMenuItem(resumeButton);
        Vbox.getChildren().add(resumeButton);

        //Buttons' UI
        var playButton = new Text("Local Game");
        applyMenuItem(playButton);
        Vbox.getChildren().add(playButton);

        var multiplayer = new Text("Multiplayer Game");
        applyMenuItem(multiplayer);
        Vbox.getChildren().add(multiplayer);

        var instructionsButton = new Text("Instructions");
        applyMenuItem(instructionsButton);
        Vbox.getChildren().add(instructionsButton);

        var muteAudio = new Text("Audio Enable: ");
        var status = new Text("");
        applyMenuItem(muteAudio);
        applyMenuItem(status);
        status.textProperty().bind(muteProperty.asString());
        HBox muteBox = new HBox();
        muteBox.getChildren().add(muteAudio);
        muteBox.getChildren().add(status);
        MultiMedia.mutProperty.bind(muteProperty.not());
        Vbox.getChildren().add(muteBox);

        var exitButton = new Text("exit");
        applyMenuItem(exitButton);
        Vbox.getChildren().add(exitButton);

        mainPane.setLeft(Vbox);
        Vbox.setPadding(new Insets(0,0,0,40));

       //Handle buttons' event
        resumeButton.setOnMouseClicked(mouseEvent -> {
            MultiMedia.playAudio("pling.wav");
            resumeGame(FileOperation.readGame());
        });
        resumeButton.setOnMouseEntered(mouseEvent -> {
            MultiMedia.playAudio("level.wav");
        });
        playButton.setOnMouseClicked(mouseEvent -> {
            MultiMedia.playAudio("pling.wav");
            startGame();
        });
        playButton.setOnMouseEntered(mouseEvent -> {
            MultiMedia.playAudio("level.wav");
        });
        muteAudio.setOnMouseEntered(mouseEvent -> {
            MultiMedia.playAudio("level.wav");
        });
        status.setOnMouseEntered(mouseEvent -> {
            MultiMedia.playAudio("level.wav");
        });
        muteBox.setOnMouseClicked(mouseEvent -> {
            muteProperty.set(!muteProperty.get());
        });

        instructionsButton.setOnMouseClicked(mouseEvent -> {
            MultiMedia.playAudio("pling.wav");
            startInstructions();
        });
        instructionsButton.setOnMouseEntered(mouseEvent -> {
            MultiMedia.playAudio("level.wav");
        });

        multiplayer.setOnMouseClicked(mouseEvent -> {
            MultiMedia.playAudio("pling.wav");
            startMultiplayerGame();
        });
        multiplayer.setOnMouseEntered(mouseEvent -> {
            MultiMedia.playAudio("level.wav");
        });

        exitButton.setOnMouseClicked(mouseEvent -> {
            MultiMedia.playAudio("pling.wav");
            App.getInstance().shutdown();
        });
        exitButton.setOnMouseEntered(mouseEvent -> {
            MultiMedia.playAudio("level.wav");
        });

    }


    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        MultiMedia.playBackgroundMusic("newMenu.mp3", true);
    }

    /**
     * Handle when the Start Game button is pressed
     */
    private void startGame() {
        gameWindow.startChallenge();
    }

    /**
     * Handle when the Resume Game button is pressed
     */
    private void resumeGame(SerializePack game) {
        gameWindow.resumeGame(game);
    }

    /**
     * Handle when the Instruction button is pressed
     */
    private void startInstructions() {
        gameWindow.startInstructions();
    }

    /**
     * Handle when the MultiplayerGame button is pressed
     */
    private void startMultiplayerGame() {
        gameWindow.startLobby();
    }

    /**
     * Apply the css to the text
     * @param appliedText text
     */
    public void applyMenuItem(Text appliedText) {
        appliedText.getStyleClass().add("menuItem");
        appliedText.setOnDragEntered(dragEvent -> {
            appliedText.getStyleClass().add("selected");
            MultiMedia.playAudio("rotate.wav");
        });
        appliedText.setOnDragExited(dragEvent -> {
            appliedText.getStyleClass().remove("selected");
        });
    }
}
