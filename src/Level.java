import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;

public class Level {

    static final HashMap<PVector, Level> levels = new HashMap<>();
    private static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
    public static Level currentLevel;
    static int complexity = 20;
    public final int width;
    public final int height;
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final PApplet sketch;
    private final PImage[] grassSet;
    private final PImage backgroundSky, backgroundClouds, backgroundCloudLayer, treasureSprite;
    public int[][] level;
    PVector levelPosition;
    PVector treasure;

    public Level(PApplet sketch, int width, int height, int[][] level) {
        this(sketch, width, height);
        this.levelPosition = new PVector(0, 0);
        this.level = level;
        levels.put(levelPosition, this);
    }

    public Level(PApplet sketch, int width, int height, PVector pos) {
        this(sketch, width, height);
        this.levelPosition = pos;
        generateLevel(); // generate new level
        levels.put(levelPosition, this);
    }

    private Level(PApplet sketch, int width, int height) {
        this.sketch = sketch; // assign sketch
        this.width = width / Main.blockSize; // assign width
        this.height = height / Main.blockSize; // assign height

        PImage tileset = sketch.loadImage("tileset.png");
        this.grassSet = new PImage[]{ // neighbour ids are ordered, top right bottom left
                Main.getImageFromTileset(tileset, 0, 0, 18, 0), // 0000, no neighbours so needs borders everywhere
                Main.getImageFromTileset(tileset, 3, 0, 18, 0), // 0001, neighbour to the left so needs border everywhere except left
                Main.getImageFromTileset(tileset, 0, 1, 18, 0), // 0010, neighbour to the bottom so needs border everywhere except bottom
                Main.getImageFromTileset(tileset, 3, 1, 18, 0), // 0011, neighbour to the left and bottom so needs border in top and right
                Main.getImageFromTileset(tileset, 1, 0, 18, 0), // 0100, neighbour to the left so needs border everywhere except left,
                Main.getImageFromTileset(tileset, 2, 0, 18, 0), // 0101, neighbour to the left and right so needs border top and bottom
                Main.getImageFromTileset(tileset, 1, 1, 18, 0), // 0110, neighbour to the right and bottom so needs border top and left
                Main.getImageFromTileset(tileset, 2, 1, 18, 0), // 0111, neighbour everywhere except top so needs border top
                Main.getImageFromTileset(tileset, 0, 7, 18, 0), // 1000, neighbour to the top so needs border everywhere except top
                Main.getImageFromTileset(tileset, 3, 7, 18, 0), // 1001, neighbour to the top and left so needs border bottom and right
                Main.getImageFromTileset(tileset, 0, 6, 18, 0), // 1010, neighbour to the top and bottom so needs border left and right
                Main.getImageFromTileset(tileset, 3, 6, 18, 0), // 1011, neighbour everywhere except right so needs border right
                Main.getImageFromTileset(tileset, 1, 7, 18, 0), // 1100, neighbour to the top and right so needs border bottom and left
                Main.getImageFromTileset(tileset, 2, 7, 18, 0), // 1101, neighbour everywhere except bottom so needs border bottom
                Main.getImageFromTileset(tileset, 1, 6, 18, 0), // 1110, neighbour everywhere except left so needs border left
                Main.getImageFromTileset(tileset, 2, 6, 18, 0), // 1111, no neighbours so needs borders everywhere
        };
        this.treasureSprite = Main.getImageFromTileset(tileset, 11, 0, 18, 0);

        PImage backgroundTileset = sketch.loadImage("background.png");
        this.backgroundSky = Main.getImageFromTileset(backgroundTileset, 0, 0, 24, 0); // get sky from tileset (top half
        this.backgroundCloudLayer = Main.getImageFromTileset(backgroundTileset, 1, 0, 24, 0); // get cloud layer from tileset (middle half
        this.backgroundClouds = Main.getImageFromTileset(backgroundTileset, 2, 0, 24, 0); // get clouds from tileset (bottom half

    }

    public static int move(int dir) {
        PVector pos = currentLevel.levelPosition.copy();

        switch (dir) {
            case UP:
                pos.y--;
                break;
            case DOWN:
                pos.y++;
                break;
            case LEFT:
                pos.x--;
                break;
            case RIGHT:
                pos.x++;
                break;
        }

        if (levels.containsKey(pos)) {
            currentLevel = levels.get(pos);
            return 0;
        } else {
            currentLevel = new Level(currentLevel.sketch, currentLevel.width * Main.blockSize, currentLevel.height * Main.blockSize, pos);
            complexity--;
            return 1;
        }
    }

    public boolean outOfBounds(int x, int y) {
        return x < 0 || x >= level.length || y < 0 || y >= level[0].length; // check if x or y is out of bounds
    }

    public boolean wallAt(PVector vec) {
        int x = (int) (vec.x / Main.blockSize);
        int y = (int) (vec.y / Main.blockSize);

        if (outOfBounds(x, y)) {
            return false;
        }

        return level[x][y] == 1;
    }

    public boolean wallAtPoint(float x, float y) {
        return outOfBounds((int) x, (int) y) || level[(int) x][(int) y] == 1;
    }

    public void draw() {
        sketch.imageMode(sketch.CORNER);
        sketch.stroke(0);
        sketch.strokeWeight(0.5f);
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                drawImage(i, j);
            }
        }


        if (treasure != null) {
            sketch.image(treasureSprite, treasure.x, treasure.y, Main.blockSize, Main.blockSize);
        }

        for (Enemy enemy : enemies) {
            enemy.draw();
        }
    }

    public void drawImage(int x, int y) {
        if (level[x][y] == 0) {
            sketch.image((y < level[0].length / 2) ? backgroundSky : (y > level[0].length / 2) ? backgroundClouds : backgroundCloudLayer, x * Main.blockSize, y * Main.blockSize, Main.blockSize, Main.blockSize);
        } else {

            int neighbourId = 0;

            neighbourId += ((outOfBounds(x, y - 1) || level[x][y - 1] == 1) ? 1 : 0) << 3;
            neighbourId += ((outOfBounds(x + 1, y) || level[x + 1][y] == 1) ? 1 : 0) << 2;
            neighbourId += ((outOfBounds(x, y + 1) || level[x][y + 1] == 1) ? 1 : 0) << 1;
            neighbourId += (outOfBounds(x - 1, y) || level[x - 1][y] == 1) ? 1 : 0;

            sketch.image(grassSet[neighbourId], x * Main.blockSize, y * Main.blockSize, Main.blockSize, Main.blockSize);
        }
    }


    public void generateLevel() {
        level = new int[width][height];

        ArrayList<int[]> surfaces = new ArrayList<>();
        int enemies = Math.max(15 - complexity - (int) (Math.random() * 2), (int) (Math.random() * 4) + 1);
        do {
            surfaces.clear();
            fillRandomLevel();
            cellularAutomata(Math.max(complexity, 1));
            findSurfaces(-1, 0, 0, surfaces);
        } while (!hasOnePatch() || surfaces.size() <= enemies);

        for (int i = 0; i < enemies; i++) {
            int[] randomSurface = surfaces.remove((int) (Math.random() * surfaces.size()));
            this.enemies.add(new Enemy(sketch, (randomSurface[0] + 0.5f) * Main.blockSize, (randomSurface[1] - 0.5f) * Main.blockSize, (randomSurface[2] + 0.5f + 1 / 6f) * Main.blockSize));
        }

        if (Player.roomsExplored >= 20 && Player.killCount >= 30) {
            int[] surface = surfaces.get(0);
            this.treasure = new PVector(Main.blockSize * (surface[0] + surface[1]) / 2f, Main.blockSize * surface[2]);
        }
    }


    public boolean hitsTreasure(Player p) {
        if (treasure == null) return false;

        return p.position.x + Main.blockSize / 2f > treasure.x && p.position.x - Main.blockSize / 2f < treasure.x + Main.blockSize && p.position.y + Main.blockSize / 2f > treasure.y && p.position.y - Main.blockSize / 2f < treasure.y + Main.blockSize;
    }

    public void findSurfaces(int start, int x, int y, ArrayList<int[]> surfaces) {
        if (y >= height - 1) return;
        if (x >= width) {
            findSurfaces(-1, 0, y + 1, surfaces);
            return;
        }

        if (level[x][y] == 0 && level[x][y + 1] != 0) {
            if (start == -1) start = x;
        } else {
            if (start != -1 && x - start >= 2) surfaces.add(new int[]{start, x - 1, y});

            start = -1;
        }

        findSurfaces(start, x + 1, y, surfaces);
    }

    public boolean hasOnePatch() {
        floodFill();

        for (int[] rows : level) {
            for (int j = 0; j < level[0].length; j++) {
                if (rows[j] == 0) {
                    return false;
                }

                if (rows[j] == 2) {
                    rows[j] = 0;
                }
            }
        }

        return true;
    }

    public void floodFill() {
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                if (level[i][j] == 0) {
                    floodFill(i, j);
                    return;
                }
            }
        }
    }

    public void floodFill(int x, int y) {
        if (outOfBounds(x, y) || level[x][y] != 0) {
            return;
        }

        level[x][y] = 2;

        floodFill(x + 1, y);
        floodFill(x - 1, y);
        floodFill(x, y + 1);
        floodFill(x, y - 1);
    }

    private void fillRandomLevel() {
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                level[i][j] = (Math.random() < 0.45) ? 1 : 0;
            }
        }
    }

    public int[] getEdge(int dir) {
        int[] edge = new int[(dir == UP || dir == DOWN) ? width : height];

        for (int i = 0; i < edge.length; i++) {
            edge[i] = level[(dir == LEFT) ? 0 : (dir == RIGHT) ? width - 1 : i][(dir == UP) ? 0 : (dir == DOWN) ? height - 1 : i];
        }

        return edge;
    }

    public void cellularAutomata(int iterations) {
        if (iterations == 0) {
            for (int x = -1; x <= 1; x += 2) {
                if (levels.containsKey(levelPosition.copy().add(x, 0))) {
                    int[] edge = levels.get(levelPosition.copy().add(x, 0)).getEdge((x == -1) ? RIGHT : LEFT);
                    System.arraycopy(edge, 0, level[(x == -1) ? 0 : width - 1], 0, edge.length);
                } else {
                    boolean hasHole = false;
                    for (int w : level[(x == -1) ? 0 : width - 1]) {
                        if (w == 0) {
                            hasHole = true;
                            break;
                        }
                    }

                    if (hasHole) continue;


                    int startingPoint, endingPoint;

                    do {
                        startingPoint = (int) (Math.random() * (height - 2)) + 2;
                        endingPoint = (int) (Math.random() * (height - 2)) + 2;
                    } while ((Math.abs(startingPoint - endingPoint) < 2 || Math.abs(startingPoint - endingPoint) > 3));

                    if (startingPoint > endingPoint) {
                        int temp = startingPoint;
                        startingPoint = endingPoint;
                        endingPoint = temp;
                    }

                    for (int i = 0; i < level[0].length; i++) {
                        level[(x == -1) ? 0 : width - 1][i] = (i >= startingPoint && i <= endingPoint) ? 0 : 1;
                        level[(x == -1) ? 1 : width - 2][i] = (i >= startingPoint && i <= endingPoint) ? 0 : 1;
                    }
                }
            }

            for (int y = -1; y <= 1; y += 2) {
                if (levels.containsKey(levelPosition.copy().add(0, y))) {
                    int[] edge = levels.get(levelPosition.copy().add(0, y)).getEdge((y == -1) ? DOWN : UP);

                    for (int i = 0; i < edge.length; i++) {
                        level[i][(y == -1) ? 0 : height - 1] = edge[i];
                    }
                } else {
                    boolean hasHole = false;
                    for (int[] w : level) {
                        if (w[(y == -1) ? 0 : height - 1] == 0) {
                            hasHole = true;
                            break;
                        }
                    }

                    if (hasHole) continue;

                    int startingPoint, endingPoint;

                    do {
                        startingPoint = (int) (Math.random() * (width - 2)) + 2;
                        endingPoint = (int) (Math.random() * (width - 2)) + 2;
                    } while ((Math.abs(startingPoint - endingPoint) < 2 || Math.abs(startingPoint - endingPoint) > 3));

                    if (startingPoint > endingPoint) {
                        int temp = startingPoint;
                        startingPoint = endingPoint;
                        endingPoint = temp;
                    }

                    for (int i = 0; i < level[0].length; i++) {
                        level[i][(y == -1) ? 0 : height - 1] = (i >= startingPoint && i <= endingPoint) ? 0 : 1;
                        level[i][(y == -1) ? 1 : height - 2] = (i >= startingPoint && i <= endingPoint) ? 0 : 1;
                    }
                }
            }

            return;
        }

        int[][] newLevel = new int[level.length][level[0].length];


        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                newLevel[i][j] = countNeighbours(i, j) >= 5 ? 1 : 0;
            }
        }


        level = newLevel;


        cellularAutomata(iterations - 1);
    }

    public int countNeighbours(int x, int y) {
        int count = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                count += (outOfBounds(i, j) || level[i][j] == 1) ? 1 : 0;
            }
        }

        return count;
    }

    public Enemy enemyHitsPlayer(Player player) {
        for (Enemy enemy : enemies) {
            if (enemy.hitsPlayer(player)) {
                return enemy;
            }
        }
        return null;
    }

    public void removeEnemy(Enemy e) {
        enemies.remove(e);
    }
}
