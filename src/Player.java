import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.KeyEvent;

public class Player {
    public static final float FALL_SPEED = 22.5f;
    public static final int JUMP_SPEED = 540;
    final static float HORIZONTAL_ACCEL = 8;
    final static float MAX_HORIZONTAL_SPEED = 4;
    final static float HORIZONTAL_DEACCEL = 6;
    private static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
    public static int killCount = 0, roomsExplored = 0;
    final PVector position, velocity = new PVector(0, 0);
    private final PApplet sketch;
    private final PImage idleSprite, mvmtSprite;
    private final boolean[] keysDown = new boolean[4]; // 0 = up, 1 = down, 2 = left. 3 = right
    private boolean jumpStarted = false;
    private PVector grapplingPoint = null;


    public Player(PApplet sketch, float x, float y) {
        killCount = 0;
        roomsExplored = 0;

        this.sketch = sketch;
        this.position = new PVector(x, y);

        PImage tileset = sketch.loadImage("characters.png");

        this.idleSprite = Main.getImageFromTileset(tileset, 0, 0, 24, 0);
        this.mvmtSprite = Main.getImageFromTileset(tileset, 1, 0, 24, 0);


    }

    public int getSign(int dir) {
        return dir == UP || dir == LEFT ? -1 : 1;
    }

    public PVector getCorner(int vertical, int horizontal) {
        return new PVector(
                position.x + getSign(horizontal) * Main.blockSize / 2f,
                position.y + getSign(vertical) * Main.blockSize / 2f
        );
    }

    public float getIntersectionPointDistance(float dist, PVector dir, PVector map, PVector distanceTravelled, PVector unitStep) {
        if (Level.currentLevel.wallAtPoint(map.x, map.y)) return dist;

        if (distanceTravelled.x < distanceTravelled.y) {
            map.x += dir.x;
            dist = distanceTravelled.x;
            distanceTravelled.x += unitStep.x * Main.blockSize;
        } else {
            map.y += dir.y;
            dist = distanceTravelled.y;
            distanceTravelled.y += unitStep.y * Main.blockSize;
        }

        return getIntersectionPointDistance(dist, dir, map, distanceTravelled, unitStep);
    }

    public PVector getIntersectionPoint(PVector start, PVector end) {
        PVector angle = end.copy().sub(start).normalize();
        PVector map = new PVector(
                (int) (start.x / Main.blockSize),
                (int) (start.y / Main.blockSize)
        );
        PVector unitStep = new PVector(Math.abs(1 / angle.x), Math.abs(1 / angle.y));
        PVector dir = new PVector(
                (angle.x > 0 ? 1 : -1),
                (angle.y > 0 ? 1 : -1)
        );
        PVector distanceTravelled = new PVector(
                ((map.x + (dir.x > 0 ? 1 : 0)) * Main.blockSize - start.x) * unitStep.x * dir.x,
                ((map.y + (dir.y > 0 ? 1 : 0)) * Main.blockSize - start.y) * unitStep.y * dir.y
        );

        float dist = getIntersectionPointDistance(Math.min(distanceTravelled.x, distanceTravelled.y), dir, map, distanceTravelled, unitStep);

        return new PVector(
                start.x + angle.x * dist,
                start.y + angle.y * dist
        );
    }


    public void draw(float deltaTime) {
        boolean onGround = (Level.currentLevel.wallAt(getCorner(DOWN, LEFT).add(0.001f, 0)) || Level.currentLevel.wallAt(getCorner(DOWN, RIGHT).sub(0.001f, 0)));
        if (keysDown[RIGHT] ^ keysDown[LEFT]) {
            velocity.x += getSign(keysDown[RIGHT] ? RIGHT : LEFT) * HORIZONTAL_ACCEL * deltaTime;
            velocity.x = PApplet.constrain(velocity.x, -MAX_HORIZONTAL_SPEED, MAX_HORIZONTAL_SPEED);
        } else {
            velocity.x = PApplet.lerp(velocity.x, 0, HORIZONTAL_DEACCEL * deltaTime);
        }

        if (!jumpStarted && keysDown[UP] && onGround) {
            Main.jumpSound.play();
            velocity.y = -JUMP_SPEED * deltaTime;
            jumpStarted = true;
        }

        if (!onGround && grapplingPoint == null)
            velocity.y += (jumpStarted && !keysDown[UP] ? 2 : 1) * FALL_SPEED * deltaTime;

        velocity.y = PApplet.constrain(velocity.y, -12, 12);

        if (sketch.mousePressed) {
            if (grapplingPoint == null) {
                grapplingPoint = getIntersectionPoint(position, new PVector(sketch.mouseX, sketch.mouseY));
            }

            sketch.fill(255, 0, 0);
            sketch.ellipse(grapplingPoint.x, grapplingPoint.y, 5, 5);

            sketch.fill(0);
            sketch.strokeWeight(2);
            sketch.line(position.x, position.y, grapplingPoint.x, grapplingPoint.y);
            PVector angle = grapplingPoint.copy().sub(position).normalize();

            angle.x *= 40 * deltaTime;
            angle.y *= 17 * deltaTime;

            velocity.add(angle);
        } else {
            grapplingPoint = null;
        }

        if (position.x <= 0) {
            position.x = sketch.width;
            roomsExplored += Level.move(LEFT);
            grapplingPoint = null;
        } else if (position.x >= sketch.width) {
            position.x = 0;
            roomsExplored += Level.move(RIGHT);
            grapplingPoint = null;
        }

        if (position.y >= sketch.height) {
            position.y = 0;
            roomsExplored += Level.move(DOWN);
            grapplingPoint = null;
        } else if (position.y <= 0) {
            position.y = sketch.height;
            roomsExplored += Level.move(UP);
            grapplingPoint = null;
        }

        if (velocity.x != 0 && Level.currentLevel.wallAt(getCorner(UP, velocity.x > 0 ? RIGHT : LEFT).add(velocity.x, 0)) || Level.currentLevel.wallAt(getCorner(DOWN, velocity.x > 0 ? RIGHT : LEFT).add(velocity.x, -0.01f))) {
            position.x = (int) ((position.x + velocity.x) / Main.blockSize) * Main.blockSize + Main.blockSize / 2f;
            velocity.x = 0;
        }

        if (velocity.y != 0 && Level.currentLevel.wallAt(getCorner(velocity.y > 0 ? DOWN : UP, LEFT).add(0, velocity.y)) || Level.currentLevel.wallAt(getCorner(velocity.y > 0 ? DOWN : UP, RIGHT).add(-0.01f, velocity.y))) {
            position.y = (int) ((position.y + velocity.y) / Main.blockSize) * Main.blockSize + Main.blockSize / 2f;
            velocity.y = 0;
            jumpStarted = false;
        }

        position.add(velocity);

        sketch.imageMode(sketch.CENTER);

        PImage sprite;

        if (onGround && (keysDown[LEFT] ^ keysDown[RIGHT])) {
            sprite = (sketch.frameCount / 10 % 2 == 0 ? idleSprite : mvmtSprite);
        } else {
            sprite = idleSprite;
        }

        if (!onGround) {
            sprite = mvmtSprite;
        }

        sketch.pushMatrix();
        sketch.scale((velocity.x >= 0) ? -1 : 1, 1);
        sketch.image(sprite, ((velocity.x >= 0) ? -1 : 1) * position.x, position.y, Main.blockSize, Main.blockSize);
        sketch.popMatrix();
    }

    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case 38:
            case 87:
            case 32:
                keysDown[UP] = true;
                break;
            case 40:
            case 83:
                keysDown[DOWN] = true;
                break;
            case 37:
            case 65:
                keysDown[LEFT] = true;
                break;
            case 39:
            case 68:
                keysDown[RIGHT] = true;
                break;
        }
    }

    public void keyReleased(KeyEvent event) {
        switch (event.getKeyCode()) {
            case 38:
            case 87:
            case 32:
                keysDown[UP] = false;
                break;
            case 40:
            case 83:
                keysDown[DOWN] = false;
                break;
            case 37:
            case 65:
                keysDown[LEFT] = false;
                break;
            case 39:
            case 68:
                keysDown[RIGHT] = false;
                break;
        }
    }
}
