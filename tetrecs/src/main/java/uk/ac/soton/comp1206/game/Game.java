package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.MultiMedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game implements Serializable {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected Grid grid;

    /**
     * Holding the current piece
     */
    protected GamePiece currentPiece;

    /**
     * Holding the following piece
     */
    protected GamePiece followingPiece;

    /**
     * The player's name
     */
    protected StringProperty name = new SimpleStringProperty();

    /**
     * The player's score
     */
    protected IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * The player's number of life
     */
    protected IntegerProperty lives = new SimpleIntegerProperty(0);

    /**
     * The player's level
     */
    protected IntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * Current multiplier
     */
    protected IntegerProperty multiplier = new SimpleIntegerProperty(0);

    /**
     * Executor to execute game loop
     */
    protected transient ScheduledExecutorService executorService = null;

    /**
     * Schedule the next loop's event
     */
    protected ScheduledFuture nextLoop = null;

    /**
     * Listener to handle nextPiece event
     */
    protected NextPieceHandler nextPieceHandler = null;

    /**
     * Listener to handle right clicked event
     */
    protected RightClickedListener rightClickedListener = null;

    /**
     * Listener to handle line cleared event
     */
    protected LineClearedListener lineClearedListener = null;

    /**
     * Listener to handle game loop
     */
    protected GameLoopListener gameLoopListener = null;

    /**
     * Listener to handle game over
     */
    protected GameOverListener gameOverListener = null;

    /**
     * List of scores
     */
    protected ArrayList<Pair<String, Integer>> scores = new ArrayList();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        startGameLoop();
    }

    /**
     * Stop the game
     */
    public void stop() {
        logger.info("Game End!");
        executorService.shutdownNow();
        MultiMedia.stopMusic();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        this.score.set(0);
        this.lives.set(3);
        this.level.set(0);
        this.multiplier.set(1);
        this.followingPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public boolean blockClicked(GameBlock gameBlock) {
        boolean add = false;
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        logger.info("Block Clicked [{},{}]", x, y);
        if(grid.centerPiece(this.currentPiece, x, y) == true) {
            nextPiece();
            add = true;
        }
        afterPiece();
        return add;
    }

    public void rightClicked(GameBlock gameBlock) {
        if (this.rightClickedListener != null) {
            this.rightClickedListener.rightClicked();
        }
    }
    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the current generated GamePiece
     * @return  current GamePiece
     */
    public GamePiece getCurrentPiece() {
        return this.currentPiece;
    }

    /**
     * Call the player's name
     * @return name's property
     */
    public StringProperty getNameProperty() {
        return this.name;
    }

    /**
     * Call the player's current lives' property
     * @return lives' property
     */
    public IntegerProperty getLivesProperty() {
        return this.lives;
    }

    /**
     * Call the player's current levels' property
     * @return levels' property
     */
    public IntegerProperty getLevelProperty() {
        return this.level;
    }

    /**
     * Call the player's current scores' property
     * @return score's property
     */
    public IntegerProperty getScoreProperty() {
        return this.score;
    }

    /**
     * Call the multiplier's property
     * @return the multiplier's property
     */
    public IntegerProperty getMultiplierProperty() {
        return this.multiplier;
    }

    /**
     * Get Score list
     * @return score list
     */
    public ArrayList<Pair<String, Integer>> getScores() {
        return scores;
    }

    /**
     * Create a new random GamePiece by calling GamePiece.createPiece
     * @return a new random GamePiece
     */
    public GamePiece spawnPiece() {
        Random random = new Random();
        return  GamePiece.createPiece(random.nextInt(15),random.nextInt(2));
    }

    /**
     * Get the next piece after placing a piece
     * @return next game piece to be placed
     */
    public GamePiece nextPiece() {
        this.currentPiece = this.followingPiece;
        this.followingPiece = spawnPiece();
        logger.info("Current piece is : {}", this.currentPiece);
        logger.info("Next piece spawned: {}", this.followingPiece);
        if(nextPieceHandler != null) {
            nextPieceHandler.nextPiece(currentPiece);
            logger.info("Current Piece [{}] is transferred to handler ", this.currentPiece);
        }
        return this.currentPiece;
    }

    /**
     * Actions to do after place a piece
     */
    public void afterPiece() {

        //Area to deal with eliminating blocks
        int lines = 0;
        HashSet<IntegerProperty> clear = new HashSet<>();
        HashSet<GameBlockCoordinate> blockCoordinates = new HashSet<>();

        for(int x = 0; x < this.cols; x++) {
            int count = 0;
            for(int i = 0; i < this.rows; i++) {
                if(this.grid.get(x, i) != 0) count++;
            }
            if (count == this.rows) {
                lines++;
                for (int j = 0; j < this.rows; j++) {
                    clear.add(this.grid.getGridProperty(x, j));
                    blockCoordinates.add(new GameBlockCoordinate(x, j));
                }
            }
        }

        for(int y = 0; y < this.rows; y++) {
            int count = 0;
            for(int i = 0; i < this.cols; i++) {
                if(this.grid.get(i, y) != 0) count++;
            }
            if (count == this.cols) {
                lines++;
                for (int j = 0; j < this.cols; j++) {
                    clear.add(this.grid.getGridProperty(j, y));
                    blockCoordinates.add(new GameBlockCoordinate(j, y));
                }
            }
        }

        for(IntegerProperty i : clear) {
            i.set(0);
        }
        score(lines, clear.size());

        //Area to deal with multiplier
        if(lines != 0) {
            multiplier.set(multiplier.add(1).get());
            if (lineClearedListener != null) {
                lineClearedListener.lineCleared(blockCoordinates);
            }
        } else {
            multiplier.set(1);

        }

        //Area to deal with level
        level.set(score.get()/1000);

    }

    /**
     * @param lines receive the number of cleared lines
     * @param blocks receive the number of cleared blocks
     */
    public void score(int lines, int blocks) {
        this.score.set(this.score.add(lines * blocks * 10 * this.multiplier.get()).get());
    }

    /**
     * @param nextPieceHandler The event to handle next piece
     */
    public void setCurrentPiece(NextPieceHandler nextPieceHandler) {
        logger.info("set next piece: {}", nextPieceHandler);
        this.nextPieceHandler = nextPieceHandler;
    }

    /**
     * The method to rotate the current piece
     * @param rotations Times of rotating
     */
    public void rotateCurrentPiece(int rotations) {
        currentPiece.rotate(rotations);
    }

    /**
     * Swap the current piece and the following piece
     */
    public void swapCurrentPiece() {
        logger.info("Current piece is swapped!");
        GamePiece tmp = this.currentPiece;
        this.currentPiece = this.followingPiece;
        this.followingPiece = tmp;
    }

    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    /**
     * @param lineClearedListener set on the LineClearedListener
     */
    public void setOnLineCleared (LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * @param gameLoopListener set on the GameLoopListener
     */
    public void setOnGameLoop (GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    /**
     * @param gameOverListener set on the GameOverListener
     */
    public void setOnGameOver (GameOverListener gameOverListener) {
        this.gameOverListener = gameOverListener;
    }
    /**
     * Calculate the delay at the maximum of either 2500 milliseconds or 12000 - 500 * the current level
     * @return The calculated delay
     */
    public int getTimerDelay() {
        return Math.max(2500, 12000 - 500 * level.get());
    }

    /**
     * The method to define a game loop
     */
    public void gameLoop() {
        logger.info("Executing Game Loop!");

        multiplier.set(1);
        if (lives.get() > 0) {
            logger.info("You lost 1 life");
            lives.set(lives.get() - 1);
        } else {
            gameOver();
        }
        nextPiece();
        int interval = getTimerDelay();

        if(gameLoopListener != null) {
            gameLoopListener.gameLoop(interval);
        }

        nextLoop = executorService.schedule(this::gameLoop, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * The method to start a game loop
     */
    public void startGameLoop() {
        nextLoop = executorService.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        if (gameLoopListener != null) {
            gameLoopListener.gameLoop(getTimerDelay());
        }
    }

    /**
     * The method to restart a game loop
     */
    public void restartGameLoop() {
        nextLoop.cancel(false);
        startGameLoop();
    }

    /**
     * Method to handle game over event
     */
    public void gameOver() {
        logger.info("Game Over!");
        if (gameOverListener != null) {
            Platform.runLater(() -> gameOverListener.gameOver());
        }
    }

    /**
     * Save the current game
     * @return ser file
     */
    public SerializePack writeSerializePack() {
        int[][] gridCoordinate = new int[5][5];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                gridCoordinate[x][y] = grid.get(x,y);
            }
        }
        return new SerializePack(name.get(), score.get(), lives.get(), level.get(), multiplier.get(), currentPiece, followingPiece, gridCoordinate, nextLoop.getDelay(TimeUnit.MILLISECONDS));
    }

    /**
     * Load game from the disk
     * @param game ser file
     */
    public void loadGame(SerializePack game) {
        this.name.set(game.name);
        this.score.set(game.score);
        this.level.set(game.level);
        this.lives.set(game.lives);
        this.multiplier.set(game.multiplier);
        this.followingPiece = game.followingPiece;
        this.currentPiece = game.currentPiece;
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                grid.set(x, y, game.gridCoordinate[x][y]);
            }
        }
        if(nextLoop != null) {
            nextLoop.cancel(false);
            nextLoop = executorService.schedule(this::gameLoop, game.delay, TimeUnit.MILLISECONDS);
            if (gameLoopListener != null) {
                gameLoopListener.gameLoop((int) game.delay);
            }
        }
    }
}
