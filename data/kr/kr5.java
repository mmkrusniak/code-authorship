import java.util.Random;

public class Enemy{
  public Random r = new Random();
  public int shipsSunk = 0;

  public Enemy(){
  }

  public String guess(){
    int[] tempPoint = new int[2];
    String shot;
    tempPoint[0] = r.nextInt(10);
    tempPoint[1] = r.nextInt(10);
    shot = Engine.GtoP(tempPoint);
    System.out.println("Your enemy guessed " + shot + ".");
    System.out.println();
    return shot;
  }

  public int getShipsSunk(){
    return shipsSunk;
  }

  public void setShipsSunk(int x){
    shipsSunk = x;
  }
}