package example;

// Environment code for project tresEnRaya
import jason.asSyntax.*;
import jason.environment.*;
import jason.asSyntax.parser.*;

//import java.lang.runtime.ExactConversionsSupport;

import java.util.logging.*;

public class Env extends Environment {

    CellType[][] board = new CellType[3][3];
    View view;

    private Logger logger = Logger.getLogger("tresEnRaya." + Env.class.getName());

    @Override
    public void init(String[] args) {
        super.init(args);
        // Inicializar el tablero vacío
        boardInit();
        // Inicializar vista
        view = new View(board);
        view.setVisible(true);
  

        // Mensaje de bienvenida en la consola
        view.logMessage("========================================");
        view.logMessage(" System Initialized");
        view.logMessage("   Grid: 3X3 | Cell Size: 120px");
        view.logMessage("========================================");
        view.logMessage("");

    }

    @Override
    public boolean executeAction(String agName, Structure action) {

        String actionName = action.getFunctor();

        switch (actionName) {
            case "play":
                char symbol = (agName.equals("xplayer")) ? 'X' : 'O';
                executePlay(agName, action, symbol);
                break;
 
            case "resetGame":
                boardInit();
                view.logMessage("Game reset by " + agName);
                view.update();
                break;
            default:
                logger.warning("Unknown action: " + action);
        }
        return true;
    }

    /**
     * Called before the end of MAS execution
     */
    @Override
    public void stop() {
        super.stop();
    }

    private void executePlay(String agName, Structure action, char symbol) {
        int X = Integer.parseInt(action.getTerm(0).toString().replace("\"", ""));
        int Y = Integer.parseInt(action.getTerm(1).toString().replace("\"", ""));

        if (board[X][Y] == CellType.EMPTY) {
            board[X][Y] = (symbol == 'X') ? CellType.X : CellType.O;
            view.logMessage("Agent " + agName + " played " + symbol + " at (" + X + ", " + Y + ")");
            view.update();
        }
    }
    /**
     * Inicializa el tablero con celdas vacías
     */
    private void boardInit() {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                board[x][y] = CellType.EMPTY;
            }
        }
    }
}
