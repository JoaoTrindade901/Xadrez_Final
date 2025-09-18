
package view;

import controller.ChessAI;
import controller.Game;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import model.board.Move;
import model.board.Position;
import model.pieces.Piece;

public class ChessGUI extends JFrame {
    private Game game;
    private JPanel boardPanel;
    private JButton[][] squares;
    private Map<String, ImageIcon> pieceIcons;
    private JTextArea moveHistoryTextArea;
    private JLabel turnLabel;
    private Color lightSquareColor;
    private Color darkSquareColor;
    private String piecesTheme;
    private ChessAI ai;
    private boolean playAgainstAI;
    private boolean aiPlaysWhite;

    public ChessGUI() {
        game = new Game();
        ai = new ChessAI(game);
        ai.setDifficulty(ChessAI.Difficulty.MEDIUM); // Definir dificuldade padrão como média
        playAgainstAI = true; // Sempre iniciar contra a IA
        aiPlaysWhite = false; // IA joga com as peças pretas
        piecesTheme = "classic"; 

        // Tema inicial
        lightSquareColor = new Color(200, 200, 255); // azul claro
        darkSquareColor = new Color(100, 149, 237);  // azul escuro

        initializeGUI();
        loadPieceIcons();
        updateBoardDisplay();
        // Garantir que a IA faça seu movimento se necessário
        SwingUtilities.invokeLater(() -> {
            playAIMoveIfNeeded();
        });
    }

    private void initializeGUI() {
        setTitle("Jogo de Xadrez em Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 630);
        setLayout(new BorderLayout());

        // PAINEL SUPERIOR
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(600, 30));
        turnLabel = new JLabel("Turno: Brancas");
        turnLabel.setHorizontalAlignment(JLabel.CENTER);
        infoPanel.add(turnLabel);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton newGameButton = new JButton("Novo Jogo");
        newGameButton.addActionListener(e -> startNewGame(true, false));
        controlPanel.add(newGameButton);

        JButton undoButton = new JButton("Desfazer");
        undoButton.addActionListener(e -> {
            boolean undone = game.undoLastMove();
            if (undone) {
                updateBoardDisplay();
                updateMoveHistory();
                turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));
            } else {
                JOptionPane.showMessageDialog(this, "Não há jogadas para desfazer!");
            }
        });
        controlPanel.add(undoButton);

        JButton saveButton = new JButton("Salvar");
        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar Jogo");
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                game.saveGame(fileToSave.getAbsolutePath());
            }
        });
        controlPanel.add(saveButton);

        JButton loadButton = new JButton("Carregar");
        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Carregar Jogo");
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToLoad = fileChooser.getSelectedFile();
                Game loadedGame = Game.loadGame(fileToLoad.getAbsolutePath());
                if (loadedGame != null) {
                    game = loadedGame;
                    updateBoardDisplay();
                    updateMoveHistory();
                    turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));
                }
            }
        });
        controlPanel.add(loadButton);

        topPanel.add(controlPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // TABULEIRO
        boardPanel = new JPanel(new GridLayout(8, 8));
        squares = new JButton[8][8];
        boolean isWhite = true;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = new JButton();
                squares[row][col].setPreferredSize(new Dimension(70, 70));
                squares[row][col].setBackground(isWhite ? lightSquareColor : darkSquareColor);

                final int r = row;
                final int c = col;
                squares[row][col].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleSquareClick(r, c);
                    }
                });

                boardPanel.add(squares[row][col]);
                isWhite = !isWhite;
            }
            isWhite = !isWhite;
        }
        add(boardPanel, BorderLayout.CENTER);

        // HISTÓRICO DE MOVIMENTOS
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(150, 600));
        JLabel historyLabel = new JLabel("Histórico de Movimentos");
        historyLabel.setHorizontalAlignment(JLabel.CENTER);
        rightPanel.add(historyLabel, BorderLayout.NORTH);
        moveHistoryTextArea = new JTextArea();
        moveHistoryTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(moveHistoryTextArea);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // MENU
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Jogo");

        JMenuItem humanVsHuman = new JMenuItem("Humano vs Humano");
        humanVsHuman.addActionListener(e -> startNewGame(false, false));
        gameMenu.add(humanVsHuman);

        JMenuItem humanVsAI = new JMenuItem("Computador vs Humano (Brancas)");
        humanVsAI.addActionListener(e -> startNewGame(true, false));
        gameMenu.add(humanVsAI);

        JMenuItem aiVsHuman = new JMenuItem("Computador vs Humano (Pretas)");
        aiVsHuman.addActionListener(e -> startNewGame(true, true));
        gameMenu.add(aiVsHuman);

        JMenuItem newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.addActionListener(e -> startNewGame(true, false));
        gameMenu.add(newGameItem);
        
        // Menu de dificuldade da IA
        JMenu difficultyMenu = new JMenu("Dificuldade da IA");
        
        JMenuItem easyItem = new JMenuItem("Fácil");
        easyItem.addActionListener(e -> {
            ai.setDifficulty(ChessAI.Difficulty.EASY);
            JOptionPane.showMessageDialog(this, "Dificuldade da IA alterada para Fácil");
        });
        difficultyMenu.add(easyItem);
        
        JMenuItem mediumItem = new JMenuItem("Médio");
        mediumItem.addActionListener(e -> {
            ai.setDifficulty(ChessAI.Difficulty.MEDIUM);
            JOptionPane.showMessageDialog(this, "Dificuldade da IA alterada para Médio");
        });
        difficultyMenu.add(mediumItem);
        
        JMenuItem hardItem = new JMenuItem("Difícil");
        hardItem.addActionListener(e -> {
            ai.setDifficulty(ChessAI.Difficulty.HARD);
            JOptionPane.showMessageDialog(this, "Dificuldade da IA alterada para Difícil");
        });
        difficultyMenu.add(hardItem);
        
        JMenuItem expertItem = new JMenuItem("Especialista");
        expertItem.addActionListener(e -> {
            ai.setDifficulty(ChessAI.Difficulty.EXPERT);
            JOptionPane.showMessageDialog(this, "Dificuldade da IA alterada para Especialista");
        });
        difficultyMenu.add(expertItem);
        
        gameMenu.addSeparator();
        gameMenu.add(difficultyMenu);

        // MENU DE CORES
        JMenu themeMenu = new JMenu("Cores do Tabuleiro");

        JMenuItem azul = new JMenuItem("Azul");
        azul.addActionListener(e -> setBoardColors(new Color(200, 200, 255), new Color(100, 149, 237)));
        themeMenu.add(azul);

        JMenuItem verde = new JMenuItem("Verde");
        verde.addActionListener(e -> setBoardColors(new Color(235, 236, 208), new Color(119, 149, 86)));
        themeMenu.add(verde);

        JMenuItem cinza = new JMenuItem("Vermelho");
        cinza.addActionListener(e -> setBoardColors(new Color(0, 0, 0), new Color(255, 0, 0)));
        themeMenu.add(cinza);

        JMenuItem marrom = new JMenuItem("Marrom");
        marrom.addActionListener(e -> setBoardColors(new Color(240, 217, 181), new Color(181, 136, 99)));
        themeMenu.add(marrom);

        JMenuItem preto = new JMenuItem("Preto");
        preto.addActionListener(e -> setBoardColors(new Color(180, 180, 180), new Color(50, 50, 50)));
        themeMenu.add(preto);

        JMenuItem neon = new JMenuItem("Neon");
        neon.addActionListener(e -> setBoardColors(new Color(180, 255, 180), new Color(0, 150, 0)));
        themeMenu.add(neon);

        menuBar.add(gameMenu);
        menuBar.add(themeMenu);
        setJMenuBar(menuBar);

        setLocationRelativeTo(null);
        setVisible(true);

        playAIMoveIfNeeded();
    }

    private void setBoardColors(Color light, Color dark) {
        lightSquareColor = light;
        darkSquareColor = dark;
        updateBoardColors();
    }

    private void startNewGame(boolean againstAI, boolean aiWhite) {
        // Sempre iniciar contra a IA, independente do parâmetro recebido
        playAgainstAI = true;
        aiPlaysWhite = aiWhite;
        
        // Salvar a dificuldade atual antes de criar uma nova instância da IA
        ChessAI.Difficulty currentDifficulty = ChessAI.Difficulty.MEDIUM;
        if (ai != null) {
            currentDifficulty = ai.getDifficulty();
        }
        
        game = new Game();
        ai = new ChessAI(game);
        ai.setDifficulty(currentDifficulty); // Manter a dificuldade atual
        
        updateBoardDisplay();
        updateMoveHistory();
        turnLabel.setText("Turno: Brancas");
        
        // Garantir que a IA faça seu movimento se necessário
        SwingUtilities.invokeLater(() -> {
            playAIMoveIfNeeded();
        });
    }

    private void updateBoardColors() {
        boolean isWhite = true;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setBackground(isWhite ? lightSquareColor : darkSquareColor);
                isWhite = !isWhite;
            }
            isWhite = !isWhite;
        }
    }

    private void updateBoardDisplay() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = game.getBoard().getPieceAt(new Position(row, col));
                if (piece == null) {
                    squares[row][col].setIcon(null);
                } else {
                    squares[row][col].setIcon(pieceIcons.get(getPieceKey(piece)));
                }
                squares[row][col].setBorder(null);
            }
        }
    }

    private void updateMoveHistory() {
        List<Move> history = game.getMoveHistory();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            if (i % 2 == 0)
                sb.append((i / 2) + 1).append(". ");
            sb.append(history.get(i).toString()).append(" ");
            if (i % 2 == 1)
                sb.append("\n");
        }
        moveHistoryTextArea.setText(sb.toString());
    }

    private void playAIMoveIfNeeded() {
        if (!playAgainstAI || game.isGameOver()) return;
        if (game.isWhiteTurn() != aiPlaysWhite) return;

        Timer timer = new Timer(500, e -> {
            ai.makeMove();
            SwingUtilities.invokeLater(() -> {
                updateBoardDisplay();
                updateMoveHistory();
                turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));
                if (game.isInCheck(game.isWhiteTurn()))
                    JOptionPane.showMessageDialog(this, "Xeque!");
                if (game.isGameOver())
                    JOptionPane.showMessageDialog(this, (game.isWhiteTurn() ? "Pretas" : "Brancas") + " vencem! Xeque-mate.");
                playAIMoveIfNeeded();
            });
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void handleSquareClick(int row, int col) {
        Position position = new Position(row, col);
        Piece selectedPiece = game.getSelectedPiece();
        clearHighlights();

        if (selectedPiece == null) {
            game.selectPiece(position);
            selectedPiece = game.getSelectedPiece();
            if (selectedPiece != null)
                highlightSelection(selectedPiece);
        } else {
            boolean moveSuccessful = game.movePiece(selectedPiece.getPosition(), position);
            if (moveSuccessful) {
                updateBoardDisplay();
                updateMoveHistory();
                turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));

                if (game.isInCheck(game.isWhiteTurn()))
                    JOptionPane.showMessageDialog(this, "Xeque!");
                if (game.isGameOver())
                    JOptionPane.showMessageDialog(this, (game.isWhiteTurn() ? "Pretas" : "Brancas") + " vencem! Xeque-mate.");

                playAIMoveIfNeeded();
            } else {
                Piece newSelectedPiece = game.getBoard().getPieceAt(position);
                if (newSelectedPiece != null && newSelectedPiece.isWhite() == game.isWhiteTurn()) {
                    game.selectPiece(position);
                    highlightSelection(game.getSelectedPiece());
                } else {
                    game.selectPiece(null);
                }
            }
        }
    }

    private void highlightSelection(Piece piece) {
        Position from = piece.getPosition();
        squares[from.getRow()][from.getColumn()]
                .setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));

        List<Position> moves = piece.getPossibleMoves();
        if (moves != null) {
            for (Position pos : moves) {
                Piece targetPiece = game.getBoard().getPieceAt(pos);
                if (targetPiece == null)
                    squares[pos.getRow()][pos.getColumn()].setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                else if (targetPiece.isWhite() != piece.isWhite())
                    squares[pos.getRow()][pos.getColumn()].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            }
        }
    }

    private void clearHighlights() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                squares[r][c].setBorder(null);
    }

    private String getPieceKey(Piece piece) {
        String color = piece.isWhite() ? "w" : "b";
        String symbol = switch (piece.getSymbol()) {
            case "K" -> "K";
            case "Q" -> "Q";
            case "R" -> "R";
            case "B" -> "B";
            case "N" -> "N";
            case "P" -> "P";
            default -> "";
        };
        return color + symbol;
    }

    private void loadPieceIcons() {
        pieceIcons = new HashMap<>();
        String[] pieces = { "king", "queen", "rook", "bishop", "knight", "pawn" };
        String[] colors = { "white", "black" };

        for (String color : colors) {
            for (String piece : pieces) {
                String key = switch (piece) {
                    case "king" -> (color.equals("white") ? "wK" : "bK");
                    case "queen" -> (color.equals("white") ? "wQ" : "bQ");
                    case "rook" -> (color.equals("white") ? "wR" : "bR");
                    case "bishop" -> (color.equals("white") ? "wB" : "bB");
                    case "knight" -> (color.equals("white") ? "wN" : "bN");
                    case "pawn" -> (color.equals("white") ? "wP" : "bP");
                    default -> "";
                };

                String path = "/resources/pieces/" + piecesTheme + "/" + color + "_" + piece + ".png";
                URL imageURL = getClass().getResource(path);
                if (imageURL != null) {
                    ImageIcon icon = new ImageIcon(imageURL);
                    Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    pieceIcons.put(key, new ImageIcon(scaled));
                } else {
                    System.err.println("Não foi possível encontrar: " + path);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
