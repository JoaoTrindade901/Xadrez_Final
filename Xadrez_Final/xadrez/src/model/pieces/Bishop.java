package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Bishop extends Piece {

    public Bishop(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
public List<Position> getPossibleMoves() {
    List<Position> moves = new ArrayList<>();
    int[][] directions = {{-1,-1},{-1,1},{1,-1},{1,1}};
    for (int[] d : directions) {
        int r = position.getRow(), c = position.getColumn();
        while (true) {
            r += d[0]; c += d[1];
            Position np = new Position(r, c);
            if (!np.isValid()) break;
            Piece at = board.getPieceAt(np);
            if (at == null) { moves.add(np); }
            else { if (at.isWhite() != isWhite) moves.add(np); break; }
        }
    }
    return moves;
}

    @Override
    public String getSymbol() {
        return "B";
    }
}