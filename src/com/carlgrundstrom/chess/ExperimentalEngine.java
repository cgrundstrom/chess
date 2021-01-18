package com.carlgrundstrom.chess;

import static com.carlgrundstrom.chess.BoardUtil.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;

public class ExperimentalEngine extends Engine {
    protected static final int WhiteWorst = Integer.MIN_VALUE;

    protected static final int BlackWorst = Integer.MAX_VALUE;

    private static long baseBranchDepth = 50L * 1000L * 1000L;

    private int depthLimit;
    protected long branchLimit;
    private int bestRating;
    private int moveCount;

    private HashMap<Integer, Integer> moveCache = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> futureMoveCache = new HashMap<Integer, Integer>();
    private ArrayList<Move> topMoves = new ArrayList<Move>();
    private final LinkedList<Move> moveQueue = new LinkedList<Move>();

//    private LinkedList<Move> interestingMoves;

    private Random random = new Random();

    public ExperimentalEngine(ProgressBarInterface progressBar) {
        super("Experimental", progressBar);
//        interestingMoves = new LinkedList<Move>();
//        interestingMoves.add(new Move(6, 2, 1, 7, Space));
//        interestingMoves.add(new Move(1, 4, 2, 4, Space));
//        interestingMoves.add(new Move(0, 7, 0, 4, Space));
//        interestingMoves.add(new Move(1, 2, 1, 4, Space));
//        interestingMoves.add(new Move(1, 7, 1, 4, WhiteRook));
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
        System.out.println();
        System.out.println("moveCache.size() = " + moveCache.size());
        System.out.println("futureMoveCache.size() = " + futureMoveCache.size());
        System.out.println("topMoves.size() = " + topMoves.size());
        System.out.println("moveQueue.size() = " + moveQueue.size());
        System.out.println("gameHistory.getRecordCount() = " + gameHistory.getRecordCount());
        System.out.println("gameHistory.getBoardMapSize() = " + gameHistory.getBoardMapSize());
        System.out.println();

        resetCounters();
        int[][] pieces = new int[8][8];
        if (progressBar != null)
            progressBar.update(0);
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                pieces[i][j] = boardIn.pieces[i][j];

        HashMap<Integer, Integer> tmp = moveCache;
        moveCache = futureMoveCache;
        futureMoveCache = tmp;
        futureMoveCache.clear();

        System.out.println("current board rating = " + ExperimentalEngineThread.evaluateBoard(pieces));

        bestRating = player == White ? WhiteWorst : BlackWorst;
        LinkedList<Move> moves = player == White ? BoardUtil.getWhiteMoves(pieces) : BoardUtil.getBlackMoves(pieces);
        int numMoves = moves.size();
        if (numMoves == 0)
            return null;

        LinkedList<RatedMove> ratedMoves = new LinkedList<RatedMove>();

        Iterator<Move> iterator = moves.iterator();
        while (iterator.hasNext()) {
            Move move = iterator.next();
            if (BoardUtil.wouldKingBeInCheck(pieces, player, move))
                iterator.remove();
            else {
                BoardUtil.makeMove(pieces, move);
                Board board = new Board(pieces);
                Integer predictedRating = moveCache.get(board.hashCode());
                ratedMoves.add(new RatedMove(move, predictedRating == null ? 0 : predictedRating));
                boolean repeatMove = gameHistory.getBoardCount(board) >= 2;
                BoardUtil.unMakeMove(pieces, move);
                if (repeatMove)
                    iterator.remove();
            }
        }

        if (player == White) {
            Collections.sort(ratedMoves, new Comparator<RatedMove>() {
                public int compare(RatedMove m1, RatedMove m2) {
                    return m2.rating - m1.rating;
                }
            });
        }
        else {
            Collections.sort(ratedMoves, new Comparator<RatedMove>() {
                public int compare(RatedMove m1, RatedMove m2) {
                    return m1.rating - m2.rating;
                }
            });
        }

        moveQueue.clear();
        for (Move move : ratedMoves)
            moveQueue.addLast(move);
        topMoves.clear();
        moveCount = 0;

        int threadCount = Runtime.getRuntime().availableProcessors() / 2;
        System.out.println("threadCount = " + threadCount);
        int depth = 0;
        ExperimentalEngineThread[] threads = new ExperimentalEngineThread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new ExperimentalEngineThread(this, boardIn, player, depth + 1, depthLimit, numMoves, null);
            threads[i].start();
        }

        for (ExperimentalEngineThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            moveQueue.add(bestMove);
            BoardUtil.makeMove(pieces, bestMove);
            bestMoves.add(new RatedMove(bestMove, ExperimentalEngineThread.evaluateBoard(pieces)));
            BoardUtil.unMakeMove(pieces, bestMove);
            ExperimentalEngineThread thread = new ExperimentalEngineThread(this, boardIn, player, depth + 1, depthLimit, numMoves, bestMoves);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (progressBar != null)
            progressBar.update(100);

        System.out.println();
        System.out.println("moveCache.size() = " + moveCache.size());
        System.out.println("futureMoveCache.size() = " + futureMoveCache.size());
        System.out.println("topMoves.size() = " + topMoves.size());
        System.out.println("moveQueue.size() = " + moveQueue.size());
        System.out.println("gameHistory.getRecordCount() = " + gameHistory.getRecordCount());
        System.out.println("gameHistory.getBoardMapSize() = " + gameHistory.getBoardMapSize());
        System.out.println();

        return bestMove;
    }

    protected synchronized void updateStatistics(
            int player2, Move move, int moveRating, int numMoves2, int totalMoves2, int totalMoveCount2,
            int totalDepth2, int totalDepthCount2, int minimumDepth2, int maximumDepth2) {
        System.out.println(Thread.currentThread().getId() + ": " + move + ", rating " + moveRating);
        if (player2 == White ? (moveRating > bestRating) : (moveRating < bestRating)) {
            bestRating = moveRating;
            topMoves.clear();
            topMoves.add(move);
        }
        else if (moveRating == bestRating)
            topMoves.add(move);

        if (progressBar != null)
            progressBar.update(100 * ++moveCount / (numMoves2 + 1));

        totalMoves += totalMoves2;
        totalMoveCount += totalMoveCount2;
        totalDepth += totalDepth2;
        totalDepthCount += totalDepthCount2;
        if (minimumDepth2 < minimumDepth)
            minimumDepth = minimumDepth2;
        if (maximumDepth2 > maximumDepth)
            maximumDepth = maximumDepth2;
    }

    protected synchronized int getCutoff() {
        return bestRating;
    }

    protected synchronized Move getMove() {
        return moveQueue.isEmpty() ? null : moveQueue.removeFirst();
    }

    protected synchronized void updateFutureCache(int[][] pieces, int moveRating) {
//        if (futureMoveCache.containsKey(move))
//            System.out.println("cache already contains: " + move + ", " + futureMoveCache.get(move) + ", " + moveRating);
//        else
//            System.out.println("adding move to cache: " + move + ", " + moveRating);
        futureMoveCache.put(new Board(pieces, true).hashCode(), moveRating);
    }
}


