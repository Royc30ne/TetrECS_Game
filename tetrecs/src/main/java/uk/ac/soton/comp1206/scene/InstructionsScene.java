package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * @author Royce_Lyu
 * @date 2021-03-24 23:59
 */
public class InstructionsScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instruction Scene");
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
        logger.info("Exit to Main Menu");
        this.scene.setOnKeyPressed((e) -> {
            this.gameWindow.startMenu();
        });
    }

    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var instructionPane = new StackPane();
        instructionPane.setMaxHeight(gameWindow.getHeight());
        instructionPane.setMaxWidth(gameWindow.getWidth());
        instructionPane.getStyleClass().add("menu-background");
        root.getChildren().add(instructionPane);

        var displayPane = new BorderPane();
        instructionPane.getChildren().add(displayPane);

        var vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);

        //The Field to place Instructions
        Text instructionsTitle = new Text("Instructions");
        instructionsTitle.getStyleClass().add("heading");
        vBox.getChildren().add(instructionsTitle);
        String picPath = InstructionsScene.class.getResource("/images/Instructions.png").toExternalForm();
        ImageView instructionsField = new ImageView(picPath);
        instructionsField.setFitWidth(gameWindow.getWidth() / 1.3D);
        instructionsField.setPreserveRatio(true);
        vBox.getChildren().add(instructionsField);

        //The Field to place pieces
        //Left
        var piecesField_1 = new GridPane();
        for (int i = 0; i < 8; i++) {
            var gameBoard = new PieceBoard(3,3,gameWindow.getWidth()/10, gameWindow.getWidth()/10);
            gameBoard.displayPiece(GamePiece.createPiece(i));
            piecesField_1.add(gameBoard,i,0);
        }
        piecesField_1.setHgap(10);
        piecesField_1.setPadding(new Insets(5,0,5,20));
        piecesField_1.setAlignment(Pos.CENTER);
        vBox.getChildren().add(piecesField_1);

        //Right
        var piecesField_2 = new GridPane();
        for (int i = 8; i < 15; i++) {
            var gameBoard = new PieceBoard(3,3,gameWindow.getWidth()/10, gameWindow.getWidth()/10);
            gameBoard.displayPiece(GamePiece.createPiece(i));
            piecesField_2.add(gameBoard,i-8,0);
        }
        piecesField_2.setPadding(new Insets(5,0,5,0));
        piecesField_2.setHgap(10);
        piecesField_2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(piecesField_2);

        displayPane.setCenter(vBox);
        BorderPane.setAlignment(vBox, Pos.CENTER);
    }
}
