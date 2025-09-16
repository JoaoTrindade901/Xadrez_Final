package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Pawn extends Piece {

    public Pawn(Board board, boolean isWhite) {
        super(board, isWhite);
    }

   @Override
public List<Position> getPossibleMoves() {
    List<Position> moves = new ArrayList<>();
    int dir = isWhite ? -1 : 1;

    Position front = new Position(position.getRow()+dir, position.getColumn());
    if (front.isValid() && board.isPositionEmpty(front)) {
        moves.add(front);

        if ((isWhite && position.getRow()==6) || (!isWhite && position.getRow()==1)) {
            Position doubleFront = new Position(position.getRow()+2*dir, position.getColumn());
            if (board.isPositionEmpty(doubleFront)) moves.add(doubleFront);
        }
    }

    // capturas diagonais
    Position capL = new Position(position.getRow()+dir, position.getColumn()-1);
    Position capR = new Position(position.getRow()+dir, position.getColumn()+1);
    if (capL.isValid()) {
        Piece at = board.getPieceAt(capL);
        if (at != null && at.isWhite()!=isWhite) moves.add(capL);
    }
    if (capR.isValid()) {
        Piece at = board.getPieceAt(capR);
        if (at != null && at.isWhite()!=isWhite) moves.add(capR);
    }
    return moves;
}

    @Override
    public String getSymbol() {
        return "P";
    }
}