package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Knight extends Piece {

    @Override
public List<Position> getPossibleMoves() {
    List<Position> moves = new ArrayList<>();
    int[][] jumps = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
    for (int[] j : jumps) {
        Position np = new Position(position.getRow()+j[0], position.getColumn()+j[1]);
        if (np.isValid()) {
            Piece at = board.getPieceAt(np);
            if (at == null || at.isWhite() != isWhite) moves.add(np);
        }
    }
    return moves;
}
    public Knight(Board board, boolean isWhite) {
        super(board, isWhite);
    }
    @Override
    public String getSymbol() {
        return "N";
    }
}