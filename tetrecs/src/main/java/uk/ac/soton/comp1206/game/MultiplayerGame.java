package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * @author Royce_Lyu
 */
public class MultiplayerGame extends Game {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    /**
     * Incoming Pieces
     */
    private ArrayDeque<GamePiece> comingPieces = new ArrayDeque();

    /**
     * Judge if the game is started
     */
    private boolean gameStarted = false;

    private final Communicator communicator;
    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param communicator communicator
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(Communicator communicator, int cols, int rows) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.addListener((message) -> {
            Platform.runLater(() -> {
                handleMessage(message.trim());
            });
        });
    }

    /**
     * Initialising the game
     */
    @Override
    public void initialiseGame() {
        logger.info("Initialising game");
        score.set(0);
        level.set(0);
        lives.set(3);
        multiplier.set(1);
        for (int i = 0; i < 5; i++) {
            communicator.send("PIECE");
        }
    }

    /**
     * Handle incoming message
     * @param message message
     */
    private void handleMessage(String message) {
        logger.info("Received message: {}", message);
        String[] components = message.split(" ",2);
        if (components[0].equals("PIECE") && components.length > 1) {
            handlePieceMsg(Integer.parseInt(components[1]));
        }
    }

    /**
     * Handle piece message
     * @param message piece message
     */
    private void handlePieceMsg(int message) {
        GamePiece gamePiece = GamePiece.createPiece(message);
        logger.info("Process piece {}", gamePiece);
        comingPieces.add(gamePiece);
        if (!gameStarted && comingPieces.size() > 2) {
            currentPiece = spawnPiece();
            nextPiece();
            gameStarted = true;
        }
    }

    /**
     * Spawn piece
     * @return current game piece
     */
    @Override
    public GamePiece spawnPiece() {
        this.communicator.send("PIECE");
        return comingPieces.pop();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * Send BOARD message to the server
     * @param gameBlock the block that was clicked
     */
    @Override
    public boolean blockClicked(GameBlock gameBlock) {
        boolean result = super.blockClicked(gameBlock);

        StringBuilder board = new StringBuilder();
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                board.append(grid.get(x, y) + " ");
            }
        }
        communicator.send("BOARD " + board);

        return result;
    }
}
