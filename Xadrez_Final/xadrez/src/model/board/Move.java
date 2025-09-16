package model.board;

import model.pieces.Piece;

public class Move implements Cloneable {
    private Position from;
    private Position to;
    private Piece piece;
    private Piece capturedPiece;
    private boolean isPromotion;
    private boolean isCastling;
    private boolean isEnPassant;

    public Move(Position from, Position to, Piece piece, Piece capturedPiece) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getPiece() {
        return piece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    public void setPromotion(boolean promotion) {
        isPromotion = promotion;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public void setCastling(boolean castling) {
        isCastling = castling;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public void setEnPassant(boolean enPassant) {
        isEnPassant = enPassant;
    }

    @Override
    public String toString() {
        return piece.getSymbol() + from.toString() + "-" + to.toString();
    }

    @Override
    public Move clone() {
        // Cria novas posições para evitar problemas de referência
        Position clonedFrom = new Position(from.getRow(), from.getColumn());
        Position clonedTo = new Position(to.getRow(), to.getColumn());
        
        // Nota: as peças são mantidas como referências porque elas serão clonadas
        // pelo Board.clone() quando necessário
        Move cloned = new Move(clonedFrom, clonedTo, piece, capturedPiece);
        
        cloned.setPromotion(isPromotion);
        cloned.setCastling(isCastling);
        cloned.setEnPassant(isEnPassant);
        return cloned;
    }

}
