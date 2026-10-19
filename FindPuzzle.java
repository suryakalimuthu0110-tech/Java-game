import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.*;

/**
 * FIND - PUZZLE
 * Java Swing version: SOLO picture puzzle, 2x2 or 3x3.
 * Choose an image, then swap the pieces until the picture is in order.
 *
 * Run:
 *   javac FindPuzzle.java
 *   java FindPuzzle
 */
public class FindPuzzle extends JFrame {
    private final Color bg = new Color(17, 24, 39);
    private final Color panelColor = new Color(49, 46, 129);
    private final Color gold = new Color(245, 158, 11);

    private BufferedImage sourceImage;
    private int size = 2;
    private int[] pieces;
    private JButton[] tiles;
    private int selected = -1;
    private long startMillis;
    private javax.swing.Timer timer;
    private JLabel timerLabel;
    private JLabel infoLabel;
    private JPanel gridPanel;
    private JLabel previewLabel;
    private boolean finished = false;

    public FindPuzzle() {
        super("FIND - PUZZLE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 760);
        setMinimumSize(new Dimension(620, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(bg);
        showHome();
    }

    private JPanel makePanel() {
        JPanel p = new JPanel();
        p.setBackground(panelColor);
        p.setBorder(new CompoundBorder(
            new LineBorder(new Color(255,255,255,70), 1, true),
            new EmptyBorder(22, 22, 22, 22)));
        return p;
    }

    private JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setBackground(color);
        b.setForeground(color == Color.WHITE || color == new Color(229,231,235) ? bg : Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12, 16, 12, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel heading(String text, int size) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.BOLD, size));
        return l;
    }

    private void setScreen(JPanel content) {
        if (timer != null) timer.stop();
        setContentPane(content);
        getContentPane().setBackground(bg);
        revalidate();
        repaint();
    }

    private void showHome() {
        JPanel root = makePanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.add(Box.createVerticalGlue());
        JLabel title = heading("🧩 FIND - PUZZLE", 38);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);
        JLabel subtitle = heading("Find the correct picture order!", 17);
        subtitle.setForeground(new Color(209, 213, 219));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(Box.createVerticalStrut(12));
        root.add(subtitle);
        root.add(Box.createVerticalStrut(35));

        JButton solo = makeButton("SOLO", gold);
        solo.setAlignmentX(Component.CENTER_ALIGNMENT);
        solo.setMaximumSize(new Dimension(380, 55));
        solo.addActionListener(e -> showSetup());
        root.add(solo);
        root.add(Box.createVerticalStrut(14));

        JLabel note = new JLabel("<html><center>Choose a photo from your computer.<br/>Tap one tile, then another tile to swap them.</center></html>");
        note.setForeground(Color.WHITE);
        note.setFont(new Font("Arial", Font.PLAIN, 14));
        note.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(note);
        root.add(Box.createVerticalGlue());
        setScreen(root);
    }

    private void showSetup() {
        JPanel root = makePanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        JLabel title = heading("Choose Your Mystery Image", 26);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(10));

        previewLabel = new JLabel("No image selected", SwingConstants.CENTER);
        previewLabel.setForeground(Color.WHITE);
        previewLabel.setPreferredSize(new Dimension(230, 190));
        previewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(previewLabel);
        root.add(Box.createVerticalStrut(12));

        JButton choose = makeButton("SELECT IMAGE", gold);
        choose.setAlignmentX(Component.CENTER_ALIGNMENT);
        choose.addActionListener(e -> chooseImage());
        root.add(choose);
        root.add(Box.createVerticalStrut(20));

        JLabel difficulty = heading("Choose Difficulty", 20);
        difficulty.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(difficulty);
        root.add(Box.createVerticalStrut(10));

        JPanel choices = new JPanel(new GridLayout(1, 2, 12, 0));
        choices.setOpaque(false);
        JButton simple = makeButton("SIMPLE — 2 × 2", Color.WHITE);
        JButton hard = makeButton("HARD — 3 × 3", Color.WHITE);
        simple.addActionListener(e -> { size = 2; startGame(); });
        hard.addActionListener(e -> { size = 3; startGame(); });
        choices.add(simple);
        choices.add(hard);
        root.add(choices);
        root.add(Box.createVerticalStrut(16));

        JButton back = makeButton("BACK", new Color(229,231,235));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> showHome());
        root.add(back);

        setScreen(root);
        if (sourceImage != null) updatePreview();
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a puzzle image");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                sourceImage = ImageIO.read(chooser.getSelectedFile());
                if (sourceImage == null) throw new Exception("Unsupported image");
                updatePreview();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open that image. Please choose a PNG or JPG file.",
                    "Image Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updatePreview() {
        Image scaled = sourceImage.getScaledInstance(190, 170, Image.SCALE_SMOOTH);
        previewLabel.setIcon(new ImageIcon(scaled));
        previewLabel.setText("");
    }

    private void startGame() {
        if (sourceImage == null) {
            JOptionPane.showMessageDialog(this, "First select an image from your gallery.");
            return;
        }
        int total = size * size;
        pieces = new int[total];
        for (int i = 0; i < total; i++) pieces[i] = i;
        // Shuffle and ensure the puzzle does not start solved.
        Random random = new Random();
        do {
            for (int i = total - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int t = pieces[i]; pieces[i] = pieces[j]; pieces[j] = t;
            }
        } while (isSolved());

        selected = -1;
        finished = false;
        buildGameScreen();
        startMillis = System.currentTimeMillis();
        timer = new javax.swing.Timer(250, e -> updateTimer());
        timer.start();
    }

    private void buildGameScreen() {
        JPanel root = makePanel();
        root.setLayout(new BorderLayout(12, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel player = heading("Player: You", 18);
        player.setHorizontalAlignment(SwingConstants.LEFT);
        top.add(player, BorderLayout.WEST);
        timerLabel = heading("00:00", 24);
        top.add(timerLabel, BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JLabel refTitle = heading("Final Image", 13);
        Image scaled = sourceImage.getScaledInstance(110, 110, Image.SCALE_SMOOTH);
        JLabel ref = new JLabel(new ImageIcon(scaled));
        ref.setBorder(new LineBorder(Color.WHITE, 2));
        right.add(refTitle, BorderLayout.NORTH);
        right.add(ref, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(size, size, 8, 8));
        gridPanel.setBackground(bg);
        gridPanel.setBorder(new EmptyBorder(10, 25, 10, 25));
        tiles = new JButton[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            final int position = i;
            JButton tile = new JButton();
            tile.setMargin(new Insets(0,0,0,0));
            tile.setFocusPainted(false);
            tile.setBorder(new LineBorder(new Color(255,255,255,100), 2, true));
            tile.setBackground(bg);
            tile.addActionListener(e -> tileClicked(position));
            tiles[i] = tile;
            gridPanel.add(tile);
        }
        root.add(gridPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8,8));
        bottom.setOpaque(false);
        infoLabel = heading("Tap one piece, then tap another piece to swap.", 14);
        infoLabel.setForeground(new Color(209,213,219));
        bottom.add(infoLabel, BorderLayout.CENTER);
        JButton back = makeButton("BACK TO MENU", new Color(229,231,235));
        back.addActionListener(e -> showHome());
        bottom.add(back, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        renderTiles();
        setScreen(root);
    }

    private void tileClicked(int pos) {
        if (finished) return;
        if (selected == -1) {
            selected = pos;
            infoLabel.setText("Piece selected. Now choose the box to swap with it.");
            renderTiles();
            return;
        }
        if (selected == pos) {
            selected = -1;
            infoLabel.setText("Selection cleared. Choose a piece.");
            renderTiles();
            return;
        }
        int temp = pieces[selected];
        pieces[selected] = pieces[pos];
        pieces[pos] = temp;
        selected = -1;
        renderTiles();
        if (isSolved()) finishGame();
        else infoLabel.setText("Good! Keep arranging the pieces.");
    }

    private void renderTiles() {
        for (int pos = 0; pos < pieces.length; pos++) {
            BufferedImage pieceImage = getPieceImage(pieces[pos]);
            tiles[pos].setIcon(new ImageIcon(pieceImage));
            tiles[pos].setText("");
            tiles[pos].setBorder(new LineBorder(pos == selected ? gold : new Color(255,255,255,100),
                pos == selected ? 5 : 2, true));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private BufferedImage getPieceImage(int piece) {
        int w = sourceImage.getWidth();
        int h = sourceImage.getHeight();
        int col = piece % size;
        int row = piece / size;
        int x1 = col * w / size;
        int y1 = row * h / size;
        int x2 = (col + 1) * w / size;
        int y2 = (row + 1) * h / size;
        BufferedImage crop = sourceImage.getSubimage(x1, y1, x2-x1, y2-y1);
        int out = Math.max(120, 420 / size);
        BufferedImage scaled = new BufferedImage(out, out, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(crop, 0, 0, out, out, null);
        g.dispose();
        return scaled;
    }

    private boolean isSolved() {
        if (pieces == null) return false;
        for (int i = 0; i < pieces.length; i++) if (pieces[i] != i) return false;
        return true;
    }

    private void updateTimer() {
        long seconds = (System.currentTimeMillis() - startMillis) / 1000;
        timerLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
    }

    private void finishGame() {
        finished = true;
        if (timer != null) timer.stop();
        long seconds = (System.currentTimeMillis() - startMillis) / 1000;
        String time = String.format("%02d:%02d", seconds / 60, seconds % 60);
        JPanel root = makePanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.add(Box.createVerticalGlue());
        JLabel medal = heading("🏆", 56);
        medal.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(medal);
        JLabel title = heading("GREAT THINKING. YOU CRACKED IT!", 26);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(14));
        JLabel completed = heading("Puzzle Completed!", 22);
        completed.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(completed);
        root.add(Box.createVerticalStrut(8));
        JLabel resultTime = heading("Completion Time: " + time, 18);
        resultTime.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(resultTime);
        root.add(Box.createVerticalStrut(25));
        JButton again = makeButton("PLAY AGAIN", gold);
        again.setAlignmentX(Component.CENTER_ALIGNMENT);
        again.addActionListener(e -> showSetup());
        root.add(again);
        root.add(Box.createVerticalStrut(10));
        JButton home = makeButton("HOME", Color.WHITE);
        home.setAlignmentX(Component.CENTER_ALIGNMENT);
        home.addActionListener(e -> showHome());
        root.add(home);
        root.add(Box.createVerticalGlue());
        setScreen(root);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FindPuzzle().setVisible(true));
    }
}
