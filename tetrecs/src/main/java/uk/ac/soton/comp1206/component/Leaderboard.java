package uk.ac.soton.comp1206.component;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * @author Royce_Lyu
 * @date 2021-04-09 22:49
 */
public class Leaderboard extends ScoreList {
    Logger logger = LogManager.getLogger(Leaderboard.class);

    /**
     * List of deal player
     */
    ArrayList<String> deadPlayers = new ArrayList();

    /**
     * Handle dead players
     *
     * @param name Dead player
     */
    public void kill(String name) {
        deadPlayers.add(name);
    }

    /**
     *Update the leader board
     */
    @Override
    public void updateList() {
        logger.info("Updating score list");
        scoreBoxes.clear();
        getChildren().clear();

        int counter = 0;
        for (Pair<String, Integer> i : scores.get()) {
            if (counter > maxShow) {
                break;
            }

            HBox scoreBox = new HBox();
            scoreBox.setOpacity(0.5);
            scoreBox.getStyleClass().add("scoreitem");
            scoreBox.setAlignment(Pos.CENTER);
            scoreBox.setSpacing(5);

            //Field to place player's name in a single score box
            Text player = new Text(i.getKey() + ":");
            player.getStyleClass().add("scorer");
            if (i.getKey().equals(myName)) {
                player.getStyleClass().add("myscore");
            }
            if (deadPlayers.contains(i.getKey())) {
                player.getStyleClass().add("deadscore");
            }
            player.setTextAlignment(TextAlignment.CENTER);

            //Field to place player's score in a single score box
            Text points = new Text(i.getValue().toString());
            points.getStyleClass().add("points");
            points.setTextAlignment(TextAlignment.CENTER);

            scoreBox.getChildren().add(player);
            scoreBox.getChildren().add(points);
            getChildren().add(scoreBox);
            scoreBoxes.add(scoreBox);

            reveal();
        }
    }
}
