package com.carlgrundstrom.chess;

import javax.swing.JProgressBar;
import java.util.LinkedList;

public class OriginalEngine extends Engine {

    public static final int WhiteRook = 5000;
    public static final int WhiteKnight = 3000;
    public static final int WhiteBishop = 3500;
    public static final int WhiteQueen = 9000;
    public static final int WhiteKing = 1000000;
    public static final int WhitePawn = 1000;

    private static final int WhiteWorst = -10000000;

    public static final int BlackRook = -5000;
    public static final int BlackKnight = -3000;
    public static final int BlackBishop = -3500;
    public static final int BlackQueen = -9000;
    public static final int BlackKing = -1000000;
    public static final int BlackPawn = -1000;

    public static final int Space = 0;

    private static final int BlackWorst = 10000000;

    private static final int knightMoves[][] = {
            {1, 2},
            {2, 1},

            {-1, 2},
            {2, -1},

            {1, -2},
            {-2, 1},

            {-1, -2},
            {-2, -1}
    };

    private static final int kingMoves[][] = {
            {-1, -1},
            {-1, 0},
            {-1, 1},

            {0, -1},
            {0, 1},

            {1, -1},
            {1, 0},
            {1, 1}
    };

    protected long branchLimit;
    private int rating;
    private int whiteRating;
    private int blackRating;
    public int pieces[][];

    private static long baseDepth = 1000L * 1000L;

    public OriginalEngine(ProgressBarInterface progressBar) {
        super("Original", progressBar);
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        switch (difficulty) {
            case Easy:
                branchLimit = baseDepth / 40L / 40L;
                break;
            case Medium:
                branchLimit = baseDepth / 40L;
                break;
            case Hard:
                branchLimit = baseDepth;
                break;
            case ExtraHard:
                branchLimit = baseDepth * 40L;
                break;
        }
    }

    public Move getWhiteMove(GameHistory gameHistory, Board board, LinkedList<RatedMove> bestMoves) {
        totalMoveCount = 0;
        copyBoard(board);
        Move bestMove = null;
        int bestRating = WhiteWorst;
        LinkedList<Move> moves = getWhiteMoves();
        int numMoves = moves.size();
        if (numMoves == 0)
            return null;
        for (Move move : moves) {
            long depth = makeMove(move, 1, numMoves);
            int moveRating = getWhiteRating(depth);
            unMakeMove(move);
            if (moveRating > bestRating) {
                bestRating = moveRating;
                bestMove = move;
            }
        }
        if (bestMove != null)
            bestMove.toPiece = board.pieces[bestMove.toRow][bestMove.toColumn];
        return bestMove;
    }

    public Move getBlackMove(GameHistory gameHistory, Board board, LinkedList<RatedMove> bestMoves) {
        totalMoveCount = 0;
        copyBoard(board);
        Move bestMove = null;
        int bestRating = BlackWorst;
        LinkedList<Move> moves = getBlackMoves();
        int numMoves = moves.size();
        if (numMoves == 0)
            return null;
        for (Move move : moves) {
            long depth = makeMove(move, 1, numMoves);
            int moveRating = getBlackRating(depth);
            unMakeMove(move);
            if (moveRating < bestRating) {
                bestRating = moveRating;
                bestMove = move;
            }
        }
        if (bestMove != null)
            bestMove.toPiece = board.pieces[bestMove.toRow][bestMove.toColumn];
        return bestMove;
    }

    private int getWhiteRating(long depth) {
//        System.out.println( "getWhiteRating( depth = " + depth + " )" );
//        print();
//        System.out.println( "rating = " + rating );
//        Chess.in.readLine();

        LinkedList<Move> moves = getBlackMoves();
        int numMoves = moves.size();
//        System.out.println( "numMoves = " + numMoves );
        if (depth > branchLimit) {
            int moveRating = rating - moves.size();
            if (rating > 0 && whiteRating != 0) {
                moveRating += 1000 + 1000 * blackRating / whiteRating;
            }
//            System.out.println( "moveRating = " + moveRating );
            return moveRating;
        }

        int bestRating = BlackWorst;
        for (Move move : moves) {
            long depth2 = makeMove(move, depth, numMoves);
            int moveRating = getBlackRating(depth2);
            unMakeMove(move);

            if (moveRating < bestRating) {
                bestRating = moveRating;
            }
        }

        return bestRating;
    }

    private int getBlackRating(long depth) {
//        System.out.println( "getBlackRating( depth = " + depth + " )" );
//        print();
//        System.out.println( "rating = " + rating );
//        Chess.in.readLine();

        LinkedList<Move> moves = getWhiteMoves();
        int numMoves = moves.size();
//        System.out.println( "numMoves = " + numMoves );
        if (depth > branchLimit) {
            int moveRating = rating + moves.size();
            if (rating > 0 && blackRating != 0) {
//                System.out.println( "rating: " + rating );
//                System.out.println( "moveRating: " + moveRating );
//                System.out.println( "whiteRating: " + whiteRating );
//                System.out.println( "blackRating: " + blackRating );
//                System.out.println( "adjust: " + ( -1 * ( 1000 + 1000 * whiteRating / blackRating ) ) );
//                Chess.in.readLine();
                moveRating -= 1000 + 1000 * whiteRating / blackRating;
            }
//            System.out.println( "moveRating = " + moveRating );
            return moveRating;
        }

        int bestRating = WhiteWorst;
        for (Move move : moves) {
            long depth2 = makeMove(move, depth, numMoves);
            int moveRating = getWhiteRating(depth2);
            unMakeMove(move);
            if (moveRating > bestRating) {
                bestRating = moveRating;
            }
        }

        return bestRating;
    }

    private void copyBoard(Board board) {
        this.pieces = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int piece;
                switch (board.pieces[i][j]) {
                    case WhitePawn:
                        piece = WhitePawn;
                        break;
                    case WhiteKnight:
                        piece = WhiteKnight;
                        break;
                    case WhiteBishop:
                        piece = WhiteBishop;
                        break;
                    case WhiteRook:
                        piece = WhiteRook;
                        break;
                    case WhiteQueen:
                        piece = WhiteQueen;
                        break;
                    case WhiteKing:
                        piece = WhiteKing;
                        break;
                    case BlackPawn:
                        piece = BlackPawn;
                        break;
                    case BlackKnight:
                        piece = BlackKnight;
                        break;
                    case BlackBishop:
                        piece = BlackBishop;
                        break;
                    case BlackRook:
                        piece = BlackRook;
                        break;
                    case BlackQueen:
                        piece = BlackQueen;
                        break;
                    case BlackKing:
                        piece = BlackKing;
                        break;
                    default:
                        piece = Space;
                }
                this.pieces[i][j] = piece;
            }
        }
        initializeRating();
    }

    private void initializeRating() {
        rating = 0;
        whiteRating = 0;
        blackRating = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int piece = pieces[i][j];
                if (piece > 0 && piece != WhiteKing) {
                    rating += piece;
                    whiteRating += piece;
                }
                else if (piece < 0 && piece != BlackKing) {
                    rating += piece;
                    blackRating += piece;
                }
            }
        }
    }

    private long makeMove(Move move, long depth, int moves) {
        totalMoveCount++;
        pieces[move.toRow][move.toColumn] = pieces[move.fromRow][move.fromColumn];
        pieces[move.fromRow][move.fromColumn] = Space;
        int piece = move.toPiece;
        if (moves < 6) {
            moves = 6;
        }
        if (piece == 0) {
            depth *= moves;
        }
        else if (piece > 0) {
            rating -= piece;
            if (piece == WhiteKing) {
                depth = branchLimit + 1;
            }
            else {
                whiteRating -= piece;
                depth *= moves / 3;
            }
        }
        else {
            rating -= piece;
            if (piece == BlackKing) {
                depth = branchLimit + 1;
            }
            else {
                blackRating -= piece;
                depth *= moves / 3;
            }
        }
        return depth;
    }

    private void unMakeMove(Move move) {
        pieces[move.fromRow][move.fromColumn] = pieces[move.toRow][move.toColumn];
        int piece = move.toPiece;
        pieces[move.toRow][move.toColumn] = piece;
        if (piece > 0) {
            rating += piece;
            if (piece != WhiteKing) {
                whiteRating += piece;
            }
        }
        else if (piece < 0) {
            rating += piece;
            if (piece != BlackKing) {
                blackRating += piece;
            }
        }
    }

    private LinkedList<Move> getWhiteMoves() {
        int i2, j2;
        int piece2;
        LinkedList<Move> moves = new LinkedList<Move>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int piece = pieces[i][j];
                if (piece <= 0) {
                    continue;
                }
                switch (piece) {
                    case WhiteRook:
                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
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
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }
                        break;

                    case WhiteQueen:
                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 > 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 < 0) {
                                break;
                            }
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
                        break;
                }
            }
        }
        return moves;
    }

    private LinkedList<Move> getBlackMoves() {
        int i2, j2;
        int piece2;
        LinkedList<Move> moves = new LinkedList<Move>();
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
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
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
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }
                        break;

                    case BlackQueen:
                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 < 0) {
                                break;
                            }
                            moves.add(new Move(i, j, piece, i2, j2, piece2));
                            if (piece2 > 0) {
                                break;
                            }
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
                        break;
                }
            }
        }
        return moves;
    }

    public void print() {
        System.out.println();
        System.out.println("   1 2 3 4 5 6 7 8");
        System.out.println();
        for (int i = 7; i >= 0; i--) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < 8; j++) {
                char ch;
                switch (pieces[i][j]) {
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
                System.out.print(" " + ch);
            }
            System.out.println("  " + (i + 1));
        }
        System.out.println();
        System.out.println("   1 2 3 4 5 6 7 8");
    }
}
