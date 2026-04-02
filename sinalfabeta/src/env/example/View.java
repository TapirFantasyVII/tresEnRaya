package example;

import javax.swing.*;

import example.CellType;

import java.awt.*;

/**
 * Vista visual 2D del almacén
 */
public class View extends JFrame {

    private CellType[][] board;

    private static final int INFO_PANEL_WIDTH = 450;
    private static final int CONSOLE_HEIGHT = 180;
    private static final int CELL_SIZE = 120;

    private int gridWidth = 3;
    private int gridHeight = 3;

    private JTextArea consoleArea;
    private JScrollPane consoleScroll;
    private TresEnRayaPanel panel;

    public View(CellType[][] board) {
        this.board = board;

        initComponents();
        setupLayout();

        setTitle("tres en raya - Jason/JaCaMo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        // Actualizar cada segundo
        Timer timer = new Timer(1000, e -> update());
        timer.start();
    }

    private void initComponents() {
        panel = new TresEnRayaPanel();

        // Panel de consola
        JPanel consolePanel = new JPanel();
        consolePanel.setLayout(new BorderLayout());
        consolePanel.setPreferredSize(new Dimension(gridWidth * CELL_SIZE + INFO_PANEL_WIDTH, CONSOLE_HEIGHT));

        JLabel consoleLabel = new JLabel("Console - Activity Log");
        consoleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        consoleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        consoleLabel.setOpaque(true);
        consoleLabel.setBackground(new Color(60, 60, 60));
        consoleLabel.setForeground(Color.WHITE);

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(new Color(200, 255, 200));
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);

        consoleScroll = new JScrollPane(consoleArea);
        consoleScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        consolePanel.add(consoleLabel, BorderLayout.NORTH);
        consolePanel.add(consoleScroll, BorderLayout.CENTER);

        // Guardar referencia al panel de consola para usarlo en setupLayout
        this.consolePanel = consolePanel;
    }

    private JPanel consolePanel;

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(consolePanel, BorderLayout.SOUTH);
    }

    /**
     * Actualiza la visualización
     */
    public void update() {
        panel.repaint();

    }

    /**
     * Añade un mensaje a la consola
     */
    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            consoleArea.append(timestamp + message + "\n");

            // Auto-scroll hacia abajo
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());

            // Limitar a 500 líneas para evitar uso excesivo de memoria
            String text = consoleArea.getText();
            String[] lines = text.split("\n");
            if (lines.length > 500) {
                StringBuilder newText = new StringBuilder();
                for (int i = lines.length - 500; i < lines.length; i++) {
                    newText.append(lines[i]).append("\n");
                }
                consoleArea.setText(newText.toString());
            }
        });
    }

    /**
     * Panel donde se dibuja el juego de tres en raya
     */
    class TresEnRayaPanel extends JPanel {

        public TresEnRayaPanel() {
            setPreferredSize(new Dimension(gridWidth * CELL_SIZE, gridHeight * CELL_SIZE));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawGrid(g2d);

        }

        private void drawGrid(Graphics2D g) {
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    int px = x * CELL_SIZE;
                    int py = y * CELL_SIZE;

                    // Fondo
                    g.setColor(new Color(250, 250, 250));
                    g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                    // Dibujar contenido
                    if (board[x][y] == CellType.X) {
                        g.setColor(Color.RED);
                        g.setStroke(new BasicStroke(5));
                        // Diagonales de la X
                        g.drawLine(px + 20, py + 20, px + CELL_SIZE - 20, py + CELL_SIZE - 20);
                        g.drawLine(px + CELL_SIZE - 20, py + 20, px + 20, py + CELL_SIZE - 20);

                    } else if (board[x][y] == CellType.O) {
                        g.setColor(Color.BLUE);
                        g.setStroke(new BasicStroke(5));
                        // Círculo (O)
                        g.drawOval(px + 20, py + 20, CELL_SIZE - 40, CELL_SIZE - 40);
                    }
                    // Borde de celda
                    g.setColor(new Color(200, 200, 200));
                    g.drawRect(px, py, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
}