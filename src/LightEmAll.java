import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.HashMap;
import java.util.Random;

// represents the game LightEmAll
class LightEmAll extends World {
  ArrayList<ArrayList<GamePiece>> board;
  ArrayList<GamePiece> nodes;
  int width;
  int height;
  int powerRow;
  int powerCol;
  int radius;

  LightEmAll(int width, int height) {
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.radius = 0;
    this.nodes = new ArrayList<GamePiece>();
    this.board = new ArrayList<ArrayList<GamePiece>>();

    this.makeRandomBoard(new Random());
  }

  // generates a random board with Kruskal’s Algorithm
  void makeRandomBoard(Random rand) {

    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();

    for (int col = 0; col < this.width; col++) {
      ArrayList<GamePiece> column = new ArrayList<GamePiece>();
      for (int row = 0; row < this.height; row++) {
        GamePiece gp = new GamePiece(row, col);
        column.add(gp);
        this.nodes.add(gp);
      }
      this.board.add(column);
    }

    ArrayList<Edge> edges = new ArrayList<>();
    for (GamePiece gp : this.nodes) {
      int row = gp.row;
      int col = gp.col;

      if (row < this.height - 1) {
        GamePiece neighbor = this.board.get(col).get(row + 1);
        edges.add(new Edge(gp, neighbor, rand.nextInt(100)));
      }
      if (col < this.width - 1) {
        GamePiece neighbor = this.board.get(col + 1).get(row);
        edges.add(new Edge(gp, neighbor, rand.nextInt(100)));
      }
    }

    UnionFind uf = new UnionFind();
    for (GamePiece gp : this.nodes) {
      uf.makeSet(gp);
    }

    Collections.sort(edges, (e1, e2) -> e1.weight - e2.weight);

    for (Edge edge : edges) {
      GamePiece a = edge.from;
      GamePiece b = edge.to;

      if (uf.find(a) != uf.find(b)) {
        uf.union(a, b);

        if (a.row == b.row) {

          if (a.col < b.col) {
            a.right = true;
            b.left = true;
          }
          else {
            a.left = true;
            b.right = true;
          }
        }
        else {

          if (a.row < b.row) {
            a.bottom = true;
            b.top = true;
          }
          else {
            a.top = true;
            b.bottom = true;
          }
        }
      }
    }

    GamePiece power = this.board.get(this.powerCol).get(this.powerRow);
    power.powerStation = true;
    power.powered = true;

    for (GamePiece gp : this.nodes) {
      int spins = rand.nextInt(4);
      for (int i = 0; i < spins; i++) {
        gp.rotate();
      }
    }
  }

  // draws the world state
  public WorldScene makeScene() {
    int tileSize = 40;
    int wireWidth = 4;
    Color wireColor = Color.LIGHT_GRAY;
    WorldScene scene = new WorldScene(this.width * tileSize, this.height * tileSize);

    for (int col = 0; col < this.width; col++) {
      for (int row = 0; row < this.height; row++) {
        GamePiece gp = this.board.get(col).get(row);
        boolean isPowerStation = (gp.row == this.powerRow && gp.col == this.powerCol
            && gp.powerStation);
        if (gp.powered) {
          wireColor = Color.YELLOW;
        }
        else {
          wireColor = Color.LIGHT_GRAY;
        }
        WorldImage tile = gp.tileImage(tileSize, wireWidth, wireColor, isPowerStation);
        scene.placeImageXY(tile, col * tileSize + tileSize / 2, row * tileSize + tileSize / 2);
      }
    }
    return scene;
  }

  // handles all the mouse clicks
  // when the mouse is clicked, it rotates the tile and gives the wire the
  // appropriate color
  public void onMouseClicked(Posn pos) {
    int tileSize = 40;
    int col = pos.x / tileSize;
    int row = pos.y / tileSize;

    if (col >= 0 && col < this.width && row >= 0 && row < this.height) {
      this.board.get(col).get(row).rotate();
      this.updatePower();
      this.checkWin();
    }
  }

  // handles all the key clicks
  // when the arrow keys are clicked, the power station movies
  public void onKeyEvent(String key) {
    GamePiece current = this.board.get(this.powerCol).get(this.powerRow);
    int newRow = this.powerRow;
    int newCol = this.powerCol;

    if (key.equals("up") && current.top && this.powerRow > 0) {
      GamePiece neighbor = this.board.get(this.powerCol).get(this.powerRow - 1);
      if (neighbor.bottom) {
        newRow--;
      }
    }
    else if (key.equals("down") && current.bottom && this.powerRow < this.height - 1) {
      GamePiece neighbor = this.board.get(this.powerCol).get(this.powerRow + 1);
      if (neighbor.top) {
        newRow++;
      }
    }
    else if (key.equals("left") && current.left && this.powerCol > 0) {
      GamePiece neighbor = this.board.get(this.powerCol - 1).get(this.powerRow);
      if (neighbor.right) {
        newCol--;
      }
    }
    else if (key.equals("right") && current.right && this.powerCol < this.width - 1) {
      GamePiece neighbor = this.board.get(this.powerCol + 1).get(this.powerRow);
      if (neighbor.left) {
        newCol++;
      }
    }

    if (newRow != this.powerRow || newCol != this.powerCol) {
      current.powerStation = false;
      this.board.get(newCol).get(newRow).powerStation = true;
      this.powerRow = newRow;
      this.powerCol = newCol;
      this.updatePower();
      this.checkWin();
    }
  }

  // updates the state of a game piece depending on if it is connected to a power
  // station or not
  void updatePower() {
    for (ArrayList<GamePiece> column : this.board) {
      for (GamePiece gp : column) {
        gp.powered = false;
      }
    }

    GamePiece start = this.board.get(this.powerCol).get(this.powerRow);
    this.dfsPower(start);
  }

  // uses depth first search to light up tiles that are connected to the power
  // station
  void dfsPower(GamePiece gp) {
    gp.powered = true;

    if (gp.top && gp.row > 0) {
      GamePiece neighbor = this.board.get(gp.col).get(gp.row - 1);
      if (neighbor.bottom && !neighbor.powered) {
        dfsPower(neighbor);
      }
    }

    if (gp.bottom && gp.row < this.height - 1) {
      GamePiece neighbor = this.board.get(gp.col).get(gp.row + 1);
      if (neighbor.top && !neighbor.powered) {
        dfsPower(neighbor);
      }
    }

    if (gp.left && gp.col > 0) {
      GamePiece neighbor = this.board.get(gp.col - 1).get(gp.row);
      if (neighbor.right && !neighbor.powered) {
        dfsPower(neighbor);
      }
    }

    if (gp.right && gp.col < this.width - 1) {
      GamePiece neighbor = this.board.get(gp.col + 1).get(gp.row);
      if (neighbor.left && !neighbor.powered) {
        dfsPower(neighbor);
      }
    }
  }

  // checks if the game is won based on whether or not all the tiles are yellow
  void checkWin() {
    for (ArrayList<GamePiece> column : this.board) {
      for (GamePiece gp : column) {
        if (!gp.powered) {
          return;
        }
      }
    }
    this.endOfWorld("You win :)");
  }

  // draws the "you win" message on the screen
  public WorldScene lastScene(String msg) {
    WorldScene scene = this.makeScene();
    WorldImage message = new TextImage(msg, 24, FontStyle.BOLD, Color.GREEN);
    scene.placeImageXY(message, this.width * 20, this.height * 20);
    return scene;
  }

}

// represents a single tile in the LightEmALl game
class GamePiece {
  int row;
  int col;
  public boolean left;
  public boolean right;
  public boolean top;
  public boolean bottom;
  public boolean powerStation;
  public boolean powered;

  GamePiece(int row, int col) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.powerStation = false;
    this.powered = false;
  }

  // draws a single GamePiece as a tile
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new OverlayImage(new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY)));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (hasPowerStation) {
      image = new OverlayImage(
          new OverlayImage(new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          image);
    }
    return image;
  }

  // rotates the GamePiece tile clockwise if it is clicked
  void rotate() {
    boolean oldTop = this.top;
    this.top = this.left;
    this.left = this.bottom;
    this.bottom = this.right;
    this.right = oldTop;
  }
}

// represents an edge in the game
class Edge {
  GamePiece from;
  GamePiece to;
  int weight;

  Edge(GamePiece from, GamePiece to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }
}

// helps keep track of which pieces are connected when building the game board
class UnionFind {
  HashMap<GamePiece, GamePiece> parent = new HashMap<>();

  // creates a new set for the given tile
  void makeSet(GamePiece item) {
    parent.put(item, item);
  }

  // finds the root of the set that the tile belongs to
  GamePiece find(GamePiece item) {
    if (parent.get(item) != item) {
      parent.put(item, find(parent.get(item))); // path compression
    }
    return parent.get(item);
  }

  // connects the sets that contain the two given tiles
  void union(GamePiece a, GamePiece b) {
    GamePiece rootA = find(a);
    GamePiece rootB = find(b);
    if (rootA != rootB) {
      parent.put(rootA, rootB);
    }
  }
}

// examples class
class Examples2 {
  void testRunGame(Tester t) {
    LightEmAll game = new LightEmAll(8, 9);
    game.bigBang(320, 360, 0.1);
  }
}

// examples class
class Examples {
  void testRunGame(Tester t) {
    LightEmAll game = new LightEmAll(4, 4);
    game.bigBang(160, 160, 0.1);
  }
}

class ExamplesGame1 {
  void testRunGame(Tester t) {
    LightEmAll game = new LightEmAll(3, 3);
    game.makeRandomBoard(new Random(2));
    game.bigBang(120, 120, 0.1);
  }
}

class Tests {

  GamePiece gp1 = new GamePiece(1, 2);

  GamePiece gp2 = new GamePiece(2, 2);

  GamePiece gp3 = new GamePiece(2, 2);

  GamePiece gp4 = new GamePiece(2, 3);

  LightEmAll game = new LightEmAll(2, 2);

  GamePiece gp00 = new GamePiece(0, 0);

  GamePiece gp01 = new GamePiece(1, 0);

  GamePiece gp10 = new GamePiece(0, 1);

  GamePiece gp11 = new GamePiece(1, 1);

  GamePiece gp000 = new GamePiece(0, 0);

  LightEmAll game2 = new LightEmAll(2, 2);

  void initData() {
    // gp1 is a ┌ shape which is not powered
    this.gp1.right = true;
    this.gp1.bottom = true;
    this.gp1.powerStation = false;
    this.gp1.powered = false;

    // gp2 is a - shape with a wire to the left which is powered
    this.gp2.left = true;
    this.gp2.powerStation = false;
    this.gp2.powered = true;

    // gp3 is a -- shape that has a power station and is powered
    this.gp3.left = true;
    this.gp3.right = true;
    this.gp3.powerStation = true;
    this.gp3.powered = true;

    // gp4 is a ┻ shape that is not powered
    this.gp4.right = true;
    this.gp4.left = true;
    this.gp4.top = true;
    this.gp4.powerStation = false;
    this.gp4.powered = false;

    this.game = new LightEmAll(2, 2);

    this.gp00 = new GamePiece(0, 0);
    this.gp01 = new GamePiece(1, 0);
    this.gp10 = new GamePiece(0, 1);
    this.gp11 = new GamePiece(1, 1);
   
    this.gp00.right = true;
    this.gp00.bottom = false;
    this.gp00.top = false;
    this.gp00.left = false;
   
    this.gp01.right = true;
    this.gp01.bottom = false;
    this.gp01.top = false;
    this.gp01.left = false;
   
    this.gp11.right = false;
    this.gp11.bottom = true;
    this.gp11.top = false;
    this.gp11.left = true;
   
    this.gp10.right = false;
    this.gp10.bottom = false;
    this.gp10.top = true;
    this.gp10.left = true;
   
    this.gp00.powerStation = true;
    this.gp00.powered = true;

    this.gp11.powered = true;

    ArrayList<GamePiece> col0 = new ArrayList<>(Arrays.asList(this.gp00, this.gp01));
    ArrayList<GamePiece> col1 = new ArrayList<>(Arrays.asList(this.gp10, this.gp11));
    this.game.board = new ArrayList<>(Arrays.asList(col0, col1));

    this.game.powerRow = 0;
    this.game.powerCol = 0;

    this.gp000 = new GamePiece(0, 0);
    this.gp000.powerStation = true;
    this.gp000.powered = true;
    // gp000 is a - shape with a wire to the left which is powered
    this.gp000.left = true;

    ArrayList<GamePiece> col00 = new ArrayList<>(Arrays.asList(this.gp000, this.gp01));
    ArrayList<GamePiece> col11 = new ArrayList<>(Arrays.asList(this.gp10, this.gp11));
    this.game2.board = new ArrayList<>(Arrays.asList(col00, col11));
  }

  // tests for makeRandomBoard
  void testMakeRandomBoard(Tester t) {
    this.initData();
    LightEmAll testGame = new LightEmAll(3, 3);
    testGame.makeRandomBoard(new Random(1)); 
    t.checkExpect(testGame.board.size(), 3); 
    t.checkExpect(testGame.board.get(0).size(), 3);

    boolean hasPowerStation = false;
    for (ArrayList<GamePiece> column : testGame.board) {
      for (GamePiece gp : column) {
        if (gp.powerStation) {
          hasPowerStation = true;
          break;
        }
      }
    }
    t.checkExpect(hasPowerStation, true);
    for (int col = 0; col < 3; col++) {
      for (int row = 0; row < 3; row++) {
        GamePiece gp = testGame.board.get(col).get(row);
        t.checkExpect(gp.row, row);
        t.checkExpect(gp.col, col);
      }
    }
  }

  // tests for makeScene
  void testMakeScene(Tester t) {
    this.initData();

    WorldScene expected = new WorldScene(80, 80);
    expected.placeImageXY(this.gp00.tileImage(40, 4, Color.YELLOW, true), 20, 20);
    expected.placeImageXY(this.gp01.tileImage(40, 4, Color.LIGHT_GRAY, false), 20, 60);
    expected.placeImageXY(this.gp10.tileImage(40, 4, Color.LIGHT_GRAY, false), 60, 20);
    expected.placeImageXY(this.gp11.tileImage(40, 4, Color.YELLOW, false), 60, 60);
    t.checkExpect(game.makeScene(), expected);
  }

  // tests for onMouseClicked
  void testOnMouseClicked(Tester t) {
    // clicking on a tile
    this.initData();

    t.checkExpect(this.gp000.right, false);
    t.checkExpect(this.gp000.top, false);
    t.checkExpect(this.gp000.left, true);
    t.checkExpect(this.gp000.bottom, false);

    Posn posn1 = new Posn(10, 10);
    this.game2.onMouseClicked(posn1);

    // should be rotated
    t.checkExpect(this.gp000.right, false);
    t.checkExpect(this.gp000.top, true);
    t.checkExpect(this.gp000.left, false);
    t.checkExpect(this.gp000.bottom, false);

    // clicking out of bounds
    this.initData();
    this.game2.onMouseClicked(new Posn(500, 500));
    t.checkExpect(this.gp000.left, true);
    t.checkExpect(this.gp01.left, false);
  }

  // tests for onKeyEvent
  void testOnKeyEvent(Tester t) {
    this.initData();

    t.checkExpect(game.powerRow, 0);
    t.checkExpect(game.powerCol, 0);
    t.checkExpect(gp00.powerStation, true);
    t.checkExpect(gp10.powerStation, false);

    this.game.onKeyEvent("right");

    t.checkExpect(game.powerRow, 0);  
    t.checkExpect(game.powerCol, 1);  
    t.checkExpect(gp00.powerStation, false);
    t.checkExpect(gp10.powerStation, true);
   
    // try to move it when it can't
    game.onKeyEvent("right");
    t.checkExpect(game.powerRow, 0);
    t.checkExpect(game.powerCol, 1);
    t.checkExpect(gp10.powerStation, true);
    t.checkExpect(gp11.powerStation, false);
  }

  // tests for updatePower
  void testUpdatePower(Tester t) {
    this.initData();
    //initial state, only the one with the power station is powered
    //until either a click or rotate
    t.checkExpect(this.gp00.powered, true);
    t.checkExpect(this.gp11.powered, true);
    t.checkExpect(this.gp01.powered, false);
    t.checkExpect(this.gp10.powered, false);
   
    // rotates it to break the wire connection
    this.gp00.rotate();
   
    t.checkExpect(this.gp00.powered, true);
    t.checkExpect(this.gp11.powered, true);
    t.checkExpect(this.gp01.powered, false);
    t.checkExpect(this.gp10.powered, false);
   
    this.game.updatePower();
    t.checkExpect(this.gp00.powered, true);
    t.checkExpect(this.gp11.powered, false);
    t.checkExpect(this.gp01.powered, false);
    t.checkExpect(this.gp10.powered, false);
   
    this.gp00.rotate();
    this.gp00.rotate();
    this.gp00.rotate();
   
    this.game.updatePower();
    t.checkExpect(this.gp00.powered, true);
    t.checkExpect(this.gp11.powered, false);
    t.checkExpect(this.gp01.powered, false);
    t.checkExpect(this.gp10.powered, true);
  }

  // tests for dfsPower
  void testDfsPower(Tester t) {
    this.initData();
    GamePiece gp1 = new GamePiece(0, 0);
    GamePiece gp2 = new GamePiece(0, 1);
    GamePiece gp3 = new GamePiece(1, 0);
    gp1.right = true;
    gp2.left = true;
    gp1.bottom = true;
    gp3.top = true;
    gp1.powerStation = true;
    
    ArrayList<GamePiece> col0 = new ArrayList<>(Arrays.asList(gp1, gp3));
    ArrayList<GamePiece> col1 = new ArrayList<>(Arrays.asList(gp2, new GamePiece(1, 1)));
    LightEmAll testGame = new LightEmAll(2, 2);
    testGame.board = new ArrayList<>(Arrays.asList(col0, col1));
    testGame.powerRow = 0;
    testGame.powerCol = 0; 
    t.checkExpect(gp2.powered, false);
    t.checkExpect(gp3.powered, false);
    testGame.dfsPower(gp1);
    t.checkExpect(gp1.powered, true);
    t.checkExpect(gp2.powered, true); 
    t.checkExpect(gp3.powered, true);
    gp1.bottom = false;
    gp3.top = false;
    testGame.dfsPower(gp1);
    t.checkExpect(gp3.powered, true); 
  }

  // tests for checkWin
  void testCheckWin(Tester t) {
    this.initData();
    WorldScene start = this.game.makeScene();
    this.game.checkWin();
    t.checkExpect(this.game.makeScene(),start);
  }

  // tests for lastScene
  void testLastScene(Tester t) {
    this.initData();
    WorldScene expected = new WorldScene(80, 80);
    expected.placeImageXY(this.gp00.tileImage(40, 4, Color.YELLOW, true), 20, 20);
    expected.placeImageXY(this.gp01.tileImage(40, 4, Color.LIGHT_GRAY, false), 20, 60);
    expected.placeImageXY(this.gp10.tileImage(40, 4, Color.LIGHT_GRAY, false), 60, 20);
    expected.placeImageXY(this.gp11.tileImage(40, 4, Color.YELLOW, false), 60, 60);
    WorldImage msgImage = new TextImage("You win!", 24, FontStyle.BOLD, Color.GREEN);
    expected.placeImageXY(msgImage, 40, 40);

    t.checkExpect(this.game.lastScene("You win!"), expected);
  }

  // tests for rotate
  void testRotate(Tester t) {
    this.initData(); 
    this.gp1.rotate();
    t.checkExpect(this.gp1.right, false);
    t.checkExpect(this.gp1.bottom, true);
    t.checkExpect(this.gp1.left, true);
    t.checkExpect(this.gp1.top, false);
    t.checkExpect(this.gp1.powered, false);
    t.checkExpect(this.gp1.powered, false);
    this.gp2.rotate();
    t.checkExpect(this.gp2.right, false);
    t.checkExpect(this.gp2.bottom, false);
    t.checkExpect(this.gp2.left, false);
    t.checkExpect(this.gp2.top, true);
    t.checkExpect(this.gp2.powered, true);
    t.checkExpect(this.gp2.powered, true);
    this.gp2.rotate();
    t.checkExpect(this.gp2.right, true);
    t.checkExpect(this.gp2.bottom, false);
    t.checkExpect(this.gp2.left, false);
    t.checkExpect(this.gp2.top, false);
    t.checkExpect(this.gp2.powered, true);
    t.checkExpect(this.gp2.powered, true);
  }

  // tests for makeSet
  void testMakeSet(Tester t) {
    UnionFind uf = new UnionFind();
    GamePiece gp1 = new GamePiece(0, 0);
    GamePiece gp2 = new GamePiece(0, 1);
    
    uf.makeSet(gp1);
    uf.makeSet(gp2);
    
    t.checkExpect(uf.parent.get(gp1), gp1);
    t.checkExpect(uf.parent.get(gp2), gp2);
    
    t.checkExpect(uf.find(gp1), gp1);
    t.checkExpect(uf.find(gp2), gp2);
    
    uf.union(gp1, gp2);
    GamePiece root1 = uf.find(gp1);
    GamePiece root2 = uf.find(gp2);
    t.checkExpect(root1, root2); 
  }
}
