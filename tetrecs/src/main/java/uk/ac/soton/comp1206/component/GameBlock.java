package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);
    private boolean center = false;
    private boolean hover = false;
    private FadeoutTimer fadeoutTimer = null;
    private boolean canBePlaced = true;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }

        if(center) {
            drawCircle();
        }

        if(hover) {
            paintHover();
        }

        if(!canBePlaced) {
            cantPlace();
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.color(0,0,0,0.4));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.color(1,1,1,0.2));
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);
        gc.setFill(Color.color(1, 1, 1.0, 0.2));
        gc.fillPolygon(new double[]{0, 0, this.width}, new double[]{0, this.height, this.height}, 3);
        gc.setFill(Color.color(1, 1, 1, 0.3));
        gc.fillRect(0.0D, 0, this.width, 3);
        gc.setFill(Color.color(1, 1, 1, 0));
        gc.fillRect(0, 0, 3, this.height);
        gc.setFill(Color.color(0, 0, 0, 0.1));
        gc.fillRect(this.width - 3, 0, this.width, this.height);
        gc.setFill(Color.color(0.0, 0.0, 0.0, 0.1));
        gc.fillRect(0, this.height - 3, this.width, this.height);
                                                                           
        //Border
        gc.setStroke(Color.color(0,0,0,0.7));
        gc.strokeRect(0,0,width,height);
    }

    /**
     * paint the hovered block
     */
    private void paintHover() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.color(1,1,1,0.6));
        gc.fillRect(0,0,width,height);
    }

    /**
     * @param isHover set the hover attribute of the block
     */
    public void setHovering(boolean isHover) {
        hover = isHover;
        paint();
    }

    /**
     * Draw a circle in the center of the block
     */
    private void drawCircle() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFill(Color.color(1,1,1,0.4));
        gc.fillOval(this.width/4,this.height/4,this.width/2,this.height/2);
    }

    /**
     * set the center attribute to the block
     */
    public void setCenter() {
        this.center = true;
        this.paint();
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Start a fadeOut animation when lines are cleared
     */
    public void clearAction() {
        fadeoutTimer = new FadeoutTimer();
        fadeoutTimer.start();
    }

    /**
     * Paint the style if the piece can't be placed
     */
    public void cantPlace() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.color(1,0,0,0.4));
        gc.fillRect(0,0,width,height);
    }

    public class FadeoutTimer extends AnimationTimer {
        double opacity = 1;

        @Override
        public void handle(long l) {
            fadeOut();
        }

        /**
         * fadeOut Animation.
         * fadeOut Colour: Purple
         */
        private void fadeOut() {
            paintEmpty();
            opacity -= 0.05;
            if (opacity <= 0) {
                stop();
                fadeoutTimer = null;
            } else {
                GraphicsContext gc = getGraphicsContext2D();
                gc.setFill(Color.color(0.5,0,0.5,opacity));
                gc.fillRect(0,0,getWidth(),getHeight());
            }
        }
    }

    public boolean isCanBePlaced(boolean bool) {
        return canBePlaced = bool;
    }
}
