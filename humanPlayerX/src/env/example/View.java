package example;

import javax.swing.*;
import example.CellType;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.function.BiConsumer;

/**
 * Vista visual 2D del juego Tres en Raya
 * El jugador humano (X) hace clic en la celda para jugar.
 */
public class View extends JFrame {

    private CellType[][] board;

    private static final int CELL_SIZE      = 130;
    private static final int GRID_PADDING   = 24;
    private static final int CONSOLE_HEIGHT = 180;
    private static final int STROKE_WIDTH   = 7;

    // Paleta de colores
    private static final Color BG_APP       = new Color(18,  18,  24);
    private static final Color BG_GRID      = new Color(28,  28,  38);
    private static final Color BG_CELL      = new Color(38,  38,  52);
    private static final Color BG_CELL_HOV  = new Color(55,  55,  78);   // hover humano
    private static final Color COLOR_GRID   = new Color(58,  58,  80);
    private static final Color COLOR_X      = new Color(255, 90,  90);
    private static final Color COLOR_O      = new Color(90,  170, 255);
    private static final Color BG_CONSOLE   = new Color(14,  14,  20);
    private static final Color FG_CONSOLE   = new Color(160, 230, 160);
    private static final Color COLOR_HEADER = new Color(24,  24,  34);

    // Estado de hover (celda bajo el cursor)
    private int hoverCol = -1;
    private int hoverRow = -1;

    // Callback: (col, row) → Env.humanPlay(col, row)
    private BiConsumer<Integer, Integer> onHumanClick;

    // ¿Es el turno del humano?
    private volatile boolean humanTurn = true;

    private JTextArea       consoleArea;
    private JScrollPane     consoleScroll;
    private JPanel          consolePanel;
    private JPanel          headerPanel;
    private JLabel          turnLabel;
    private TresEnRayaPanel gamePanel;

    private int gridCols;
    private int gridRows;

    public View(CellType[][] board) {
        this.board    = board;
        this.gridCols = board.length;
        this.gridRows = board[0].length;

        initComponents();
        setupLayout();

        setTitle("Tres en Raya · Humano vs IA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_APP);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        Timer timer = new Timer(500, e -> update());
        timer.start();
    }

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /** Registra el callback que se invoca cuando el humano hace clic en una celda válida. */
    public void setOnHumanClick(BiConsumer<Integer, Integer> callback) {
        this.onHumanClick = callback;
    }

    /** Activa o desactiva el turno humano (bloquea clics durante el turno de la IA). */
    public void setHumanTurn(boolean enabled) {
        this.humanTurn = enabled;
        SwingUtilities.invokeLater(() -> {
            if (enabled) {
                turnLabel.setText("  ● Tu turno  (X)");
                turnLabel.setForeground(COLOR_X);
            } else {
                turnLabel.setText("  ○ IA pensando…  (O)");
                turnLabel.setForeground(COLOR_O);
            }
            gamePanel.repaint();
        });
    }

    /** Muestra un mensaje de fin de partida en el header. */
    public void setGameOverMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            turnLabel.setText("  " + msg);
            turnLabel.setForeground(new Color(220, 200, 80));
        });
    }

    public void update() {
        gamePanel.repaint();
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            consoleArea.append(timestamp + message + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());

            String text = consoleArea.getText();
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

    // -------------------------------------------------------------------------
    // Inicialización
    // -------------------------------------------------------------------------
    private void initComponents() {
        gamePanel = new TresEnRayaPanel();

        // ── Turno label ───────────────────────────────────────────────────────
        turnLabel = new JLabel("  ● Tu turno  (X)", SwingConstants.LEFT);
        turnLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        turnLabel.setForeground(COLOR_X);

        JLabel titleLabel = new JLabel("TRES EN RAYA  ·  Humano vs IA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        titleLabel.setForeground(new Color(180, 180, 200));

        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        headerPanel.add(titleLabel,  BorderLayout.CENTER);
        headerPanel.add(turnLabel,   BorderLayout.WEST);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_GRID),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

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
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_APP);
        add(headerPanel,  BorderLayout.NORTH);
        add(gamePanel,    BorderLayout.CENTER);
        add(consolePanel, BorderLayout.SOUTH);
    }

    // =========================================================================
    // Panel del tablero
    // =========================================================================
    class TresEnRayaPanel extends JPanel {

        private final int canvasW = gridCols * CELL_SIZE + GRID_PADDING * 2;
        private final int canvasH = gridRows * CELL_SIZE + GRID_PADDING * 2;

        public TresEnRayaPanel() {
            setPreferredSize(new Dimension(canvasW, canvasH));
            setBackground(BG_GRID);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // ── Hover ─────────────────────────────────────────────────────────
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int col = (e.getX() - GRID_PADDING) / CELL_SIZE;
                    int row = (e.getY() - GRID_PADDING) / CELL_SIZE;

                    boolean inside = col >= 0 && col < gridCols && row >= 0 && row < gridRows;
                    int newCol = inside ? col : -1;
                    int newRow = inside ? row : -1;

                    if (newCol != hoverCol || newRow != hoverRow) {
                        hoverCol = newCol;
                        hoverRow = newRow;
                        repaint();
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoverCol = -1;
                    hoverRow = -1;
                    repaint();
                }

                // ── Clic ──────────────────────────────────────────────────────
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!humanTurn || onHumanClick == null) return;

                    int col = (e.getX() - GRID_PADDING) / CELL_SIZE;
                    int row = (e.getY() - GRID_PADDING) / CELL_SIZE;

                    if (col < 0 || col >= gridCols || row < 0 || row >= gridRows) return;
                    if (board[col][row] != CellType.EMPTY) return;

                    onHumanClick.accept(col, row);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);

            drawCells(g2);
            drawGridLines(g2);
            drawSymbols(g2);
        }

        private void drawCells(Graphics2D g) {
            for (int col = 0; col < gridCols; col++) {
                for (int row = 0; row < gridRows; row++) {
                    int px = GRID_PADDING + col * CELL_SIZE;
                    int py = GRID_PADDING + row * CELL_SIZE;

                    boolean isHover = humanTurn
                            && col == hoverCol && row == hoverRow
                            && board[col][row] == CellType.EMPTY;

                    RoundRectangle2D cell = new RoundRectangle2D.Float(
                            px + 4, py + 4,
                            CELL_SIZE - 8, CELL_SIZE - 8,
                            14, 14);

                    g.setColor(isHover ? BG_CELL_HOV : BG_CELL);
                    g.fill(cell);

                    // Borde de hover destacado
                    if (isHover) {
                        g.setColor(new Color(COLOR_X.getRed(), COLOR_X.getGreen(), COLOR_X.getBlue(), 80));
                        g.setStroke(new BasicStroke(2f));
                        g.draw(cell);
                    }
                }
            }
        }

        private void drawGridLines(Graphics2D g) {
            g.setColor(COLOR_GRID);
            g.setStroke(new BasicStroke(1.5f));

            int totalW = gridCols * CELL_SIZE;
            int totalH = gridRows * CELL_SIZE;

            for (int col = 1; col < gridCols; col++) {
                int x = GRID_PADDING + col * CELL_SIZE;
                g.drawLine(x, GRID_PADDING, x, GRID_PADDING + totalH);
            }
            for (int row = 1; row < gridRows; row++) {
                int y = GRID_PADDING + row * CELL_SIZE;
                g.drawLine(GRID_PADDING, y, GRID_PADDING + totalW, y);
            }

            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(80, 80, 110));
            g.drawRoundRect(GRID_PADDING, GRID_PADDING, totalW, totalH, 6, 6);
        }

        private void drawSymbols(Graphics2D g) {
            g.setStroke(new BasicStroke(STROKE_WIDTH,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int col = 0; col < gridCols; col++) {
                for (int row = 0; row < gridRows; row++) {
                    int px = GRID_PADDING + col * CELL_SIZE;
                    int py = GRID_PADDING + row * CELL_SIZE;
                    CellType cell = board[col][row];
                    if      (cell == CellType.X) drawX(g, px, py);
                    else if (cell == CellType.O) drawO(g, px, py);
                }
            }
        }

        private void drawX(Graphics2D g, int px, int py) {
            int margin = 28;
            int x1 = px + margin,             y1 = py + margin;
            int x2 = px + CELL_SIZE - margin,  y2 = py + CELL_SIZE - margin;
            g.setColor(new Color(180, 40, 40, 60));
            g.drawLine(x1+2, y1+2, x2+2, y2+2);
            g.drawLine(x2+2, y1+2, x1+2, y2+2);
            g.setColor(COLOR_X);
            g.drawLine(x1, y1, x2, y2);
            g.drawLine(x2, y1, x1, y2);
        }

        private void drawO(Graphics2D g, int px, int py) {
            int margin = 22;
            int ox = px + margin, oy = py + margin;
            int od = CELL_SIZE - margin * 2;
            g.setColor(new Color(40, 100, 200, 60));
            g.drawOval(ox+2, oy+2, od, od);
            g.setColor(COLOR_O);
            g.drawOval(ox, oy, od, od);
        }
    }
}