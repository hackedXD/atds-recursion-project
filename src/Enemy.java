import processing.core.PApplet;
import processing.core.PImage;

public class Enemy {
    private static final PImage[] enemySprites = new PImage[2];
    PApplet sketch;
    float x, y, w, h;
    float start, end;
    private int dir = 1;


    public Enemy(PApplet sketch, float x1, float x2, float y) {
        this.sketch = sketch;

        this.start = x1;
        this.end = x2;

        this.x = x1;
        this.y = y;
        this.w = Main.blockSize * 2 / 3f;
        this.h = Main.blockSize * 2 / 3f;

        if (enemySprites[0] == null) {
            PImage tileset = sketch.loadImage("characters.png");
            enemySprites[0] = Main.getImageFromTileset(tileset, 0, 2, 24, 0);
            enemySprites[1] = Main.getImageFromTileset(tileset, 1, 2, 24, 0);
        }
    }

    public boolean hitsPlayer(Player p) {
        return p.position.x + Main.blockSize / 2f > x - w / 2f && p.position.x - Main.blockSize / 2f < x + w / 2f && p.position.y + Main.blockSize / 2f > y - h / 2f && p.position.y - Main.blockSize / 2f < y + h / 2f;
    }

    public void draw() {
        if (sketch.frameCount % 2 == 0) {
            int mvmt = dir * 5;

            if (dir == 1 && x + mvmt > end + Main.blockSize) {
                dir = -1;
                mvmt = 0;
            } else if (dir == -1 && x + mvmt < start) {
                dir = 1;
                mvmt = 0;
            }


            x += mvmt;
        }

        sketch.pushMatrix();
        sketch.translate(x, y);
        sketch.scale(-dir, 1);
        sketch.imageMode(sketch.CENTER);
        sketch.image(enemySprites[0], -1, -4, w * 1.5f, h * 1.5f);
        sketch.popMatrix();
    }
}
