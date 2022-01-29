package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.FileOperation;
import uk.ac.soton.comp1206.Utility.MultiMedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.SerializePack;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements Serializable {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;

    /**
     * Property to hold player's live
     */
    protected IntegerProperty livesProperty = new SimpleIntegerProperty();

    /**
     * Property to hold player's score
     */
    protected IntegerProperty scoresProperty = new SimpleIntegerProperty();

    /**
     * Property to hold player's multiplier
     */
    protected IntegerProperty multiplierProperty = new SimpleIntegerProperty();

    /**
     * Property to hold player's level
     */
    protected IntegerProperty levelProperty = new SimpleIntegerProperty();

    /**
     * Property of the highest score
     */
    protected IntegerProperty hiScore = new SimpleIntegerProperty();

    /**
     * UI of the current piece
     */
    protected PieceBoard nextPiece;

    /**
     * UI of the following piece
     */
    protected PieceBoard followingPiece;

    /**
     * Block's X coordinate under keyboard mode
     */
    protected int keyboardX = 0;

    /**
     * Block's Y coordinate under keyboard mode
     */
    protected int keyboardY = 0;

    /**
     * Keyboard mode's property
     */
    protected BooleanProperty keyboardMode = new SimpleBooleanProperty(false);

    /**
     * UI to hold the game board
     */
    protected GameBoard board;

    /**
     * UI of the time bar
     */
    protected Rectangle timer = new Rectangle();

    /**
     * StackPane to hold time bar
     * */
    protected StackPane timeStack;

    protected SerializePack serializePack;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    public ChallengeScene(SerializePack game, GameWindow gameWindow) {
        super(gameWindow);
        this.serializePack = game;
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);
        MultiMedia.playAudio("explode.wav");
        MultiMedia.playBackgroundMusic("game.mp3", true);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        //UI on the Top
        GridPane topArea = new GridPane();

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

        topArea.setPadding(new Insets(5, 10, 5, 10));
        topArea.add(scoreBox,0,0);
        topArea.add(livesBox,1,0);
        topArea.add(multiBox,2,0);
        topArea.setVgap(10);
        mainPane.setTop(topArea);

        board = new GameBoard(game, game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        board.setHoverReact(true);
        board.getKeyboardMode().bind(this.keyboardMode);
        mainPane.setCenter(board);

        var sideBar = new VBox();
        livesProperty.bind(game.getLivesProperty());
        scoresProperty.bind(game.getScoreProperty());
        levelProperty.bind(game.getLevelProperty());
        multiplierProperty.bind(game.getMultiplierProperty());
        sideBar.setSpacing(20);
        sideBar.setPrefWidth(gameWindow.getWidth()/4);


        //UI area to display highest scores
        var hiScoreBox = new HBox();
        hiScoreBox.setAlignment(Pos.CENTER_LEFT);
        var hiScoreLabel = new Text("Hiscore :");
        var hiScoreField = new Text("0");
        hiScoreLabel.getStyleClass().add("hiscore");
        hiScoreField.textProperty().bind(this.hiScore.asString());
        hiScoreField.getStyleClass().add("hiscore");
        hiScoreBox.getChildren().addAll(new Node[] {hiScoreLabel,hiScoreField});

        //UI area to display current level
        var levelBox = new HBox();
        var levelLabel = new Text("Level: ");
        var levelField = new Text("0");
        levelBox.setAlignment(Pos.CENTER_LEFT);
        levelLabel.getStyleClass().add("level");
        levelField.textProperty().bind(this.levelProperty.asString());
        levelField.getStyleClass().add("level");
        levelBox.getChildren().add(levelLabel);
        levelBox.getChildren().add(levelField);

        Text saveGame = new Text("SAVE");
        applySave(saveGame);
        saveGame.setOnMouseClicked(event -> {
            FileOperation.writeGame(this.game.writeSerializePack());
        });

        Text saveAndQuit = new Text("SAVE & QUIT");
        applySave(saveAndQuit);
        saveAndQuit.setOnMouseClicked(event -> {
            FileOperation.writeGame(this.game.writeSerializePack());
            endGame();
        });

        sideBar.getChildren().add(hiScoreBox);
        sideBar.getChildren().add(levelBox);

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
        sideBar.getChildren().add(saveGame);
        sideBar.getChildren().add(saveAndQuit);
        mainPane.setRight(sideBar);

        //UI of the time bar
        timeStack = new StackPane();
        BorderPane.setMargin(timeStack, new Insets(5,10,5,5));
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
     * Handle the event when a block is righted clicked
     */
    void rightClicked() {
        keyboardMode.set(false);
        game.swapCurrentPiece();
        this.nextPiece.displayPiece(game.getCurrentPiece());
        this.followingPiece.displayPiece(game.getFollowingPiece());
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    void blockClicked(GameBlock gameBlock) {
        MultiMedia.playAudio("place.wav");
        keyboardMode.set(false);
        if (game.blockClicked(gameBlock)) {
            logger.info("Block placed");
            game.restartGameLoop();
        }
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        if (game == null) {
            game = new Game(5, 5);
        }
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising {}", this.getClass());
        game.setCurrentPiece(this::nextPiece);
        scene.setOnKeyPressed(this::handleKeys);
        game.setOnLineCleared(this::clearLine);
        game.setOnGameLoop(this::gameLoop);
        game.getScoreProperty().addListener(this::getHiScore);
        ArrayList<Pair<String, Integer>> scores = FileOperation.loadScores();
        hiScore.set(scores.get(0).getValue());
        game.start();
        if (serializePack != null) {
            logger.info("Successfully loading the game!");
            game.loadGame(serializePack);
        }
        game.setOnGameOver(() -> {
            game.stop();
            gameWindow.startScore(game);
        });
        this.game.getLevelProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > oldValue.intValue()) {
                    MultiMedia.playAudio("level.wav");
                }
            }
        });
        this.game.getLivesProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > oldValue.intValue()) {
                    MultiMedia.playAudio("lifegain.wav");
                } else {
                    MultiMedia.playAudio("lifelose.wav");
                }
            }
        });
    }


    /**
     * Set the TimeBar animation during a game loop
     * @param nextLoop the interval between two loops
     */
    void gameLoop(int nextLoop) {
        Timeline timeline = new Timeline(new KeyFrame[]{new KeyFrame(Duration.ZERO, new KeyValue[]{new KeyValue(this.timer.fillProperty(), Color.GREEN)}), new KeyFrame(Duration.ZERO, new KeyValue[]{new KeyValue(this.timer.widthProperty(), this.timeStack.getWidth())}), new KeyFrame(new Duration((double)nextLoop * 0.5D), new KeyValue[]{new KeyValue(this.timer.fillProperty(), Color.YELLOW)}), new KeyFrame(new Duration((double)nextLoop * 0.75D), new KeyValue[]{new KeyValue(this.timer.fillProperty(), Color.RED)}), new KeyFrame(new Duration((double)nextLoop), new KeyValue[]{new KeyValue(this.timer.widthProperty(), 0)})});
        timeline.play();
    }

    /**
     * Set the blocks to be cleared when lines are full;
     * @param gameBlockCoordinates blocks to be cleared
     */
    void clearLine(HashSet<GameBlockCoordinate> gameBlockCoordinates) {
        for(GameBlockCoordinate i : gameBlockCoordinates) {
            board.fadeOut(board.getBlock(i.getX(),i.getY()));
        }

        MultiMedia.playAudio("clear.wav");
    }

    /**
     * The method to handle the key events
     * @param keyEvent Receive the keyEvents when the keyboard is clicked
     */
    void handleKeys(KeyEvent keyEvent) {
        keyboardMode.set(true);
        if ((keyEvent.getCode().equals(KeyCode.UP) || keyEvent.getCode().equals(KeyCode.W)) && keyboardY > 0) {
            keyboardY--;
        }
        if ((keyEvent.getCode().equals(KeyCode.DOWN) || keyEvent.getCode().equals(KeyCode.S)) && keyboardY < game.getRows() - 1) {
            keyboardY++;
        }
        if ((keyEvent.getCode().equals(KeyCode.A) || keyEvent.getCode().equals(KeyCode.LEFT)) && keyboardX > 0) {
            keyboardX--;
        }
        if ((keyEvent.getCode().equals(KeyCode.D) || keyEvent.getCode().equals(KeyCode.RIGHT)) && keyboardX < game.getCols() - 1) {
            keyboardX++;
        }
        if(keyEvent.getCode().equals(KeyCode.ENTER) || keyEvent.getCode().equals(KeyCode.X)) {
            if(this.game.getGrid().centerPiece(this.game.getCurrentPiece(), keyboardX, keyboardY) == true) {
                MultiMedia.playAudio("place.wav");
                game.restartGameLoop();
                game.afterPiece();
                this.game.nextPiece();
            }
        }
        logger.info("Keyboard coordinate [{},{}]",keyboardX,keyboardY);

        if (keyEvent.getCode().equals(KeyCode.SPACE) || keyEvent.getCode().equals(KeyCode.R)) {
            swapBlock();
        }

        if (keyEvent.getCode().equals(KeyCode.Q) || keyEvent.getCode().equals(KeyCode.Z) || keyEvent.getCode().equals(KeyCode.OPEN_BRACKET)) {
            rotateBlock(3);
        }
        if (keyEvent.getCode().equals(KeyCode.E) || keyEvent.getCode().equals(KeyCode.C) || keyEvent.getCode().equals(KeyCode.CLOSE_BRACKET)) {
            rotateBlock(1);
        }

        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            endGame();
        }

        board.setHover(board.getBlock(keyboardX, keyboardY));
        board.setPreview(board.getHovering());
    }

    /**
     * Display the next piece
     * @param nextPiece next piece to be showed on the screen
     */
    public void nextPiece(GamePiece nextPiece) {
        logger.info("Display next piece:" + nextPiece);
        this.nextPiece.displayPiece(nextPiece);
        this.followingPiece.displayPiece(game.getFollowingPiece());
    }

    /**
     * End the game
     */
    public void endGame() {
        game.stop();
        this.gameWindow.startMenu();
    }

    /**
     * Rotating the block clockwise once
     * @param block The block to be rotated
     */
    public void rotateBlock(GameBlock block) {
        this.rotateBlock(1);
    }

    /**
     * Rotating the block
     * @param rotations Times of clockwise rotation
     */
    public void rotateBlock(int rotations) {
        logger.info("Rotate current block");
        MultiMedia.playAudio("rotate.wav");
        this.game.rotateCurrentPiece(rotations);
        this.nextPiece.displayPiece(this.game.getCurrentPiece());
    }

    /**
     * Swapping current block and following block called by the listener
     * @param block current block
     */
    public void swapBlock(GameBlock block) {
        swapBlock();
    }

    /**
     * Swapping current block and following block
     */
    public void swapBlock(){
        logger.info("Swapping current block!");
        MultiMedia.playAudio("rotate.wav");
        this.game.swapCurrentPiece();
        this.nextPiece.displayPiece(this.game.getCurrentPiece());
        this.followingPiece.displayPiece(this.game.getFollowingPiece());
    }

    /**
     * Compare this game's score with high score, and enable current score to high score after exceeding the local hiScore.
     * @param value observable local list
     */
    public void getHiScore(Observable value) {
        if (scoresProperty.get() > hiScore.get()) {
            hiScore.set(scoresProperty.get());
        }
    }

    /**
     * Set the rotate animation
     * @param hBox
     */
    public void rotateAnime(HBox hBox) {
        double play_time =Math.abs(90)*0.02;
        RotateTransition rotateTransition = new  RotateTransition(Duration.seconds(play_time),  hBox);
        rotateTransition.setFromAngle(-40);
        rotateTransition.setToAngle(40);
        rotateTransition.setCycleCount(-1);
        rotateTransition.setAutoReverse(true);
        rotateTransition.play();
    }

    /**
     * Apply the text style
     * @param appliedText
     */
    public void applySave(Text appliedText) {
        appliedText.getStyleClass().add("saveItem");
        appliedText.setOnDragEntered(dragEvent -> {
            appliedText.getStyleClass().add("selected");
            MultiMedia.playAudio("rotate.wav");
        });
        appliedText.setOnDragExited(dragEvent -> {
            appliedText.getStyleClass().remove("selected");
        });
    }
}
