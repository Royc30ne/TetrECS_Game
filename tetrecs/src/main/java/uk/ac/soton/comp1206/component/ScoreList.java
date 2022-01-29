package uk.ac.soton.comp1206.component;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;

/**
 * @author Royce_Lyu
 * @date 2021-04-05 1:29
 */
public class ScoreList extends VBox {

    private static final Logger logger = LogManager.getLogger(ScoreList.class);
    protected SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty();
    protected ArrayList<HBox> scoreBoxes = new ArrayList();
    protected StringProperty myName = new SimpleStringProperty();
    protected int maxShow = 10;

    public ScoreList() {
        getStyleClass().add("scorelist");
        setAlignment(Pos.CENTER);
        setSpacing(1.5);
        scores.addListener((ListChangeListener<? super Pair<String, Integer>>) (e) -> updateList());
        myName.addListener((e) ->
                Platform.runLater(() -> {
                            updateList();
                }));
    }

    /**
     * Method to animate the display of the scores
     */
    public void reveal() {
        logger.info("Revealing {} scores", scoreBoxes.size());
        ArrayList<Transition> transitions = new ArrayList();
        for(HBox i : scoreBoxes) {
            HBox scoreBox = i;
            FadeTransition fadeTransition = new FadeTransition(new Duration(300), scoreBox);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            transitions.add(fadeTransition);
        }

        SequentialTransition transition = new SequentialTransition(transitions.toArray((e) -> {
            return new Animation[e];
        }));
        transition.play();
    }

    /**
     * Method to update the score list
     */
    public void updateList() {
        logger.info("Updating score list");
        scoreBoxes.clear();
        getChildren().clear();

        int counter = 0;
        for(Pair<String, Integer> i : scores.get()) {
            if(counter > maxShow) {
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

    /**
     * Set the max shown box
     * @param number
     */
    public void setMaxShow(int number) {
        maxShow = number;
    }

    /**
     * Call myName Property
     * @return myName Property
     */
    public StringProperty myNameProperty() {
        return myName;
    }

    /**
     * Call scoresProperty
     * @return scores Property
     */
    public SimpleListProperty<Pair<String, Integer>> scoresProperty() {
        return scores;
    }
}
