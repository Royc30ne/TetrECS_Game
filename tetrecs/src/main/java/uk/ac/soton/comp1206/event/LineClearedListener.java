package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

/**
 * @author Royce_Lyu
 * @date 2021-03-31 18:51
 */
public interface LineClearedListener {
    /**
     * Handle the incoming block coordinates.
     * @param blockCoordinates block coordinates to be cleared
     */
    void lineCleared(HashSet<GameBlockCoordinate> blockCoordinates);
}
