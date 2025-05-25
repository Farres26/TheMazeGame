package Game;

import java.util.Random;

public final class Maze {
    private static final int WIDTH = 25;  //Width of the maze
    private static final int HEIGHT = 25; //Height of the maze

    private static final char WALL = '#';
    private static final char EMPTY = ' ';
    private static final char PLAYER = 'P';
    private static final char MONSTER = 'M';
    private static final char KEY = 'K';
    private static final char TRAP = 'T';
    private static final char DOOR = 'D';

    private final char[][] grid = new char[HEIGHT][WIDTH];  //2D grid representing the maze
    private final Random random = new Random();             //RNG
    private Player player;
    private Monster monster;
    private int totalKeys = 0;                              //Total number of keys in the maze

    //Predefined set of 6x6 chunks which PrintMaze will randomly
    //select one from & arrange into a 4x4 grid
    private final char[][][] chunks = {
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ','#',' '},
            {'#',' ','#',' ','#',' '},
            {' ',' ','#','#','#',' '},
            {'#',' ','#',' ','#',' '},
            {'#',' ',' ',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#',' ','#',' '},
            {' ',' ','#',' ','#',' '},
            {'#',' ','#','#','#',' '},
            {'#',' ',' ',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#','#','#','#'},
            {' ',' ','#',' ',' ',' '},
            {'#',' ','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#','#','#','#','#',' '},
            {' ',' ',' ',' ',' ',' '},
            {'#',' ','#','#','#',' '},
            {'#',' ','#',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#','#','#','#'},
            {' ',' ',' ','#',' ',' '},
            {'#',' ','#','#',' ','#'},
            {'#',' ',' ',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#','#','#','#'},
            {' ',' ','#',' ',' ',' '},
            {'#',' ','#','#','#',' '},
            {'#',' ',' ',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#',' ','#',' '},
            {' ',' ','#','#','#',' '},
            {'#','#','#',' ','#',' '},
            {'#',' ',' ',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#',' ','#',' '},
            {' ',' ','#',' ','#',' '},
            {'#',' ','#',' ','#',' '},
            {'#',' ','#',' ','#',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#',' ','#',' '},
            {' ',' ','#','#','#',' '},
            {'#',' ',' ',' ','#',' '},
            {'#',' ','#',' ','#',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#','#','#',' ','#','#'},
            {' ',' ',' ',' ',' ',' '},
            {'#',' ','#','#',' ','#'},
            {'#',' ','#',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#','#',' ','#'},
            {' ',' ',' ',' ',' ',' '},
            {'#',' ','#','#',' ','#'},
            {'#',' ','#',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#','#','#',' '},
            {' ',' ',' ',' ','#',' '},
            {'#',' ','#','#','#',' '},
            {'#',' ','#',' ',' ',' '}
        },
        {
            {'#','#','#',' ','#','#'},
            {'#',' ',' ',' ',' ',' '},
            {'#',' ','#',' ','#',' '},
            {' ',' ','#',' ','#',' '},
            {'#',' ','#',' ','#',' '},
            {'#',' ',' ',' ',' ',' '}
        }
    };

    //Constructor: builds the maze and places all game entities
    public Maze() {
        generateMaze();
        placeEntities();
        printMaze();
    }

    //Creates the layout of the maze by filling it with random chunks
    private void generateMaze() {
        //Initialize the entire grid with empty spaces
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                grid[y][x] = EMPTY;
            }
        }

        //Fill the maze by placing 6x6 chunks into a 4x4 grid
        for (int chunkY = 0; chunkY < 4; chunkY++) {
            for (int chunkX = 0; chunkX < 4; chunkX++) {
                int chunkIndex = random.nextInt(chunks.length);
                char[][] selectedChunk = chunks[chunkIndex];

                //Copy the selected chunk into the correct spot in the maze grid
                for (int y = 0; y < selectedChunk.length; y++) {
                    for (int x = 0; x < selectedChunk[y].length; x++) {
                        grid[chunkY * 6 + y][chunkX * 6 + x] = selectedChunk[y][x];
                    }
                }
            }
        }

        //Create walls along the outer borders of the maze
        for (int y = 0; y < HEIGHT; y++) {
            grid[y][0] = WALL; //Left side
        }
        for (int x = 0; x < WIDTH; x++) {
            grid[0][x] = WALL; //Top side
        }
        for (int y = 0; y < HEIGHT; y++) {
            grid[y][WIDTH - 1] = WALL; //Right side
        }
        for (int x = 0; x < WIDTH; x++) {
            grid[HEIGHT - 1][x] = WALL; //Bottom side
        }
    }

    //Places the player, monster, keys, door, and trap into the maze
    private void placeEntities() {
        //Place player and monster at starting positions
        player = new Player(1, 1);
        monster = new Monster(23, 23);
        grid[player.getY()][player.getX()] = PLAYER;
        grid[monster.getY()][monster.getX()] = MONSTER;

        //Place six keys randomly in the maze
        for (int i = 0; i < 4; i++) {
            placeEntity(KEY);
            totalKeys++;
        }

        //Place one door randomly
        placeEntity(DOOR);

        //Place seven traps randomly
        for (int i = 0; i < 12; i++) {
            placeEntity(TRAP);
}

    }

    //Finds a valid empty spot and places the specified entity there
    private void placeEntity(char entity) {
        int x, y;
        do {
            //Generate random position inside the maze
            x = random.nextInt(WIDTH - 2) + 1;
            y = random.nextInt(HEIGHT - 2) + 1;
        } while (!isValidSpawn(entity, x, y));
        grid[y][x] = entity;
    }

    //Checks if an entity can be placed at the given location
    private boolean isValidSpawn(char entity, int x, int y) {
        //Spot must be empty
        if (grid[y][x] != EMPTY) return false;

        //Check surrounding (orthogonal) cells
        int walls = 0;
        int empty = 0;
        int traps = 0;
        int[][] deltas = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : deltas) {
            int nx = x + d[0], ny = y + d[1];
            char c = (nx < 0||nx>=WIDTH||ny<0||ny>=HEIGHT) ? WALL : grid[ny][nx];
            if (c == WALL)       walls++;
            else if (c == EMPTY) empty++;
            else if (c == TRAP)  traps++;
        }

        //Placement rules
        switch (entity) {
            case KEY:
            case DOOR:
                //Must have 3 walls orthogonally adjacent
                return walls >= 3;

            case TRAP:
                //Must have at least 3 empty neighbors and no adjacent traps
                return empty >= 3 && traps == 0;

            default:
                return false;
        }
    }

    public int getTotalKeys() {
        return totalKeys;
    }

    public char getCell(int x, int y) {
        return grid[y][x];
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public Player getPlayer() {
        return player;
    }

    public Monster getMonster() {
        return monster;
    }

    //Updates an entity's position in the maze grid
    public void updatePosition(int oldX, int oldY, int newX, int newY, char entity) {
        if (grid[oldY][oldX] == entity) {
            grid[oldY][oldX] = EMPTY; //Clear old position
        }
        grid[newY][newX] = entity;     //Set new position
    }

    //Directly set a specific cell in the grid
    public void setCell(int x, int y, char entity) {
        grid[y][x] = entity;
    }

    //Checks if a specific cell can be walked through (not a wall)
    public boolean isWalkable(int x, int y) {
        return (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && grid[y][x] != WALL);
    }

    //Prints the current maze layout
    public void printMaze() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                System.out.print(grid[y][x]);
            }
            System.out.println();
        }
    }
}
