package com.carlgrundstrom.chess;

import static com.carlgrundstrom.chess.BoardUtil.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;

public class DisplayBoard extends JPanel {
    private String style;
    private JLabel[][] pieces;
//    private Color lightSquareColor = new Color(185, 185, 185);
//    private Color darkSquareColor = new Color(102, 102, 102);
    private HashMap<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();
    private double iconScale;
    private int tallestPieceHeight;
    private int shortestPieceHeight;
    private Image lightSquareImage;
    private Image darkSquareImage;

    public static final int squareSizeInPixels = 96;

    private static ClassLoader classLoader = Chess.class.getClassLoader();

    public DisplayBoard(String style) throws Exception {
        super(new GridLayout(8, 8));
        this.style = style;

        pieces = new JLabel[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                pieces[i][j] = new JLabel();
        initializeIconCache();

        for (int i = 7; i >= 0; i--)
            for (int j = 0; j < 8; j++)
                add(pieces[i][j]);
    }

    public void setStyle(String style) throws Exception {
        this.style = style;
        initializeIconCache();
    }

    private void initializeIconCache() throws Exception {
        String shortestPiece = "com/carlgrundstrom/chess/images/" + style + "/WhitePawn.png";
        URL iconURL = classLoader.getResource(shortestPiece);
        if (iconURL == null)
            throw new Exception("Error loading image: " + shortestPiece);
        ImageIcon icon = new ImageIcon(iconURL);
        shortestPieceHeight = icon.getIconHeight();

        String tallestPiece = "com/carlgrundstrom/chess/images/" + style + "/WhiteKing.png";
        iconURL = classLoader.getResource(tallestPiece);
        if (iconURL == null)
            throw new Exception("Error loading image: " + tallestPiece);
        icon = new ImageIcon(iconURL);
        tallestPieceHeight = icon.getIconHeight();

        iconScale = (double)(squareSizeInPixels - 4) / (double)tallestPieceHeight;

        String lightSquare = "com/carlgrundstrom/chess/images/" + style + "/LightSquare.png";
        iconURL = classLoader.getResource(lightSquare);
        if (iconURL == null)
            throw new Exception("Error loading image: " + lightSquare);
        icon = new ImageIcon(iconURL);
        lightSquareImage = icon.getImage();

        String darkSquare = "com/carlgrundstrom/chess/images/" + style + "/DarkSquare.png";
        iconURL = classLoader.getResource(darkSquare);
        if (iconURL == null)
            throw new Exception("Error loading image: " + darkSquare);
        icon = new ImageIcon(iconURL);
        darkSquareImage = icon.getImage();

        iconCache.put("SpaceLight", new ImageIcon(lightSquareImage));
        iconCache.put("SpaceDark", new ImageIcon(darkSquareImage));

        addIcon("WhitePawn", "com/carlgrundstrom/chess/images/" + style + "/WhitePawn.png");
        addIcon("WhiteRook", "com/carlgrundstrom/chess/images/" + style + "/WhiteRook.png");
        addIcon("WhiteKnight", "com/carlgrundstrom/chess/images/" + style + "/WhiteKnight.png");
        addIcon("WhiteBishop", "com/carlgrundstrom/chess/images/" + style + "/WhiteBishop.png");
        addIcon("WhiteQueen", "com/carlgrundstrom/chess/images/" + style + "/WhiteQueen.png");
        addIcon("WhiteKing", "com/carlgrundstrom/chess/images/" + style + "/WhiteKing.png");
        addIcon("BlackPawn", "com/carlgrundstrom/chess/images/" + style + "/BlackPawn.png");
        addIcon("BlackRook", "com/carlgrundstrom/chess/images/" + style + "/BlackRook.png");
        addIcon("BlackKnight", "com/carlgrundstrom/chess/images/" + style + "/BlackKnight.png");
        addIcon("BlackBishop", "com/carlgrundstrom/chess/images/" + style + "/BlackBishop.png");
        addIcon("BlackQueen", "com/carlgrundstrom/chess/images/" + style + "/BlackQueen.png");
        addIcon("BlackKing", "com/carlgrundstrom/chess/images/" + style + "/BlackKing.png");
    }

    private void addIcon(String name, String path) throws Exception {
        URL iconURL = classLoader.getResource(path);
        if (iconURL == null)
            throw new Exception("Error loading image: " + path);
        ImageIcon icon = new ImageIcon(iconURL);
        Image image = icon.getImage();
        int width = image.getWidth(null);
        int height = image.getHeight(null);

//        double p = (double)(height - shortestPieceHeight / 2) / (double)(tallestPieceHeight - shortestPieceHeight / 2);
//        double iconScale2;
//        if (iconScale < 1.0)
//            iconScale2 = 1.0 - p * (1.0 - iconScale);
//        else
//            iconScale2 = iconScale; //1.0 + p * (iconScale - 1.0);

        int newWidth = (int)Math.round(width * iconScale);
        int newHeight = (int)Math.round(height * iconScale);

        int x = squareSizeInPixels / 2 - newWidth / 2;
        int y = squareSizeInPixels / 2 - newHeight / 2;

        BufferedImage b;
        Graphics2D g;

        b = new BufferedImage(squareSizeInPixels, squareSizeInPixels, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D)b.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(lightSquareImage, 0, 0, squareSizeInPixels, squareSizeInPixels, null);
        g.drawImage(image, x, y, newWidth, newHeight, null);
        g.dispose();
        iconCache.put(name + "Light", new ImageIcon(b));

        b = new BufferedImage(squareSizeInPixels, squareSizeInPixels, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D)b.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(darkSquareImage, 0, 0, squareSizeInPixels, squareSizeInPixels, null);
        g.drawImage(image, x, y, newWidth, newHeight, null);
        g.dispose();
        iconCache.put(name + "Dark", new ImageIcon(b));
    }

    public void update(Board board, int userColor, boolean rotateBoard) {
        if (rotateBoard)
            userColor *= -1;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String piece;
                int i2 = userColor == White ? i : 7 - i;
                int j2 = userColor == White ? j : 7 - j;
                switch (board.pieces[i2][j2]) {
                    case WhitePawn:
                        piece = "WhitePawn";
                        break;
                    case WhiteRook:
                        piece = "WhiteRook";
                        break;
                    case WhiteKnight:
                        piece = "WhiteKnight";
                        break;
                    case WhiteBishop:
                        piece = "WhiteBishop";
                        break;
                    case WhiteQueen:
                        piece = "WhiteQueen";
                        break;
                    case WhiteKing:
                        piece = "WhiteKing";
                        break;
                    case BlackPawn:
                        piece = "BlackPawn";
                        break;
                    case BlackRook:
                        piece = "BlackRook";
                        break;
                    case BlackKnight:
                        piece = "BlackKnight";
                        break;
                    case BlackBishop:
                        piece = "BlackBishop";
                        break;
                    case BlackQueen:
                        piece = "BlackQueen";
                        break;
                    case BlackKing:
                        piece = "BlackKing";
                        break;
                    default:
                        piece = "Space";
                }
                String squareColor = (i2 % 2 == 0) ^ (j2 % 2 == 0) ? "Light" : "Dark";
                pieces[i][j].setIcon(iconCache.get(piece + squareColor));
            }
        }
    }

}
