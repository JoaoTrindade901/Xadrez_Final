package controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.board.Board;
import model.board.Move;
import model.board.Position;
import model.pieces.*;

public class Game {
    private Board board;
    private boolean isWhiteTurn;
    private boolean isGameOver;
    private Piece selectedPiece;
    private Position lastPawnDoubleMove;
    private int movesSinceLastCaptureOrPawnMove;
    private List<Move> moveHistory = new ArrayList<>();

    public Game() {
        board = new Board();
        isWhiteTurn = true;
        isGameOver = false;
        setupPieces();
        moveHistory = new ArrayList<>();
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    private void setupPieces() {
        // Colocar peças na posição inicial
        // brancas
        board.placePiece(new Rook(board, true), new Position(7, 0));
        board.placePiece(new Knight(board, true), new Position(7, 1));
        board.placePiece(new Bishop(board, true), new Position(7, 2));
        board.placePiece(new Queen(board, true), new Position(7, 3));
        board.placePiece(new King(board, true), new Position(7, 4));
        board.placePiece(new Bishop(board, true), new Position(7, 5));
        board.placePiece(new Knight(board, true), new Position(7, 6));
        board.placePiece(new Rook(board, true), new Position(7, 7));
        for (int c = 0; c < 8; c++)
            board.placePiece(new Pawn(board, true), new Position(6, c));

        // pretas
        board.placePiece(new Rook(board, false), new Position(0, 0));
        board.placePiece(new Knight(board, false), new Position(0, 1));
        board.placePiece(new Bishop(board, false), new Position(0, 2));
        board.placePiece(new Queen(board, false), new Position(0, 3));
        board.placePiece(new King(board, false), new Position(0, 4));
        board.placePiece(new Bishop(board, false), new Position(0, 5));
        board.placePiece(new Knight(board, false), new Position(0, 6));
        board.placePiece(new Rook(board, false), new Position(0, 7));
        for (int c = 0; c < 8; c++)
            board.placePiece(new Pawn(board, false), new Position(1, c));

        // Peças pretas (mesma lógica)
        // ...
    }

    public boolean movePieceDirect(Position from, Position to) {
        return movePiece(from, to);
    }

    public Board getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public void selectPiece(Position position) {
        Piece piece = board.getPieceAt(position);

        // Só pode selecionar peça da cor do jogador atual
        if (piece != null && piece.isWhite() == isWhiteTurn) {
            selectedPiece = piece;
        }
    }

    public boolean movePiece(Position from, Position to) {
        Piece piece = board.getPieceAt(from);
        if (piece == null || piece.isWhite() != isWhiteTurn || isGameOver)
            return false;

        if (!piece.canMoveTo(to) || moveCausesCheck(piece, to))
            return false;

        Piece capturedPiece = board.getPieceAt(to);
        board.removePiece(from);
        board.placePiece(piece, to);

        Move move = new Move(from, to, piece, capturedPiece);

        if (piece instanceof King && Math.abs(to.getColumn() - from.getColumn()) == 2) {
            move.setCastling(true);
            
            // Movimento de roque - mover a torre também
            int rookColumn = to.getColumn() > from.getColumn() ? 7 : 0;
            int newRookColumn;
            
            // Determinar a nova coluna da torre com base no tipo de roque
            if (to.getColumn() == 6) { // Roque curto (lado do rei)
                newRookColumn = 5;
            } else { // Roque longo (lado da rainha)
                newRookColumn = 3;
            }
            
            Position rookPosition = new Position(from.getRow(), rookColumn);
            Position newRookPosition = new Position(from.getRow(), newRookColumn);
            
            Piece rook = board.getPieceAt(rookPosition);
            if (rook != null) {
                board.removePiece(rookPosition);
                board.placePiece(rook, newRookPosition);
                rook.setHasMoved(true);
            }
        }
        else if (piece instanceof Pawn && Math.abs(to.getRow() - from.getRow()) == 2)
            move.setEnPassant(true);
        else if (piece instanceof Pawn && (to.getRow() == 0 || to.getRow() == 7))
            move.setPromotion(true);

        moveHistory.add(move);
        checkSpecialConditions(piece, to);
        checkGameStatus(from, to);

        isWhiteTurn = !isWhiteTurn;
        selectedPiece = null;

        // Captura en passant
        if (piece instanceof Pawn) {
            if (Math.abs(to.getRow() - from.getRow()) == 2)
                lastPawnDoubleMove = to;
            else if (Math.abs(to.getColumn() - from.getColumn()) == 1 && capturedPiece == null) {
                Position capturedPawnPos = new Position(from.getRow(), to.getColumn());
                board.removePiece(capturedPawnPos);
            } else
                lastPawnDoubleMove = null;
        } else
            lastPawnDoubleMove = null;

        return true;
    }

    boolean moveCausesCheck(Piece piece, Position destination) {
        Board tempBoard = board.clone();
        Position originalPos = piece.getPosition();

        Piece tempPiece = tempBoard.getPieceAt(originalPos);
        tempBoard.removePiece(originalPos);
        tempBoard.placePiece(tempPiece, destination);

        // Encontrar posição do rei da mesma cor
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = tempBoard.getPieceAt(new Position(r, c));
                if (p instanceof King && p.isWhite() == piece.isWhite()) {
                    kingPos = p.getPosition();
                    break;
                }
            }
            if (kingPos != null)
                break;
        }

        return tempBoard.isUnderAttack(kingPos, !piece.isWhite());
    }

    private void checkSpecialConditions(Piece piece, Position destination) {
        // Verificar promoção de peão
        if (piece instanceof Pawn) {
            if ((piece.isWhite() && destination.getRow() == 0) ||
                    (!piece.isWhite() && destination.getRow() == 7)) {

                // Perguntar ao jogador para qual peça deseja promover
                String[] options = { "Rainha", "Torre", "Bispo", "Cavalo" };
                int choice = JOptionPane.showOptionDialog(null,
                        "Escolha uma peça para promoção:",
                        "Promoção de Peão",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                // Criar a nova peça
                Piece newPiece;
                switch (choice) {
                    case 0:
                        newPiece = new Queen(board, piece.isWhite());
                        break;
                    case 1:
                        newPiece = new Rook(board, piece.isWhite());
                        break;
                    case 2:
                        newPiece = new Bishop(board, piece.isWhite());
                        break;
                    case 3:
                        newPiece = new Knight(board, piece.isWhite());
                        break;
                    default:
                        newPiece = new Queen(board, piece.isWhite());
                }

                // Substituir o peão pela nova peça
                board.removePiece(destination);
                board.placePiece(newPiece, destination);
            }
        }

        // Verificar outras condições especiais (en passant, etc.)
        // ..
    }

    private void checkGameStatus(Position originalPosition, Position destination) {
        boolean whiteKingInCheck = isInCheck(true);
        boolean blackKingInCheck = isInCheck(false);

        if (whiteKingInCheck && isCheckmate(true)) {
            isGameOver = true;
            // Pretas vencem
        } else if (blackKingInCheck && isCheckmate(false)) {
            isGameOver = true;
            // Brancas vencem
        }

        // Verificar empate (stalemate, repetição, etc.)
        // ...

        // O movimento de roque agora é tratado no método checkSpecialConditions da classe Board
    }

    public boolean isInCheck(boolean whiteKing) {
        // Encontrar a posição do rei
        Position kingPosition = null;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);

                if (piece instanceof King &&
                        piece.isWhite() == whiteKing) {
                    kingPosition = pos;
                    break;
                }
            }
            if (kingPosition != null)
                break;
        }

        // Verificar se alguma peça adversária pode capturar o rei
        return board.isUnderAttack(kingPosition, !whiteKing);
    }

    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);

        // Mover a peça de volta
        board.removePiece(lastMove.getTo());
        board.placePiece(lastMove.getPiece(), lastMove.getFrom());

        // Restaurar peça capturada, se houver
        if (lastMove.getCapturedPiece() != null) {
            board.placePiece(lastMove.getCapturedPiece(),
                    lastMove.getTo());
        }

        // Lidar com casos especiais (roque, en passant, promoção)
        if (lastMove.isCastling()) {
            // Desfazer o movimento do rei
            Piece king = lastMove.getPiece();
            Position from = lastMove.getFrom();
            Position to = lastMove.getTo();
            board.removePiece(to);
            board.placePiece(king, from);
            king.setHasMoved(false);

            // Desfazer o movimento da torre
            int rookOriginalCol = (to.getColumn() == 6) ? 7 : 0;
            int rookNewCol = (to.getColumn() == 6) ? 5 : 3;
            
            // Corrigir a posição da torre para o roque do lado da rainha
            if (to.getColumn() == 2) {
                rookOriginalCol = 0; // A torre originalmente estava na coluna 0
                rookNewCol = 3; // A torre está na coluna 3 após o roque do lado da rainha
            }

            Position rookNewPos = new Position(from.getRow(), rookNewCol);
            Position rookOriginalPos = new Position(from.getRow(), rookOriginalCol);

            Piece rook = board.getPieceAt(rookNewPos);
            if (rook != null) {
                board.removePiece(rookNewPos);
                board.placePiece(rook, rookOriginalPos);
                rook.setHasMoved(false);
            }
        }

        // Restaurar o turno
        isWhiteTurn = !isWhiteTurn;

        return true;
    }

    private boolean isCheckmate(boolean whiteKing) {
        if (!isInCheck(whiteKing)) {
            return false;
        }

        // Verificar se há algum movimento legal para sair do xeque
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);

                if (piece != null && piece.isWhite() == whiteKing) {
                    for (Object obj : piece.getPossibleMoves()) {
                        Position movePos = (Position) obj;
                        // Testar se o movimento tira o rei do xeque
                        if (!moveCausesCheck(piece, movePos)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void saveGame(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {

            // Salvar o estado do jogo
            oos.writeObject(board);
            oos.writeBoolean(isWhiteTurn);
            oos.writeObject(moveHistory);
            // Salvar outras informações relevantes

            System.out.println("Jogo salvo com sucesso em: " + filePath);
        } catch (IOException e) {
            System.err.println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    public static Game loadGame(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath))) {

            Game game = new Game(); // Construtor especial sem setup inicial

            // Carregar o estado do jogo
            game.board = (Board) ois.readObject();
            game.isWhiteTurn = ois.readBoolean();
            game.moveHistory = (List<Move>) ois.readObject();
            // Carregar outras informações relevantes

            System.out.println("Jogo carregado com sucesso de: " + filePath);
            return game;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar o jogo: " + e.getMessage());
            return null;
        }
    }
}