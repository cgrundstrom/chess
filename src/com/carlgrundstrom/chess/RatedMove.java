package com.carlgrundstrom.chess;

public class RatedMove extends Move {
    public int rating;

    public RatedMove(Move move, int rating) {
        super(move.fromRow, move.fromColumn, move.fromPiece, move.toRow, move.toColumn, move.toPiece);
        this.rating = rating;
    }
}
