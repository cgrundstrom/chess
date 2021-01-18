package com.carlgrundstrom.chess;

import java.util.HashMap;
import java.util.LinkedList;

public class GameHistory {
    private LinkedList<Record> records = new LinkedList<>();
    private HashMap<Board, Integer> boardMap = new HashMap<>();

    public int getRecordCount() {
        return records.size();
    }

    public int getBoardMapSize() {
        return boardMap.size();
    }
    
    public void add(Move move, Board boardIn) {
        Board board = boardIn.clone();
        records.addLast(new Record(move, board));
        Integer count = boardMap.get(board);
        if (count == null)
            count = 1;
        else
            count++;
        boardMap.put(board, count);
    }

    public Move remove() {
        Record record = records.removeLast();
        Integer count = boardMap.get(record.board);
        if (count == 1)
            boardMap.remove(record.board);
        else
            boardMap.put(record.board, --count);
        return record.move;
    }

    public int getBoardCount(Board board) {
        Integer count = boardMap.get(board);
        if (count == null)
            count = 0;
        return count;
    }

    public void clear() {
        records.clear();
        boardMap.clear();
    }

    private static class Record {
        private Move move;
        private Board board;

        private Record(Move move, Board board) {
            this.move = move;
            this.board = board;
        }
    }
}
