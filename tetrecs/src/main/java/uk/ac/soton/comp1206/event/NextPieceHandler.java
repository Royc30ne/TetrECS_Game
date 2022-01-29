package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * @author Royce_Lyu
 * @date 2021-03-24 2:33
 */
public interface NextPieceHandler {
    /**
     * Handle the next piece when a piece is placed.
     * @param nextPiece next piece to be displayed on the PieceBoard
     */
    void nextPiece(GamePiece nextPiece);
}
