package com.carlgrundstrom.chess;

import junit.framework.TestCase;
import static com.carlgrundstrom.chess.BoardUtil.*;

public class UnitTest extends TestCase {
    private static final int White = 1;
    private static final int Black = -1;

    public void tXst001() {
        Board b1 = new Board();
        b1.pieces[7][5] = WhiteBishop;
        b1.pieces[7][6] = BlackKnight;
        b1.pieces[7][7] = BlackKing;
        b1.pieces[6][7] = BlackBishop;
        b1.pieces[5][7] = WhiteBishop;
        b1.pieces[4][7] = WhiteKing;

        Board b2 = new Board();
        b2.pieces[7][5] = WhiteBishop;
        b2.pieces[7][6] = BlackKnight;
        b2.pieces[7][7] = BlackKing;
        b2.pieces[6][7] = BlackBishop;
        b2.pieces[6][6] = WhiteBishop;
        b2.pieces[4][7] = WhiteKing;

        runProblem(b1, b2, White, 1);
    }

    public void tXst002() {
        Board b1 = new Board();
        b1.pieces[7][6] = BlackKing;
        b1.pieces[6][2] = BlackQueen;
        b1.pieces[6][5] = BlackPawn;
        b1.pieces[6][7] = BlackPawn;
        b1.pieces[5][4] = BlackKnight;
        b1.pieces[5][6] = BlackPawn;
        b1.pieces[3][0] = WhiteQueen;
        b1.pieces[2][3] = WhitePawn;
        b1.pieces[2][5] = WhitePawn;
        b1.pieces[2][7] = WhitePawn;
        b1.pieces[1][2] = WhiteRook;
        b1.pieces[1][4] = WhiteKing;
        b1.pieces[0][7] = BlackRook;

        Board b2 = new Board();
        b2.pieces[7][6] = BlackKing;
        b2.pieces[1][4] = BlackQueen;
        b2.pieces[6][5] = BlackPawn;
        b2.pieces[6][7] = BlackPawn;
        b2.pieces[5][4] = BlackKnight;
        b2.pieces[5][6] = BlackPawn;
        b2.pieces[3][0] = WhiteQueen;
        b2.pieces[2][3] = WhitePawn;
        b2.pieces[4][3] = WhitePawn;
        b2.pieces[6][3] = WhitePawn;
        b2.pieces[2][4] = WhiteKing;
        b2.pieces[0][4] = BlackRook;

        runProblem(b1, b2, Black, 5);
    }

    public void runProblem(Board board, Board expectedResult, int player, int expectedMoves) {
        Engine engine = new ExperimentalEngine(null);

        int moves = 0;
        int winningPlayer = player;
        System.out.println(board);
        while (true) {
            Move move = player == Black ? engine.getBlackMove(null, board, null) : engine.getWhiteMove(null, board, null);
            assertTrue("too many moves", ++moves <= expectedMoves);
            assertNotNull("no moves possible", move);
            System.out.println("move = " + move);
            BoardUtil.makeMove(board.pieces, move);
            System.out.println(board);
            if (BoardUtil.isCheckMated(board.pieces, winningPlayer == White ? Black : White))
                break;
            player = player == White ? Black : White;
        }
        assertTrue("too few moves", moves == expectedMoves);
        assertEquals("unexpected board", board, expectedResult);
    }

    public void testWhiteKingMoves() {
        MoveTest moveTest = new MoveTest() {
            public void run() {
                int piece = WhiteKing;
                b.pieces[5][5] = piece;
                Move[] expectedMoves = {
                        new Move(5, 5, piece, 4, 4, Space),
                        new Move(5, 5, piece, 4, 5, Space),
                        new Move(5, 5, piece, 4, 6, Space),
                        new Move(5, 5, piece, 5, 4, Space),
                        new Move(5, 5, piece, 5, 6, Space),
                        new Move(5, 5, piece, 6, 4, Space),
                        new Move(5, 5, piece, 6, 5, Space),
                        new Move(5, 5, piece, 6, 6, Space),
                };
                Move[] moves = BoardUtil.getWhiteMoves(b.pieces).toArray(new Move[0]);
                checkResults(moves, expectedMoves);
            }
        };
        moveTest.run();
    }

    public void testBlackKingMoves() {
        MoveTest moveTest = new MoveTest() {
            public void run() {
                int piece = BlackKing;
                b.pieces[5][5] = piece;
                Move[] expectedMoves = {
                        new Move(5, 5, piece, 4, 4, Space),
                        new Move(5, 5, piece, 4, 5, Space),
                        new Move(5, 5, piece, 4, 6, Space),
                        new Move(5, 5, piece, 5, 4, Space),
                        new Move(5, 5, piece, 5, 6, Space),
                        new Move(5, 5, piece, 6, 4, Space),
                        new Move(5, 5, piece, 6, 5, Space),
                        new Move(5, 5, piece, 6, 6, Space),
                };
                Move[] actualMoves = BoardUtil.getBlackMoves(b.pieces).toArray(new Move[0]);
                checkResults(actualMoves, expectedMoves);
            }
        };
        moveTest.run();
    }

    public abstract class MoveTest {
        protected int whoseTurn = White;
        protected Board b = new Board();

        public abstract void run();

        public void checkResults(Move[] actualMoves, Move[] expectedMoves) {
            assertEquals("Number of moves returned", expectedMoves.length, actualMoves.length);
            for (Move actualMove : actualMoves) {
                boolean found = false;
                for (Move expectedMove : expectedMoves) {
                    if (actualMove.equals(expectedMove)) {
                        found = true;
                        break;
                    }
                }
                assertTrue("Actual move " + actualMove + " not found", found);
            }
            for (Move expectedMove : expectedMoves) {
                boolean found = false;
                for (Move actualMove : actualMoves) {
                    if (expectedMove.equals(actualMove)) {
                        found = true;
                        break;
                    }
                }
                assertTrue("Expected move " + expectedMove + " not found", found);
            }
        }
    }
}
