package controller;

import model.board.Board;
import model.board.Move;
import model.board.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChessAI {
    private final Game game;
    private final Random random = new Random();
    private Difficulty difficulty = Difficulty.MEDIUM;
    
    // Tabela de pontuação das peças
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;

    // Exemplo de tabela de valor posicional para o cavalo (Knight)
    private static final int[] KNIGHT_POSITION_TABLE = new int[]{
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    public enum Difficulty {
        EASY,// Profundidade 1-2, 40% chance de movimento subótimo
        MEDIUM,// Profundidade 2-3, 20% chance de movimento subótimo
        HARD, // Profundidade 3-4, 5% chance de movimento subótimo
        EXPERT // Profundidade 4-5, sem movimentos subótimos
    }
    
    public ChessAI(Game game) {
        this.game = game;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public void makeMove() {
        System.out.println("A IA está pensando...");
        int depth = getBaseDepthByDifficulty();
        
        Move bestMove = findBestMove(depth);
        
        if (bestMove == null) {
            System.out.println("Nenhum movimento válido encontrado! A IA se rendeu.");
            return;
        }
        
        try {
            if (game.movePieceDirect(bestMove.getFrom(), bestMove.getTo())) {
                System.out.printf("A IA moveu %s de %s para %s%n", bestMove.getPiece().getSymbol(), bestMove.getFrom(), bestMove.getTo());
            }
        } catch (Exception e) {
            System.err.println("Erro ao mover a peça: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int getBaseDepthByDifficulty() {
        switch (difficulty) {
            case EASY: return 1;
            case MEDIUM: return 2;
            case HARD: return 3;
            case EXPERT: return 4;
            default: return 2;
        }
    }

    private int getMovePriority(Move move) {
        int priority = 0;
        if (move.getCapturedPiece() != null) {
            priority += getPieceValue(move.getCapturedPiece()) * 10;
        }
        if (move.getPiece() instanceof King && Math.abs(move.getFrom().getColumn() - move.getTo().getColumn()) == 2) {
            priority += 50;
        }
        return priority;
    }

    public Move findBestMove(int baseDepth) {
        System.out.println("AI: Começando a busca por um movimento...");
        int depth = getAdjustedDepth(baseDepth);
        List<Move> possibleMoves = getAllValidMoves(game.getBoard(), game.isWhiteTurn());
        
        System.out.println("AI: Encontrou " + possibleMoves.size() + " movimentos válidos para analisar.");
        
        if (possibleMoves.isEmpty()) {
            System.out.println("AI: Nenhum movimento válido encontrado! A IA se rendeu.");
            return null;
        }
        
        Collections.sort(possibleMoves, (m1, m2) -> getMovePriority(m2) - getMovePriority(m1));
        
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        List<Move> goodMoves = new ArrayList<>();
        
        if (difficulty == Difficulty.EASY && random.nextInt(100) < 40) {
            return possibleMoves.get(random.nextInt(possibleMoves.size()));
        }
        
        for (Move move : possibleMoves) {
            Board testBoard = game.getBoard().clone();
            makeTestMove(testBoard, move);
            
            // Lógica NegaMax: o valor do próximo nível é negado
            int moveValue = -minimax(testBoard, depth - 1, -beta, -alpha, !game.isWhiteTurn());
            
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
                alpha = Math.max(alpha, bestValue);
                goodMoves.clear();
                goodMoves.add(move);
            } else if (moveValue >= bestValue - 50) {
                goodMoves.add(move);
            }
        }
        
        if (!goodMoves.isEmpty() && bestMove != null) {
            int randomChance = random.nextInt(100);
            switch (difficulty) {
                case MEDIUM:
                    if (randomChance < 20) return goodMoves.get(random.nextInt(goodMoves.size()));
                    break;
                case HARD:
                    if (randomChance < 5) return goodMoves.get(random.nextInt(goodMoves.size()));
                    break;
                case EXPERT:
                    break;
            }
        }
        
        if (bestMove == null && !possibleMoves.isEmpty()) {
            bestMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
        }
        
        return bestMove;
    }
    
    private int getAdjustedDepth(int baseDepth) {
        int depth = baseDepth;
        
        switch (difficulty) {
            case EASY:
                depth = 1;
                break;
            case MEDIUM:
                depth = Math.min(3, baseDepth);
                break;
            case HARD:
                depth = Math.min(4, baseDepth);
                break;
            case EXPERT:
                if (isEndgame(game.getBoard())) {
                    depth = Math.min(5, baseDepth + 1);
                } else {
                    depth = Math.min(4, baseDepth);
                }
                break;
        }
        
        return depth;
    }
    
    private boolean isEndgame(Board board) {
        int totalMaterial = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPieceAt(new Position(r, c));
                if (p != null && !(p instanceof King)) {
                    totalMaterial += getPieceValue(p);
                }
            }
        }
        return totalMaterial < 1500;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        if (depth == 0) {
            return evaluateBoard(board, isMaximizingPlayer);
        }
        
        List<Move> moves = getAllValidMoves(board, isMaximizingPlayer);
        if (moves.isEmpty()) {
            if (isKingInCheck(board, isMaximizingPlayer)) {
                return -KING_VALUE; // Checkmate
            }
            return 0; // Draw
        }
        
        Collections.sort(moves, (m1, m2) -> getMovePriority(m2) - getMovePriority(m1));
        
        int bestValue = Integer.MIN_VALUE;
        
        for (Move move : moves) {
            Board testBoard = board.clone();
            makeTestMove(testBoard, move);
            
            int value = -minimax(testBoard, depth - 1, -beta, -alpha, !isMaximizingPlayer);
            
            bestValue = Math.max(bestValue, value);
            alpha = Math.max(alpha, bestValue);
            
            if (alpha >= beta) {
                break; // Alpha-Beta pruning
            }
        }
        return bestValue;
    }

    private void makeTestMove(Board board, Move move) {
        try {
            Piece piece = board.getPieceAt(move.getFrom());
            if (piece == null) return;

            Piece captured = board.getPieceAt(move.getTo());
            if (captured != null) board.removePiece(move.getTo());

            board.removePiece(move.getFrom());
            board.placePiece(piece, move.getTo());

            if (piece instanceof King && Math.abs(move.getTo().getColumn() - move.getFrom().getColumn()) == 2) {
                int row = move.getFrom().getRow();
                int rookFromCol = move.getTo().getColumn() == 6 ? 7 : 0;
                int rookToCol = move.getTo().getColumn() == 6 ? 5 : 3;
                Piece rook = board.getPieceAt(new Position(row, rookFromCol));
                if (rook != null) {
                    board.removePiece(new Position(row, rookFromCol));
                    board.placePiece(rook, new Position(row, rookToCol));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro em makeTestMove: " + e.getMessage());
            e.printStackTrace();
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
        int mobilityValue = 0;
        int kingProtectionValue = 0;
        int centerControlValue = 0;
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.getPieceAt(pos);
                if (p != null) {
                    int pieceValue = getPieceValue(p);
                    boolean isPieceWhite = p.isWhite();
                    
                    if (p instanceof Pawn) {
                        if (isPassedPawn(board, pos, isPieceWhite)) {
                            pieceValue += 30 + (isPieceWhite ? (7 - r) * 5 : r * 5);
                        }
                        if (isPawnDoubled(board, pos, isPieceWhite)) {
                            pieceValue -= 15;
                        }
                        if (isPawnIsolated(board, pos, isPieceWhite)) {
                            pieceValue -= 10;
                        }
                        if (c >= 2 && c <= 5) {
                            pieceValue += 5;
                        }
                    } else if (p instanceof Rook) {
                        if (isColumnOpen(board, c)) {
                            pieceValue += 25;
                        } else if (isColumnSemiOpen(board, c, isPieceWhite)) {
                            pieceValue += 15;
                        }
                        if (areRooksConnected(board, pos, isPieceWhite)) {
                            pieceValue += 20;
                        }
                    } else if (p instanceof Knight) {
                        int tableIndex = isPieceWhite ? r * 8 + c : (7 - r) * 8 + c;
                        pieceValue += KNIGHT_POSITION_TABLE[tableIndex];
                    } else if (p instanceof Bishop) {
                        int diagonalLength = countDiagonalLength(board, pos);
                        pieceValue += diagonalLength * 2;
                        if (hasBishopPair(board, isPieceWhite)) {
                            pieceValue += 30;
                        }
                    } else if (p instanceof Queen) {
                        if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5)) {
                            pieceValue += 5;
                        }
                    } else if (p instanceof King) {
                        if (isEndgame(board)) {
                            double distanceFromCenter = Math.abs(3.5 - (double) r) + Math.abs(3.5 - (double) c);
                            kingProtectionValue += isPieceWhite == isWhiteTurn ? 
                                    (4 - distanceFromCenter) * 10 : 
                                    -(4 - distanceFromCenter) * 10;
                        } else {
                            if ((isPieceWhite && r >= 6) || (!isPieceWhite && r <= 1)) {
                                kingProtectionValue += isPieceWhite == isWhiteTurn ? 30 : -30;
                            }
                            int pawnShieldValue = countPawnShield(board, pos, isPieceWhite);
                            kingProtectionValue += isPieceWhite == isWhiteTurn ? 
                                    pawnShieldValue * 10 : 
                                    -pawnShieldValue * 10;
                        }
                    }
                    
                    if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5)) {
                        centerControlValue += isPieceWhite == isWhiteTurn ? 8 : -8;
                    }
                    
                    if (!(p instanceof Pawn) && !(p instanceof King)) {
                        boolean isInStartPosition = (isPieceWhite && r == 7) || (!isPieceWhite && r == 0);
                        if (!isInStartPosition) {
                            pieceValue += isPieceWhite == isWhiteTurn ? 10 : -10;
                        }
                    }
                    
                    List<Position> possibleMoves = p.getPossibleMoves();
                    if (possibleMoves != null) {
                        long movesCount = possibleMoves.stream().filter(dest -> isValidMove(board, p, dest)).count();
                        mobilityValue += isPieceWhite == isWhiteTurn ? movesCount : -movesCount;
                    }
                    
                    value += isPieceWhite == isWhiteTurn ? pieceValue : -pieceValue;
                }
            }
        }
        
        if (isKingInCheck(board, isWhiteTurn)) value -= 60;
        if (isKingInCheck(board, !isWhiteTurn)) value += 50;
        
        value += mobilityValue * 0.5;
        value += kingProtectionValue;
        value += centerControlValue;
        
        return value;
    }

    private int getPieceValue(Piece p) {
        if (p == null) return 0;
        if (p instanceof Pawn) return PAWN_VALUE;
        if (p instanceof Knight) return KNIGHT_VALUE;
        if (p instanceof Bishop) return BISHOP_VALUE;
        if (p instanceof Rook) return ROOK_VALUE;
        if (p instanceof Queen) return QUEEN_VALUE;
        if (p instanceof King) return KING_VALUE;
        return 0;
    }

    private boolean isColumnOpen(Board board, int col) {
        for (int r = 0; r < 8; r++) {
            Piece p = board.getPieceAt(new Position(r, col));
            if (p instanceof Pawn) return false;
        }
        return true;
    }
    
    private boolean isColumnSemiOpen(Board board, int col, boolean forWhite) {
        boolean hasEnemyPawn = false;
        for (int r = 0; r < 8; r++) {
            Piece p = board.getPieceAt(new Position(r, col));
            if (p instanceof Pawn) {
                if (p.isWhite() == forWhite) return false;
                hasEnemyPawn = true;
            }
        }
        return hasEnemyPawn;
    }
    
    private boolean isPawnDoubled(Board board, Position pos, boolean isWhite) {
        int col = pos.getColumn();
        for (int r = 0; r < 8; r++) {
            if (r != pos.getRow()) {
                Piece p = board.getPieceAt(new Position(r, col));
                if (p instanceof Pawn && p.isWhite() == isWhite) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isPawnIsolated(Board board, Position pos, boolean isWhite) {
        int col = pos.getColumn();
        boolean hasFriendlyPawnLeft = false;
        if (col > 0) {
            for (int r = 0; r < 8; r++) {
                Piece p = board.getPieceAt(new Position(r, col - 1));
                if (p instanceof Pawn && p.isWhite() == isWhite) {
                    hasFriendlyPawnLeft = true;
                    break;
                }
            }
        }
        boolean hasFriendlyPawnRight = false;
        if (col < 7) {
            for (int r = 0; r < 8; r++) {
                Piece p = board.getPieceAt(new Position(r, col + 1));
                if (p instanceof Pawn && p.isWhite() == isWhite) {
                    hasFriendlyPawnRight = true;
                    break;
                }
            }
        }
        return !hasFriendlyPawnLeft && !hasFriendlyPawnRight;
    }
    
    private boolean areRooksConnected(Board board, Position rookPos, boolean isWhite) {
        int row = rookPos.getRow();
        int col = rookPos.getColumn();
        
        for (int c = 0; c < 8; c++) {
            if (c != col) {
                Piece p = board.getPieceAt(new Position(row, c));
                if (p instanceof Rook && p.isWhite() == isWhite) {
                    return true;
                }
            }
        }
        
        for (int r = 0; r < 8; r++) {
            if (r != row) {
                Piece p = board.getPieceAt(new Position(r, col));
                if (p instanceof Rook && p.isWhite() == isWhite) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private int countDiagonalLength(Board board, Position pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        int count = 0;
        
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            int lineCount = 0;
            
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                Piece p = board.getPieceAt(new Position(r, c));
                if (p == null) {
                    lineCount++;
                } else {
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
            
            count += lineCount;
        }
        
        return count;
    }
    
    private boolean hasBishopPair(Board board, boolean isWhite) {
        boolean hasLightSquareBishop = false;
        boolean hasDarkSquareBishop = false;
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.getPieceAt(pos);
                
                if (p instanceof Bishop && p.isWhite() == isWhite) {
                    if ((r + c) % 2 == 0) {
                        hasDarkSquareBishop = true;
                    } else {
                        hasLightSquareBishop = true;
                    }
                }
            }
        }
        
        return hasLightSquareBishop && hasDarkSquareBishop;
    }
    
    private int countPawnShield(Board board, Position kingPos, boolean isWhite) {
        int row = kingPos.getRow();
        int col = kingPos.getColumn();
        int count = 0;
        
        int pawnRow = isWhite ? row - 1 : row + 1;
        
        if (pawnRow >= 0 && pawnRow < 8) {
            for (int c = Math.max(0, col - 1); c <= Math.min(7, col + 1); c++) {
                Piece p = board.getPieceAt(new Position(pawnRow, c));
                if (p instanceof Pawn && p.isWhite() == isWhite) {
                    count++;
                }
            }
        }
        
        return count;
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
        System.out.println("AI: Gerando movimentos válidos...");
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
        System.out.println("AI: Concluído a geração de movimentos. Total de movimentos: " + moves.size());
        return moves;
    }

    private boolean isValidMove(Board board, Piece piece, Position destination) {
        System.out.println("AI: Verificando se o movimento de " + piece.getSymbol() + " de " + piece.getPosition() + " para " + destination + " é válido...");
        
        Board temp = board.clone();
        Piece tempPiece = temp.getPieceAt(piece.getPosition());
        if (tempPiece == null) {
            System.err.println("AI: Erro crítico - Peça não encontrada no tabuleiro clonado!");
            return false;
        }

        // Simula o movimento no tabuleiro temporário
        Piece captured = temp.getPieceAt(destination);
        if (captured != null) temp.removePiece(destination);
        temp.removePiece(piece.getPosition());
        temp.placePiece(tempPiece, destination);
        
        return !isKingInCheck(temp, piece.isWhite());
    }
}