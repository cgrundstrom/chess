package com.carlgrundstrom.chess;

import static com.carlgrundstrom.chess.BoardUtil.*;

import java.util.LinkedList;

public class ExperimentalEngineThread extends Thread {
    public static final int RookValue = 50000;
    public static final int KnightValue = 30000;
    public static final int BishopValue = 35000;
    public static final int QueenValue = 90000;
    public static final int PawnValue = 10000;

    private int[][] pieces2 = new int[8][8];
    private int player2;
    private int depth2;
    private int numMoves2;
    private int totalMoves2;
    private int totalMoveCount2;
    private int totalDepth2;
    private int totalDepthCount2;
    private int minimumDepth2;
    private int maximumDepth2;
    private LinkedList<RatedMove> bestMoves2;
    private ExperimentalEngine engine;
    private int depthLimit2;

    public static int[][] protectValues;
    public static int[][] attackValues;

    private static final int BlackWins = -10 * 1000 * 1000;
    private static final int WhiteWins = 10 * 1000 * 1000;
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

    static {
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

    public ExperimentalEngineThread(ExperimentalEngine engine, Board boardIn, int player, int depth, int depthLimit, int numMoves, LinkedList<RatedMove> bestMoves) {
        setDaemon(true);
        this.engine = engine;
        this.player2 = player;
        this.depth2 = depth;
        this.depthLimit2 = depthLimit;
        this.numMoves2 = numMoves;
        this.bestMoves2 = bestMoves;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                this.pieces2[i][j] = boardIn.pieces[i][j];
    }

    public void run() {
        Move move;
        while ((move = engine.getMove()) != null) {
            resetStatistics();
            BoardUtil.makeMove(pieces2, move);
            int moveRating = getRating(move, player2 * -1, depth2 + 1, numMoves2, numMoves2, engine.getCutoff()); // interestingMove != null && move.equals(interestingMove),
            BoardUtil.unMakeMove(pieces2, move);
            engine.updateStatistics(player2, move, moveRating, numMoves2, totalMoves2, totalMoveCount2,
                    totalDepth2, totalDepthCount2, minimumDepth2, maximumDepth2);
        }
    }

    private void resetStatistics() {
        totalMoves2 = 0;
        totalMoveCount2 = 0;
        totalDepth2 = 0;
        totalDepthCount2 = 0;
        minimumDepth2 = Integer.MAX_VALUE;
        maximumDepth2 = 0;
    }

    private int getRating(Move lastMove, int player, int depth, long branchDepth, int numOtherPlayerMoves, int cutOff) { // boolean debug,
        totalMoves2 += numOtherPlayerMoves;
        totalMoveCount2++;

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
        if (depth > depthLimit2) {
//        if (branchDepth > branchLimit) {
            if (lastMove.toPiece == Space) {
                depth--;
                totalDepth2 += depth;
                totalDepthCount2++;
                if (depth < minimumDepth2)
                    minimumDepth2 = depth;
                if (depth > maximumDepth2)
                    maximumDepth2 = depth;
                return evaluateBoard(pieces2);
            }
            moves = player == White ? BoardUtil.getWhiteAttackingMoves(pieces2, lastMove.toRow, lastMove.toColumn) : BoardUtil.getBlackAttackingMoves(pieces2, lastMove.toRow, lastMove.toColumn);
            numMoves = moves.size();
        }
        else {
            moves = player == White ? BoardUtil.getWhiteMoves(pieces2) : BoardUtil.getBlackMoves(pieces2);
            numMoves = moves.size();
            if (numMoves == 0)
                return 0; // Stalemate
        }

        int bestRating = player == White ? ExperimentalEngine.WhiteWorst : ExperimentalEngine.BlackWorst;
        long newBranchDepth = branchDepth * (numMoves == 1 ? 2 : numMoves);
        Move bestMove = null;
        for (Move move : moves) {
            BoardUtil.makeMove(pieces2, move);

            int moveRating;
            if (player == White) {
                if (move.toPiece == BlackKing)
                    moveRating = WhiteWins;
                else
                    moveRating = getRating(move, player * -1, depth + 1, newBranchDepth, numMoves, bestRating); // interestingMove != null && move.equals(interestingMove)
            }
            else {
                if (move.toPiece == WhiteKing)
                    moveRating = BlackWins;
                else
                    moveRating = getRating(move, player * -1, depth + 1, newBranchDepth, numMoves, bestRating); // interestingMove != null && move.equals(interestingMove)
            }

            if (depth == 3)
                engine.updateFutureCache(pieces2, moveRating);

            BoardUtil.unMakeMove(pieces2, move);

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
            totalDepth2 += depth;
            totalDepthCount2++;
            if (depth < minimumDepth2)
                minimumDepth2 = depth;
            else if (depth > maximumDepth2)
                maximumDepth2 = depth;
            return evaluateBoard(pieces2);
        }

        if (player == White) {
            if (bestRating == BlackWins) {
                if (BoardUtil.isKingInCheck(pieces2, White))
                    bestRating += depth;
                else
                    bestRating = 0;
            }
        }
        else {
            if (bestRating == WhiteWins) {
                if (BoardUtil.isKingInCheck(pieces2, Black))
                    bestRating -= depth;
                else
                    bestRating = 0;
            }
        }

        if (bestMoves2 != null) {
            BoardUtil.makeMove(pieces2, bestMove);
            bestMoves2.add(new RatedMove(bestMove, evaluateBoard(pieces2)));
            getRating(bestMove, player * -1, depth + 1, newBranchDepth, numMoves, bestRating);
            BoardUtil.unMakeMove(pieces2, bestMove);
        }

        return bestRating;
    }

    protected static int evaluateBoard(int[][] pieces) {
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
