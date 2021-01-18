package com.carlgrundstrom.chess;

import static com.carlgrundstrom.chess.BoardUtil.*;

public class Move {
    public int fromColumn;
    public int fromRow;
    public int fromPiece;
    public int toColumn;
    public int toRow;
    public int toPiece;
    public boolean pawnPromotion;

    public Move(int fromRow, int fromColumn, int fromPiece, int toRow, int toColumn, int toPiece) {
        this.fromRow = fromRow;
        this.fromColumn = fromColumn;
        this.fromPiece = fromPiece;
        this.toRow = toRow;
        this.toColumn = toColumn;
        this.toPiece = toPiece;
        if (fromPiece == WhitePawn) {
            if (toRow == 7)
                pawnPromotion = true;
        }
        else if (fromPiece == BlackPawn) {
            if (toRow == 0)
                pawnPromotion = true;
        }
    }

    public String toString() {
        return BoardUtil.printPiece(fromPiece) + " moves from " + (char)('a' + fromColumn) + (fromRow + 1) + " to " + (char)('a' + toColumn) + (toRow + 1) +
                (toPiece == Space ? "" : ", captures " + BoardUtil.printPiece(toPiece));
    }

    public boolean equals(Object o) {
        if (!(o instanceof Move))
            return false;
        Move move2 = (Move)o;
        return fromColumn == move2.fromColumn
                && fromRow == move2.fromRow
                && toColumn == move2.toColumn
                && toRow == move2.toRow
                && fromPiece == move2.fromPiece
                && toPiece == move2.toPiece
                && pawnPromotion == move2.pawnPromotion;
    }

    public int hashCode() {
        Integer hash = fromColumn
                & (fromRow << 4)
                & ((fromPiece + 6) << 8)
                & (toColumn << 12)
                & (toRow << 16)
                & ((toPiece + 6) << 20);
        return hash.hashCode();
    }
}
