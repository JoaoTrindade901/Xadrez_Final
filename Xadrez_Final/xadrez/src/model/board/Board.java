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

    public boolean isUnderAttack(Position position, boolean byWhite) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null && piece.isWhite() == byWhite) {

                    if (canPieceAttackPosition(piece, position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean canPieceAttackPosition(Piece piece, Position targetPosition) {
        if (piece == null || targetPosition == null || !targetPosition.isValid()) {
            return false;
        }
        
        Position currentPos = piece.getPosition(); // Posição atual da peça
        if (currentPos == null || !currentPos.isValid()) return false;

        int rowDiff = Math.abs(currentPos.getRow() - targetPosition.getRow());
        int colDiff = Math.abs(currentPos.getColumn() - targetPosition.getColumn());

        if (piece instanceof Pawn) {
            int direction = piece.isWhite() ? -1 : 1;
            // Ataques diagonais do peão
            if (targetPosition.getRow() == currentPos.getRow() + direction && colDiff == 1) {
                return true; // Peões atacam diagonais
            }
            return false;
        } else if (piece instanceof Knight) {
            return (rowDiff == 1 && colDiff == 2) || (rowDiff == 2 && colDiff == 1);
        } else if (piece instanceof King) {
            // Rei pode se mover para qualquer uma das 8 casas adjacentes (sem considerar xeque)
            return rowDiff <= 1 && colDiff <= 1;
        } else if (piece instanceof Rook) {
            if (currentPos.getRow() == targetPosition.getRow()) { // Movimento horizontal
                return !isPathBlockedHorizontal(currentPos, targetPosition);
            }
            if (currentPos.getColumn() == targetPosition.getColumn()) { // Movimento vertical
                return !isPathBlockedVertical(currentPos, targetPosition);
            }
            return false;
        } else if (piece instanceof Bishop) {
            if (rowDiff == colDiff) { // Movimento diagonal
                return !isPathBlockedDiagonal(currentPos, targetPosition);
            }
            return false;
        } else if (piece instanceof Queen) {
            if (currentPos.getRow() == targetPosition.getRow()) { // Horizontal
                return !isPathBlockedHorizontal(currentPos, targetPosition);
            }
            if (currentPos.getColumn() == targetPosition.getColumn()) { // Vertical
                return !isPathBlockedVertical(currentPos, targetPosition);
            }
            if (rowDiff == colDiff) { // Diagonal
                return !isPathBlockedDiagonal(currentPos, targetPosition);
            }
            return false;
        }
        return false;
    }
    
    private boolean isPathBlockedHorizontal(Position start, Position end) {
        int row = start.getRow();
        int colStart = Math.min(start.getColumn(), end.getColumn());
        int colEnd = Math.max(start.getColumn(), end.getColumn());
        for (int c = colStart + 1; c < colEnd; c++) {
            if (!isPositionEmpty(new Position(row, c))) return true;
        }
        return false;
    }

    private boolean isPathBlockedVertical(Position start, Position end) {
        int col = start.getColumn();
        int rowStart = Math.min(start.getRow(), end.getRow());
        int rowEnd = Math.max(start.getRow(), end.getRow());
        for (int r = rowStart + 1; r < rowEnd; r++) {
            if (!isPositionEmpty(new Position(r, col))) return true;
        }
        return false;
    }

    private boolean isPathBlockedDiagonal(Position start, Position end) {
        int rowDir = (end.getRow() > start.getRow()) ? 1 : -1;
        int colDir = (end.getColumn() > start.getColumn()) ? 1 : -1;

        int r = start.getRow() + rowDir;
        int c = start.getColumn() + colDir;

        while (r != end.getRow() && c != end.getColumn()) {
            if (!isPositionEmpty(new Position(r, c))) return true;
            r += rowDir;
            c += colDir;
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
        return true;
    }

    private void checkSpecialConditions(Piece piece, Position destination) {
        // Implementação do roque
        if (piece instanceof King && Math.abs(destination.getColumn() - piece.getPosition().getColumn()) == 2) {
            int rookColumn = destination.getColumn() > piece.getPosition().getColumn() ? 7 : 0;
            int newRookColumn = destination.getColumn() > piece.getPosition().getColumn() ? 5 : 3;
            
            Position rookPosition = new Position(piece.getPosition().getRow(), rookColumn);
            Position newRookPosition = new Position(piece.getPosition().getRow(), newRookColumn);
            
            Piece rook = getPieceAt(rookPosition);
            if (rook != null) {
                removePiece(rookPosition);
                placePiece(rook, newRookPosition);
                rook.setHasMoved(true);
            }
        }    
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
            clonedBoard.moveHistory.add(move.clone());
        }

        return clonedBoard;
    }

    private Piece clonePiece(Piece piece, Board clonedBoard) {
        // As peças precisam ser criadas com o novo tabuleiro clonado
        Piece newPiece = null;
        if (piece instanceof King)
            newPiece = new King(clonedBoard, piece.isWhite());
        else if (piece instanceof Queen)
            newPiece = new Queen(clonedBoard, piece.isWhite());
        else if (piece instanceof Rook)
            newPiece = new Rook(clonedBoard, piece.isWhite());
        else if (piece instanceof Bishop)
            newPiece = new Bishop(clonedBoard, piece.isWhite());
        else if (piece instanceof Knight)
            newPiece = new Knight(clonedBoard, piece.isWhite());
        else if (piece instanceof Pawn)
            newPiece = new Pawn(clonedBoard, piece.isWhite());
        
        if (newPiece != null) {
            newPiece.setHasMoved(piece.getHasMoved());
        }
        return newPiece;
    }
}