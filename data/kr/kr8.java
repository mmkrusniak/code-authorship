import java.util.Scanner;

public class Player{

  public int shipsSunk = 0;
  public Scanner s = new Scanner(System.in);
  public Player(){

  }

  public String guess(){
    String shot;
    System.out.println();
    System.out.println("Where will you shoot?");
    shot = s.nextLine();
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