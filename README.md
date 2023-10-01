# ATDS Recursion Project 1

### [Video Demo](https://www.loom.com/share/c3f49db1f25a4a05a3352a902039fc3d?sid=172e141a-f94d-4481-be1d-b1babe053005)
### [Gameplay Demo (i swear its beatable i just havent done it yet)](https://www.loom.com/share/b6626183fb3146929c19d250133f17c1?sid=c40645a9-67f8-4582-bce5-ac59578f4ff0)
* shows progression system

### Game Description

**Game Name:** Fractivia\
**Purpose of Game:** To find the treasure in the recursively generated world. To find treasure, you need to explore 20
rooms and kill 30 enemies.\
**Type of Game:** Platformer\
**Rationale For Creating Game:** I wanted to make a platformer, as for my game jam project last year, I made a half
completed one. My goal was to recreate the idea behind that game using Java and recursion.\
**Evidence of Ingenuity:** Used recursion to generate the world, Used recursion to generate enemies, Used recursion to
work on ray casting. Has a player controller (self made) with grappling hook (also self made). To cut it short, the
entire game was made by myself, and this idea is unique so you won't find it anywhere else

### Incorporation of Recursion
1. Used to create a world
   * Algorithm works as follows:
     * Fills a random grid
     * Then, loops through the grid, checking the amount of neighbours each cell has
     * If it has more than 4 neighbours, it becomes a wall
     * If it has less than 4 neighbours, it becomes a floor
     * This algorithm is then recursively called until the iteration (controls complexity) parameter is 0
     * Then, it checks the edges of the world, and if there isn't an exit, it creates one
```java
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
    
```
2. Used to check the world
   * The world uses recursion to build it self. The main part of the algorithm is not specifically unique, as it just
     redoes cellular automata, a commonly known algorithm recursively. However, the part that ensures the level is playable, works as follows.
     * Starts by finding any random hole
     * Then, runs a recursive algorithm that checks if the hole is connected to any holes, and sets it to a value of 2
     * Then after the recursive algorithm, any part of that map, linked to the random hole is now a 2
     * Then it checks if any holes remain, if they do that means a part of the map is accesible
```java
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
```
3. Used to generate enemies
   * Completely custom algorithm to find places and values for where the enemies can go. It works as follows.
     * Starts by checking a starting point (usually top left corner)
     * Then moves leftwards, checking if that spot doesn't have a wall and the wall before it does
     * If it finds a streak of these, it adds them to a provided arraylist
     * Once it reaches the end, it goes back to the start and a row below
     * Then, it just generates enemies randomly based on these surfaces
```java
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
```
4. Used for raycasting for grappling hooke
    * Uses raycasting and the grid based system to make sure it only checks the points where the angle collides with an x or y column line
    * Does so using a little bit of vector math
```java
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
```

### How to play the Game
* There is a tutorial button in game explaining this
* Left Arrow to move Left
* Right Arrow to move Right
* Up Arrow to Jump
* Click to grapple to wall
* Jump on enemy to kill enemy
* Kill 30 enemies and explore 20 rooms to make treasure spawn
* Reach treasure to win

### Data Structures Used
* ArrayLists for storing enemies, surfaces, enemies and more
* 2D Arrays for storing the level and doing math with it
* Flattened 2D Arrays when fixing cursor
* HashMaps for storing level positions
* Vectors for storing positions and doing math with them

### Have you been able to meet your initial game plan (success criteria)?
* Yes. I made a full fledged, long lasting, engaging game with a clear start and endpoint. This was my specific goal

### Assets Used
* Kenney's Pixel Platformer Pack - https://www.kenney.nl/assets/pixel-platformer
* Kenney's Pixel UI Pack - https://www.kenney.nl/assets/pixel-ui-pack
* PressStart2P Font - https://fonts.google.com/specimen/Press+Start+2P
* Mario Sunshine Soundtrack

