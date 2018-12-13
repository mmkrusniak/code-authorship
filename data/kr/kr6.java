import java.util.Scanner;
import java.util.Random;

public class Engine{
  public static Board b1;
  public static Board b2;
  public static Player p;
  public static Enemy e;
  public static int currentPlayer;
  public static boolean playing = true;
  public static Scanner s = new Scanner(System.in);
  public static Random r = new Random();

  public static Carrier car1;
  public static Battleship bat1;
  public static Destroyer des1;
  public static Submarine sub1;
  public static Patrol pat1;

  public static Carrier car2;
  public static Battleship bat2;
  public static Destroyer des2;
  public static Submarine sub2;
  public static Patrol pat2;

  public static void main(String[]args){
    b1 = new Board(1);
    b2 = new Board(2);
    currentPlayer = 1;
    placeShips();
    gameLoop();
  }

  public static void gameLoop(){
    p = new Player();
    e = new Enemy();
    String shot;
    while(playing){
      currentPlayer = 1;
      shot = p.guess();
      checkForHit(2, shot);
      checkForSunk(2);
      if(p.getShipsSunk() == 5){
        playing = false;
        System.out.println();
        System.out.println("YOU WON!");
      }

      currentPlayer = 2;
      shot = e.guess();
      checkForHit(1, shot);
      checkForSunk(1);
      if(e.getShipsSunk() == 5){
        playing = false;
        System.out.println();
        System.out.println("YOU LOST!");
      }
      b1.update();
      printBoards();
    }
  }

  public static void placeShips(){
    String pos;
    int orient;
    int[] tempPoint = new int[2];
    boolean placing = true;
    b1.print();

    while(placing){
      System.out.println();
      System.out.println("Where would like to place the Carrier?");
      pos = s.nextLine();
      System.out.println();
      System.out.println("Which way would you like to orient it? (MAKE SURE IT FITS)");
      System.out.println("1-UP, 2-RIGHT, 3-DOWN, 4-LEFT");
      orient = s.nextInt();
      if(checkPos(5, pos, orient)){
        car1 = new Carrier(getLocations(pos, 5, orient), orient);
        placing = false;
      }
    }
    placing = true;

    while(placing){
      System.out.println();
      System.out.println("Where would like to place the Battleship?");
      s.nextLine();
      pos = s.nextLine();
      System.out.println();
      System.out.println("Which way would you like to orient it? (MAKE SURE IT FITS)");
      System.out.println("1-UP, 2-RIGHT, 3-DOWN, 4-LEFT");
      orient = s.nextInt();
      if(checkPos(4, pos, orient)){
        bat1 = new Battleship(getLocations(pos, 4, orient), orient);
        placing = false;
      }
    }
    placing = true;

    while(placing){
      System.out.println();
      System.out.println("Where would like to place the Destroyer?");
      s.nextLine();
      pos = s.nextLine();
      System.out.println();
      System.out.println("Which way would you like to orient it? (MAKE SURE IT FITS)");
      System.out.println("1-UP, 2-RIGHT, 3-DOWN, 4-LEFT");
      orient = s.nextInt();
      if(checkPos(3, pos, orient)){
        des1 = new Destroyer(getLocations(pos, 3, orient), orient);
        placing = false;
      }
    }
    placing = true;

    while(placing){
      System.out.println();
      System.out.println("Where would like to place the Submarine?");
      s.nextLine();
      pos = s.nextLine();
      System.out.println();
      System.out.println("Which way would you like to orient it? (MAKE SURE IT FITS)");
      System.out.println("1-UP, 2-RIGHT, 3-DOWN, 4-LEFT");
      orient = s.nextInt();
      if(checkPos(3, pos, orient)){
        sub1 = new Submarine(getLocations(pos, 3, orient), orient);
        placing = false;
      }
    }
    placing = true;

    while(placing){
      System.out.println();
      System.out.println("Where would like to place the Patrol?");
      s.nextLine();
      pos = s.nextLine();
      System.out.println();
      System.out.println("Which way would you like to orient it? (MAKE SURE IT FITS)");
      System.out.println("1-UP, 2-RIGHT, 3-DOWN, 4-LEFT");
      orient = s.nextInt();
      if(checkPos(2, pos, orient)){
        pat1 = new Patrol(getLocations(pos, 2, orient), orient);
        placing = false;
      }
    }
    placing = true;

    while(placing){
      tempPoint[0] = r.nextInt(10);
      tempPoint[1] = r.nextInt(10);
      orient = r.nextInt(4)+1;
      if(checkPos(5, GtoP(tempPoint), orient)){
        car2 = new Carrier(getLocations(GtoP(tempPoint), 5, orient), orient);
        placing = false;
      }
    }
    placing = true;

    while(placing){
      tempPoint[0] = r.nextInt(10);
      tempPoint[1] = r.nextInt(10);
      orient = r.nextInt(4)+1;
      if(checkPos(4, GtoP(tempPoint), orient)){
        if(checkOverlap(4, getLocations(GtoP(tempPoint), 4, orient))){
          bat2 = new Battleship(getLocations(GtoP(tempPoint), 4, orient), orient);
          placing = false;
        }
      }
    }
    placing = true;

    while(placing){
      tempPoint[0] = r.nextInt(10);
      tempPoint[1] = r.nextInt(10);
      orient = r.nextInt(4)+1;
      if(checkPos(3, GtoP(tempPoint), orient)){
        if(checkOverlap(3, getLocations(GtoP(tempPoint), 3, orient))){
          des2 = new Destroyer(getLocations(GtoP(tempPoint), 3, orient), orient);
          placing = false;
        }
      }
    }
    placing = true;

    while(placing){
      tempPoint[0] = r.nextInt(10);
      tempPoint[1] = r.nextInt(10);
      orient = r.nextInt(4)+1;
      if(checkPos(3, GtoP(tempPoint), orient)){
        if(checkOverlap(2, getLocations(GtoP(tempPoint), 3, orient))){
          sub2 = new Submarine(getLocations(GtoP(tempPoint), 3, orient), orient);
          placing = false;
        }
      }
    }
    placing = true;

    while(placing){
      tempPoint[0] = r.nextInt(10);
      tempPoint[1] = r.nextInt(10);
      orient = r.nextInt(4)+1;
      if(checkPos(2, GtoP(tempPoint), orient)){
        if(checkOverlap(1, getLocations(GtoP(tempPoint), 2, orient))){
          pat2 = new Patrol(getLocations(GtoP(tempPoint), 2, orient), orient);
          placing = false;
        }
      }
    }
    placing = true;

    b1.update();
    printBoards();
  }

  public static boolean checkPos(int shipSize, String pos, int orient){
    int[] point = PtoG(pos);
    if(orient == 1){
      if(point[1] - shipSize-1 >= 0)return true;
    }else if(orient == 2){
      if(point[0] + shipSize-1 <= 9)return true;
    }else if(orient == 3){
      if(point[1] + shipSize-1 <= 9)return true;
    }else if(orient == 4){
      if(point[0] - shipSize-1 >= 0)return true;
    }
    return false;
  }

  public static String[] getLocations(String pos, int size, int orient){
    int[] tempGrid = PtoG(pos);
    String[] locs = new String[size];
    locs[0] = pos;
    if(orient == 1){
      for(int i = 1; i < size; i++){
        tempGrid[1] -= i;
        locs[i] = GtoP(tempGrid);
        tempGrid[1] += i;
      }
    }else if(orient == 2){
      for(int i = 1; i < size; i++){
        tempGrid[0] += i;
        locs[i] = GtoP(tempGrid);
        tempGrid[0] -= i;
      }
    }else if(orient ==3){
      for(int i = 1; i < size; i++){
        tempGrid[1] += i;
        locs[i] = GtoP(tempGrid);
        tempGrid[1] -= i;
      }
    }else if(orient == 4){
      for(int i = 1; i < size; i++){
        tempGrid[0] -= i;
        locs[i] = GtoP(tempGrid);
        tempGrid[0] += i;
      }
    }
    return locs;
  }

  public static String GtoP(int[] point){
    String pos = "";
    switch(point[1]){
      case 0:pos += "a";
      break;
      case 1:pos += "b";
      break;
      case 2:pos += "c";
      break;
      case 3:pos += "d";
      break;
      case 4:pos += "e";
      break;
      case 5:pos += "f";
      break;
      case 6:pos += "g";
      break;
      case 7:pos += "h";
      break;
      case 8:pos += "i";
      break;
      case 9:pos += "j";
      break;
    }
    pos += point[0];
    return pos;
  }

  public static int[] PtoG(String pos){
    int[] point = new int[2];

    switch(pos.charAt(0)){
      case 'a':point[1] = 0;
      break;
      case 'b':point[1] = 1;
      break;
      case 'c':point[1] = 2;
      break;
      case 'd':point[1] = 3;
      break;
      case 'e':point[1] = 4;
      break;
      case 'f':point[1] = 5;
      break;
      case 'g':point[1] = 6;
      break;
      case 'h':point[1] = 7;
      break;
      case 'i':point[1] = 8;
      break;
      case 'j':point[1] = 9;
      break;
    }
    point[0] = Character.getNumericValue(pos.charAt(1));
    return point;
  }

  public static boolean checkOverlap(int shipSize, String[] locs){
    if(shipSize == 4){
      if(checkSim(locs, car2.getLocations()))return true;
    }else if(shipSize == 3){
      if(checkSim(locs, car2.getLocations()) && checkSim(locs, bat2.getLocations()))return true;
    }else if(shipSize == 2){
      if(checkSim(locs, car2.getLocations()) && checkSim(locs, bat2.getLocations()) && checkSim(locs, des2.getLocations()))return true;
    }else if(shipSize == 1){
      if(checkSim(locs, car2.getLocations()) && checkSim(locs, bat2.getLocations()) && checkSim(locs, des2.getLocations()) && checkSim(locs, sub2.getLocations()))return true;
    }
    return false;
  }

  public static boolean checkSim(String[] s1, String[] s2){
    for(int i = 0; i < s1.length; i++){
      for(int j = 0; j < s2.length; j++){
        if(s1[i].equals(s2[j]))return false;
      }
    }
    return true;
  }

  public static void checkForHit(int board, String shot){
    boolean missed = true;
    if(board == 1){
      if(car1.isHit(shot)){
        missed = false;
      }else if(bat1.isHit(shot)){
        missed = false;
      }else if(des1.isHit(shot)){
        missed = false;
      }else if(sub1.isHit(shot)){
        missed = false;
      }else if(pat1.isHit(shot)){
        missed = false;
      }else{
        System.out.println("Your enemy missed!");
        System.out.println();
        b1.updatePoint(shot, "*");
      }
    }else{
      if(car2.isHit(shot)){
        missed = false;
      }else if(bat2.isHit(shot)){
        missed = false;
      }else if(des2.isHit(shot)){
        missed = false;
      }else if(sub2.isHit(shot)){
        missed = false;
      }else if(pat2.isHit(shot)){
        missed = false;
      }else{
        System.out.println("You missed!");
        System.out.println();
        b2.updatePoint(shot, "*");
      }
    }
  }

  public static void checkForSunk(int board){
    if(board == 1){
      car1.isSunk();
      bat1.isSunk();
      des1.isSunk();
      sub1.isSunk();
      pat1.isSunk();
    }else{
      car2.isSunk();
      bat2.isSunk();
      des2.isSunk();
      sub2.isSunk();
      pat2.isSunk();
    }
  }



  public static void printBoards(){
    System.out.println();
    System.out.println("YOUR BOARD");
    b1.print();
    System.out.println();
    System.out.println("ENEMY BOARD");
    b2.print();
  }

  public static int getCurrentPlayer(){
    return currentPlayer;
  }
}