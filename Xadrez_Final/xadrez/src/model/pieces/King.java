package model.pieces;

import model.board.Board;
import model.board.Position;
import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        int[][] dirs = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
        
        // Movimentos normais do Rei
        for (int[] d : dirs) {
            Position np = new Position(position.getRow() + d[0], position.getColumn() + d[1]);
            if (np.isValid()) {
                Piece at = board.getPieceAt(np);
                if (at == null || at.isWhite() != isWhite) {
                    moves.add(np);
                }
            }
        }
        if (!this.getHasMoved()) { 
            // Roque do lado do rei (King Side)
            Position rookKingSidePos = new Position(position.getRow(), 7);
            Piece rookKingSide = board.getPieceAt(rookKingSidePos);
            
            if (rookKingSide instanceof Rook && !rookKingSide.getHasMoved()) { 
                // Verifica se as casas entre o rei e a torre estão vazias
                if (board.isPositionEmpty(new Position(position.getRow(), 5)) &&
                    board.isPositionEmpty(new Position(position.getRow(), 6))) {
                    
                    // Adiciona o movimento do roque. A validação de xeque virá de fora.
                    moves.add(new Position(position.getRow(), 6));
                }
            }

            // Roque do lado da rainha (Queen Side)
            Position rookQueenSidePos = new Position(position.getRow(), 0);
            Piece rookQueenSide = board.getPieceAt(rookQueenSidePos);
            
            if (rookQueenSide instanceof Rook && !rookQueenSide.getHasMoved()) {
                // Verifica se as casas entre o rei e a torre estão vazias
                if (board.isPositionEmpty(new Position(position.getRow(), 1)) &&
                    board.isPositionEmpty(new Position(position.getRow(), 2)) &&
                    board.isPositionEmpty(new Position(position.getRow(), 3))) {
                    
                    // Adiciona o movimento do roque. A validação de xeque virá de fora.
                    moves.add(new Position(position.getRow(), 2));
                }
            }
        }
        return moves;
    }

    @Override
    public String getSymbol() {
        return "K";
    }
}