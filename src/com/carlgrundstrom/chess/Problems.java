package com.carlgrundstrom.chess;

import static com.carlgrundstrom.chess.BoardUtil.*;

import java.util.ArrayList;
import java.util.List;

public class Problems {
    private static ArrayList<Problem> problemList;

    public synchronized static List<Problem> getProblemList() {
        if (problemList == null) {
            problemList = new ArrayList<>();
            problemList.add(new CheckMateInTwoMoves_001());
            problemList.add(new CheckMateInTwoMoves_002());
            problemList.add(new CheckMateInTwoMoves_003());
            problemList.add(new CheckMateInThreeMoves_001());
            problemList.add(new CheckMateInThreeMoves_002());
            problemList.add(new PawnPromotionInThreeMoves_001());
            problemList.add(new EndGame_001());
        }
        return problemList;
    }

    public static class Problem {
        protected String name;
        protected int whoseTurn;
        protected int userColor;
        protected Board b;

        public Problem(String name) {
            this.name = name;
            whoseTurn = White;
            userColor = White;
            b = new Board();
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++)
                    b.pieces[i][j] = Space;
        }

        public String getName() {
            return name;
        }
    }

    public static class CheckMateInTwoMoves_001 extends Problem {
        public CheckMateInTwoMoves_001() {
            super("Checkmate in two moves #1");
            b.pieces[7][2] = BlackKing;
            b.pieces[7][3] = BlackRook;
            b.pieces[7][7] = BlackRook;
            b.pieces[6][0] = BlackPawn;
            b.pieces[6][1] = BlackPawn;
            b.pieces[6][4] = BlackBishop;
            b.pieces[6][5] = BlackPawn;
            b.pieces[6][6] = BlackPawn;
            b.pieces[6][7] = BlackPawn;
            b.pieces[5][2] = BlackPawn;
            b.pieces[5][4] = BlackBishop;
            b.pieces[5][5] = BlackQueen;
            b.pieces[3][4] = WhiteQueen;
            b.pieces[3][5] = WhiteBishop;
            b.pieces[2][2] = WhitePawn;
            b.pieces[2][3] = WhiteBishop;
            b.pieces[1][0] = WhitePawn;
            b.pieces[1][1] = WhitePawn;
            b.pieces[1][2] = WhitePawn;
            b.pieces[1][5] = WhitePawn;
            b.pieces[1][6] = WhitePawn;
            b.pieces[1][7] = WhitePawn;
            b.pieces[0][3] = WhiteRook;
            b.pieces[0][4] = WhiteRook;
            b.pieces[0][7] = WhiteKing;
        }
    }

    public static class CheckMateInTwoMoves_002 extends Problem {
        public CheckMateInTwoMoves_002() {
            super("Checkmate in two moves #2");
            whoseTurn = Black;
            userColor = Black;
            b.pieces[7][0] = BlackBishop;
            b.pieces[7][4] = BlackRook;
            b.pieces[7][6] = BlackKing;
            b.pieces[6][0] = BlackPawn;
            b.pieces[6][5] = BlackPawn;
            b.pieces[6][6] = BlackPawn;
            b.pieces[6][7] = BlackPawn;
            b.pieces[5][1] = BlackPawn;
            b.pieces[5][2] = BlackQueen;
            b.pieces[2][1] = WhitePawn;
            b.pieces[2][3] = WhitePawn;
            b.pieces[2][6] = WhitePawn;
            b.pieces[1][0] = WhitePawn;
            b.pieces[1][2] = WhitePawn;
            b.pieces[1][5] = WhiteQueen;
            b.pieces[1][6] = WhiteKnight;
            b.pieces[1][7] = WhitePawn;
            b.pieces[0][3] = WhiteBishop;
            b.pieces[0][6] = WhiteKing;
        }
    }

    public static class CheckMateInTwoMoves_003 extends Problem {
        public CheckMateInTwoMoves_003() {
            super("Checkmate in two moves #3");
            b.pieces[4][7] = WhiteQueen;
            b.pieces[3][3] = BlackKing;
            b.pieces[1][0] = WhiteKnight;
            b.pieces[1][3] = WhiteBishop;
            b.pieces[0][3] = WhiteKing;
        }
    }
    public static class CheckMateInThreeMoves_001 extends Problem {
        public CheckMateInThreeMoves_001() {
            super("Checkmate in three moves #1");
            whoseTurn = Black;
            userColor = Black;
            b.pieces[7][6] = BlackKing;
            b.pieces[6][2] = BlackQueen;
            b.pieces[6][5] = BlackPawn;
            b.pieces[6][7] = BlackPawn;
            b.pieces[5][4] = BlackKnight;
            b.pieces[5][6] = BlackPawn;
            b.pieces[3][0] = WhiteQueen;
            b.pieces[2][3] = WhitePawn;
            b.pieces[2][5] = WhitePawn;
            b.pieces[2][7] = WhitePawn;
            b.pieces[1][2] = WhiteRook;
            b.pieces[1][4] = WhiteKing;
            b.pieces[0][7] = BlackRook;
        }
    }

    public static class CheckMateInThreeMoves_002 extends Problem {
        public CheckMateInThreeMoves_002() {
            super("Checkmate in three moves #2");
            b.pieces[7][3] = BlackRook;
            b.pieces[7][5] = BlackKnight;
            b.pieces[7][7] = BlackKing;
            b.pieces[6][3] = WhitePawn;
            b.pieces[6][7] = BlackPawn;
            b.pieces[5][0] = BlackPawn;
            b.pieces[5][1] = BlackPawn;
            b.pieces[5][5] = BlackQueen;
            b.pieces[4][2] = BlackPawn;
            b.pieces[4][3] = WhiteQueen;
            b.pieces[4][4] = BlackPawn;
            b.pieces[3][0] = WhitePawn;
            b.pieces[3][2] = WhitePawn;
            b.pieces[3][6] = WhiteBishop;
            b.pieces[2][3] = BlackBishop;
            b.pieces[2][6] = WhiteRook;
            b.pieces[1][1] = WhitePawn;
            b.pieces[1][7] = WhitePawn;
            b.pieces[0][6] = WhiteKing;
        }
    }

    public static class PawnPromotionInThreeMoves_001 extends Problem {
        public PawnPromotionInThreeMoves_001() {
            super("Pawn promotion in three moves #2");
            b.fromString(
                    "bN      " +
                    "      n " +
                    "  PPp   " +
                    "        " +
                    "Q       " +
                    "       k" +
                    " PB     " +
                    "        "
            );
        }
    }

    public static class EndGame_001 extends Problem {
        public EndGame_001() {
            super("End Game #1");
            whoseTurn = White;
            userColor = Black;
            b.fromString(
                    "        " +
                    "     k  " +
                    "     p p" +
                    "    n p " +
                    "       r" +
                    "        " +
                    "      K " +
                    "        "
            );
        }
    }
}
