public class Board{
  public String[][] board = {{"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"},
  {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-"}};

  public int owner;

  public Board(int owner){
    this.owner = owner;
  }

  public void print(){
    System.out.println("   0 1 2 3 4 5 6 7 8 9");
    for(int i = 0; i < board.length; i++){
      switch(i){
        case 0: System.out.print("A ");
        break;
        case 1: System.out.print("B ");
        break;
        case 2: System.out.print("C ");
        break;
        case 3: System.out.print("D ");
        break;
        case 4: System.out.print("E ");
        break;
        case 5: System.out.print("F ");
        break;
        case 6: System.out.print("G ");
        break;
        case 7: System.out.print("H ");
        break;
        case 8: System.out.print("I ");
        break;
        case 9: System.out.print("J ");
                break;
      }
      for(int j = 0; j < board[i].length; j++){
        if(j != 9)System.out.print(" " + board[i][j]);
        else System.out.println(" " + board[i][j]);
      }
    }
  }

  public void update(){
    int [] tempPoint = new int[2];
    if(owner == 1){
      for(int i = 0; i < 5; i++){
        tempPoint = Engine.PtoG(Engine.car1.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.car1.getState(i);
      }
      for(int i = 0; i < 4; i++){
        tempPoint = Engine.PtoG(Engine.bat1.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.bat1.getState(i);
      }
      for(int i = 0; i < 3; i++){
        tempPoint = Engine.PtoG(Engine.des1.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.des1.getState(i);
      }
      for(int i = 0; i < 3; i++){
        tempPoint = Engine.PtoG(Engine.sub1.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.sub1.getState(i);
      }
      for(int i = 0; i < 2; i++){
        tempPoint = Engine.PtoG(Engine.pat1.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.pat1.getState(i);
      }
    }

    if(owner == 2){
      for(int i = 0; i < 5; i++){
        tempPoint = Engine.PtoG(Engine.car2.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.car2.getState(i);
      }
      for(int i = 0; i < 4; i++){
        tempPoint = Engine.PtoG(Engine.bat2.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.bat2.getState(i);
      }
      for(int i = 0; i < 3; i++){
        tempPoint = Engine.PtoG(Engine.des2.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.des2.getState(i);
      }
      for(int i = 0; i < 3; i++){
        tempPoint = Engine.PtoG(Engine.sub2.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.sub2.getState(i);
      }
      for(int i = 0; i < 2; i++){
        tempPoint = Engine.PtoG(Engine.pat2.getLocation(i));
        board[tempPoint[1]][tempPoint[0]] = Engine.pat2.getState(i);
      }
    }
  }

  public void updatePoint(String point, String type){
    int[] tempPoint = new int[2];
    tempPoint = Engine.PtoG(point);
    board[tempPoint[1]][tempPoint[0]] = type;
  }
}