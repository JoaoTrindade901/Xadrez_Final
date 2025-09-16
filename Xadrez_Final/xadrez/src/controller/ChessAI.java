package controller;

import model.board.Board;
import model.board.Move;
import model.board.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ChessAI {
    private final Game game;
    private final Random random = new Random();

    public ChessAI(Game game) {
        this.game = game;
    }

    // Executa o melhor movimento baseado em heurísticas simples
    public void makeMove() {
        System.out.println("IA está pensando...");
        List<Move> allValidMoves = getAllValidMoves(game.getBoard(), game.isWhiteTurn());

        if (allValidMoves.isEmpty()) {
            System.out.println("Nenhum movimento válido encontrado!");
            return;
        }

        // Priorizar movimentos de captura ou roque
        allValidMoves.sort(Comparator.comparingInt((Move m) -> getMovePriority(m)).reversed());

        // Tenta o melhor movimento
        for (Move move : allValidMoves) {
            if (game.movePieceDirect(move.getFrom(), move.getTo())) {
                System.out.println("IA move " + move.getPiece().getSymbol() + " de " + move.getFrom() + " para " + move.getTo());
                return;
            }
        }
    }

    private int getMovePriority(Move move) {
        int priority = 0;
        if (move.getCapturedPiece() != null) {
            priority += getPieceValue(move.getCapturedPiece()) * 10; // Captura importante
        }
        if (move.getPiece() instanceof King) {
            // Dar valor extra para movimentos de roque
            if (Math.abs(move.getFrom().getColumn() - move.getTo().getColumn()) == 2) priority += 50;
        }
        return priority;
    }

    // Minimax com alfa-beta para movimentos estratégicos
    public Move findBestMove(int depth) {
        List<Move> possibleMoves = getAllPossibleMoves(game.getBoard(), game.isWhiteTurn());
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        if (possibleMoves.isEmpty()) return null;

        for (Move move : possibleMoves) {
            Board testBoard = game.getBoard().clone();
            makeTestMove(testBoard, move);

            int moveValue = -minimax(testBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !game.isWhiteTurn());
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }

        if (bestMove == null) {
            bestMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
        }

        return bestMove;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isWhiteTurn) {
        if (depth == 0) return evaluateBoard(board, isWhiteTurn);

        List<Move> moves = getAllPossibleMoves(board, isWhiteTurn);
        if (moves.isEmpty()) {
            // Xeque-mate ou empate
            if (isKingInCheck(board, isWhiteTurn)) return isWhiteTurn ? -100000 : 100000;
            return 0; // empate
        }

        int best = isWhiteTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : moves) {
            Board testBoard = board.clone();
            makeTestMove(testBoard, move);

            int eval = minimax(testBoard, depth - 1, alpha, beta, !isWhiteTurn);
            if (isWhiteTurn) {
                best = Math.max(best, eval);
                alpha = Math.max(alpha, eval);
            } else {
                best = Math.min(best, eval);
                beta = Math.min(beta, eval);
            }
            if (beta <= alpha) break; // poda alfa-beta
        }

        return best;
    }

    private void makeTestMove(Board board, Move move) {
        Piece piece = board.getPieceAt(move.getFrom());
        if (piece == null) return;

        Piece captured = board.getPieceAt(move.getTo());
        if (captured != null) board.removePiece(move.getTo());

        board.removePiece(move.getFrom());
        board.placePiece(piece, move.getTo());

        // Se for roque, mover torre também
        if (piece instanceof King && Math.abs(move.getTo().getColumn() - move.getFrom().getColumn()) == 2) {
            int row = move.getFrom().getRow();
            int rookFromCol = move.getTo().getColumn() == 6 ? 7 : 0;
            int rookToCol = move.getTo().getColumn() == 6 ? 5 : 3;
            Piece rook = board.getPieceAt(new Position(row, rookFromCol));
            board.removePiece(new Position(row, rookFromCol));
            board.placePiece(rook, new Position(row, rookToCol));
        }
    }

    private boolean isKingInCheck(Board board, boolean whiteKing) {
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPieceAt(new Position(r, c));
                if (p instanceof King && p.isWhite() == whiteKing) {
                    kingPos = new Position(r, c);
                    break;
                }
            }
            if (kingPos != null) break;
        }
        if (kingPos == null) return false;
        return board.isUnderAttack(kingPos, !whiteKing);
    }

    private int evaluateBoard(Board board, boolean isWhiteTurn) {
        int value = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPieceAt(new Position(r, c));
                if (p != null) {
                    int pieceValue = getPieceValue(p);

                    // Bônus para peões passados
                    if (p instanceof Pawn && isPassedPawn(board, new Position(r, c), p.isWhite())) pieceValue += 25;

                    // Bônus para torres em colunas abertas
                    if (p instanceof Rook && isColumnOpen(board, c)) pieceValue += 20;

                    // Centro do tabuleiro
                    if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5)) pieceValue += 10;

                    value += p.isWhite() == isWhiteTurn ? pieceValue : -pieceValue;
                }
            }
        }

        if (isKingInCheck(board, isWhiteTurn)) value -= 50;
        if (isKingInCheck(board, !isWhiteTurn)) value += 30;

        return value;
    }

    private int getPieceValue(Piece p) {
        if (p == null) return 0;
        if (p instanceof Pawn) return 100;
        if (p instanceof Knight) return 300;
        if (p instanceof Bishop) return 300;
        if (p instanceof Rook) return 500;
        if (p instanceof Queen) return 900;
        if (p instanceof King) return 10000;
        return 0;
    }

    private boolean isColumnOpen(Board board, int col) {
        for (int r = 0; r < 8; r++) {
            Piece p = board.getPieceAt(new Position(r, col));
            if (p instanceof Pawn) return false;
        }
        return true;
    }

    private boolean isPassedPawn(Board board, Position pos, boolean isWhite) {
        int dir = isWhite ? -1 : 1;
        for (int r = pos.getRow() + dir; r >= 0 && r < 8; r += dir) {
            for (int c = pos.getColumn() - 1; c <= pos.getColumn() + 1; c++) {
                if (c >= 0 && c < 8) {
                    Piece p = board.getPieceAt(new Position(r, c));
                    if (p instanceof Pawn && p.isWhite() != isWhite) return false;
                }
            }
        }
        return true;
    }

    private List<Move> getAllValidMoves(Board board, boolean forWhite) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.getPieceAt(pos);
                if (p != null && p.isWhite() == forWhite) {
                    List<Position> possibleMoves = p.getPossibleMoves();
                    if (possibleMoves != null) {
                        for (Position dest : possibleMoves) {
                            if (isValidMove(board, p, dest)) {
                                moves.add(new Move(pos, dest, p, board.getPieceAt(dest)));
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    private List<Move> getAllPossibleMoves(Board board, boolean forWhite) {
        return getAllValidMoves(board, forWhite);
    }

    private boolean isValidMove(Board board, Piece piece, Position destination) {
        Board temp = board.clone();
        Piece tempPiece = temp.getPieceAt(piece.getPosition());
        Piece captured = temp.getPieceAt(destination);
        if (captured != null) temp.removePiece(destination);
        temp.removePiece(piece.getPosition());
        temp.placePiece(tempPiece, destination);
        return !isKingInCheck(temp, piece.isWhite());
    }
}