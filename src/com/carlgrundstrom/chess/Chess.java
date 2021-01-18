package com.carlgrundstrom.chess;

import static com.carlgrundstrom.chess.BoardUtil.*;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class Chess extends JFrame implements ProgressBarInterface {
    static public PropertyManager propertyManager;

    private boolean computerIsThinking;
    private final DisplayBoard displayBoard;
    private int whoseTurn;
    private boolean rotateBoard;
    private final GameHistory gameHistory = new GameHistory();
    private final LinkedList<Move> takeBackMoveHistory = new LinkedList<>();
    private final LinkedList<RatedMove> anticipatedMoves = new LinkedList<>();
    protected boolean showLookAhead;
    protected boolean gameOver;
    private JMenuItem replayMoveMenuItem;
    private Engine.Difficulty difficulty;
    public static Chess THIS;

    public static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private enum Mode {
        UserPlaysWhite,
        UserPlaysBlack,
        ComputerPlaysComputer,
        UserPlaysUser,
    }

    public static void main(String[] args) {
        try {
            propertyManager = new PropertyManager(".chess");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        THIS = new Chess();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int userColor = White;
    private Board board;
    private Engine engine1;
    //private Engine engine2;
    private Point start;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    private Mode mode = Mode.UserPlaysWhite;

    public Chess() throws Exception {
        super();
        String engineName1 = propertyManager.getString("engineName1", "Standard");
        switch (engineName1) {
            case "Standard":
                engine1 = new StandardEngine(this);
                break;
            case "Experimental":
                engine1 = new ExperimentalEngine(this);
                break;
            case "Original":
                engine1 = new OriginalEngine(this);
                break;
            default:
                engine1 = new StandardEngine(this);
                break;
        }

        //String engineName2 = propertyManager.getString("engineName2", "Experimental");
        //switch (engineName2) {
        //    case "Standard":
        //        engine2 = new StandardEngine(this);
        //        break;
        //    case "Experimental":
        //        engine2 = new ExperimentalEngine(this);
        //        break;
        //    case "Original":
        //        engine2 = new OriginalEngine(this);
        //        break;
        //    default:
        //        engine2 = new StandardEngine(this);
        //        break;
        //}

        String difficultyString = Chess.propertyManager.getString("difficulty", String.valueOf(Engine.Difficulty.Medium));
        try {
            difficulty = Engine.Difficulty.valueOf(difficultyString);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            difficulty = Engine.Difficulty.Medium;
        }
        engine1.setDifficulty(difficulty);
        //engine2.setDifficulty(difficulty);

        setTitle("Chess");
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());

        setJMenuBar(new MyMenuBar());

        displayBoard = new DisplayBoard("tanBrown");
        pane.add(displayBoard, BorderLayout.CENTER);

        statusLabel = new JLabel("White's move");
        progressBar = new MyProgressBar();

        Box southBox = Box.createHorizontalBox();
        southBox.add(statusLabel);
        southBox.add(Box.createHorizontalGlue());
        southBox.add(progressBar);

        pane.add(southBox, BorderLayout.SOUTH);

        newGame();

        displayBoard.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                start = e.getPoint();
            }

            public void mouseReleased(MouseEvent e) {
                if (whoseTurn == userColor || mode == Mode.UserPlaysUser) {
                    Point end = e.getPoint();
                    int fromRow = 7 - start.y / DisplayBoard.squareSizeInPixels;
                    int fromColumn = start.x / DisplayBoard.squareSizeInPixels;
                    int toRow = 7 - end.y / DisplayBoard.squareSizeInPixels;
                    int toColumn = end.x / DisplayBoard.squareSizeInPixels;

                    if (rotateBoard ^ userColor == Black) {
                        fromRow = 7 - fromRow;
                        fromColumn = 7 - fromColumn;
                        toRow = 7 - toRow;
                        toColumn = 7 - toColumn;
                    }
                    Move move = new Move(fromRow, fromColumn, board.pieces[fromRow][fromColumn], toRow, toColumn, board.pieces[toRow][toColumn]);

                    if (!gameOver && BoardUtil.isValidMove(board.pieces, whoseTurn == White ? White : Black, move)) {
                        if (BoardUtil.wouldKingBeInCheck(board.pieces, whoseTurn == White ? White : Black, move))
                            showMessage("Illegal move: your king is in check");
                        else {
                            System.out.println("move = " + move);
                            BoardUtil.makeMove(board.pieces, move);

                            gameHistory.add(move, board);
                            takeBackMoveHistory.clear();
                            anticipatedMoves.clear();
                            replayMoveMenuItem.setEnabled(false);

                            displayBoard.update(board, userColor, rotateBoard);
                            System.out.println(board);

                            whoseTurn = whoseTurn == White ? Black : White;
                            evaluateGameStatus(whoseTurn);
                            if (!gameOver) {
                                if (mode == Mode.UserPlaysWhite && whoseTurn == Black
                                     || mode == Mode.UserPlaysBlack && whoseTurn == White) {
                                    ComputerMove computerMove = new ComputerMove(engine1, userColor == White ? Black : White);
                                    computerMove.start();
                                }
                            }
                        }
                    }
                }
            }
        });
        displayBoard.update(board, userColor, rotateBoard);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Chess.this.dispose();
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                Point location = e.getComponent().getLocation();
                try {
                    propertyManager.setProperty("mainWindowLocationX", location.x);
                    propertyManager.setProperty("mainWindowLocationY", location.y);
                    propertyManager.write();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

//            public void componentResized(ComponentEvent e) {
//                Dimension size = e.getComponent().getSize();
//                try {
//                    propertyManager.setProperty("mainWindowSizeX", size.width);
//                    propertyManager.setProperty("mainWindowSizeY", size.height);
//                    propertyManager.write();
//                }
//                catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
        });

        // Set the window size based on the user's preferences, but make sure we never exceed the current desktop size
        int locationX = propertyManager.getInteger("mainWindowLocationX", 50);
        int locationY = propertyManager.getInteger("mainWindowLocationY", 50);

        pack();
//        int sizeX = propertyManager.getInteger("mainWindowSizeX", 1000);
//        int sizeY = propertyManager.getInteger("mainWindowSizeY", 1000);
        Dimension size = getSize();
        int sizeX = size.width;
        int sizeY = size.height;

        Rectangle r = getScreenSize();
        if (r.width < locationX + sizeX) {
            if (r.width < sizeX) {
                locationX = 0;
                sizeX = r.width;
            } else
                locationX = r.width - sizeX;
            propertyManager.setProperty("mainWindowLocationX", locationX);
            propertyManager.setProperty("mainWindowSizeX", sizeX);
        }
        if (r.height < locationY + sizeY) {
            if (r.height < sizeY) {
                locationY = 0;
                sizeY = r.height;
            } else
                locationY = r.height - sizeY;
            propertyManager.setProperty("mainWindowSizeY", sizeY);
            propertyManager.setProperty("mainWindowLocationY", locationY);
        }
        propertyManager.write();
//        setSize(sizeX, sizeY);
        setLocation(locationX, locationY);

        setVisible(true);
    }

    public void update(int percent) {
        final int finalPercent = percent;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(finalPercent);
            }
        });
    }

    /**
     * Returns the screen size of the video display. This support multiple monitors by returning a size that
     * spans all attached monitors
     */
    private Rectangle getScreenSize() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        Rectangle virtualBounds = new Rectangle();
        for (GraphicsDevice gd : gds) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            virtualBounds = virtualBounds.union(gc.getBounds());
        }
        return virtualBounds;
    }

    private void setStatus(String status) {
        if (SwingUtilities.isEventDispatchThread())
            statusLabel.setText(status);
        else {
            final String statusFinal = status;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setStatus(statusFinal);
                }
            });
        }
    }

    private void updateBoard() {
        if (SwingUtilities.isEventDispatchThread())
            displayBoard.update(board, userColor, rotateBoard);
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    displayBoard.update(board, userColor, rotateBoard);
                }
            });
        }
    }

    private void showMessage(String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            statusLabel.setText(message);
            JOptionPane.showMessageDialog(null, message);
        }
        else {
            final String messageFinal = message;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setStatus(messageFinal);
                    JOptionPane.showMessageDialog(null, messageFinal);
                }
            });
        }
    }

    private void newGame() {
        board = new Board();
        whoseTurn = board.initializeStandardLayout();
        userColor = whoseTurn;
        displayBoard.update(board, userColor, rotateBoard);
        reset();
        setStatus("New Game, " + (whoseTurn == White ? "White's" : "Black's") + " turn to move");
    }

    private void setUpProblem(Problems.Problem problem) {
        board = problem.b.clone();
        whoseTurn = problem.whoseTurn;
        userColor = problem.userColor;
        displayBoard.update(board, userColor, rotateBoard);
        reset();
        setStatus("Problem \"" + problem.getName() + "\", " + (whoseTurn == White ? "White's" : "Black's") + " turn to move");
    }

    private void reset() {
        gameHistory.clear();
        takeBackMoveHistory.clear();
        anticipatedMoves.clear();
        gameOver = false;
    }

    protected ComputerMove currentComputerMove;

    protected class ComputerMove extends Thread {
        private int player;
        private Engine engine;

        public ComputerMove(Engine engine, int player) {
            this.engine = engine;
            this.player = player;
            setDaemon(true);
        }

        public void run() {
            if (!gameOver) {
                computerIsThinking = true;
                currentComputerMove = this;
//                progressBar.setValue(0);
//                progressBar.setVisible(true);
                progressBar.setStringPainted(true);
                int moves = 0;
                do {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    setStatus("Computer is moving for " + (whoseTurn == White ? "White" : "Black") + " using \"" + engine.getName() +  "\" engine, please wait");

                    long startTime = System.currentTimeMillis();
                    anticipatedMoves.clear();
                    Move move = player == Black ? engine.getBlackMove(gameHistory, board, anticipatedMoves) : engine.getWhiteMove(gameHistory, board, anticipatedMoves);
                    long endTime = System.currentTimeMillis();
                    long timeUsed = endTime - startTime;
                    engine.incrementTotalTimeUsed(timeUsed);

                    if (anticipatedMoves.size() > 1) {
//                        System.out.println();
//                        System.out.println("*** Anticipated Moves ***");
//                        Board b = board.clone();
//                        System.out.println(b);
//                        int turn = whoseTurn;
//                        for (Move m : anticipatedMoves) {
//                            System.out.println();
//                            System.out.println((turn == White ? "White" : "Black") +  " moves " + m);
//                            Board.makeMove(b.pieces, m);
//                            System.out.println(b);
//                            turn *= -1;
//                        }
                        anticipatedMoves.removeFirst();
                    }
                    replayMoveMenuItem.setEnabled(anticipatedMoves.size() > 0);
                    
                    if (move != null) {
                        BoardUtil.makeMove(board.pieces, move);

                        gameHistory.add(move, board);
                        takeBackMoveHistory.clear();

                        setStatus(move.toString());

                        updateBoard();

                        System.out.println();
                        System.out.println("*** Move ***");
                        System.out.println(board);
                        DecimalFormat fmt1 = new DecimalFormat("###,###,###,###");
                        DecimalFormat fmt2 = new DecimalFormat("0.0");
                        System.out.println(fmt1.format(engine.getMoveCount()) + " moves evaluated");
                        System.out.println(engine.getAverageMoves() + " average moves");
                        System.out.println(engine.getMinimumDepth() + " minimum depth");
                        System.out.println(engine.getMaximumDepth() + " maximum depth");
                        System.out.println(fmt2.format(engine.getAverageDepth()) + " average depth");
                        System.out.println("move = " + move);
                        System.out.println("time used = " + fmt1.format(timeUsed) + "ms");
                        System.out.println("total time used = " + secondsToString(engine.getTotalTimeUsed() / 1000));

                        whoseTurn = whoseTurn == White ? Black : White;
                        evaluateGameStatus(whoseTurn);
                    }
                    else {
                        evaluateGameStatus(whoseTurn);
                        if (!gameOver) {
                            showMessage("Computer cannot move");
                            gameOver = true;
                        }
                    }

                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    player = player == White ? Black : White;
                    engine = engine1;
                    //engine = engine == engine1 ? engine2 : engine1;
                }
                while (mode == Mode.ComputerPlaysComputer && !gameOver && ++moves < 100);
                computerIsThinking = false;
                currentComputerMove = null;
//                progressBar.setVisible(false);
                progressBar.setStringPainted(false);
            }
        }

        //public synchronized void showLookAhead(Board board, Move move, int rating) {
        //    System.out.println(board);
        //    displayBoard.update(board, userColor, rotateBoard);
        //    setStatus(move + ", rating = " + rating);
        //    try {
        //        wait();
        //    }
        //    catch (InterruptedException ignored) {
        //    }
        //}

        public synchronized void continueMoving() {
            notify();
        }
    }

    private void evaluateGameStatus(int whoseTurn) {
        if (BoardUtil.isCheckMated(board.pieces, whoseTurn)) {
            showMessage("Checkmate! " + (whoseTurn == White ? "Black" : "White") + " wins");
            gameOver = true;
        }
        else if (BoardUtil.isStaleMated(board.pieces, whoseTurn)) {
            showMessage((whoseTurn == White ? "White" : "Black") + " is stalemated. Game is a draw");
            gameOver = true;
        }
        else if (BoardUtil.isDrawByRepetition(gameHistory, board.pieces, whoseTurn)) {
            showMessage("Draw by repetition, all possible moves for " + (whoseTurn == White ? "White" : "Black") + " have been repeated twice");
            gameOver = true;
        }
        else {
            int count = gameHistory.getBoardCount(board);
            if (count == 2)
                showMessage("This board position has occurred twice (three times is a draw)");
        }
    }

    private void waitForComputer() {
        while (computerIsThinking) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException ignored) {
            }
        }
    }

    private class MyMenuBar extends JMenuBar {
        public MyMenuBar() {
            super();
            add(new GameMenu());
            add(new MoveMenu());
            add(new EngineMenu());
            add(new StyleMenu());
            add(new ProblemMenu());
//            add(new DebugMenu());
        }

        private class GameMenu extends JMenu {
            public GameMenu() {
                super("Game");
                setMnemonic(KeyEvent.VK_G);

                JMenuItem item = new JMenuItem("New Game");
                add(item);
                item.setToolTipText("Start a new game");
                item.setMnemonic(KeyEvent.VK_N);
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        newGame();
                    }
                });

                addSeparator();

                JRadioButtonMenuItem item1 = new JRadioButtonMenuItem("User plays white");
                add(item1);
                item1.setToolTipText("The user plays white and the computer plays black");
                item1.setMnemonic(KeyEvent.VK_W);
                item1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (mode != Mode.UserPlaysWhite) {
                            mode = Mode.UserPlaysWhite;
                            if (userColor == Black) {
                                waitForComputer();
                                ((JRadioButtonMenuItem)e.getSource()).setSelected(true);
                                userColor = White;
                                displayBoard.update(board, userColor, rotateBoard);
                                if (whoseTurn == Black) {
                                    ComputerMove computerMove = new ComputerMove(engine1, Black);
                                    computerMove.start();
                                }
                            }
                        }
                    }
                });

                JRadioButtonMenuItem item2 = new JRadioButtonMenuItem("User plays black");
                add(item2);
                item2.setToolTipText("The user plays black and the computer plays white");
                item2.setMnemonic(KeyEvent.VK_B);
                item2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (mode != Mode.UserPlaysBlack) {
                            mode = Mode.UserPlaysBlack;
                            if (userColor == White) {
                                waitForComputer();
                                ((JRadioButtonMenuItem)e.getSource()).setSelected(true);
                                userColor = Black;
                                displayBoard.update(board, userColor, rotateBoard);
                                if (whoseTurn == White) {
                                    ComputerMove computerMove = new ComputerMove(engine1, White);
                                    computerMove.start();
                                }
                            }
                        }
                    }
                });

                JRadioButtonMenuItem item3 = new JRadioButtonMenuItem("Computer plays Computer");
//                add(item3);
                item3.setToolTipText("The computer plays both sides");
                item3.setMnemonic(KeyEvent.VK_C);
                item3.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (mode != Mode.ComputerPlaysComputer) {
                            mode = Mode.ComputerPlaysComputer;
                            waitForComputer();
                            ((JRadioButtonMenuItem)e.getSource()).setSelected(true);
                            whoseTurn = whoseTurn == White ? Black : White;
                            mode = Mode.UserPlaysWhite;
                            ComputerMove computerMove = new ComputerMove(engine1, White);
                            computerMove.start();
                        }
                    }
                });

                JRadioButtonMenuItem item4 = new JRadioButtonMenuItem("User plays User");
                add(item4);
                item4.setToolTipText("The user plays both sides");
                item4.setMnemonic(KeyEvent.VK_U);
                item4.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (mode != Mode.UserPlaysUser) {
                            mode = Mode.UserPlaysUser;
                            ((JRadioButtonMenuItem)e.getSource()).setSelected(true);
                        }
                    }
                });

                ButtonGroup group = new ButtonGroup();
                group.add(item1);
                group.add(item2);
                group.add(item3);
                group.add(item4);
                item1.setSelected(true);

                JCheckBoxMenuItem itemA = new JCheckBoxMenuItem("Rotate board");
                add(itemA);
                itemA.setToolTipText("User's pieces are on the top of the board");
                itemA.setMnemonic(KeyEvent.VK_R);
                itemA.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        rotateBoard = !rotateBoard;
                        ((JCheckBoxMenuItem)e.getSource()).setSelected(rotateBoard);
                        displayBoard.update(board, userColor, rotateBoard);
                    }
                });
                itemA.setSelected(false);

                addSeparator();

                item = new JMenuItem("Exit");
                item.setToolTipText("Exit the Chess Program");
                item.setMnemonic(KeyEvent.VK_E);
                add(item);
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Chess.this.dispose();
                    }
                });
            }
        }

        private class MoveMenu extends JMenu {
            public MoveMenu() {
                super("Move");
                setMnemonic(KeyEvent.VK_M);

                JMenuItem item = new JMenuItem("Take back move");
                add(item);
                item.setToolTipText("Take back the last move");
                item.setMnemonic(KeyEvent.VK_Z);
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (!computerIsThinking) {
                            if (gameHistory.getRecordCount() > 0) {
                                Move move = gameHistory.remove();
                                takeBackMoveHistory.addFirst(move);
                                replayMoveMenuItem.setEnabled(true);
                                BoardUtil.unMakeMove(board.pieces, move);
                                whoseTurn = whoseTurn == White ? Black : White;
                                gameOver = false;
                                displayBoard.update(board, userColor, rotateBoard);
                                setStatus("Took back move");
                            }
                        }
                    }
                });

                replayMoveMenuItem = new JMenuItem("Replay move");
                add(replayMoveMenuItem);
                replayMoveMenuItem.setToolTipText("Replay the last move that was taken back");
                replayMoveMenuItem.setMnemonic(KeyEvent.VK_Y);
                replayMoveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
                replayMoveMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (!computerIsThinking) {
                            Move move;
                            if (takeBackMoveHistory.size() > 0) {
                                move = takeBackMoveHistory.removeFirst();
                                setStatus("Replayed move");
                            }
                            else if (anticipatedMoves.size() > 0) {
                                move = anticipatedMoves.removeFirst();
                                RatedMove ratedMove = (RatedMove)move;
                                setStatus("Anticipated move, board rating is " + ratedMove.rating);
                            }
                            else
                                move = null;
                            if (move != null) {
                                BoardUtil.makeMove(board.pieces, move);
                                gameHistory.add(move, board);
                                whoseTurn = whoseTurn == White ? Black : White;
                                displayBoard.update(board, userColor, rotateBoard);
                                evaluateGameStatus(whoseTurn);
                            }
                            if (takeBackMoveHistory.size() == 0 && anticipatedMoves.size() == 0)
                                replayMoveMenuItem.setEnabled(false);
                        }
                    }
                });
                replayMoveMenuItem.setEnabled(false);

                item = new JMenuItem("Suggest move");
                add(item);
                item.setToolTipText("Have the computer make a move");
                item.setMnemonic(KeyEvent.VK_S);
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ComputerMove computerMove = new ComputerMove(engine1, whoseTurn);
                        computerMove.start();
                    }
                });
            }
        }

        private class EngineMenu extends JMenu {
            public EngineMenu() {
                super("Engine");
                JMenuItem item1 = new JRadioButtonMenuItem("Standard Engine");
                add(item1);
                item1.setToolTipText("Use the Standard Engine");
                item1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        engine1 = new StandardEngine(Chess.this);
                        engine1.setDifficulty(difficulty);
                        propertyManager.setProperty("engineName1", "Standard");
                        propertyManager.write();
                    }
                });

                JMenuItem item2 = new JRadioButtonMenuItem("Experimental Engine");
                add(item2);
                item2.setToolTipText("Use the Experimental Engine");
                item2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        engine1 = new ExperimentalEngine(Chess.this);
                        engine1.setDifficulty(difficulty);
                        propertyManager.setProperty("engineName1", "Experimental");
                        propertyManager.write();
                    }
                });

                JMenuItem item3 = new JRadioButtonMenuItem("Original Engine");
                add(item3);
                item3.setToolTipText("Use the Original Engine");
                item3.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        engine1 = new OriginalEngine(Chess.this);
                        engine1.setDifficulty(difficulty);
                        propertyManager.setProperty("engineName1", "Original");
                        propertyManager.write();
                    }
                });

                ButtonGroup group = new ButtonGroup();
                group.add(item1);
                group.add(item2);
                group.add(item3);
                if (engine1 instanceof StandardEngine)
                    item1.setSelected(true);
                else if (engine1 instanceof ExperimentalEngine)
                    item2.setSelected(true);
                else if (engine1 instanceof OriginalEngine)
                    item3.setSelected(true);

                addSeparator();

                item1 = new JRadioButtonMenuItem("Easy");
                add(item1);
                item1.setToolTipText("Set the difficulty level to easy (looks ahead 4 moves)");
                item1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        difficulty = Engine.Difficulty.Easy;
                        Chess.propertyManager.setProperty("difficulty", difficulty);
                        Chess.propertyManager.write();
                        engine1.setDifficulty(difficulty);
                    }
                });

                item2 = new JRadioButtonMenuItem("Medium");
                add(item2);
                item2.setToolTipText("Set the difficulty level to medium (looks ahead 5 moves)");
                item2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        difficulty = Engine.Difficulty.Medium;
                        Chess.propertyManager.setProperty("difficulty", difficulty);
                        Chess.propertyManager.write();
                        engine1.setDifficulty(difficulty);
                    }
                });

                item3 = new JRadioButtonMenuItem("Hard");
                add(item3);
                item3.setToolTipText("Set the difficulty level to hard (looks ahead 6 moves)");
                item3.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        difficulty = Engine.Difficulty.Hard;
                        Chess.propertyManager.setProperty("difficulty", difficulty);
                        Chess.propertyManager.write();
                        engine1.setDifficulty(difficulty);
                    }
                });

                JMenuItem item4 = new JRadioButtonMenuItem("Extra Hard");
                add(item4);
                item4.setToolTipText("Set the difficulty level to extra hard (looks ahead 7 moves)");
                item4.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        difficulty = Engine.Difficulty.ExtraHard;
                        Chess.propertyManager.setProperty("difficulty", difficulty);
                        Chess.propertyManager.write();
                        engine1.setDifficulty(difficulty);
                    }
                });

                group = new ButtonGroup();
                group.add(item1);
                group.add(item2);
                group.add(item3);
                group.add(item4);
                switch (engine1.getDifficulty()) {
                    case Easy:
                        item1.setSelected(true);
                        break;
                    case Medium:
                        item2.setSelected(true);
                        break;
                    case Hard:
                        item3.setSelected(true);
                        break;
                    case ExtraHard:
                        item4.setSelected(true);
                        break;
                }
            }
        }

        private class StyleMenu extends JMenu {
            public StyleMenu() {
                super("Style");
                setMnemonic(KeyEvent.VK_S);

                JMenuItem item1 = new JRadioButtonMenuItem("Tan and Brown");
                add(item1);
                item1.setToolTipText("Use tan and brown pieces");
                item1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            displayBoard.setStyle("tanBrown");
                            displayBoard.update(board, userColor, rotateBoard);
                        }
                        catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                JMenuItem item2 = new JRadioButtonMenuItem("White and Black");
                add(item2);
                item1.setToolTipText("Use white and black pieces");
                item2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            displayBoard.setStyle("whiteBlack");
                            displayBoard.update(board, userColor, rotateBoard);
                        }
                        catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                ButtonGroup group = new ButtonGroup();
                group.add(item1);
                group.add(item2);
                item1.setSelected(true);
            }
        }

        private class ProblemMenu extends JMenu {
            public ProblemMenu() {
                super("Problem");
                setMnemonic(KeyEvent.VK_D);

                for (Problems.Problem problem : Problems.getProblemList()) {
                    final Problems.Problem problemFinal = problem;
                    JMenuItem item = new JMenuItem(problem.getName());
                    add(item);
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            setUpProblem(problemFinal);
                        }
                    });
                }
            }
        }

        private class DebugMenu extends JMenu {
            public DebugMenu() {
                super("Debug");
                setMnemonic(KeyEvent.VK_D);

                JMenuItem item = new JCheckBoxMenuItem("Show look ahead");
                add(item);
                item.setToolTipText("Have the computer show each move as it looks ahead");
                item.setMnemonic(KeyEvent.VK_L);
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showLookAhead = !showLookAhead;
                        ((JCheckBoxMenuItem)e.getSource()).setSelected(showLookAhead);
                    }
                });

                item = new JMenuItem("Continue");
                add(item);
                item.setToolTipText("Continue to the next look ahead move");
                item.setMnemonic(KeyEvent.VK_C);
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (currentComputerMove != null)
                            currentComputerMove.continueMoving();
                    }
                });
            }
        }
    }

    private static class MyProgressBar extends JProgressBar {
        public MyProgressBar() {
            super();
        }

        public Dimension getPreferredSize() {
            return new Dimension(75, 15);
        }

        public Dimension getMaximumSize() {
            return new Dimension(75, 15);
        }
    }

    /**
     * Prints a time in seconds in a human-readable format
     */
    private static String secondsToString(long seconds) {
        long days = seconds / (24 * 60 * 60);
        seconds -= days * 24 * 60 * 60;

        long hours = seconds / (60 * 60);
        seconds -= hours * 60 * 60;

        long minutes = seconds / 60;
        seconds -= minutes * 60;

        StringBuilder b = new StringBuilder();
        if (days > 0) {
            b.append(days);
            b.append(" day");
            if (days != 1)
                b.append('s');
            b.append(", ");

            b.append(hours);
            b.append(" hour");
            if (hours != 1)
                b.append('s');
        }
        else if (hours > 0) {
            b.append(hours);
            b.append(" hour");
            if (hours != 1)
                b.append('s');
            b.append(", ");

            b.append(minutes);
            b.append(" minute");
            if (minutes != 1)
                b.append('s');
        }
        else if (minutes > 0) {
            b.append(minutes);
            b.append(" minute");
            if (minutes != 1)
                b.append('s');
            b.append(", ");

            b.append(seconds);
            b.append(" second");
            if (seconds != 1)
                b.append('s');
        }
        else {
            b.append(seconds);
            b.append(" second");
            if (seconds != 1)
                b.append('s');
        }
        return b.toString();
    }
}
