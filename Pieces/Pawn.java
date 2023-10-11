package Pieces;

public class Pawn extends Piece {

    public Pawn(char color) {
        pieceType = 'p';
        moveType = 'f';
        pieceName = "Pawn";
        this.color = color;
    }
}