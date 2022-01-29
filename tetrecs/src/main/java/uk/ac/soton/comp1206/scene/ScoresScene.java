package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.FileOperation;
import uk.ac.soton.comp1206.Utility.MultiMedia;
import uk.ac.soton.comp1206.component.ScoreList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Royce_Lyu
 */
public class ScoresScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    /**
     * Last played game. (To receive a new score)
     */
    Game game;

    /**
     * List of local score to bind with score list
     */
    private SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty();

    /**
     * List of remote score to bind with remote score list
     */
    private SimpleListProperty<Pair<String, Integer>> remoteScores = new SimpleListProperty();

    /**
     * Observable list of local score
     */
    private ObservableList<Pair<String, Integer>> scoreList;

    /**
     * Observable list of remote score
     */
    private ObservableList<Pair<String, Integer>> remoteScoreList;

    /**
     * Temp Arraylist to hold remote score list
     */
    private ArrayList<Pair<String, Integer>> remoteScoresTMP = new ArrayList();

    /**
     *
     */
    private StringProperty myName = new SimpleStringProperty("Name");

    /**
     * BorderPane to hold UI
     */
    private BorderPane mainPane;

    /**
     * Communicator to receive remote score list from the server
     */
    private Communicator communicator;

    /**
     * Timer to count down to return to menu
     */
    Timer timer;

    /**
     * control the refresh of scorelist
     */
    private boolean needRefresh = true;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        logger.info("Creating Score Scene");
        this.communicator = gameWindow.getCommunicator();
        this.game = game;
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
        MultiMedia.playAudio("explode.wav");
        MultiMedia.playBackgroundMusic("score.mp3", false);
        communicator.addListener((message) -> {
            Platform.runLater(() -> {
                loadOnlineScores(message.trim());
            });
        });

        //Set the timer when counting down to 0, the scene will return to menu
        clearTimer();
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    logger.info("Timer runs out");
                    ScoresScene.this.gameWindow.startMenu();
                });
            }
        };

        timer.schedule(timerTask,15000);

        communicator.send("HISCORES");
        //Press any key to return to menu
        scene.setOnKeyPressed(keyEvent -> {
            clearTimer();
            gameWindow.startMenu();
        });
    }

    /**
     * Load online score to generate remote score list
     * @param message received message from the server
     */
    private void loadOnlineScores(String message) {
        logger.info("Receive message: {}", message);
        remoteScoresTMP.clear();
        String[] components = message.split(" ", 2);
        String source = "null";
        if (components[0].equals("HISCORES")) {
            if (components.length > 1) {
                source = components[1];
            }
        }

        logger.info("Processing sources");
        String[] lines = source.split("\n");
        for (String line : lines) {
            String[] parts = line.split(":", 2);
            String name = parts[0];
            logger.info("Add new remote Pairs {}: {}", name, parts[1]);
            remoteScoresTMP.add(new Pair(parts[0], Integer.parseInt(parts[1])));
        }

        remoteScoresTMP.sort((a, b) -> {
            return (b.getValue()).compareTo(a.getValue());
        });

        remoteScoreList.clear();
        remoteScoreList.addAll(remoteScoresTMP);
        updateScore();
    }


    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        StackPane stackPane = new StackPane();
        stackPane.setMaxWidth(gameWindow.getWidth());
        stackPane.setMaxHeight(gameWindow.getHeight());
        stackPane.getStyleClass().add("menu-background");
        root.getChildren().add(stackPane);

        mainPane = new BorderPane();

        //UI of Big title
        Text gameOver = new Text("Game Over");
        gameOver.getStyleClass().add("menuTitle");
        BorderPane.setAlignment(gameOver, Pos.CENTER);
        mainPane.setTop(gameOver);

        //UI of score titles
        Text thisGame = new Text("This Game");
        Text onlineScore = new Text("Online Score");
        thisGame.getStyleClass().add("scoretitle");
        onlineScore.getStyleClass().add("scoretitle");

        //UI to hold local score list
        var localList = new ScoreList();
        ObservableList<Pair<String, Integer>> wrapper;
        scoreList = FXCollections.observableArrayList(FileOperation.loadScores());
        if (game.getScores().isEmpty()) {
            localScores.set(scoreList);
        } else {
            wrapper = FXCollections.observableArrayList(game.getScores());
            localScores.set(wrapper);
        }
        localList.scoresProperty().bind(localScores);
        localList.myNameProperty().bind(myName);

        //UI to hold remote score list
        var remoteList = new ScoreList();
        remoteScoreList = FXCollections.observableArrayList(remoteScoresTMP);
        remoteScores.set(remoteScoreList);
        remoteList.scoresProperty().bind(remoteScores);
        remoteList.myNameProperty().bind(myName);

        var scoreListField = new GridPane();
        scoreListField.setAlignment(Pos.CENTER);
        scoreListField.setHgap(30);
        scoreListField.add(thisGame,0,0);
        scoreListField.add(onlineScore,1,0);
        scoreListField.add(localList,0,1);
        scoreListField.add(remoteList,1,1);

        mainPane.setCenter(scoreListField);
        stackPane.getChildren().add(mainPane);
    }

    /**
     * Update the score list file after playing a game.
     */
    public void updateScore() {
        logger.info("Writing new score list");
        int currentLocalGameScore = game.getScoreProperty().get();
        if(needRefresh) {
            ArrayList<Pair<String, Integer>> temp = new ArrayList();
            temp.addAll(scoreList);
            for (Pair<String, Integer> i : scoreList) {
                if(currentLocalGameScore > i.getValue()) {
                    temp.add(new Pair("Local Gamer", currentLocalGameScore));
                    break;
                }
            }
            temp.sort((a, b) -> {
                return (b.getValue()).compareTo(a.getValue());
            });;

            scoreList.clear();
            scoreList.addAll(temp);
            FileOperation.writeScores(scoreList);
        }

        int remoteCounter = 0;
        for (Pair<String, Integer> i : remoteScoreList) {
            if (currentLocalGameScore > i.getValue()) {
                logger.info("You beat scores in remote list");
                TextField name = new TextField();
                name.setPromptText("Enter your name");
                name.setPrefWidth(gameWindow.getWidth()/2);
                name.requestFocus();
                Button button = new Button("Submit");
                button.setDefaultButton(true);
                var submitField = new VBox();
                submitField.getChildren().add(name);
                submitField.getChildren().add(button);
                mainPane.setBottom(submitField);
                button.setOnAction((e) -> {
                    needRefresh = false;
                    String myName = name.getText().replace(":", "");
                    this.myName.set(myName);
                    submitField.getChildren().removeAll();
                    communicator.send("HISCORE " + myName + ":" + currentLocalGameScore);
                    communicator.send("HISCORES");
                    MultiMedia.playAudio("pling.wav");
                });
            }
            remoteCounter++;
        }
    }

    /**
     * Clear the timer
     */
    public void clearTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
}
