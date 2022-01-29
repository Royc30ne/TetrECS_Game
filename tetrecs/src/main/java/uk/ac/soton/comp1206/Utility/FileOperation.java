package uk.ac.soton.comp1206.Utility;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.SerializePack;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Royce_Lyu
 * @date 2021-04-06 21:04
 */
public class FileOperation {
    private final static Logger logger = LogManager.getLogger(FileOperation.class);

    /**
     * Load local scores from the storage
     * @return list of local scores
     */
    public static ArrayList<Pair<String, Integer>> loadScores() {
        logger.info("Load local scores from the disk");
        ArrayList output = new ArrayList();
        try {
            Path path = Paths.get("scores.txt");
            if (Files.notExists(path)) {
                initialise();
            }
            List<String> scores = Files.readAllLines(path);
            for (String i : scores) {
                String score = i;
                String[] components = score.split(":");
                output.add(new Pair(components[0], Integer.parseInt(components[1])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * Write an array of scores to the local disk
     */
    public static void writeScores(List<Pair<String, Integer>> localScores) {
        logger.info("Writing the score list to local disk!");
        localScores.sort((a, b) -> (b.getValue().compareTo(a.getValue())));

        try {
            Path path = Paths.get("scores.txt");
            StringBuilder output = new StringBuilder();
            int counter = 0;

            for(Pair<String, Integer> i : localScores) {
                counter++;
                output.append(i.getKey() + ":" + i.getValue() + "\n");
                if (counter >= 10) break;
            }

            Files.writeString(path, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initialise
     */
    public static void initialise() {
        ArrayList<Pair<String, Integer>> ini = new ArrayList();
        ini.add(new Pair("Local Gamer", 100));
        writeScores(ini);
    }

    /**
     * Write saved game to the storage
     * @param game ser file
     */
    public static void writeGame(SerializePack game) {
        try
        {
            FileOutputStream fileOut = new FileOutputStream("game.ser");
            //Create an ObjectOutputStream output stream
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            //Serialize the object to file
            out.writeObject(game);
            out.close();
            fileOut.close();
            logger.info("Game is saved");
        }catch(IOException i)
        {
            i.printStackTrace();
        }
    }

    /**
     * Read game file from the storage
     * @return ser file
     */
    public static SerializePack readGame() {
        SerializePack returnValue = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("game.ser"));
            //Create an ObjectInputStream input stream
            returnValue = (SerializePack) ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Game is successfully read!");
        return returnValue;
    }
}
