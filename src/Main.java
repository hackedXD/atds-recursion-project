import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.sound.Sound;
import processing.sound.SoundFile;

public class Main extends PApplet {
    public static final int blockSize = 40;
    public static SoundFile introSound, gameSound, deadSound, jumpSound, winSound, currentSound;
    final int width = 1280, height = 720;
    Player player;
    int prevTime;
    private GameState state = GameState.LOADING;
    private PImage pixelUI, panelImage, cursorNormal, cursorHover;
    private Button exitButton, retryButton, startButton, tutorialButton, backFromTutorialButton, muteButton;

    public static void main(String[] passedArgs) {
        PApplet.main("Main");
    }

    public static PImage getImageFromTileset(PImage tileset, int x, int y, int tileSize, int margin) {
        return tileset.get(x * tileSize + x * margin, y * tileSize + y * margin, tileSize, tileSize); // get image from tileset based on index x and y
    }

    public void settings() {
        size(width, height);
        noSmooth();
        pixelDensity(displayDensity());
    }


    public void reframeCursor(PImage cursor, int cursorWidth) {
        cursor.loadPixels();

        int[] pixels = new int[cursorWidth * cursorWidth];

        for (int i = cursorWidth - 1; i >= 0; i--) {
            for (int j = cursorWidth - 1; j >= 0; j--) {
                if (j >= (cursorWidth - 16) && i >= (cursorWidth - 16)) {
                    pixels[i + j * cursorWidth] = cursor.pixels[i - (cursorWidth - 16) + (j - (cursorWidth - 16)) * 16];
                }
            }
        }

        cursor.resize(cursorWidth, cursorWidth);
        cursor.pixels = pixels;
    }

    public PImage getImageFromGrid(PImage[][] grid, int width, int height) {
        int tileSize = grid[0][0].width;
        PImage image = createImage(width * tileSize, height * tileSize, ARGB);
        image.loadPixels();

        for (int i = 0; i < width * tileSize; i++) {
            for (int j = 0; j < height * tileSize; j++) {
                image.pixels[i + j * width * tileSize] = grid[(j < tileSize) ? 0 : (j >= (height - 1) * tileSize) ? 2 : 1][(i < tileSize) ? 0 : (i >= (width - 1) * tileSize) ? 2 : 1].pixels[i % tileSize + j % tileSize * tileSize];
            }
        }

        image.updatePixels();

        return image;
    }

    public void setup() {
        windowTitle("Recursive Platformer");
        textFont(createFont("PressStart2P.ttf", 14));

        new Thread(() -> {
            Sound s = new Sound(this);
            s.sampleRate(35000);
            introSound = new SoundFile(this, "intro.mp3");
            gameSound = new SoundFile(this, "game.mp3");
            deadSound = new SoundFile(this, "dead.mp3");
            jumpSound = new SoundFile(this, "jump.wav");
            winSound = new SoundFile(this, "win.mp3");

            currentSound = introSound;
            currentSound.loop();

            pixelUI = loadImage("ui_pixel.png");
            cursorNormal = getUIElement(2, 32);
            cursorHover = getUIElement(0, 25);

            reframeCursor(cursorNormal, 32);
            reframeCursor(cursorHover, 24);

            cursor(cursorNormal);

            PImage[][] panels = {
                    {
                            getUIElement(25, 13), // top left
                            getUIElement(26, 13), // top middle
                            getUIElement(27, 13), // top right
                    },
                    {
                            getUIElement(13, 14), // middle left
                            getUIElement(14, 14), // middle
                            getUIElement(15, 14), // middle right
                    },
                    {
                            getUIElement(13, 15), // bottom left
                            getUIElement(14, 15), // bottom middle
                            getUIElement(15, 15), // bottom right
                    },
            };

            PImage[][] buttonPanel = {
                    {
                            getUIElement(18 - 12, 2),
                            getUIElement(19 - 12, 2),
                            getUIElement(20 - 12, 2),
                    },
                    {
                            getUIElement(18 - 12, 3),
                            getUIElement(19 - 12, 3),
                            getUIElement(20 - 12, 3),
                    },
                    {
                            getUIElement(18 - 12, 4),
                            getUIElement(19 - 12, 4),
                            getUIElement(20 - 12, 4),
                    },
            };

            this.panelImage = getImageFromGrid(panels, 8, 6);
            this.exitButton = new Button(getUIElement(21, 6), width / 2f + 4 * 48 - 6, height / 2f - (3 * 48) + 6, 24, 24);
            this.startButton = new Button(getImageFromGrid(buttonPanel, 7, 3), width / 2f, height / 2f - 32, 48 * 7, 48 * 2);
            this.tutorialButton = new Button(getImageFromGrid(buttonPanel, 7, 3), width / 2f, height / 2f + 72, 48 * 7, 48 * 2);
            this.backFromTutorialButton = new Button(getImageFromGrid(buttonPanel, 4, 3), 60, 24, 24 * 4, 36);
            this.retryButton = new Button(getImageFromGrid(buttonPanel, 4, 3), width / 2f, height / 2f + 30, 24 * 4, 36);
            this.muteButton = new Button(getImageFromGrid(buttonPanel, 8, 3), width - 52, 22, 12 * 8, 36);
            prevTime = millis();

            state = GameState.MENU;
        }).start();
    }

    public PImage getUIElement(int x, int y) {
        return getImageFromTileset(pixelUI, x, y, 16, 2);
    }

    public void startGame() {
        state = GameState.GAME;
        Level.levels.clear();
        Level.complexity = 20;
        int[][] level = {
                {1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        };

        Level.currentLevel = new Level(this, width, height, level);
        player = new Player(this, 3 * Main.blockSize, 3 * Main.blockSize);
    }

    public void draw() {
        // evangelic catalyst

        float deltaTime = (millis() - prevTime) / 1000f;
        prevTime = millis();

        background(0);

        switch (state) {
            case LOADING:
                textAlign(CENTER);
                background(30, 30, 46);
                text("Loading Assets", width / 2f, height / 2f);
                break;
            case MENU:
                background(30, 30, 46);

                imageMode(CENTER);
                image(panelImage, width / 2f, height / 2f, 48 * 8, 48 * 6);

                cursor(cursorNormal);

                if (exitButton.draw()) {
                    if (mousePressed) {
                        exit();
                    } else {
                        cursor(cursorHover);
                    }
                }

                if (startButton.draw()) {
                    if (mousePressed) {
                        startGame();
                    } else {
                        cursor(cursorHover);
                    }
                }

                if (tutorialButton.draw()) {
                    if (mousePressed) {
                        state = GameState.TUTORIAL;
                    } else {
                        cursor(cursorHover);
                    }
                }

                textAlign(CENTER);

                fill(255);
                text("Recursive Platformer", width / 2f, height / 2f - 110);

                fill(255, 100, 0);
                text("START", width / 2f, height / 2f - 28);
                text("TUTORIAL", width / 2f, height / 2f + 76);

                fill(255);
                break;
            case GAME:
                if (currentSound != gameSound) {
                    currentSound.stop();
                    currentSound = gameSound;
                    currentSound.loop(32f / 41f);
                }

                Level.currentLevel.draw();

                if (muteButton.draw()) {
                    if (mousePressed) {
                        if (currentSound.isPlaying()) currentSound.pause();
                        else currentSound.play();

                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        cursor(cursorHover);
                    }
                }

                fill(255, 0, 0);
                textAlign(CENTER);
                text(currentSound.isPlaying() ? "mute" : "unmute", width - 52, 28);

                player.draw(deltaTime);

                Enemy e = Level.currentLevel.enemyHitsPlayer(player);
                if (e != null) {
                    if (player.velocity.y > 0) {
                        Level.currentLevel.removeEnemy(e);
                        player.velocity.y = -Player.JUMP_SPEED * deltaTime;
                        Player.killCount++;
                        jumpSound.play();
                    } else {
                        state = GameState.GAME_OVER;
                    }
                }

                if (Level.currentLevel.hitsTreasure(player)) {
                    state = GameState.GAME_WIN;
                }

                textAlign(LEFT);
                fill(0);
                text("Enemies Killed: " + Player.killCount, 10, 20);
                text("Rooms Explored: " + Player.roomsExplored, 10, 40);
                text("Level Pos: (" + (int) Level.currentLevel.levelPosition.x + ", " + (int) -Level.currentLevel.levelPosition.y + ")", 10, 60);
                break;
            case TUTORIAL:
                background(30, 30, 46);

                textAlign(CENTER);
                fill(255);
                text("Use ▶️ to move right.", width / 2f, height / 2f - 150);
                text("Use ◀️ to move left.", width / 2f, height / 2f - 120);
                text("Use UP to jump.", width / 2f, height / 2f - 90);
                text("Click your mouse to grapple to the nearest wall", width / 2f, height / 2f - 60);
                text("Goal: Find your ancient treasure.", width / 2f, height / 2f);
                text("You must kill atleast 30 enemies and explore 20 rooms for the treasure to reveal itself.", width / 2f, height / 2f + 30);

                cursor(cursorNormal);
                if (backFromTutorialButton.draw()) {
                    if (mousePressed) {
                        state = GameState.MENU;
                    } else {
                        cursor(cursorHover);
                    }
                }

                fill(255, 100, 0);
                text("◀️", 60, 32);


                break;
            case GAME_OVER:
                if (currentSound != deadSound) {
                    currentSound.stop();
                    currentSound = deadSound;
                    currentSound.play(32f / 41f);
                }

                background(30, 30, 46);
                fill(255);
                textAlign(CENTER);
                text("GAME OVER", width / 2f, height / 2f);
                if (currentSound.percent() > 0 || currentSound.isPlaying()) {
                    text("You may play again in " + (int) (currentSound.duration() * (1 - currentSound.percent() / 100) + 1) + " seconds.", width / 2f, height / 2f + 30);
                } else {
                    cursor(cursorNormal);
                    if (retryButton.draw()) {
                        if (mousePressed) {
                            startGame();
                        } else {
                            cursor(cursorHover);
                        }
                    }

                    fill(255, 100, 0);
                    text("▶️", width / 2f, height / 2f + 36);

                }

                break;
            case GAME_WIN:
                if (currentSound != winSound) {
                    currentSound.stop();
                    currentSound = winSound;
                    currentSound.play(32f / 41f);
                }

                background(30, 30, 46);
                fill(255);
                textAlign(CENTER);
                text("You Won!", width / 2f, height / 2f);
                if (currentSound.percent() > 0 || currentSound.isPlaying()) {
                    text("You may play again in " + (int) (currentSound.duration() * (1 - currentSound.percent() / 100) + 1) + " seconds.", width / 2f, height / 2f + 30);
                } else {
                    cursor(cursorNormal);
                    if (retryButton.draw()) {
                        if (mousePressed) {
                            startGame();
                        } else {
                            cursor(cursorHover);
                        }
                    }

                    fill(255, 100, 0);
                    text("▶️", width / 2f, height / 2f + 36);
                }


                break;
        }
    }

    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == 27) {
            return;
        }

        if (state == GameState.GAME) player.keyPressed(event);
    }

    public void keyReleased(KeyEvent event) {
        if (state == GameState.GAME) player.keyReleased(event);
    }

    enum GameState {
        LOADING,
        MENU,
        GAME,
        TUTORIAL,
        GAME_OVER,
        GAME_WIN
    }

    class Button {
        private final PImage image;
        private final float x, y, sizeX, sizeY;

        public Button(PImage image, float x, float y, float sizeX, float sizeY) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        private boolean draw() {
            imageMode(CENTER);
            image(image, x, y, sizeX, sizeY);

            return (mouseX >= x - sizeX / 2f && mouseX <= x + sizeX / 2f && mouseY >= y - sizeY / 2f && mouseY <= y + sizeY / 2f);
        }
    }
}