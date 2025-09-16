package model.board;

import model.pieces.Bishop;
import model.pieces.King;
import model.pieces.Knight;
import model.pieces.Pawn;
import model.pieces.Piece;
import model.pieces.Queen;
import model.pieces.Rook;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] pieces;
    private List<Move> moveHistory;
    private boolean isWhiteTurn;
    private Position lastPawnDoubleMove;

    public Board() {
        pieces = new Piece[8][8];
        moveHistory = new ArrayList<>();
        isWhiteTurn = true;
        lastPawnDoubleMove = null;
    }

    // Retorna a peça na posição ou null
    public Piece getPieceAt(Position position) {
        if (position == null || !position.isValid()) {
            return null;
        }
        return pieces[position.getRow()][position.getColumn()];
    }

    // Coloca uma peça na posição
    public void placePiece(Piece piece, Position position) {
        if (position == null || !position.isValid())
            return;
        pieces[position.getRow()][position.getColumn()] = piece;
        if (piece != null) {
            piece.setPosition(position);
        }
    }

    // Remove peça da posição
    public void removePiece(Position position) {
        if (position == null || !position.isValid())
            return;
        pieces[position.getRow()][position.getColumn()] = null;
    }

    // Verifica se a posição está vazia
    public boolean isPositionEmpty(Position position) {
        return getPieceAt(position) == null;
    }

    // Limpa o tabuleiro
    public void clear() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                pieces[row][col] = null;
            }
        }
    }

    // Verifica se a posição está sob ataque
    public boolean isUnderAttack(Position position, boolean byWhite) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null && piece.isWhite() == byWhite) {
                    if (piece.canMoveTo(position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Move uma peça de uma posição para outra
    public boolean movePiece(Piece selectedPiece, Position destination) {
        if (selectedPiece == null || destination == null || !destination.isValid()) {
            return false;
        }

        Piece capturedPiece = getPieceAt(destination);
        Position originalPosition = selectedPiece.getPosition();

        removePiece(originalPosition);
        placePiece(selectedPiece, destination);

        Move move = new Move(originalPosition, destination, selectedPiece, capturedPiece);
        moveHistory.add(move);

        checkSpecialConditions(selectedPiece, destination);

        isWhiteTurn = !isWhiteTurn;

        // lastPawnDoubleMove = ... (implementar depois, se necessário)
        return true;
    }

    private void checkSpecialConditions(Piece piece, Position destination) {
        // Implementação do roque
        if (piece instanceof King && Math.abs(destination.getColumn() - piece.getPosition().getColumn()) == 2) {
            // É um movimento de roque
            int rookColumn = destination.getColumn() > piece.getPosition().getColumn() ? 7 : 0;
            int newRookColumn = destination.getColumn() > piece.getPosition().getColumn() ? 5 : 3;
            
            Position rookPosition = new Position(piece.getPosition().getRow(), rookColumn);
            Position newRookPosition = new Position(piece.getPosition().getRow(), newRookColumn);
            
            Piece rook = getPieceAt(rookPosition);
            if (rook != null) {
                removePiece(rookPosition);
                placePiece(rook, newRookPosition);
                
                // Marcar que a torre se moveu
                rook.setHasMoved(true);
            }
        }
        
        // Marcar que a peça se moveu
        piece.setHasMoved(true);
    }

    public Board clone() {
        Board clonedBoard = new Board();
        clonedBoard.isWhiteTurn = this.isWhiteTurn;
        clonedBoard.lastPawnDoubleMove = (this.lastPawnDoubleMove != null)
                ? new Position(this.lastPawnDoubleMove.getRow(), this.lastPawnDoubleMove.getColumn())
                : null;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = this.pieces[row][col];
                if (piece != null) {
                    Piece clonedPiece = clonePiece(piece, clonedBoard);
                    clonedBoard.placePiece(clonedPiece, new Position(row, col));
                }
            }
        }

        clonedBoard.moveHistory = new ArrayList<>();
        for (Move move : this.moveHistory) {
            clonedBoard.moveHistory.add(move.clone()); // precisa clone() em Move
        }

        return clonedBoard;
    }

    private Piece clonePiece(Piece piece, Board clonedBoard) {
        if (piece instanceof King)
            return new King(clonedBoard, piece.isWhite());
        if (piece instanceof Queen)
            return new Queen(clonedBoard, piece.isWhite());
        if (piece instanceof Rook)
            return new Rook(clonedBoard, piece.isWhite());
        if (piece instanceof Bishop)
            return new Bishop(clonedBoard, piece.isWhite());
        if (piece instanceof Knight)
            return new Knight(clonedBoard, piece.isWhite());
        if (piece instanceof Pawn)
            return new Pawn(clonedBoard, piece.isWhite());
        return null;
    }

}
