import javax.swing.*;
import java.awt.*;
import Pieces.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class GUI {

    // Game Variables
    private static Piece[][] board;
    private ArrayList<Pair<Integer, Integer, Boolean>> moves = new ArrayList<>();
    private ArrayList<Pair<Integer, Integer, Boolean>> checkSquares = new ArrayList<>();
    private int lastValidX = -1;
    private int lastValidY = -1;
    private boolean isWhitesTurn = true;
    private boolean isOver = false;
    private boolean didMove = true;

    // GUI Components
    private JPanel pnlMain;

    public GUI() {
        pnlMain = new JPanel();
        pnlMain.setLayout(new GridBagLayout());

        if (!isOver)
            createPanel();
    }

    // Create GUI
    void createPanel() {
        pnlMain.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    if (isOver)
                        board[i][j].isKillable = true;
                    else {
                        if (isWhitesTurn && board[i][j].color == 'b')
                            board[i][j].isKillable = true;
                        else if (!isWhitesTurn && board[i][j].color == 'w')
                            board[i][j].isKillable = true;

                        if (isWhitesTurn && board[i][j].color == 'w')
                            board[i][j].isKillable = false;
                        else if (!isWhitesTurn && board[i][j].color == 'b')
                            board[i][j].isKillable = false;
                    }
                }
                JLabel label = new JLabel();
                if (board[i][j] != null) {
                    label.setIcon(
                            new ImageIcon(
                                    board[i][j].getSprite().getImage().getScaledInstance(74, 74,
                                            Image.SCALE_DEFAULT)));
                    if (!board[i][j].isKillable) {
                        label.addMouseListener(onPressEvent(j, i));
                    } else {
                        label.setName(j + " " + i);
                        label.addMouseListener(move(j, i, label));
                    }
                } else {
                    label.setIcon(new ImageIcon(
                            new ImageIcon("src\\Pieces\\blank.png").getImage().getScaledInstance(74, 74,
                                    Image.SCALE_DEFAULT)));
                    label.setName(j + " " + i);
                    label.addMouseListener(move(j, i, label));
                }
                label.setOpaque(true);
                label.setBorder(BorderFactory.createLineBorder(Color.black));

                int isMoveSquare = movesContains(j, i);
                if (isMoveSquare == 0)
                    label.setBackground(Color.BLUE);
                else if (isMoveSquare == 1)
                    label.setBackground(Color.RED);
                else if (i == lastValidY && j == lastValidX && !didMove) {
                    label.setBackground(Color.YELLOW);
                } else {
                    if ((i + j) % 2 == 1)
                        label.setBackground(Color.BLACK);
                    else
                        label.setBackground(Color.WHITE);
                }

                // GUI Properties
                gbc = new GridBagConstraints();
                gbc.gridx = j;
                gbc.gridy = i;
                gbc.weightx = 1;
                if (j == 0)
                    gbc.insets = new Insets(0, 350, 0, 0);
                if (j == 7)
                    gbc.insets = new Insets(0, 0, 0, 350);
                pnlMain.add(label, gbc);
            }
        }

        pnlMain.revalidate();
    }

    // Returns -1, 0, or 1 determining if moves contains the given x and y and
    // telling whether it is a kill tile
    public int movesContains(int x, int y) {
        for (Pair<Integer, Integer, Boolean> e : moves) {
            if (e.x == x && e.y == y)
                return e.isKill ? 1 : 0;
        }
        return -1;
    }

    // Mouse Move Logic
    public MouseAdapter move(int x, int y, JLabel label) {
        return new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                String name = label.getName();
                int x = Integer.parseInt(name.split(" ")[0]);
                int y = Integer.parseInt(name.split(" ")[1]);
                int contains = movesContains(x, y);

                if (contains == 1 && board[y][x] != null && board[y][x].pieceType == 'i')
                    isOver = true;

                if (contains != -1) {
                    didMove = true;

                    // Pawn En Passant Logic
                    if (board[lastValidY][lastValidX].pieceType == 'p' && Math.abs(lastValidY - y) > 1)
                        board[lastValidY][lastValidX].lastMovedTwice = true;
                    else
                        board[lastValidY][lastValidX].lastMovedTwice = false;

                    if (board[lastValidY][lastValidX].pieceType == 'p' && lastValidX != x) {
                        if (board[lastValidY][lastValidX].color == 'w') {
                            board[y][x] = board[lastValidY][lastValidX];
                            board[lastValidY][lastValidX] = null;
                            board[y + 1][x] = null;
                        } else {
                            board[y][x] = board[lastValidY][lastValidX];
                            board[lastValidY][lastValidX] = null;
                            board[y - 1][x] = null;
                        }
                        isWhitesTurn = !isWhitesTurn;
                        moves.clear();
                        createPanel();
                    } else {
                        // Basic Move Logic
                        if (board[lastValidY][lastValidX].hasNotMoved)
                            board[lastValidY][lastValidX].hasNotMoved = false;
                        board[y][x] = board[lastValidY][lastValidX];
                        board[lastValidY][lastValidX] = null;
                        isWhitesTurn = !isWhitesTurn;
                        moves.clear();
                        createPanel();
                    }
                }
            }
        };
    }

    // Mouse Logic
    public MouseAdapter onPressEvent(int x, int y) {
        Piece piece = board[y][x];
        switch (piece.pieceType) {

            // Pawn Moves
            case 'p':
                return pawnMoves(x, y, piece);

            // Knight Moves
            case 'k':
                return knightMoves(x, y, piece);

            // Rook Moves
            case 'r':
                return rookMoves(x, y, piece);

            // Bishop Moves
            case 'b':
                return bishopMoves(x, y, piece);

            // Queen Moves
            case 'q':
                return queenMoves(x, y, piece);

            // King Moves
            case 'i':
                return kingMoves(x, y, piece);

            default:
                return new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println("Invalid Event");
                    }
                };
        }
    }

    // Pawn Moves
    public MouseAdapter pawnMoves(int x, int y, Piece piece) {
        // White Pieces
        if (piece.color == 'w') {
            return new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    didMove = false;
                    if (y == 0)
                        return;
                    moves.clear();
                    lastValidX = x;
                    lastValidY = y;

                    // First Move
                    if (piece.hasNotMoved) {
                        if (board[y - 1][x] == null) {
                            moves.add(new Pair(x, y - 1, false));
                            if (board[y - 2][x] == null)
                                moves.add(new Pair(x, y - 2, false));
                        }
                    } else if (board[y - 1][x] == null)
                        moves.add(new Pair(x, y - 1, false));

                    // En Passant
                    if (x < 7 && board[y][x + 1] != null && board[y][x + 1].lastMovedTwice
                            && board[y - 1][x + 1] == null && piece.color != board[y][x + 1].color) {
                        moves.add(new Pair(x + 1, y - 1, true));
                    }
                    if (x > 0 && board[y][x - 1] != null && board[y][x - 1].lastMovedTwice
                            && board[y - 1][x - 1] == null && piece.color != board[y][x - 1].color) {
                        moves.add(new Pair(x - 1, y - 1, true));
                    }

                    // Basic Moves
                    if (x > 0 && board[y - 1][x - 1] != null && board[y - 1][x - 1].color != piece.color)
                        moves.add(new Pair(x - 1, y - 1, true));
                    if (x < 7 && board[y - 1][x + 1] != null && board[y - 1][x + 1].color != piece.color)
                        moves.add(new Pair(x + 1, y - 1, true));
                    createPanel();
                    return;
                }
            };

            // Black Pieces
        } else {
            return new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    didMove = false;
                    if (y == 7)
                        return;
                    moves.clear();
                    lastValidX = x;
                    lastValidY = y;

                    if (piece.hasNotMoved) {
                        if (board[y + 1][x] == null) {
                            moves.add(new Pair(x, y + 1, false));
                            if (board[y + 2][x] == null)
                                moves.add(new Pair(x, y + 2, false));
                        }
                    } else if (board[y + 1][x] == null)
                        moves.add(new Pair(x, y + 1, false));

                    if (x < 7 && board[y][x + 1] != null && board[y][x + 1].lastMovedTwice
                            && board[y + 1][x + 1] == null && piece.color != board[y][x + 1].color) {
                        moves.add(new Pair(x + 1, y + 1, true));
                    }
                    if (x > 0 && board[y][x - 1] != null && board[y][x - 1].lastMovedTwice
                            && board[y + 1][x - 1] == null && piece.color != board[y][x - 1].color) {
                        moves.add(new Pair(x - 1, y + 1, true));
                    }

                    if (x > 0 && board[y + 1][x - 1] != null && board[y + 1][x - 1].color != piece.color)
                        moves.add(new Pair(x - 1, y + 1, true));
                    if (x < 7 && board[y + 1][x + 1] != null && board[y + 1][x + 1].color != piece.color)
                        moves.add(new Pair(x + 1, y + 1, true));
                    createPanel();
                    return;
                }

            };
        }
    }

    // Knight Moves
    public MouseAdapter knightMoves(int x, int y, Piece piece) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                didMove = false;
                moves.clear();
                lastValidX = x;
                lastValidY = y;

                // Vertical Moves
                for (int j = -1; j <= 1; j += 2) {
                    for (int i = -2; i <= 2; i += 4) {
                        int newX = x + j;
                        int newY = y + i;
                        boolean isValid = newX >= 0 && newX <= 7 && newY >= 0 && newY <= 7;
                        if (isValid) {
                            if (board[newY][newX] != null && board[newY][newX].color != piece.color)
                                moves.add(new Pair(newX, newY, true));
                            else if (board[newY][newX] == null)
                                moves.add(new Pair(newX, newY, false));
                        }
                    }
                }

                // Horizontal Moves
                for (int j = -2; j <= 2; j += 4) {
                    for (int i = -1; i <= 1; i += 2) {
                        int newX = x + j;
                        int newY = y + i;
                        boolean isValid = newX >= 0 && newX <= 7 && newY >= 0 && newY <= 7;
                        if (isValid) {
                            if (board[newY][newX] != null && board[newY][newX].color != piece.color)
                                moves.add(new Pair(newX, newY, true));
                            else if (board[newY][newX] == null)
                                moves.add(new Pair(newX, newY, false));
                        }
                    }
                }

                createPanel();
                return;
            }
        };
    }

    // Rook Moves
    public MouseAdapter rookMoves(int x, int y, Piece piece) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                didMove = false;
                moves.clear();
                lastValidX = x;
                lastValidY = y;

                // Horizontal Moves
                // Right
                int curX = x + 1;
                while (curX < 8) {
                    if (board[y][curX] != null) {
                        if (board[y][curX].color != piece.color)
                            moves.add(new Pair(curX, y, true));
                        curX = 8;
                    } else
                        moves.add(new Pair(curX, y, false));
                    curX++;
                }

                // Left
                curX = x - 1;
                while (curX >= 0) {
                    if (board[y][curX] != null) {
                        if (board[y][curX].color != piece.color)
                            moves.add(new Pair(curX, y, true));
                        curX = -1;
                    } else
                        moves.add(new Pair(curX, y, false));
                    curX--;
                }

                // Up
                int curY = y - 1;
                while (curY >= 0) {
                    if (board[curY][x] != null) {
                        if (board[curY][x].color != piece.color)
                            moves.add(new Pair(x, curY, true));
                        curY = -1;
                    } else
                        moves.add(new Pair(x, curY, false));
                    curY--;
                }

                // Down
                curY = y + 1;
                while (curY < 8) {
                    if (board[curY][x] != null) {
                        if (board[curY][x].color != piece.color)
                            moves.add(new Pair(x, curY, true));
                        curY = 8;
                    } else
                        moves.add(new Pair(x, curY, false));
                    curY++;
                }

                createPanel();
                return;
            }
        };
    }

    // Bishop Moves
    public MouseAdapter bishopMoves(int x, int y, Piece piece) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                didMove = false;
                moves.clear();
                lastValidX = x;
                lastValidY = y;

                for (int i = -1; i <= 1; i += 2) {
                    for (int j = -1; j <= 1; j += 2) {
                        int count = 1;
                        int newX = x + i * count;
                        int newY = y + j * count;
                        while (((newX >= 0) && (newX <= 7)) && ((newY >= 0) && (newY <= 7))) {
                            if (board[newY][newX] == null) {
                                moves.add(new Pair(newX, newY, false));
                                count++;
                            } else if (board[newY][newX].color != piece.color) {
                                moves.add(new Pair(newX, newY, true));
                                break;
                            } else
                                break;
                            newX = x + i * count;
                            newY = y + j * count;
                        }
                    }
                }

                createPanel();
                return;
            }
        };
    }

    // Queen Moves
    public MouseAdapter queenMoves(int x, int y, Piece piece) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                didMove = false;
                moves.clear();
                lastValidX = x;
                lastValidY = y;

                // A queen is a composition of rook and bishop so both their moves are run
                // Rook moves

                // Horizontal Moves
                // Right
                int curX = x + 1;
                while (curX < 8) {
                    if (board[y][curX] != null) {
                        if (board[y][curX].color != piece.color)
                            moves.add(new Pair(curX, y, true));
                        curX = 8;
                    } else
                        moves.add(new Pair(curX, y, false));
                    curX++;
                }

                // Left
                curX = x - 1;
                while (curX >= 0) {
                    if (board[y][curX] != null) {
                        if (board[y][curX].color != piece.color)
                            moves.add(new Pair(curX, y, true));
                        curX = -1;
                    } else
                        moves.add(new Pair(curX, y, false));
                    curX--;
                }

                // Up
                int curY = y - 1;
                while (curY >= 0) {
                    if (board[curY][x] != null) {
                        if (board[curY][x].color != piece.color)
                            moves.add(new Pair(x, curY, true));
                        curY = -1;
                    } else
                        moves.add(new Pair(x, curY, false));
                    curY--;
                }

                // Down
                curY = y + 1;
                while (curY < 8) {
                    if (board[curY][x] != null) {
                        if (board[curY][x].color != piece.color)
                            moves.add(new Pair(x, curY, true));
                        curY = 8;
                    } else
                        moves.add(new Pair(x, curY, false));
                    curY++;
                }

                // Bishop Moves
                for (int i = -1; i <= 1; i += 2) {
                    for (int j = -1; j <= 1; j += 2) {
                        int count = 1;
                        int newX = x + i * count;
                        int newY = y + j * count;
                        while (((newX >= 0) && (newX <= 7)) && ((newY >= 0) && (newY <= 7))) {
                            if (board[newY][newX] == null) {
                                moves.add(new Pair(newX, newY, false));
                                count++;
                            } else if (board[newY][newX].color != piece.color) {
                                moves.add(new Pair(newX, newY, true));
                                break;
                            } else
                                break;
                            newX = x + i * count;
                            newY = y + j * count;
                        }
                    }
                }

                createPanel();
                return;
            }
        };

    }

    // King Moves
    public MouseAdapter kingMoves(int x, int y, Piece piece) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                didMove = false;
                moves.clear();
                lastValidX = x;
                lastValidY = y;

                // Performing a basic BFS to search all adjacent tiles
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0)
                            continue;
                        int newX = x + i;
                        int newY = y + j;
                        if (newY < 0 || newY > 7 || newX < 0 || newX > 7)
                            continue;
                        if (board[newY][newX] == null)
                            moves.add(new Pair(newX, newY, false));
                        else if (board[newY][newX].color != piece.color)
                            moves.add(new Pair(newX, newY, true));

                    }
                }

                createPanel();
                return;
            }
        };

    }

    // Generate Starting Board
    public static Piece[][] initializeStartingBoard(boolean isWhite) {
        Piece[][] boardToReturn = new Piece[8][8];
        if (isWhite) {
            // Black Side
            boardToReturn[0][0] = new Rook('b');
            boardToReturn[0][1] = new Knight('b');
            boardToReturn[0][2] = new Bishop('b');
            boardToReturn[0][3] = new Queen('b');
            boardToReturn[0][4] = new King('b');
            boardToReturn[0][5] = new Bishop('b');
            boardToReturn[0][6] = new Knight('b');
            boardToReturn[0][7] = new Rook('b');
            for (int i = 0; i < 8; i++)
                boardToReturn[1][i] = new Pawn('b');

            // White Side
            boardToReturn[7][0] = new Rook('w');
            boardToReturn[7][1] = new Knight('w');
            boardToReturn[7][2] = new Bishop('w');
            boardToReturn[7][3] = new Queen('w');
            boardToReturn[7][4] = new King('w');
            boardToReturn[7][5] = new Bishop('w');
            boardToReturn[7][6] = new Knight('w');
            boardToReturn[7][7] = new Rook('w');
            for (int i = 0; i < 8; i++)
                boardToReturn[6][i] = new Pawn('w');
        }

        return boardToReturn;
    }

    // UI Creation Method
    public JPanel getUI() {
        return pnlMain;
    }

    // Main method to compile UI and execute program
    public static void main(String[] args) {
        board = initializeStartingBoard(true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Chess");
                frame.getContentPane().add(new GUI().getUI()).setBackground(new Color(150, 75, 0, 255));
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setExtendedState(Frame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }
        });
    }
}