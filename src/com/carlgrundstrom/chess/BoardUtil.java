package com.carlgrundstrom.chess;

import java.util.LinkedList;

public class BoardUtil implements Cloneable {
    public static final int White = 1;
    public static final int Black = -1;

    protected int[][] pieces;

    protected static final int Space = 0;
    protected static final int Pawn = 1;
    protected static final int Knight = 2;
    protected static final int Bishop = 3;
    protected static final int Rook = 4;
    protected static final int Queen = 5;
    protected static final int King = 6;

    protected static final int WhitePawn = Pawn;
    protected static final int WhiteKnight = Knight;
    protected static final int WhiteBishop = Bishop;
    protected static final int WhiteRook = Rook;
    protected static final int WhiteQueen = Queen;
    protected static final int WhiteKing = King;

    protected static final int BlackPawn = -Pawn;
    protected static final int BlackKnight = -Knight;
    protected static final int BlackBishop = -Bishop;
    protected static final int BlackRook = -Rook;
    protected static final int BlackQueen = -Queen;
    protected static final int BlackKing = -King;

    public static String printPiece(int piece) {
        String s;
        switch (piece) {
            case Space: s = "Space"; break;
            case WhitePawn: s = "White Pawn"; break;
            case WhiteKnight: s = "White Knight"; break;
            case WhiteBishop: s = "White Bishop"; break;
            case WhiteRook: s = "White Rook"; break;
            case WhiteQueen: s = "White Queen"; break;
            case WhiteKing: s = "White King"; break;
            case BlackPawn: s = "Black Pawn"; break;
            case BlackKnight: s = "Black Knight"; break;
            case BlackBishop: s = "Black Bishop"; break;
            case BlackRook: s = "Black Rook"; break;
            case BlackQueen: s = "Black Queen"; break;
            case BlackKing: s = "Black King"; break;
            default: s = "invalid (" + piece + ")";
        }
        return s;
    }

    public static String printPieceSimple(int piece) {
        String s;
        switch (piece) {
            case Space: s = "Space"; break;
            case WhitePawn: s = "Pawn"; break;
            case WhiteKnight: s = "Knight"; break;
            case WhiteBishop: s = "Bishop"; break;
            case WhiteRook: s = "Rook"; break;
            case WhiteQueen: s = "Queen"; break;
            case WhiteKing: s = "King"; break;
            case BlackPawn: s = "Pawn"; break;
            case BlackKnight: s = "Knight"; break;
            case BlackBishop: s = "Bishop"; break;
            case BlackRook: s = "Rook"; break;
            case BlackQueen: s = "Queen"; break;
            case BlackKing: s = "King"; break;
            default: s = "invalid (" + piece + ")";
        }
        return s;
    }

    public static String printPieceShort(int piece) {
        char ch;
        switch (piece) {
            case WhiteRook:
                ch = 'R';
                break;
            case WhiteKnight:
                ch = 'N';
                break;
            case WhiteBishop:
                ch = 'B';
                break;
            case WhiteQueen:
                ch = 'Q';
                break;
            case WhiteKing:
                ch = 'K';
                break;
            case WhitePawn:
                ch = 'P';
                break;

            case BlackRook:
                ch = 'r';
                break;
            case BlackKnight:
                ch = 'n';
                break;
            case BlackBishop:
                ch = 'b';
                break;
            case BlackQueen:
                ch = 'q';
                break;
            case BlackKing:
                ch = 'k';
                break;
            case BlackPawn:
                ch = 'p';
                break;

            case Space:
                ch = '-';
                break;

            default:
                ch = 'X';
        }
        return String.valueOf(ch);
    }

    private static final int[][] knightMoves = {
            {1, 2},
            {2, 1},

            {-1, 2},
            {2, -1},

            {1, -2},
            {-2, 1},

            {-1, -2},
            {-2, -1}
    };

    private static final int[][] kingMoves = {
            {-1, -1},
            {-1, 0},
            {-1, 1},

            {0, -1},
            {0, 1},

            {1, -1},
            {1, 0},
            {1, 1}
    };

    public static void makeMove(int[][] pieces, Move move) {
        int piece = pieces[move.fromRow][move.fromColumn];
        if (move.pawnPromotion) {
            if (piece > 0)
                piece = WhiteQueen;
            else
                piece = BlackQueen;
        }
        else {
            switch (piece) {
                // Castle
                case WhiteKing:
                    if (move.fromColumn == 4) {
                        if (move.toColumn == 6) {
                            pieces[0][5] = WhiteRook;
                            pieces[0][7] = Space;
                        }
                        else if (move.toColumn == 2) {
                            pieces[0][3] = WhiteRook;
                            pieces[0][0] = Space;
                        }
                    }
                    break;
                case BlackKing:
                    if (move.fromColumn == 4) {
                        if (move.toColumn == 6) {
                            pieces[7][5] = BlackRook;
                            pieces[7][7] = Space;
                        }
                        else if (move.toColumn == 2) {
                            pieces[7][3] = BlackRook;
                            pieces[7][0] = Space;
                        }
                    }
                    break;
            }
        }
        pieces[move.toRow][move.toColumn] = piece;
        pieces[move.fromRow][move.fromColumn] = Space;
    }

    public static void unMakeMove(int[][] pieces, Move move) {
        int piece = pieces[move.toRow][move.toColumn];
        if (move.pawnPromotion)
            piece = piece > 0 ? WhitePawn : BlackPawn;
        else {
            switch (piece) {
                // Castle
                case WhiteKing:
                    if (move.fromColumn == 4) {
                        if (move.toColumn == 6) {
                            pieces[0][5] = Space;
                            pieces[0][7] = WhiteRook;
                        }
                        else if (move.toColumn == 2) {
                            pieces[0][3] = Space;
                            pieces[0][0] = WhiteRook;
                        }
                    }
                    break;
                case BlackKing:
                    if (move.fromColumn == 4) {
                        if (move.toColumn == 6) {
                            pieces[7][5] = Space;
                            pieces[7][7] = BlackRook;
                        }
                        else if (move.toColumn == 2) {
                            pieces[7][3] = Space;
                            pieces[7][0] = BlackRook;
                        }
                    }
                    break;
            }
        }

        pieces[move.fromRow][move.fromColumn] = piece;
        pieces[move.toRow][move.toColumn] = move.toPiece;
    }

    public static LinkedList<Move> getWhiteMoves(int[][] pieces) {
        int i2, j2;
        int piece2;
        LinkedList<Move> moves = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int piece = pieces[i][j];
                if (piece <= 0)
                    continue;
                switch (piece) {
                    case WhiteRook:
                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }
                        break;

                    case WhiteKnight:
                        for (int x = 0; x < 8; x++) {
                            i2 = i + knightMoves[x][0];
                            j2 = j + knightMoves[x][1];
                            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                                piece2 = pieces[i2][j2];
                                if (piece2 <= 0) {
                                    moves.add(new Move(i, j, piece, i2, j2, piece2));
                                }
                            }
                        }
                        break;

                    case WhiteBishop:
                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }
                        break;

                    case WhiteQueen:
                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0)
                                break;
                        }
                        break;

                    case WhiteKing:
                        for (int x = 0; x < 8; x++) {
                            i2 = i + kingMoves[x][0];
                            j2 = j + kingMoves[x][1];
                            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                                piece2 = pieces[i2][j2];
                                if (piece2 <= 0) {
                                    moves.add(new Move(i, j, piece, i2, j2, piece2));
                                }
                            }
                        }

                        // Castle
                        if (i == 0 && j == 4) {
                            if (pieces[0][5] == Space && pieces[0][6] == Space && pieces[0][7] == WhiteRook)
                                moves.add(new Move(i, j, piece, 0, 6, Space));
                            if (pieces[0][3] == Space && pieces[0][2] == Space && pieces[0][1] == Space && pieces[0][0] == WhiteRook)
                                moves.add(new Move(i, j, piece, 0, 2, Space));
                        }
                        break;

                    case WhitePawn:
                        // Start move
                        if (i == 1 && pieces[2][j] == Space && pieces[3][j] == Space) {
                            moves.add(new Move(i, j, piece, 3, j, Space));
                        }

                        // Move one forward
                        if (i < 7 && pieces[i + 1][j] == Space) {
                            moves.add(new Move(i, j, piece, i + 1, j, Space));
                        }

                        // Attack diagonal left
                        if (i < 7 && j > 0) {
                            piece2 = pieces[i + 1][j - 1];
                            if (piece2 < 0) {
                                moves.add(new Move(i, j, piece, i + 1, j - 1, piece2));
                            }
                        }

                        // Attack diagonal right
                        if (i < 7 && j < 7) {
                            piece2 = pieces[i + 1][j + 1];
                            if (piece2 < 0) {
                                moves.add(new Move(i, j, piece, i + 1, j + 1, piece2));
                            }
                        }

                        // Pawn promotion
                        if (i == 6 && pieces[7][j] == Space) {
                            Move move = new Move(6, j, piece, 7, j, Space);
                            move.pawnPromotion = true;
                            moves.add(move);
                        }
                        break;
                }
            }
        }
        return moves;
    }

    public static LinkedList<Move> getBlackMoves(int[][]pieces) {
        int i2, j2;
        int piece2;
        LinkedList<Move> moves = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int piece = pieces[i][j];
                if (piece >= 0) {
                    continue;
                }
                switch (piece) {
                    case BlackRook:
                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }
                        break;

                    case BlackKnight:
                        for (int x = 0; x < 8; x++) {
                            i2 = i + knightMoves[x][0];
                            j2 = j + knightMoves[x][1];
                            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                                piece2 = pieces[i2][j2];
                                if (piece2 >= 0) {
                                    moves.add(new Move(i, j, piece, i2, j2, piece2));
                                }
                            }
                        }
                        break;

                    case BlackBishop:
                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }
                        break;

                    case BlackQueen:
                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0)
                                break;
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0)
                                break;
                        }
                        break;

                    case BlackKing:
                        for (int x = 0; x < 8; x++) {
                            i2 = i + kingMoves[x][0];
                            j2 = j + kingMoves[x][1];
                            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                                piece2 = pieces[i2][j2];
                                if (piece2 >= 0) {
                                    moves.add(new Move(i, j, piece, i2, j2, piece2));
                                }
                            }
                        }

                        // Castle
                        if (i == 7 && j == 4) {
                            if (pieces[7][5] == Space && pieces[7][6] == Space && pieces[7][7] == BlackRook)
                                moves.add(new Move(i, j, piece, 7, 6, Space));
                            if (pieces[7][3] == Space && pieces[7][2] == Space && pieces[7][1] == Space && pieces[7][0] == BlackRook)
                                moves.add(new Move(i, j, piece, 7, 2, Space));
                        }
                        break;

                    case BlackPawn:
                        // Start move
                        if (i == 6 && pieces[5][j] == Space && pieces[4][j] == Space) {
                            moves.add(new Move(i, j, piece, 4, j, Space));
                        }

                        // Move one forward
                        if (i > 0 && pieces[i - 1][j] == Space) {
                            moves.add(new Move(i, j, piece, i - 1, j, Space));
                        }

                        // Attack diagonal right
                        if (i > 0 && j > 0) {
                            piece2 = pieces[i - 1][j - 1];
                            if (piece2 > 0) {
                                moves.add(new Move(i, j, piece, i - 1, j - 1, piece2));
                            }
                        }

                        // Attack diagonal left
                        if (i > 0 && j < 7) {
                            piece2 = pieces[i - 1][j + 1];
                            if (piece2 > 0) {
                                moves.add(new Move(i, j, piece, i - 1, j + 1, piece2));
                            }
                        }

                        // Pawn promotion
                        if (i == 1 && pieces[0][j] == Space) {
                            Move move = new Move(1, j, piece, 0, j, Space);
                            move.pawnPromotion = true;
                            moves.add(move);
                        }
                        break;
                }
            }
        }
        return moves;
    }

    public static LinkedList<Move> getWhiteAttackingMoves(int[][]pieces, int i, int j) {
        LinkedList<Move> moves = new LinkedList<>();
        int piece = pieces[i][j];
        int i2, j2;
        int piece2;

        // North
        for (i2 = i + 1; i2 < 8; i2++) {
            piece2 = pieces[i2][j];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case WhiteRook:
                case WhiteQueen:
                    moves.add(new Move(i2, j, piece2, i, j, piece));
                    break;
                case WhiteKing:
                    if (i2 == i + 1)
                        moves.add(new Move(i2, j, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // South
        for (i2 = i - 1; i2 >= 0; i2--) {
            piece2 = pieces[i2][j];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case WhiteRook:
                case WhiteQueen:
                    moves.add(new Move(i2, j, piece2, i, j, piece));
                    break;
                case WhiteKing:
                    if (i2 == i - 1)
                        moves.add(new Move(i2, j, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // East
        for (j2 = j - 1; j2 >= 0; j2--) {
            piece2 = pieces[i][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case WhiteRook:
                case WhiteQueen:
                    moves.add(new Move(i, j2, piece2, i, j, piece));
                    break;
                case WhiteKing:
                    if (j2 == j - 1)
                        moves.add(new Move(i, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // West
        for (j2 = j + 1; j2 < 8; j2++) {
            piece2 = pieces[i][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case WhiteRook:
                case WhiteQueen:
                    moves.add(new Move(i, j2, piece2, i, j, piece));
                    break;
                case WhiteKing:
                    if (j2 == j + 1)
                        moves.add(new Move(i, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // NE
        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
            piece2 = pieces[i2][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case WhiteBishop:
                case WhiteQueen:
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
                case WhiteKing:
                    if (i2 == i + 1)
                        moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // SE
        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
            piece2 = pieces[i2][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case WhiteBishop:
                case WhiteQueen:
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
                case WhiteKing:
                    if (i2 == i - 1)
                        moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // NW
        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
            piece2 = pieces[i2][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case WhiteBishop:
                case WhiteQueen:
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
                case WhiteKing:
                    if (i2 == i + 1)
                        moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // SW
        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
            piece2 = pieces[i2][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case WhiteBishop:
                case WhiteQueen:
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
                case WhiteKing:
                    if (i2 == i - 1)
                        moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        if (i > 1) {
            if (j > 0 && pieces[i - 1][j - 1] == WhitePawn)
                moves.add(new Move(i - 1, j - 1, WhitePawn, i, j, piece));
            if (j < 7 && pieces[i - 1][j + 1] == WhitePawn)
                moves.add(new Move(i - 1, j + 1, WhitePawn, i, j, piece));
        }

        for (int x = 0; x < 8; x++) {
            i2 = i + knightMoves[x][0];
            j2 = j + knightMoves[x][1];
            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                piece2 = pieces[i2][j2];
                if (piece2 == WhiteKnight)
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
            }
        }

        return moves;
    }

    public static LinkedList<Move> getBlackAttackingMoves(int[][]pieces, int i, int j) {
        LinkedList<Move> moves = new LinkedList<>();
        int piece = pieces[i][j];
        int i2, j2;
        int piece2;

        // North
        for (i2 = i + 1; i2 < 8; i2++) {
            piece2 = pieces[i2][j];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case BlackRook:
                case BlackQueen:
                    moves.add(new Move(i2, j, piece2, i, j, piece));
                    break;
                case BlackKing:
                    if (i2 == i + 1)
                        moves.add(new Move(i2, j, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // South
        for (i2 = i - 1; i2 >= 0; i2--) {
            piece2 = pieces[i2][j];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case BlackRook:
                case BlackQueen:
                    moves.add(new Move(i2, j, piece2, i, j, piece));
                    break;
                case BlackKing:
                    if (i2 == i - 1)
                        moves.add(new Move(i2, j, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // East
        for (j2 = j - 1; j2 >= 0; j2--) {
            piece2 = pieces[i][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case BlackRook:
                case BlackQueen:
                    moves.add(new Move(i, j2, piece2, i, j, piece));
                    break;
                case BlackKing:
                    if (j2 == j - 1)
                        moves.add(new Move(i, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // West
        for (j2 = j + 1; j2 < 8; j2++) {
            piece2 = pieces[i][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case BlackRook:
                case BlackQueen:
                    moves.add(new Move(i, j2, piece2, i, j, piece));
                    break;
                case BlackKing:
                    if (j2 == j + 1)
                        moves.add(new Move(i, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // NE
        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
            piece2 = pieces[i2][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case BlackBishop:
                case BlackQueen:
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
                case BlackKing:
                    if (i2 == i + 1)
                        moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // SE
        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
            piece2 = pieces[i2][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case BlackBishop:
                case BlackQueen:
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
                case BlackKing:
                    if (i2 == i - 1)
                        moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // NW
        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
            piece2 = pieces[i2][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case BlackBishop:
                case BlackQueen:
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
                case BlackKing:
                    if (i2 == i + 1)
                        moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        // SW
        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
            piece2 = pieces[i2][j2];
            if (piece2 == Space)
                continue;
            switch (piece2) {
                case BlackBishop:
                case BlackQueen:
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
                case BlackKing:
                    if (i2 == i - 1)
                        moves.add(new Move(i2, j2, piece2, i, j, piece));
                    break;
            }
            break;
        }

        if (i < 6) {
            if (j > 0 && pieces[i + 1][j - 1] == BlackPawn)
                moves.add(new Move(i + 1, j - 1, BlackPawn, i, j, piece));
            if (j < 7 && pieces[i + 1][j + 1] == BlackPawn)
                moves.add(new Move(i + 1, j + 1, BlackPawn, i, j, piece));
        }

        for (int x = 0; x < 8; x++) {
            i2 = i + knightMoves[x][0];
            j2 = j + knightMoves[x][1];
            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                piece2 = pieces[i2][j2];
                if (piece2 == BlackKnight)
                    moves.add(new Move(i2, j2, piece2, i, j, piece));
            }
        }

        return moves;
    }

    public static boolean isValidMove(int[][] pieces, int color, Move move) {
        LinkedList<Move> moves = color == White ? getWhiteMoves(pieces) : getBlackMoves(pieces);
        return moves.contains(move);
    }

    public static boolean wouldKingBeInCheck(int[][] pieces, int color, Move move) {
        makeMove(pieces, move);
        boolean result = isKingInCheck(pieces, color);
        unMakeMove(pieces, move);
        return result;
    }

    public static boolean isKingInCheck(int[][] pieces, int color) {
        LinkedList<Move> moves2 = color == White ? getBlackMoves(pieces) : getWhiteMoves(pieces);
        boolean result = false;
        for (Move move2 : moves2) {
            if (color == White && move2.toPiece == WhiteKing
                    || color == Black && move2.toPiece == BlackKing) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean isStaleMated(int[][] pieces, int color) {
        if (isKingInCheck(pieces, color))
            return false;
        LinkedList<Move> moves = color == White ? getWhiteMoves(pieces) : getBlackMoves(pieces);

        for (Move move : moves) {
            makeMove(pieces, move);
            LinkedList<Move> moves2 = color == White ? getBlackMoves(pieces) : getWhiteMoves(pieces);
            boolean capturedKing = false;
            for (Move move2 : moves2) {
                if (color == White && move2.toPiece == WhiteKing
                        || color == Black && move2.toPiece == BlackKing) {
                    capturedKing = true;
                    break;
                }
            }
            unMakeMove(pieces, move);
            if (!capturedKing)
                return false;
        }
        return true;
    }

    public static boolean isDrawByRepetition(GameHistory gameHistory, int[][] pieces, int color) {
        LinkedList<Move> moves = color == White ? getWhiteMoves(pieces) : getBlackMoves(pieces);
        for (Move move : moves) {
            makeMove(pieces, move);
            boolean repeatMove = isKingInCheck(pieces, color) || gameHistory.getBoardCount(new Board(pieces)) >= 2;
            unMakeMove(pieces, move);
            if (!repeatMove)
                return false;
        }
        return true;
    }

    public static boolean isCheckMated(int[][] pieces, int color) {
        if (!isKingInCheck(pieces, color))
            return false;
        LinkedList<Move> moves = color == White ? getWhiteMoves(pieces) : getBlackMoves(pieces);
        for (Move move : moves) {
            makeMove(pieces, move);
            LinkedList<Move> moves2 = color == White ? getBlackMoves(pieces) : getWhiteMoves(pieces);
            boolean capturedKing = false;
            for (Move move2 : moves2) {
                if (color == White && move2.toPiece == WhiteKing
                        || color == Black && move2.toPiece == BlackKing) {
                    capturedKing = true;
                    break;
                }
            }
            unMakeMove(pieces, move);
            if (!capturedKing)
                return false;
        }
        return true;
    }
}
