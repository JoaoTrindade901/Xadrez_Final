# ♟️ Chess Game em Java

Um jogo de xadrez desenvolvido em **Java**, utilizando **Swing** para a interface gráfica.  
O projeto implementa as principais regras do xadrez, incluindo **roque**, **promoção de peão** e **en passant**.

---

## 🚀 Funcionalidades

- Tabuleiro 8x8 com cores personalizadas.
- Peças com movimentos legais validados.
- Captura de peças.
- **Roque** (pequeno e grande).
- **Promoção de peão**.
- **En passant**.
- Detecção de **xeque** e **xeque-mate**.
- Histórico de jogadas.
- Interface gráfica simples e intuitiva em **Java Swing**.

---

## 🛠️ Tecnologias

- **Java 17+**
- **Swing** (GUI)
- Programação **Orientada a Objetos**

---

## 📂 Estrutura do Projeto

src/
├── model/
│ ├── board/ # Classes do tabuleiro (Board, Position, Move)
│ ├── pieces/ # Classes das peças (King, Queen, Rook, Bishop, Knight, Pawn)
│ └── Piece.java # Classe base para todas as peças
│
├── controller/
│ └── Game.java # Lógica principal do jogo
  └── ChessAI.java # IA para jogar contra
│
└── Resources/
  └── pieces # imagens das peças do jogo
  └── sounds # Sons do jogo (não adicionado)  
└── view/
└── ChessGUI.java # Interface gráfica com Swing
