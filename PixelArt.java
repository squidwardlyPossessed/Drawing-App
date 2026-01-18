import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Stack;
import javax.swing.*;

public class PixelArt {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pixel Art Studio");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);

        DrawingPanel canvas = new DrawingPanel(50, 50, 10);
        frame.add(canvas, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton colorBtn = new JButton("Color");
        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(frame, "Pick Color", canvas.getCurrentColor());
            if (c != null) canvas.setCurrentColor(c);
        });
        JButton undoBtn = new JButton("Undo");
        undoBtn.addActionListener(e -> canvas.undo());
        sidebar.add(undoBtn);
        sidebar.add(Box.createVerticalStrut(10));
        JButton redoBtn = new JButton("Redo");
        redoBtn.addActionListener(e -> canvas.redo());
        sidebar.add(redoBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(colorBtn);
        sidebar.add(Box.createVerticalStrut(10));

        JButton eraseBtn = new JButton("Eraser");
        eraseBtn.addActionListener(e -> canvas.setCurrentColor(Color.WHITE));
        sidebar.add(eraseBtn);
        sidebar.add(Box.createVerticalStrut(10));

        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> canvas.clear());
        sidebar.add(clearBtn);

        frame.add(sidebar, BorderLayout.WEST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}


class DrawingPanel extends JPanel {
    private final int ROWS, COLS, CELL_SIZE;
    private final int PANEL_WIDTH, PANEL_HEIGHT;
    private final BufferedImage backBuffer;
    private Color currentColor = Color.BLACK;
    private final Stack<int[]> undoStack = new Stack<>();
    private final Stack<int[]> redoStack = new Stack<>();

    public void saveState(){
        int[] pixels = new int[PANEL_WIDTH * PANEL_HEIGHT];
        backBuffer.getRGB(0, 0, PANEL_WIDTH, PANEL_HEIGHT, pixels, 0, PANEL_WIDTH);
        undoStack.push(pixels);
        redoStack.clear();
    }

    public void undo(){
        if(!undoStack.isEmpty()){
            int[] pixels = undoStack.pop();
            int[] currentPixels = new int[PANEL_WIDTH * PANEL_HEIGHT];
            backBuffer.getRGB(0, 0, PANEL_WIDTH, PANEL_HEIGHT, currentPixels, 0, PANEL_WIDTH);
            redoStack.push(currentPixels);
            backBuffer.setRGB(0, 0, PANEL_WIDTH, PANEL_HEIGHT, pixels, 0, PANEL_WIDTH);
            repaint();
        }
    }

    public void redo(){
        if(!redoStack.isEmpty()){
            int[] pixels = redoStack.pop();
            int[] currentPixels = new int[PANEL_WIDTH * PANEL_HEIGHT];
            backBuffer.getRGB(0, 0, PANEL_WIDTH, PANEL_HEIGHT, currentPixels, 0, PANEL_WIDTH);
            undoStack.push(currentPixels);
            backBuffer.setRGB(0, 0, PANEL_WIDTH, PANEL_HEIGHT, pixels, 0, PANEL_WIDTH);
            repaint();
        }
    }   

    public DrawingPanel(int rows, int cols, int cellSize) {
        this.ROWS = rows;
        this.COLS = cols;
        this.CELL_SIZE = cellSize;
        
        this.PANEL_WIDTH = cols * cellSize;
        this.PANEL_HEIGHT = rows * cellSize;

        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        backBuffer = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = backBuffer.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g2.setColor(Color.LIGHT_GRAY);

        for (int r = 0; r <= ROWS; r++) {
            g2.drawLine(0, r * CELL_SIZE, PANEL_WIDTH, r * CELL_SIZE);
        }
        for (int c = 0; c <= COLS; c++) {
            g2.drawLine(c * CELL_SIZE, 0, c * CELL_SIZE, PANEL_HEIGHT);
        }

        MouseAdapter input = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                saveState();
                paintPixel(e.getX(), e.getY());
            }
            @Override
            public void mouseDragged(MouseEvent e) { paintPixel(e.getX(), e.getY()); }
        };
        addMouseListener(input);
        addMouseMotionListener(input);
    }

    public void setCurrentColor(Color c) { this.currentColor = c; }
    public Color getCurrentColor() { return this.currentColor; }

    public void clear() {
        saveState();

        Graphics2D g2 = backBuffer.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g2.setColor(Color.LIGHT_GRAY); 
        
        for (int r = 0; r <= ROWS; r++) {
            g2.drawLine(0, r * CELL_SIZE, PANEL_WIDTH, r * CELL_SIZE);
        }
        for (int c = 0; c <= COLS; c++) {
            g2.drawLine(c * CELL_SIZE, 0, c * CELL_SIZE, PANEL_HEIGHT);
        }
        
        g2.dispose();
        repaint();
    }

    private void paintPixel(int mouseX, int mouseY) {
        int c = mouseX / CELL_SIZE;
        int r = mouseY / CELL_SIZE;

        if (c >= 0 && c < COLS && r >= 0 && r < ROWS) {
            Graphics2D g2 = backBuffer.createGraphics();
            g2.setColor(currentColor);
            g2.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            
            g2.dispose();
            repaint(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backBuffer, 0, 0, null);
    }
}