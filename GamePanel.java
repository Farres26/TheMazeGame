package Game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JFrame;

public class GamePanel extends JPanel implements KeyListener {
    private final Maze maze;
    private final int cellSize = 40;
    private final JLabel statusLabel;
    private final JLabel timerLabel;
    private final JLabel monsterStatusLabel;

    private int currentDx = 0, currentDy = 0;
    private int desiredDx = 0, desiredDy = 0;
    private boolean movementActive = false;
    private boolean gameEnded = false;
    private boolean isStuck = false;

    public static int runNum = 0;
    private int elapsedTime = 0;

    //Monster speed scales with each run, starting at 400 and multiplied by 0.75 per run
    int baseMonsterDelay = 400;
    int adjustedMonsterDelay = (int)(baseMonsterDelay * Math.pow(0.75, runNum));

    public GamePanel(Maze maze) {
        if (GamePanel.runNum == 0) {
            String tutorialMessage = """
                You are trapped in a dungeon with a monster.
                You must escape by collecting all four yellow keys and escaping through the green door.
                
                There are twelve orange traps scattered around the maze.
                Stepping in one will freeze you in place for one second, destroying the trap.
                The monster can also step in traps, but it is not frozen by them.
                
                The monster can hear you if you are close to it, and see you if you are within its line of sight.
                
                Once time is up, the game is not over, but the monster will become enraged and know where you are constantly.
                
                Once you have escaped, you will be in the maze again. Because you cannot escape.
                It is slow now, but every time you escape, it will be faster and faster, until eventually you cannot outrun it.
                
                Good luck.
                """;
        
            JOptionPane.showMessageDialog(this, tutorialMessage, "Tutorial", JOptionPane.INFORMATION_MESSAGE);
        }
        
        this.maze = maze;
        setPreferredSize(new Dimension(maze.getWidth() * cellSize + 150, maze.getHeight() * cellSize));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        //Status label: shows how many keys have been collected
        statusLabel = new JLabel("Keys Collected: 0 / " + maze.getTotalKeys());
        statusLabel.setBounds(maze.getWidth() * cellSize + 20, 20, 150, 30);
        this.add(statusLabel);

        //Displays current run count
        JLabel runLabel = new JLabel("Runs Completed: " + runNum);
        runLabel.setBounds(maze.getWidth() * cellSize + 20, 60, 150, 345);
        this.add(runLabel);

        //Displays elapsed time
        timerLabel = new JLabel("Time: 0s");
        timerLabel.setBounds(maze.getWidth() * cellSize + 20, 100, 150, 300);
        this.add(timerLabel);

        //Label showing monster status
        monsterStatusLabel = new JLabel(" ");
        monsterStatusLabel.setBounds(maze.getWidth() * cellSize + 20, 140, 150, 260);
        this.add(monsterStatusLabel);

        //Game timer (1 second interval)
        Timer timeTimer = new Timer(1000, e -> {
            if (!gameEnded) {
                elapsedTime++;
                timerLabel.setText("Time remaining: " + (60 - elapsedTime) + "s");
                if (elapsedTime >= 60) {
                    System.out.println("Monster is enraged!");
                    monsterStatusLabel.setText("Monster is enraged!");
                    timerLabel.setText("Time remaining: 0s");
                }
            }
        });
        timeTimer.start();

        //Monster movement timer
        Timer monsterTimer = new Timer(adjustedMonsterDelay, e -> {
            if (!gameEnded) {
                Monster monster = maze.getMonster();
                Player player = maze.getPlayer();

                monster.move(player, maze);

                if (monster.checkIfCaught(player)) {
                    gameEnded = true;
                    JOptionPane.showMessageDialog(this, "GAME OVER! The monster caught you!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }

                repaint();
            }
        });
        monsterTimer.start();

        //Player movement timer
        Timer movementTimer = new Timer(150, e -> {
            if (movementActive && !gameEnded && !isStuck) {
                Player player = maze.getPlayer();
                int tryX = player.getX() + desiredDx;
                int tryY = player.getY() + desiredDy;
        
                //Try turning if possible
                if (maze.isWalkable(tryX, tryY)) {
                    currentDx = desiredDx;
                    currentDy = desiredDy;
                }
        
                //Try moving in current direction
                int newX = player.getX() + currentDx;
                int newY = player.getY() + currentDy;
                if (maze.isWalkable(newX, newY)) {
                    movePlayer(currentDx, currentDy);
                }
            }
        });
        movementTimer.start();        

        requestFocusInWindow(); //Force focus for key input
    }

    //Triggers certain events upon the player moving onto a special tile type
    private void movePlayer(int dx, int dy) {
        Player player = maze.getPlayer();
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;

        if (!maze.isWalkable(newX, newY)) return;

        char targetCell = maze.getCell(newX, newY);

        //Collect key
        if (targetCell == 'K') {
            player.collectKey();
            maze.setCell(newX, newY, ' ');
            statusLabel.setText("Keys Collected: " + player.getKeysCollected() + " / " + maze.getTotalKeys());
        }

        //Trigger trap (freezes player for 1 second)
        if (targetCell == 'T') {
            isStuck = true;
            Timer trapTimer = new Timer(1000, e -> isStuck = false);
            trapTimer.setRepeats(false);
            trapTimer.start();
        }

        //Door logic
        if (targetCell == 'D') {
            if (player.getKeysCollected() >= maze.getTotalKeys()) {
                if (!gameEnded) {
                    gameEnded = true;
                    int response = JOptionPane.showConfirmDialog(this, "YOU ESCAPED! Play again?", "Victory", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        runNum++; //Track number of runs
                        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                        topFrame.dispose();
                        SwingUtilities.invokeLater(() -> {
                            JFrame newFrame = new JFrame("Escape the Dungeon");
                            Maze newMaze = new Maze();
                            GamePanel newPanel = new GamePanel(newMaze);
                            newFrame.add(newPanel);
                            newFrame.pack();
                            newFrame.setLocationRelativeTo(null);
                            newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            newFrame.setVisible(true);
                        });
                    } else {
                        System.exit(0);
                    }
                }
                return;
            } else {
                if (!gameEnded) {
                    JOptionPane.showMessageDialog(this, "You need all the keys before escaping!", "Door Locked", JOptionPane.WARNING_MESSAGE);
                }
                return;
            }
        }

        //Move player and update game state
        maze.updatePosition(player.getX(), player.getY(), newX, newY, 'P');
        player.setPosition(newX, newY);

        //Check for monster encounter after move
        if (maze.getMonster().checkIfCaught(player)) {
            if (!gameEnded) {
                gameEnded = true;
                JOptionPane.showMessageDialog(this, "GAME OVER! The monster caught you!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        }

        repaint();
    }

    //Paints the maze and legend on the side.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                char cell = maze.getCell(x, y);
                switch (cell) {
                    case '#': g.setColor(Color.BLACK); break;
                    case ' ': g.setColor(Color.WHITE); break;
                    case 'P': g.setColor(Color.BLUE); break;
                    case 'K': g.setColor(Color.YELLOW); break;
                    case 'T': g.setColor(Color.ORANGE); break;
                    case 'M': g.setColor(Color.RED); break;
                    case 'D': g.setColor(Color.GREEN); break;
                    default:  g.setColor(Color.PINK); break;
                }
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        drawKeyLegend(g);
    }

    //Prints the legend explaining each game symbol
    private void drawKeyLegend(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawString("KEY LEGEND:", maze.getWidth() * cellSize + 20, 60);

        g.setColor(Color.BLUE);
        g.fillRect(maze.getWidth() * cellSize + 20, 80, 20, 20);
        g.setColor(Color.BLACK);
        g.drawString("Player (P)", maze.getWidth() * cellSize + 50, 95);

        g.setColor(Color.YELLOW);
        g.fillRect(maze.getWidth() * cellSize + 20, 110, 20, 20);
        g.setColor(Color.BLACK);
        g.drawString("Key (K)", maze.getWidth() * cellSize + 50, 125);

        g.setColor(Color.RED);
        g.fillRect(maze.getWidth() * cellSize + 20, 140, 20, 20);
        g.setColor(Color.BLACK);
        g.drawString("Monster (M)", maze.getWidth() * cellSize + 50, 155);

        g.setColor(Color.ORANGE);
        g.fillRect(maze.getWidth() * cellSize + 20, 170, 20, 20);
        g.setColor(Color.BLACK);
        g.drawString("Trap (T)", maze.getWidth() * cellSize + 50, 185);

        g.setColor(Color.GREEN);
        g.fillRect(maze.getWidth() * cellSize + 20, 200, 20, 20);
        g.setColor(Color.BLACK);
        g.drawString("Door (D)", maze.getWidth() * cellSize + 50, 215);
    }

    //Handle keyboard input
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: desiredDx = 0; desiredDy = -1; movementActive = true; break;
            case KeyEvent.VK_S: desiredDx = 0; desiredDy = 1; movementActive = true; break;
            case KeyEvent.VK_A: desiredDx = -1; desiredDy = 0; movementActive = true; break;
            case KeyEvent.VK_D: desiredDx = 1; desiredDy = 0; movementActive = true; break;
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {
        movementActive = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}