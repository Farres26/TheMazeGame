package Game;

import javax.swing.*;

public class Game {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Escape the Dungeon");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                Maze maze = new Maze();
                GamePanel gamePanel = new GamePanel(maze);
                frame.add(gamePanel);

                frame.pack(); //frame to fit size
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                gamePanel.requestFocusInWindow(); //keyboard input is registered
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
