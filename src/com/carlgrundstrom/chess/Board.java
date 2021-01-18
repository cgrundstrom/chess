package com.carlgrundstrom.chess;

import java.util.LinkedList;
import static com.carlgrundstrom.chess.BoardUtil.*;

public class Board implements Cloneable {

    protected int[][] pieces;

    public Board() {
        pieces = new int[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                pieces[i][j] = Space;
    }

    public Board(int[][] piecesIn) {
        pieces = new int[8][8];
        for (int i = 0; i < 8; i++)
            System.arraycopy(piecesIn[i], 0, pieces[i], 0, 8);
    }

    public Board(int[][] piecesIn, boolean noCopy) {
        pieces = piecesIn;
    }

    public Board clone() {
        Board b = new Board();
        for (int i = 0; i < 8; i++)
            System.arraycopy(pieces[i], 0, b.pieces[i], 0, 8);
        return b;
    }

    public int initializeStandardLayout() {
        pieces[0][0] = WhiteRook;
        pieces[0][1] = WhiteKnight;
        pieces[0][2] = WhiteBishop;
        pieces[0][3] = WhiteQueen;
        pieces[0][4] = WhiteKing;
        pieces[0][5] = WhiteBishop;
        pieces[0][6] = WhiteKnight;
        pieces[0][7] = WhiteRook;

        for (int i = 0; i < 8; i++)
            pieces[1][i] = WhitePawn;

        pieces[7][0] = BlackRook;
        pieces[7][1] = BlackKnight;
        pieces[7][2] = BlackBishop;
        pieces[7][3] = BlackQueen;
        pieces[7][4] = BlackKing;
        pieces[7][5] = BlackBishop;
        pieces[7][6] = BlackKnight;
        pieces[7][7] = BlackRook;

        for (int i = 0; i < 8; i++)
            pieces[6][i] = BlackPawn;

        for (int i = 2; i < 6; i++)
            for (int j = 0; j < 8; j++)
                pieces[i][j] = Space;
        return White;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("   a b c d e f g h\n");
        for (int i = 7; i >= 0; i--) {
            b.append(i + 1).append(" ");
            for (int j = 0; j < 8; j++)
                b.append(" ").append(printPieceShort(pieces[i][j]));
            b.append("  ").append(i + 1).append('\n');
        }
        b.append("   a b c d e f g h");
        return b.toString();
    }

    public int hashCode() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 7; j++)
                b.append(printPieceShort(pieces[i][j]));
        return b.toString().hashCode();
    }

    public boolean equals(Object object) {
        if (!(object instanceof Board))
            return false;
        Board b = (Board)object;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (b.pieces[i][j] != pieces[i][j])
                    return false;
        return true;
    }

    public void fromString(String s) {
        int k = 0;
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                int piece = Integer.MAX_VALUE;
                while (piece == Integer.MAX_VALUE) {
                    char ch = s.charAt(k++);
                    switch (ch) {
                        case ' ':
                            piece = Space;
                            break;
                        case 'R':
                            piece = WhiteRook;
                            break;
                        case 'N':
                            piece = WhiteKnight;
                            break;
                        case 'B':
                            piece = WhiteBishop;
                            break;
                        case 'Q':
                            piece = WhiteQueen;
                            break;
                        case 'K':
                            piece = WhiteKing;
                            break;
                        case 'P':
                            piece = WhitePawn;
                            break;
                        case 'r':
                            piece = BlackRook;
                            break;
                        case 'n':
                            piece = BlackKnight;
                            break;
                        case 'b':
                            piece = BlackBishop;
                            break;
                        case 'q':
                            piece = BlackQueen;
                            break;
                        case 'k':
                            piece = BlackKing;
                            break;
                        case 'p':
                            piece = BlackPawn;
                            break;
                    }
                }
                pieces[i][j] = piece;
            }
        }
    }
}
