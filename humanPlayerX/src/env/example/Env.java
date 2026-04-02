package example;

// Environment code for project tresEnRaya
import jason.asSyntax.*;
import jason.environment.*;

import java.util.logging.*;

public class Env extends Environment {

    CellType[][] board = new CellType[3][3];
    View view;

    // ¿Ha terminado la partida?
    private volatile boolean gameOver = false;

    private Logger logger = Logger.getLogger("tresEnRaya." + Env.class.getName());

    @Override
    public void init(String[] args) {
        super.init(args);
        boardInit();

        this.view = new View(board);

        // ── Callback de clic humano ──────────────────────────────────────────
        // Cuando el humano hace clic en (col, row), ejecutamos su jugada aquí,
        // actualizamos el tablero y notificamos a oplayer con un percepto.
        view.setOnHumanClick((col, row) -> humanPlay(col, row));

        view.setVisible(true);
        view.setHumanTurn(true);   // el humano (X) siempre va primero

        view.logMessage("========================================");
        view.logMessage(" Sistema iniciado  |  Humano (X) vs IA (O)");
        view.logMessage(" Haz clic en una celda para jugar");
        view.logMessage("========================================");
    }

    // -------------------------------------------------------------------------
    // Jugada del humano — llamada desde el EDT (hilo de Swing)
    // -------------------------------------------------------------------------
    private synchronized void humanPlay(int col, int row) {
        if (gameOver) return;
        if (board[col][row] != CellType.EMPTY) return;

        // Colocar X en el tablero
        board[col][row] = CellType.X;
        view.logMessage("Humano jugó X en (" + col + ", " + row + ")");
        view.update();

        // Bloquear UI mientras la IA piensa
        view.setHumanTurn(false);

        // Comprobar si el humano ganó
        if (checkWin(CellType.X)) {
            gameOver = true;
            view.logMessage("¡Has ganado! ");
            view.setGameOverMessage("¡Ganaste! ");
            return;
        }
        if (checkDraw()) {
            gameOver = true;
            view.logMessage("¡Empate!");
            view.setGameOverMessage("Empate ");
            return;
        }

        // Notificar al agente oplayer: perceived played(x, col, row)
        // El agente tiene: +played(Simb,X,Y) : otherSymbol(Simb) <- ...
        // "otherSymbol(x)" es verdadero para oplayer (mySymbol(o))
        addPercept("oplayer",
                Literal.parseLiteral("played(x," + col + "," + row + ")"));
    }

    // -------------------------------------------------------------------------
    // Acciones de los agentes Jason
    // -------------------------------------------------------------------------
    @Override
    public boolean executeAction(String agName, Structure action) {
        String actionName = action.getFunctor();

        switch (actionName) {
            case "play":
                // Solo oplayer (la IA) ejecuta esta acción
                executePlay(agName, action, 'O');
                break;

            case "resetGame":
                boardInit();
                gameOver = false;
                view.setHumanTurn(true);
                view.logMessage("Partida reiniciada.");
                view.update();
                break;

            default:
                logger.warning("Acción desconocida: " + action);
        }
        return true;
    }

    private synchronized void executePlay(String agName, Structure action, char symbol) {
        if (gameOver) return;

        int col = Integer.parseInt(action.getTerm(0).toString().replace("\"", ""));
        int row = Integer.parseInt(action.getTerm(1).toString().replace("\"", ""));

        if (board[col][row] != CellType.EMPTY) {
            logger.warning("Celda ocupada: (" + col + "," + row + ")");
            return;
        }

        board[col][row] = CellType.O;
        view.logMessage("IA jugó O en (" + col + ", " + row + ")");
        view.update();

        if (checkWin(CellType.O)) {
            gameOver = true;
            view.logMessage("La IA gana. ¡Inténtalo de nuevo!");
            view.setGameOverMessage("IA gana ");
            return;
        }
        if (checkDraw()) {
            gameOver = true;
            view.logMessage("¡Empate!");
            view.setGameOverMessage("Empate ");
            return;
        }

        // Devolver el turno al humano
        view.setHumanTurn(true);
    }

    // -------------------------------------------------------------------------
    // Lógica de victoria / empate
    // -------------------------------------------------------------------------
    private boolean checkWin(CellType s) {
        // Filas y columnas
        for (int i = 0; i < 3; i++) {
            if (board[i][0]==s && board[i][1]==s && board[i][2]==s) return true;
            if (board[0][i]==s && board[1][i]==s && board[2][i]==s) return true;
        }
        // Diagonales
        if (board[0][0]==s && board[1][1]==s && board[2][2]==s) return true;
        if (board[2][0]==s && board[1][1]==s && board[0][2]==s) return true;
        return false;
    }

    private boolean checkDraw() {
        for (int c = 0; c < 3; c++)
            for (int r = 0; r < 3; r++)
                if (board[c][r] == CellType.EMPTY) return false;
        return true;
    }

    // -------------------------------------------------------------------------
    private void boardInit() {
        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board[x].length; y++)
                board[x][y] = CellType.EMPTY;
    }

    @Override
    public void stop() {
        super.stop();
    }
}