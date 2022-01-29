package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.MultiMedia;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;

/**
 * @author Royce_Lyu
 */
public class MultiplayerScene extends ChallengeScene {
    Logger logger = LogManager.getLogger(MultiplayerScene.class);

    /**
     * Communicator
     */
    private Communicator communicator;

    /**
     * Remote score array list
     */
    private ArrayList<Pair<String, Integer>> remoteScores = new ArrayList();

    /**
     * Remote score list
     */
    private ObservableList<Pair<String, Integer>> remoteScoreList;

    /**
     * Leader board
     */
    private Leaderboard leaderBoard;

    /**
     * Player's name
     */
    private StringProperty myName = new SimpleStringProperty("");

    /**
     * Chat mode switch
     */
    private BooleanProperty chatMode = new SimpleBooleanProperty(false);

    /**
     * Message Box
     */
    private VBox msgContent;

    /**
     * Send message area;
     */
    private TextField sendMsg;

    /**
     * Scroller Pane
     */
    private ScrollPane msgBox;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        this.communicator = gameWindow.getCommunicator();
    }

    @Override
    public void initialise() {
        logger.info("Initialising {}", getClass());
        MultiMedia.playBackgroundMusic("game_start.wav",false);
        MultiMedia.playBackgroundMusic("game.mp3", true);
        game.setCurrentPiece(this::nextPiece);
        scene.setOnKeyPressed(this::handleKeys);
        game.setOnLineCleared(this::clearLine);
        game.setOnGameLoop(this::gameLoop);
        game.getScoreProperty().addListener(this::getHiScore);
        game.start();
        communicator.send("NICK");
        communicator.send("SCORES");
        this.game.getScoreProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                communicator.send("SCORE " + newValue);
            }
        });
        this.game.getLivesProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > oldValue.intValue()) {
                    MultiMedia.playAudio("lifegain.wav");
                    communicator.send("LIVES " + newValue);
                } else {
                    MultiMedia.playAudio("lifelose.wav");
                    communicator.send("LIVES " + newValue);
                }
            }
        });
        this.game.getLevelProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > oldValue.intValue()) {
                    MultiMedia.playAudio("level.wav");
                }
            }
        });
        communicator.addListener((message) -> {
            Platform.runLater(() -> {
                handleMessage(message);
            });
        });
        game.setOnGameOver(() -> {
            game.stop();
            communicator.send("DIE");
            gameWindow.startScore(game);
        });
    }

    /**
     * Handle the incoming message
     * @param message incoming message
     */
    public void handleMessage(String message) {
        logger.info("Received message: {}", message);
        String[] components = message.split(" ",2);
        if (components[0].equals("SCORES") && components.length > 1) {
            handleScores(components[1]);
        } else if (components[0].equals("NICK") && components.length > 1) {
            if (!components[1].contains(":")) {
                setName(components[1]);
            }
        } else if (components[0].equals("MSG")) {
            handleMSG(components[1]);
        }
     }

    /**
     * Handle message
     * @param source
     */
    private void handleMSG(String source) {
        String[] components = source.split(":", 2);
        String name = components[0];
        String content = components[1];
        if (name.equals(myName.get())) {
            chatMode.set(false);
        }
        Text msgLine = new Text("<" + name + ">: " + content);
        msgContent.getChildren().add(msgLine);
        msgBox.setVvalue(1);
    }

    /**
     * Set the player's name
     * @param name player's name
     */
    private void setName(String name) {
        myName.set(name);
    }

    /**
     * Handle the score message
     * @param scoreMsg messages contian score info
     */
    private void handleScores(String scoreMsg) {
        remoteScores.clear();
        String[] scoreLines = scoreMsg.split("\n");
        for (String line : scoreLines) {
            String[] components = line.split(":");
            String name = components[0];
            int score = Integer.parseInt(components[1]);
            String lives = components[2];
            if (lives.equals("DEAD")) {
                leaderBoard.kill(name);
            }
            remoteScores.add(new Pair(name, score));
        }

        this.remoteScores.sort((a, b) -> {
            return (b.getValue()).compareTo(a.getValue());
        });
        game.getScores().clear();
        game.getScores().addAll(remoteScores);
        remoteScoreList.clear();
        remoteScoreList.addAll(remoteScores);
    }

    /**
     *
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);
        MultiMedia.playBackgroundMusic("game.mp3", true);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);


        //UI on the Top
        GridPane topArea = new GridPane();
        Text title = new Text("Multiplayer Game");
        title.getStyleClass().add("title");
        title.setTextAlignment(TextAlignment.CENTER);

        //UI area to display current scores
        var scoreBox = new HBox();
        scoreBox.setAlignment(Pos.CENTER);
        var scoreLabel = new Text("Score: ");
        var scoreField = new Text("0");
        scoreLabel.getStyleClass().add("heading");
        scoreField.textProperty().bind(this.scoresProperty.asString());
        scoreField.getStyleClass().add("score");
        scoreBox.setPadding(new Insets(20,0,0,20));
        scoreBox.getChildren().addAll(new Node[] {scoreLabel, scoreField});
        rotateAnime(scoreBox);

        //UI area to display current number of lives
        var livesBox = new HBox();
        var livesLabel = new Text("Lives: ");
        var livesField = new Text("0");
        livesBox.setAlignment(Pos.CENTER_LEFT);
        livesLabel.getStyleClass().add("heading");
        livesField.textProperty().bind(this.livesProperty.asString());
        livesField.getStyleClass().add("lives");
        livesBox.setPadding(new Insets(20,0,0,20));
        livesBox.getChildren().addAll(new Node[]{livesLabel, livesField});
        rotateAnime(livesBox);

        //UI area to display current multiplier
        var multiBox = new HBox();
        var multiLabel = new Text("X ");
        var multiField = new Text("0");
        multiBox.setAlignment(Pos.CENTER_LEFT);
        multiLabel.getStyleClass().add("heading");
        multiField.textProperty().bind(this.multiplierProperty.asString());
        multiField.getStyleClass().add("bigtitle");
        multiBox.getChildren().add(multiLabel);
        multiBox.getChildren().add(multiField);
        multiBox.setPadding(new Insets(20,0,0,30));
        rotateAnime(multiBox);

        Text tips = new Text("   Press T to chat");
        tips.getStyleClass().add("heading");

        topArea.setPadding(new Insets(5, 10, 5, 10));
        topArea.add(scoreBox,0,0);
        topArea.add(livesBox,1,0);
        topArea.add(multiBox,2,0);
        topArea.add(tips,3,0);
        topArea.setVgap(10);
        mainPane.setTop(topArea);

        GridPane.setHgrow(title, Priority.ALWAYS);
        GridPane.setHalignment(title, HPos.CENTER);
        mainPane.setTop(topArea);

        //UI of the game board
        GridPane centerArea = new GridPane();
        board = new GameBoard(game, game.getGrid(),gameWindow.getWidth()/2.15,gameWindow.getWidth()/2.15);
        board.setHoverReact(true);
        board.getKeyboardMode().bind(keyboardMode);
        chatMode.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (t1.equals(true)) {
                    keyboardMode.set(false);
                }
            }
        });
        centerArea.add(board,0,0);

        //UI of the chat area
        msgBox = new ScrollPane();
        msgContent = new VBox();
        msgBox.setContent(msgContent);
        sendMsg = new TextField();
        msgBox.setPrefHeight(30);
        msgBox.getStyleClass().add("scroller");
        msgContent.getStyleClass().add("message");
        sendMsg.visibleProperty().bind(chatMode);
        chatMode.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (t1.equals(true)) {
                    sendMsg.requestFocus();
                }
                Platform.runLater(() -> {
                    sendMsg.clear();
                });
            }
        });

        centerArea.setVgap(1);
        centerArea.add(msgBox,0,1);
        centerArea.add(sendMsg,0,2);
        centerArea.setPadding(new Insets(20,20,0,150));
        mainPane.setCenter(centerArea);

        //Binding properties
        livesProperty.bind(game.getLivesProperty());
        scoresProperty.bind(game.getScoreProperty());
        levelProperty.bind(game.getLevelProperty());
        multiplierProperty.bind(game.getMultiplierProperty());
        var sideBar = new VBox();
        sideBar.setSpacing(20);
        sideBar.setPrefWidth(gameWindow.getWidth()/4);

        //UI to hold LeaderBoard
        remoteScoreList = FXCollections.observableArrayList(remoteScores);
        SimpleListProperty<Pair<String, Integer>> wrapper = new SimpleListProperty(remoteScoreList);
        leaderBoard = new Leaderboard();
        leaderBoard.myNameProperty().bind(myName);
        leaderBoard.getStyleClass().add("leaderboard");
        leaderBoard.scoresProperty().bind(wrapper);
        leaderBoard.setMaxShow(5);
        sideBar.getChildren().add(leaderBoard);

        //UI area to display next piece
        Text nextPieceTitle = new Text("NEXT PIECE");
        nextPieceTitle.getStyleClass().add("heading");
        nextPiece = new PieceBoard(3,3,this.gameWindow.getWidth()/7,this.gameWindow.getWidth()/7);
        nextPiece.showCenter();
        sideBar.getChildren().add(nextPieceTitle);
        sideBar.getChildren().add(nextPiece);

        //UI area to display following piece
        followingPiece = new PieceBoard(3,3,this.gameWindow.getWidth()/9,this.gameWindow.getWidth()/9);
        sideBar.getChildren().add(followingPiece);
        mainPane.setRight(sideBar);

        //UI of the time bar
        timeStack = new StackPane();
        BorderPane.setMargin(timeStack, new Insets(5,5,5,5));
        timer.setHeight(20);
        timer.setWidth(gameWindow.getWidth());
        timer.setFill(Color.AQUA);
        timeStack.getChildren().add(timer);
        timeStack.setAlignment(Pos.CENTER);
        mainPane.setBottom(timeStack);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnRightClicked(this::rightClicked);
        nextPiece.setOnBlockClick(this::rotateBlock);
        followingPiece.setOnBlockClick(this::swapBlock);
    }

    /**
     * End the Multiplayer Game
     */
    @Override
    public void endGame() {
        communicator.send("DIE");
        game.stop();
        super.endGame();
    }

    /**
     * @param keyEvent Receive the keyEvents when the keyboard is clicked
     */
    @Override
    void handleKeys(KeyEvent keyEvent) {
        super.handleKeys(keyEvent);
        if (keyEvent.getCode().equals(KeyCode.T)) {
            if (!chatMode.get()) {
                chatMode.set(!chatMode.get());
            }
        }

        if (chatMode.get() && keyEvent.getCode().equals(KeyCode.ENTER)) {
            communicator.send("MSG " + sendMsg.getText());
            sendMsg.clear();
            chatMode.set(false);
        }
    }
}
