package model.pieces;

import java.util.List;
import model.board.Position;

public abstract class Piece {
    protected Position position;
    protected boolean isWhite;
    protected boolean hasMoved;

    protected model.board.Board board;

    public Piece(model.board.Board board, boolean isWhite) {
        this.board = board;
        this.isWhite = isWhite;
        this.hasMoved = false;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
    public boolean getHasMoved() {
        return hasMoved;
    }
    
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public abstract List<Position> getPossibleMoves();
    public boolean canMoveTo(Position targetPosition) {
        List<Position> possibleMoves = getPossibleMoves();
        return possibleMoves != null && possibleMoves.contains(targetPosition);
    }

    public abstract String getSymbol();
}