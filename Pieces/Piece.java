package Pieces;

import javax.swing.ImageIcon;

public class Piece {
    public String pieceName;
    public char pieceType;
    public char moveType;
    public char color;
    public boolean isKillable;

    // Some pieces have rules with having moved
    public boolean hasNotMoved = true;

    // Pawn Variables
    public boolean lastMovedTwice = false;

    public ImageIcon getSprite() {
        ImageIcon sprite = new ImageIcon("src\\Pieces\\" + color + this.pieceName + ".png");
        return sprite;
    }
}