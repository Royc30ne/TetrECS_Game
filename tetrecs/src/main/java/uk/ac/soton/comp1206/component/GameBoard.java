package uk.ac.soton.comp1206.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;

import java.util.ArrayList;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    GameBlock[][] blocks;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;
    private RightClickedListener rightClickedListener;
    private boolean hoverReact = false;
    private BooleanProperty keyboardMode = new SimpleBooleanProperty(false);
    private GameBlock hovering;
    private Game game;
    private ArrayList<GameBlock> previews = new ArrayList<>();
    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Game game, Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;
        this.game = game;
        //Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols,rows);

        //Build the GameBoard
        build();
    }

    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}",cols,rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));


        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                blockClicked(block);
            } else {
                rightClicked(block);
            }
        });

        if (game != null) {
            block.setOnMouseEntered((e) -> {
                setHover(block);
            });
            block.setOnMouseExited((e) -> {
                setUnhover(block);
            });
        }
        return block;
    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Set the listener to handle an event when a block is right clicked
     * @param listener listener to add
     */
    public void setOnRightClicked(RightClickedListener listener) {
        this.rightClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param block block clicked on
     */
    private void blockClicked(GameBlock block) {
        logger.info("Block clicked: {}", block);

        if(blockClickedListener != null) {
            blockClickedListener.blockClicked(block);
        }
    }

    /**
     * Triggered when a block is right clicked. Call the attached listener
     * @param block block clicked on
     */
    private void rightClicked(GameBlock block) {
        logger.info("Block right clicked : {}", block);

        if(rightClickedListener != null) {
            rightClickedListener.rightClicked();
        }
    }


    /**
     * @param block Set the block to be hovered
     */
    public void setHover(GameBlock block) {
        if (hoverReact) {
            if(keyboardMode.get() && hovering != null) {
                setUnhover(hovering);
                setUnPreview(hovering);
            }
            this.hovering = block;
            setPreview(hovering);
        }
    }

    /**
     * @param block Set the block to be unhovered
     */
    public void setUnhover(GameBlock block) {
        if (hoverReact) {
            block.setHovering(false);
            setUnPreview(hovering);
        }
    }

    /**
     * Decide whether to turn on the hovering animation of the board
     * @param hoverReact switch to turn on or off the hover animation
     */
    public void setHoverReact(boolean hoverReact) {
        this.hoverReact = hoverReact;
    }

    /**
     * @return the KeyboardMode Property
     */
    public BooleanProperty getKeyboardMode() {
        return keyboardMode;
    }

    /**
     * play the fadeout animation to the cleared blocks
     * @param block block to be cleared
     */
    public void fadeOut(GameBlock block) {
        block.clearAction();
    }

    /**
     * Set the blocks to preview mode
     * @param block
     */
    public void setPreview(GameBlock block) {
        boolean canplace = grid.canPlacePiece(game.getCurrentPiece(), hovering.getX() - 1, hovering.getY() - 1);

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if(game != null) {
                    if (game.getCurrentPiece().getBlocks()[x][y] != 0) {
                        if (x + block.getX() - 1 < rows && y + block.getY() - 1 < cols && x + block.getX() - 1 > -1 && y + block.getY() - 1 > -1) {
                            previews.add(this.blocks[x + block.getX() - 1][y + block.getY() - 1]);
                        }
                    }
                }
            }
        }
        for (GameBlock i : previews) {
            i.isCanBePlaced(canplace);
            i.setHovering(true);
        }
    }

    /**
     * Set block to unpreview mode
     * @param block
     */
    public void setUnPreview(GameBlock block) {
        for (GameBlock i : previews) {
            i.isCanBePlaced(true);
            i.setHovering(false);
        }
        previews.clear();
    }

    /**
     * Get the hovering block
     * @return game block
     */
    public GameBlock getHovering() {
        return hovering;
    }
}
