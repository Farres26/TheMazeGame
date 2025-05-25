package Game;

import java.util.*;
import javax.swing.Timer;

/* The Monster class represents the enemy in the maze.
   It can pathfind, roam randomly, detect the player via line of sight or proximity,
   and becomes enraged after a set time. */
public class Monster {
    private int x, y;                         //Current position of the monster
    private int lastMoveX = 0, lastMoveY = 0; //Last movement direction for smarter roaming

    private Timer rageTimer;                  //Timer to track when monster becomes enraged
    private int elapsedTime = 0;              //Time passed since monster spawned
    private static boolean enraged = false;   //Static flag to indicate rage state

    public static boolean isEnraged() {
        return enraged;
    }

    public Monster(int x, int y) {
        enraged = false;
        elapsedTime = 0;
        this.x = x;
        this.y = y;

        //Start rage timer; after 60 seconds, monster becomes enraged
        rageTimer = new Timer(1000, e -> {
            elapsedTime++;
            if (elapsedTime >= 60) {
                enraged = true;
                rageTimer.stop();
            }
        });
        rageTimer.start();
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //Moves the monster toward the player or roams randomly if out of range.
    public void move(Player player, Maze maze) {
        if (isPlayerWithinRange(player, 3) || seesPlayer(player, maze)) {
            pathfindTo(player.getX(), player.getY(), maze);
        } else {
            roamRandomly(maze);
        }
    }

    //Pathfinding using BFS toward a target coordinate.
    public void pathfindTo(int targetX, int targetY, Maze maze) {
        class PathNode {
            int x, y;
            PathNode prev;
            PathNode(int x, int y, PathNode previous) {
                this.x = x;
                this.y = y;
                this.prev = previous;
            }
        }

        Queue<PathNode> explore = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        explore.add(new PathNode(x, y, null));
        visited.add(x + "," + y);

        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};
        PathNode foundPath = null;

        while (!explore.isEmpty()) {
            PathNode current = explore.poll();

            if (current.x == targetX && current.y == targetY) {
                foundPath = current;
                break;
            }

            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];
                String key = newX + "," + newY;

                if (!maze.isWalkable(newX, newY)) continue;

                char cell = maze.getCell(newX, newY);
                if (cell == 'K' || cell == 'D' || visited.contains(key)) continue;

                explore.add(new PathNode(newX, newY, current));
                visited.add(key);
            }
        }

        if (foundPath != null && foundPath.prev != null) {
            PathNode step = foundPath;
            while (step.prev != null && step.prev.prev != null) {
                step = step.prev;
            }
            maze.updatePosition(x, y, step.x, step.y, 'M');
            setPosition(step.x, step.y);
        }
    }

    //Roams the maze in a semi-random way, avoiding dead ends.
    public void roamRandomly(Maze maze) {
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        List<int[]> validMoves = new ArrayList<>();

        for (int[] dir : directions) {
            int newX = x + dir[0], newY = y + dir[1];

            //Avoid moving backwards
            if (dir[0] == -lastMoveX && dir[1] == -lastMoveY) continue;

            if (canMoveTo(newX, newY, maze) && !isDeadEnd(newX, newY, maze)) {
                validMoves.add(dir);
            }
        }

        //If no smart moves, just pick any walkable one
        if (validMoves.isEmpty()) {
            for (int[] dir : directions) {
                int newX = x + dir[0], newY = y + dir[1];
                if (canMoveTo(newX, newY, maze)) {
                    validMoves.add(dir);
                }
            }
        }

        if (!validMoves.isEmpty()) {
            int[] move = validMoves.get((int)(Math.random() * validMoves.size()));
            int newX = x + move[0], newY = y + move[1];
            maze.updatePosition(x, y, newX, newY, 'M');
            setPosition(newX, newY);
            lastMoveX = move[0];
            lastMoveY = move[1];
        }
    }

    /* Moves directly toward the player by comparing coordinates.
       Used if already adjacent and not pathfinding.*/
    public void chasePlayer(Player player, Maze maze) {
        int dx = Integer.compare(player.getX(), x);
        int dy = Integer.compare(player.getY(), y);

        int newX = x + dx, newY = y + dy;
        if (canMoveTo(newX, newY, maze)) {
            maze.updatePosition(x, y, newX, newY, 'M');
            setPosition(newX, newY);
        }
    }

    //Checks if the monster is within a given range of the player.
    private boolean isPlayerWithinRange(Player player, int range) {
        if (Monster.isEnraged()) return true;
        int dx = Math.abs(player.getX() - x);
        int dy = Math.abs(player.getY() - y);
        return dx + dy <= range;
    }

    //Checks if the monster can see the player in straight lines (no wall in between).
    public boolean seesPlayer(Player player, Maze maze) {
        //Check left
        for (int i = x - 1; i >= 0; i--) {
            if (maze.getCell(i, y) == '#') break;
            if (player.getX() == i && player.getY() == y) return true;
        }
        //Check up
        for (int i = y - 1; i >= 0; i--) {
            if (maze.getCell(x, i) == '#') break;
            if (player.getX() == x && player.getY() == i) return true;
        }
        //Check down
        for (int i = y + 1; i < maze.getHeight(); i++) {
            if (maze.getCell(x, i) == '#') break;
            if (player.getX() == x && player.getY() == i) return true;
        }
        //Check right
        for (int i = x + 1; i < maze.getWidth(); i++) {
            if (maze.getCell(i, y) == '#') break;
            if (player.getX() == i && player.getY() == y) return true;
        }

        return false;
    }

    //Checks if the monster is occupying the same cell as the player.
    public boolean checkIfCaught(Player player) {
        return player.getX() == x && player.getY() == y;
    }

    //Determines if a given location is a dead end.
    private boolean isDeadEnd(int x, int y, Maze maze) {
        int walkableCount = 0;
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] d : directions) {
            if (canMoveTo(x + d[0], y + d[1], maze)) {
                walkableCount++;
            }
        }

        return walkableCount <= 1; //0 or 1 exits = dead end
    }

    //Validates whether the monster can move to a given tile.
    private boolean canMoveTo(int newX, int newY, Maze maze) {
        if (!maze.isWalkable(newX, newY)) return false;
        char cell = maze.getCell(newX, newY);
        return cell != 'K' && cell != 'D'; //Don't step on keys or doors
    }
}
