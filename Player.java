package Game;

public final class Player {
    private int x, y;
    private int keysCollected;
    private static final char PLAYER_SYMBOL = 'P';

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.keysCollected = 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getKeysCollected() { return keysCollected; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void collectKey() {
        keysCollected++;
    }
}
