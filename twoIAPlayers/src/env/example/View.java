package example;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Vista visual 2D del juego Tres en Raya
 */
public class View extends JFrame {

    private CellType[][] board;

    private static final int CELL_SIZE      = 130;
    private static final int GRID_PADDING   = 24;   // espacio interior del panel de juego
    private static final int CONSOLE_HEIGHT = 180;
    private static final int STROKE_WIDTH   = 7;

    // Paleta de colores
    private static final Color BG_APP        = new Color(18,  18,  24);   // fondo general
    private static final Color BG_GRID       = new Color(28,  28,  38);   // fondo del tablero
    private static final Color BG_CELL       = new Color(38,  38,  52);   // celda vacía
    private static final Color BG_CELL_HOVER = new Color(48,  48,  66);   // celda hover (futuro)
    private static final Color COLOR_GRID    = new Color(58,  58,  80);   // líneas de cuadrícula
    private static final Color COLOR_X       = new Color(255, 90,  90);   // X - rojo coral
    private static final Color COLOR_O       = new Color(90,  170, 255);  // O - azul claro
    private static final Color BG_CONSOLE    = new Color(14,  14,  20);
    private static final Color FG_CONSOLE    = new Color(160, 230, 160);
    private static final Color COLOR_HEADER  = new Color(24,  24,  34);

    private JTextArea    consoleArea;
    private JScrollPane  consoleScroll;
    private JPanel       consolePanel;
    private TresEnRayaPanel gamePanel;

    private int gridCols;
    private int gridRows;

    public View(CellType[][] board) {
        this.board    = board;
        this.gridCols = board.length;
        this.gridRows = board[0].length;

        initComponents();
        setupLayout();

        setTitle("Tres en Raya · JaCaMo / Jason");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_APP);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        Timer timer = new Timer(1000, e -> update());
        timer.start();
    }

    // -------------------------------------------------------------------------
    // Inicialización de componentes
    // -------------------------------------------------------------------------
    private void initComponents() {
        gamePanel = new TresEnRayaPanel();

        // ── Header ───────────────────────────────────────────────────────────
        JLabel titleLabel = new JLabel("  TRES EN RAYA", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        titleLabel.setForeground(new Color(200, 200, 220));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        titleLabel.setPreferredSize(new Dimension(0, 40));

        // ── Consola ───────────────────────────────────────────────────────────
        JLabel consoleLabel = new JLabel("  ▶  Activity Log", SwingConstants.LEFT);
        consoleLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        consoleLabel.setForeground(new Color(130, 200, 130));
        consoleLabel.setOpaque(true);
        consoleLabel.setBackground(new Color(20, 36, 20));
        consoleLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        consoleLabel.setPreferredSize(new Dimension(0, 30));

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        consoleArea.setBackground(BG_CONSOLE);
        consoleArea.setForeground(FG_CONSOLE);
        consoleArea.setCaretColor(FG_CONSOLE);
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);
        consoleArea.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        consoleScroll = new JScrollPane(consoleArea);
        consoleScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        consoleScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_GRID));

        consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBackground(BG_CONSOLE);
        consolePanel.setPreferredSize(new Dimension(0, CONSOLE_HEIGHT));
        consolePanel.add(consoleLabel, BorderLayout.NORTH);
        consolePanel.add(consoleScroll, BorderLayout.CENTER);

        // Panel superior con título
        this.headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_GRID));
    }

    private JPanel headerPanel;

    // -------------------------------------------------------------------------
    // Layout
    // -------------------------------------------------------------------------
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_APP);

        add(headerPanel,  BorderLayout.NORTH);
        add(gamePanel,    BorderLayout.CENTER);
        add(consolePanel, BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------
    public void update() {
        gamePanel.repaint();
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            consoleArea.append(timestamp + message + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());

            // Límite de 500 líneas
            String text  = consoleArea.getText();
            String[] lines = text.split("\n");
            if (lines.length > 500) {
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 500; i < lines.length; i++) {
                    sb.append(lines[i]).append("\n");
                }
                consoleArea.setText(sb.toString());
            }
        });
    }

    // =========================================================================
    // Panel del tablero
    // =========================================================================
    class TresEnRayaPanel extends JPanel {

        // Tamaño real del canvas (sin padding de la ventana)
        private final int canvasW = gridCols * CELL_SIZE + GRID_PADDING * 2;
        private final int canvasH = gridRows * CELL_SIZE + GRID_PADDING * 2;

        public TresEnRayaPanel() {
            setPreferredSize(new Dimension(canvasW, canvasH));
            setBackground(BG_GRID);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,  RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);

            drawCells(g2);
            drawGridLines(g2);
            drawSymbols(g2);
        }

        /** Dibuja fondos de celda con sombra sutil */
        private void drawCells(Graphics2D g) {
            for (int col = 0; col < gridCols; col++) {
                for (int row = 0; row < gridRows; row++) {
                    int px = GRID_PADDING + col * CELL_SIZE;
                    int py = GRID_PADDING + row * CELL_SIZE;

                    // Celda con esquinas redondeadas
                    RoundRectangle2D cell = new RoundRectangle2D.Float(
                            px + 4, py + 4,
                            CELL_SIZE - 8, CELL_SIZE - 8,
                            14, 14);

                    g.setColor(BG_CELL);
                    g.fill(cell);
                }
            }
        }

        /** Dibuja las líneas de la cuadrícula (solo líneas internas) — BUG FIX:
         *  antes se dibujaba un rect por celda, generando bordes dobles. */
        private void drawGridLines(Graphics2D g) {
            g.setColor(COLOR_GRID);
            g.setStroke(new BasicStroke(1.5f));

            int totalW = gridCols * CELL_SIZE;
            int totalH = gridRows * CELL_SIZE;

            // Líneas verticales internas
            for (int col = 1; col < gridCols; col++) {
                int x = GRID_PADDING + col * CELL_SIZE;
                g.drawLine(x, GRID_PADDING, x, GRID_PADDING + totalH);
            }
            // Líneas horizontales internas
            for (int row = 1; row < gridRows; row++) {
                int y = GRID_PADDING + row * CELL_SIZE;
                g.drawLine(GRID_PADDING, y, GRID_PADDING + totalW, y);
            }

            // Marco exterior
            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(80, 80, 110));
            g.drawRoundRect(GRID_PADDING, GRID_PADDING, totalW, totalH, 6, 6);
        }

        /** Dibuja X y O con trazo suavizado */
        private void drawSymbols(Graphics2D g) {
            g.setStroke(new BasicStroke(STROKE_WIDTH,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));

            for (int col = 0; col < gridCols; col++) {
                for (int row = 0; row < gridRows; row++) {
                    int px = GRID_PADDING + col * CELL_SIZE;
                    int py = GRID_PADDING + row * CELL_SIZE;

                    CellType cell = board[col][row];

                    if (cell == CellType.X) {
                        drawX(g, px, py);
                    } else if (cell == CellType.O) {
                        drawO(g, px, py);
                    }
                }
            }
        }

        private void drawX(Graphics2D g, int px, int py) {
            int margin = 28;
            int x1 = px + margin,          y1 = py + margin;
            int x2 = px + CELL_SIZE - margin, y2 = py + CELL_SIZE - margin;

            // Sombra
            g.setColor(new Color(180, 40, 40, 60));
            g.drawLine(x1 + 2, y1 + 2, x2 + 2, y2 + 2);
            g.drawLine(x2 + 2, y1 + 2, x1 + 2, y2 + 2);

            // Trazo principal
            g.setColor(COLOR_X);
            g.drawLine(x1, y1, x2, y2);
            g.drawLine(x2, y1, x1, y2);
        }

        private void drawO(Graphics2D g, int px, int py) {
            int margin = 22;
            int ox = px + margin;
            int oy = py + margin;
            int od = CELL_SIZE - margin * 2;

            // Sombra
            g.setColor(new Color(40, 100, 200, 60));
            g.drawOval(ox + 2, oy + 2, od, od);

            // Trazo principal
            g.setColor(COLOR_O);
            g.drawOval(ox, oy, od, od);
        }
    }
}