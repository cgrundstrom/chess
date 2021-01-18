package com.carlgrundstrom.chess;

import java.util.LinkedList;

public abstract class Engine {
    protected int minimumDepth;
    protected int maximumDepth;
    protected long totalMoves;
    protected int totalMoveCount;
    protected long totalDepth;
    protected int totalDepthCount;

    private long totalTimeUsed;
    private String name;
    protected Difficulty difficulty;
    protected ProgressBarInterface progressBar;

    public enum Difficulty {
        Easy,
        Medium,
        Hard,
        ExtraHard
    }

    public Engine(String name, ProgressBarInterface progressBar) {
        this.name = name;
        this.progressBar = progressBar;
    }

    public void incrementTotalTimeUsed(long timeUsed) {
        totalTimeUsed += timeUsed;
    }

    public long getTotalTimeUsed() {
        return totalTimeUsed;
    }

    public int getMinimumDepth() {
        return minimumDepth == Integer.MAX_VALUE ? 0 : minimumDepth;
    }

    public int getMaximumDepth() {
        return maximumDepth;
    }

    public int getAverageMoves() {
        return (int)Math.round(totalMoveCount > 0 ? (double)totalMoves / (double)totalMoveCount : 0.0);
    }

    public double getAverageDepth() {
        return totalDepthCount > 0 ? (double)totalDepth / (double)totalDepthCount : 0.0;
    }

    public String getName() {
        return name;
    }

    public int getMoveCount() {
        return totalMoveCount;
    }

    protected void resetCounters() {
        minimumDepth = Integer.MAX_VALUE;
        maximumDepth = 0;

        totalMoves = 0;
        totalMoveCount = 0;

        totalDepth = 0;
        totalDepthCount = 0;
    }

    public abstract void setDifficulty(Difficulty difficulty);

    public Difficulty getDifficulty() {
        return difficulty;
    }

    abstract public Move getWhiteMove(GameHistory gameHistory, Board board, LinkedList<RatedMove> bestMoves);

    abstract public Move getBlackMove(GameHistory gameHistory, Board board, LinkedList<RatedMove> bestMoves);
}
