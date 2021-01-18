package com.carlgrundstrom.chess;

import static com.carlgrundstrom.chess.BoardUtil.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.ArrayList;

public class StandardEngine extends Engine {
    public static final int RookValue = 50000;
    public static final int KnightValue = 30000;
    public static final int BishopValue = 35000;
    public static final int QueenValue = 90000;
    public static final int PawnValue = 10000;

    private static final int WhiteWins = 10 * 1000 * 1000;
    private static final int WhiteWorst = Integer.MIN_VALUE;

    public static int[][] protectValues;
    public static int[][] attackValues;

    private static final int BlackWins = -10 * 1000 * 1000;
    private static final int BlackWorst = Integer.MAX_VALUE;

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

    private static long baseBranchDepth = 50L * 1000L * 1000L;

    private long depthLimit;
    protected long branchLimit;

    private int pieces[][] = new int[8][8];
    private HashMap<Move, Integer> moveCache = new HashMap<Move, Integer>();
    private HashMap<Move, Integer> futureMoveCache = new HashMap<Move, Integer>();

//    private LinkedList<Move> interestingMoves;

    private Random random = new Random();

    public StandardEngine(ProgressBarInterface progressBar) {
        super("Standard", progressBar);
//        interestingMoves = new LinkedList<Move>();
//        interestingMoves.add(new Move(6, 2, 1, 7, Space));
//        interestingMoves.add(new Move(1, 4, 2, 4, Space));
//        interestingMoves.add(new Move(0, 7, 0, 4, Space));
//        interestingMoves.add(new Move(1, 2, 1, 4, Space));
//        interestingMoves.add(new Move(1, 7, 1, 4, WhiteRook));

        protectValues = new int[7][7];

        protectValues[Pawn][Space] = 10;
        protectValues[Pawn][Pawn] = 40;
        protectValues[Pawn][Knight] = 30;
        protectValues[Pawn][Bishop] = 30;
        protectValues[Pawn][Rook] = 20;
        protectValues[Pawn][Queen] = 10;

        protectValues[Knight][Space] = 10;
        protectValues[Knight][Pawn] = 5;
        protectValues[Knight][Knight] = 15;
        protectValues[Knight][Bishop] = 15;
        protectValues[Knight][Rook] = 10;
        protectValues[Knight][Queen] = 10;

        protectValues[Bishop][Space] = 10;
        protectValues[Bishop][Pawn] = 5;
        protectValues[Bishop][Knight] = 15;
        protectValues[Bishop][Bishop] = 15;
        protectValues[Bishop][Rook] = 10;
        protectValues[Bishop][Queen] = 10;

        protectValues[Rook][Space] = 10;
        protectValues[Rook][Pawn] = 5;
        protectValues[Rook][Knight] = 10;
        protectValues[Rook][Bishop] = 10;
        protectValues[Rook][Rook] = 15;
        protectValues[Rook][Queen] = 10;

        protectValues[Queen][Space] = 10;
        protectValues[Queen][Pawn] = 5;
        protectValues[Queen][Knight] = 10;
        protectValues[Queen][Bishop] = 10;
        protectValues[Queen][Rook] = 10;
        protectValues[Queen][Queen] = 15;

        protectValues[King][Space] = 0;
        protectValues[King][Pawn] = 5;
        protectValues[King][Knight] = 10;
        protectValues[King][Bishop] = 10;
        protectValues[King][Rook] = 10;
        protectValues[King][Queen] = 10;

        attackValues = new int[7][7];
//        for (int i = 0; i < 7; i++)
//            for (int j = 0; j < 7; j++)
//                attackValues[i][j] = protectValues[i][j] / 2;
        attackValues[Pawn][Space] = 10;
        attackValues[Pawn][Pawn] = 50;
        attackValues[Pawn][Knight] = 100;
        attackValues[Pawn][Bishop] = 100;
        attackValues[Pawn][Rook] = 150;
        attackValues[Pawn][Queen] = 200;

        attackValues[Knight][Space] = 10;
        attackValues[Knight][Pawn] = 10;
        attackValues[Knight][Knight] = 0;
        attackValues[Knight][Bishop] = 30;
        attackValues[Knight][Rook] = 100;
        attackValues[Knight][Queen] = 150;

        attackValues[Bishop][Space] = 10;
        attackValues[Bishop][Pawn] = 10;
        attackValues[Bishop][Knight] = 30;
        attackValues[Bishop][Bishop] = 0;
        attackValues[Bishop][Rook] = 100;
        attackValues[Bishop][Queen] = 150;

        attackValues[Rook][Space] = 10;
        attackValues[Rook][Pawn] = 10;
        attackValues[Rook][Knight] = 10;
        attackValues[Rook][Bishop] = 10;
        attackValues[Rook][Rook] = 0;
        attackValues[Rook][Queen] = 150;

        attackValues[Queen][Space] = 10;
        attackValues[Queen][Pawn] = 10;
        attackValues[Queen][Knight] = 20;
        attackValues[Queen][Bishop] = 20;
        attackValues[Queen][Rook] = 20;
        attackValues[Queen][Queen] = 0;

        attackValues[King][Space] = 0;
        attackValues[King][Pawn] = 10;
        attackValues[King][Knight] = 20;
        attackValues[King][Bishop] = 20;
        attackValues[King][Rook] = 20;
        attackValues[King][Queen] = 20;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        switch (difficulty) {
            case Easy:
                branchLimit = baseBranchDepth / 40L / 40L;
                depthLimit = 4;
                break;
            case Medium:
                branchLimit = baseBranchDepth / 40L;
                depthLimit = 5;
                break;
            case Hard:
                branchLimit = baseBranchDepth;
                depthLimit = 6;
                break;
            case ExtraHard:
                branchLimit = baseBranchDepth * 40L;
                depthLimit = 7;
                break;
        }
    }

    public Move getWhiteMove(GameHistory gameHistory, Board board, LinkedList<RatedMove> bestMoves) {
        return getMove(gameHistory, board, bestMoves, White);
    }

    public Move getBlackMove(GameHistory gameHistory, Board board, LinkedList<RatedMove> bestMoves) {
        return getMove(gameHistory, board, bestMoves, Black);
    }

    private Move getMove(GameHistory gameHistory, Board boardIn, LinkedList<RatedMove> bestMoves, int player) {
//        board = board.clone();
        resetCounters();
        if (progressBar != null)
            progressBar.update(0);
        for (int i = 0; i < 8; i++)
            System.arraycopy(boardIn.pieces[i], 0, this.pieces[i], 0, 8);

        HashMap<Move, Integer> tmp = moveCache;
        moveCache = futureMoveCache;
        futureMoveCache = tmp;
        futureMoveCache.clear();

        System.out.println("current board rating = " + evaluateBoard());

        int bestRating = player == White ? WhiteWorst : BlackWorst;
        LinkedList<Move> moves = player == White ? BoardUtil.getWhiteMoves(pieces) : BoardUtil.getBlackMoves(pieces);
        int numMoves = moves.size();
        if (numMoves == 0)
            return null;

        if (player == White) {
            moves.sort(new Comparator<Move>() {
                public int compare(Move m1, Move m2) {
                    Integer i1 = moveCache.get(m1);
                    int r1 = i1 == null ? BlackWorst : i1;

                    Integer i2 = moveCache.get(m2);
                    int r2 = i2 == null ? BlackWorst : i2;

                    return Integer.compare(r2, r1);
                }
            });
        }
        else {
            moves.sort(new Comparator<Move>() {
                public int compare(Move m1, Move m2) {
                    Integer i1 = moveCache.get(m1);
                    int r1 = i1 == null ? WhiteWorst : i1;

                    Integer i2 = moveCache.get(m2);
                    int r2 = i2 == null ? WhiteWorst : i2;

                    return Integer.compare(r1, r2);
                }
            });
        }

        int depth = 0;
//        boolean debug = interestingMoves != null;
//        Move interestingMove = debug && interestingMoves.size() > 0 ? interestingMoves.removeFirst() : null;

        ArrayList<Move> topMoves = new ArrayList<Move>();

        int moveCount = 0;
        for (Move move : moves) {
            if (BoardUtil.wouldKingBeInCheck(pieces, player, move))
                continue;

            BoardUtil.makeMove(pieces, move);
            boolean repeatMove = gameHistory.getBoardCount(new Board(pieces)) >= 2;
            if (!repeatMove) {
                int moveRating = getRating(move, player * -1, depth + 1, numMoves, numMoves, bestRating, null); // interestingMove != null && move.equals(interestingMove),
                System.out.println(move + ", rating " + moveRating);

                if (player == White ? (moveRating > bestRating) : (moveRating < bestRating)) {
                    bestRating = moveRating;
                    topMoves.clear();
                    topMoves.add(move);
                }
                else if (moveRating == bestRating)
                    topMoves.add(move);
            }
            BoardUtil.unMakeMove(pieces, move);

            if (progressBar != null)
                progressBar.update(100 * ++moveCount / numMoves);
        }

        Move bestMove = null;
        if (topMoves.size() == 1)
            bestMove = topMoves.get(0);
        else if (topMoves.size() > 1) {
            int r = Math.abs(random.nextInt());
            int idx = r % topMoves.size();
            bestMove = topMoves.get(idx);
        }

        if (bestMove != null && bestMoves != null) {
            BoardUtil.makeMove(pieces, bestMove);
            bestMoves.add(new RatedMove(bestMove, evaluateBoard()));
            getRating(bestMove, player * -1, depth + 1, numMoves, numMoves, bestRating, bestMoves);
            BoardUtil.unMakeMove(pieces, bestMove);
        }

        if (progressBar != null)
            progressBar.update(100);
        System.out.println("Best move: " + bestMove + ", rating " + bestRating);
        return bestMove;
    }

    private int getRating(Move lastMove, int player, int depth, long branchDepth, int numOtherPlayerMoves, int cutOff, LinkedList<RatedMove> bestMoves) { // boolean debug,
        totalMoves += numOtherPlayerMoves;
        totalMoveCount++;

//        Move interestingMove;
//        if (debug) {
//            Board b = writeToBoard();
//            System.out.println(b);
//            if (interestingMoves.size() > 0) {
//                interestingMove = interestingMoves.removeFirst();
//                branchDepth = 1;
//            }
//            else
//                interestingMove = null;
//        }
//        else
//            interestingMove = null;

        LinkedList<Move> moves;
        int numMoves;
        if (depth > depthLimit) {
//        if (branchDepth > branchLimit) {
            if (lastMove.toPiece == Space) {
                depth--;
                totalDepth += depth;
                totalDepthCount++;
                if (depth < minimumDepth)
                    minimumDepth = depth;
                if (depth > maximumDepth)
                    maximumDepth = depth;
                return evaluateBoard();
            }
            moves = player == White ? BoardUtil.getWhiteAttackingMoves(pieces, lastMove.toRow, lastMove.toColumn) : BoardUtil.getBlackAttackingMoves(pieces, lastMove.toRow, lastMove.toColumn);
            numMoves = moves.size();
        }
        else {
            moves = player == White ? BoardUtil.getWhiteMoves(pieces) : BoardUtil.getBlackMoves(pieces);
            numMoves = moves.size();
            if (numMoves == 0)
                return 0; // Stalemate
        }

        int bestRating = player == White ? WhiteWorst : BlackWorst;
        long newBranchDepth = branchDepth * (numMoves == 1 ? 2 : numMoves);
        Move bestMove = null;
        for (Move move : moves) {
            BoardUtil.makeMove(pieces, move);

            int moveRating;
            if (player == White) {
                if (move.toPiece == BlackKing)
                    moveRating = WhiteWins;
                else
                    moveRating = getRating(move, player * -1, depth + 1, newBranchDepth, numMoves, bestRating, null); // interestingMove != null && move.equals(interestingMove)
            }
            else {
                if (move.toPiece == WhiteKing)
                    moveRating = BlackWins;
                else
                    moveRating = getRating(move, player * -1, depth + 1, newBranchDepth, numMoves, bestRating, null); // interestingMove != null && move.equals(interestingMove)
            }

            BoardUtil.unMakeMove(pieces, move);

            if (depth == 2)
                futureMoveCache.put(move, moveRating);

            if (player == White) {
                if (moveRating > bestRating) {
                    bestRating = moveRating;
                    bestMove = move;
                    if (bestRating > cutOff)
                        break;
                }
            }
            else {
                if (moveRating < bestRating) {
                    bestRating = moveRating;
                    bestMove = move;
                    if (bestRating < cutOff)
                        break;
                }
            }
        }

        if (bestMove == null) {
            depth--;
            totalDepth += depth;
            totalDepthCount++;
            if (depth < minimumDepth)
                minimumDepth = depth;
            else if (depth > maximumDepth)
                maximumDepth = depth;
            return evaluateBoard();
        }

        if (player == White) {
            if (bestRating == BlackWins) {
                if (BoardUtil.isKingInCheck(pieces, White))
                    bestRating += depth;
                else
                    bestRating = 0;
            }
        }
        else {
            if (bestRating == WhiteWins) {
                if (BoardUtil.isKingInCheck(pieces, Black))
                    bestRating -= depth;
                else
                    bestRating = 0;
            }
        }

        if (bestMoves != null) {
            BoardUtil.makeMove(pieces, bestMove);
            bestMoves.add(new RatedMove(bestMove, evaluateBoard()));
            getRating(bestMove, player * -1, depth + 1, newBranchDepth, numMoves, bestRating, bestMoves);
            BoardUtil.unMakeMove(pieces, bestMove);
        }

        return bestRating;
    }

    private int evaluateBoard() {
        int protectRating = 0;
        int attackRating = 0;
        int materialRating = 0;
        int pawnRating = 0;
        int i2, j2;
        int piece2;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int piece = pieces[i][j];
                if (piece == 0)
                    continue;
                switch (piece) {
                    case WhiteRook:
                        materialRating += RookValue;

                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 == Space) {
                                protectRating += protectValues[Rook][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Rook][piece2];
                            else
                                attackRating += attackValues[Rook][-piece2];
                            break;
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 == Space) {
                                protectRating += protectValues[Rook][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Rook][piece2];
                            else
                                attackRating += attackValues[Rook][-piece2];
                            break;
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Rook][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Rook][piece2];
                            else
                                attackRating += attackValues[Rook][-piece2];
                            break;
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Rook][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Rook][piece2];
                            else
                                attackRating += attackValues[Rook][-piece2];
                            break;
                        }
                        break;

                    case WhiteKnight:
                        materialRating += KnightValue;
                        for (int x = 0; x < 8; x++) {
                            i2 = i + knightMoves[x][0];
                            j2 = j + knightMoves[x][1];
                            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                                piece2 = pieces[i2][j2];
                                if (piece2 == Space)
                                    protectRating += protectValues[Knight][Space];
                                else if (piece2 > 0)
                                    protectRating += protectValues[Knight][piece2];
                                else
                                    attackRating += attackValues[Knight][-piece2];
                            }
                        }
                        break;

                    case WhiteBishop:
                        materialRating += BishopValue;

                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Bishop][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Bishop][piece2];
                            else
                                attackRating += attackValues[Bishop][-piece2];
                            break;
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Bishop][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Bishop][piece2];
                            else
                                attackRating += attackValues[Bishop][-piece2];
                            break;
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Bishop][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Bishop][piece2];
                            else
                                attackRating += attackValues[Bishop][-piece2];
                            break;
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Bishop][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Bishop][piece2];
                            else
                                attackRating += attackValues[Bishop][-piece2];
                            break;
                        }
                        break;

                    case WhiteQueen:
                        materialRating += QueenValue;

                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 == Space) {
                                protectRating += protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Queen][piece2];
                            else
                                attackRating += attackValues[Queen][-piece2];
                            break;
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 == Space) {
                                protectRating += protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Queen][piece2];
                            else
                                attackRating += attackValues[Queen][-piece2];
                            break;
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Queen][piece2];
                            else
                                attackRating += attackValues[Queen][-piece2];
                            break;
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Queen][piece2];
                            else
                                attackRating += attackValues[Queen][-piece2];
                            break;
                        }

                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Queen][piece2];
                            else
                                attackRating += attackValues[Queen][-piece2];
                            break;
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Queen][piece2];
                            else
                                attackRating += attackValues[Queen][-piece2];
                            break;
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Queen][piece2];
                            else
                                attackRating += attackValues[Queen][-piece2];
                            break;
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating += protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 > 0)
                                protectRating += protectValues[Queen][piece2];
                            else
                                attackRating += attackValues[Queen][-piece2];
                            break;
                        }
                        break;

                    case WhiteKing:
                        for (int x = 0; x < 8; x++) {
                            i2 = i + kingMoves[x][0];
                            j2 = j + kingMoves[x][1];
                            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                                piece2 = pieces[i2][j2];
                                if (piece2 == Space)
                                    protectRating += protectValues[King][Space];
                                else if (piece2 > 0)
                                    protectRating += protectValues[King][piece2];
                                else
                                    attackRating += attackValues[King][-piece2];
                            }
                        }
                        break;

                    case WhitePawn:
                        materialRating += PawnValue;

                        // Attack diagonal left
                        if (i < 7 && j > 0) {
                            piece2 = pieces[i + 1][j - 1];
                            if (piece2 == Space)
                                protectRating += protectValues[Pawn][Space];
                            else if (piece2 > 0)
                                protectRating += protectValues[Pawn][piece2];
                            else
                                attackRating += attackValues[Pawn][-piece2];
                        }

                        // Attack diagonal right
                        if (i < 7 && j < 7) {
                            piece2 = pieces[i + 1][j + 1];
                            if (piece2 == Space)
                                protectRating += protectValues[Pawn][Space];
                            else if (piece2 > 0)
                                protectRating += protectValues[Pawn][piece2];
                            else
                                attackRating += attackValues[Pawn][-piece2];
                        }

                        // Pawn promotion likelihood
                        for (i2 = 7; i2 > i; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 < 0)
                                break;
                        }
                        if (i2 == i)
                            pawnRating += i * 50;
                        break;

                    case BlackRook:
                        materialRating -= RookValue;

                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Rook][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Rook][-piece2];
                            else
                                attackRating -= attackValues[Rook][piece2];
                            break;
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Rook][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Rook][-piece2];
                            else
                                attackRating -= attackValues[Rook][piece2];
                            break;
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Rook][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Rook][-piece2];
                            else
                                attackRating -= attackValues[Rook][piece2];
                            break;
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Rook][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Rook][-piece2];
                            else
                                attackRating -= attackValues[Rook][piece2];
                            break;
                        }
                        break;

                    case BlackKnight:
                        materialRating -= KnightValue;
                        for (int x = 0; x < 8; x++) {
                            i2 = i + knightMoves[x][0];
                            j2 = j + knightMoves[x][1];
                            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                                piece2 = pieces[i2][j2];
                                if (piece2 == Space)
                                    protectRating -= protectValues[Knight][Space];
                                else if (piece2 < 0)
                                    protectRating -= protectValues[Knight][-piece2];
                                else
                                    attackRating -= attackValues[Knight][piece2];
                            }
                        }
                        break;

                    case BlackBishop:
                        materialRating -= BishopValue;

                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Bishop][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Bishop][-piece2];
                            else
                                attackRating -= attackValues[Bishop][piece2];
                            break;
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Bishop][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Bishop][-piece2];
                            else
                                attackRating -= attackValues[Bishop][piece2];
                            break;
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Bishop][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Bishop][-piece2];
                            else
                                attackRating -= attackValues[Bishop][piece2];
                            break;
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Bishop][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Bishop][-piece2];
                            else
                                attackRating -= attackValues[Bishop][piece2];
                            break;
                        }
                        break;

                    case BlackQueen:
                        materialRating -= QueenValue;

                        // North
                        for (i2 = i + 1; i2 < 8; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Queen][-piece2];
                            else
                                attackRating -= attackValues[Queen][piece2];
                            break;
                        }

                        // South
                        for (i2 = i - 1; i2 >= 0; i2--) {
                            piece2 = pieces[i2][j];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Queen][-piece2];
                            else
                                attackRating -= attackValues[Queen][piece2];
                            break;
                        }

                        // East
                        for (j2 = j - 1; j2 >= 0; j2--) {
                            piece2 = pieces[i][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Queen][-piece2];
                            else
                                attackRating -= attackValues[Queen][piece2];
                            break;
                        }

                        // West
                        for (j2 = j + 1; j2 < 8; j2++) {
                            piece2 = pieces[i][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Queen][-piece2];
                            else
                                attackRating -= attackValues[Queen][piece2];
                            break;
                        }

                        // NE
                        for (i2 = i + 1, j2 = j + 1; i2 < 8 && j2 < 8; i2++, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Queen][-piece2];
                            else
                                attackRating -= attackValues[Queen][piece2];
                            break;
                        }

                        // SE
                        for (i2 = i - 1, j2 = j + 1; i2 >= 0 && j2 < 8; i2--, j2++) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Queen][-piece2];
                            else
                                attackRating -= attackValues[Queen][piece2];
                            break;
                        }

                        // NW
                        for (i2 = i + 1, j2 = j - 1; i2 < 8 && j2 >= 0; i2++, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Queen][-piece2];
                            else
                                attackRating -= attackValues[Queen][piece2];
                            break;
                        }

                        // SW
                        for (i2 = i - 1, j2 = j - 1; i2 >= 0 && j2 >= 0; i2--, j2--) {
                            piece2 = pieces[i2][j2];
                            if (piece2 == Space) {
                                protectRating -= protectValues[Queen][Space];
                                continue;
                            }
                            if (piece2 < 0)
                                protectRating -= protectValues[Queen][-piece2];
                            else
                                attackRating -= attackValues[Queen][piece2];
                            break;
                        }
                        break;

                    case BlackKing:
                        for (int x = 0; x < 8; x++) {
                            i2 = i + kingMoves[x][0];
                            j2 = j + kingMoves[x][1];
                            if (i2 >= 0 && i2 < 8 && j2 >= 0 && j2 < 8) {
                                piece2 = pieces[i2][j2];
                                if (piece2 == Space)
                                    protectRating -= protectValues[King][Space];
                                else if (piece2 < 0)
                                    protectRating -= protectValues[King][-piece2];
                                else
                                    attackRating -= attackValues[King][piece2];
                            }
                        }
                        break;

                    case BlackPawn:
                        materialRating -= PawnValue;

                        // Attack diagonal right
                        if (i > 0 && j > 0) {
                            piece2 = pieces[i - 1][j - 1];
                            if (piece2 == Space)
                                protectRating -= protectValues[Pawn][Space];
                            else if (piece2 < 0)
                                protectRating -= protectValues[Pawn][-piece2];
                            else
                                attackRating -= attackValues[Pawn][piece2];
                        }

                        // Attack diagonal left
                        if (i > 0 && j < 7) {
                            piece2 = pieces[i - 1][j + 1];
                            if (piece2 == Space)
                                protectRating -= protectValues[Pawn][Space];
                            else if (piece2 < 0)
                                protectRating -= protectValues[Pawn][-piece2];
                            else
                                attackRating -= attackValues[Pawn][piece2];
                        }

                        // Pawn promotion likelihood
                        for (i2 = 0; i2 < i; i2++) {
                            piece2 = pieces[i2][j];
                            if (piece2 > 0 && piece2 != WhiteKing)
                                break;
                        }
                        if (i2 == i)
                            pawnRating -= (7 - i) * 50;
                        break;
                }
            }
        }
       return materialRating + protectRating + attackRating + pawnRating;
    }
}
