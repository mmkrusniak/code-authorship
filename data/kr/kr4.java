public class Destroyer extends Ship{
  String[] states = {"0", "0", "0"};
  public Destroyer(String[] locations, int orient){
    this.locations = locations;
    name = "Destroyer";
    life = 3;
    locations = new String[life];
  }

  public boolean isHit(String shot){
    boolean hit = false;
    for(int i = 0; i < locations.length; i++){
      if(shot.equals(locations[i])){
        hit = true;
        life--;
        states[i] = "X";
        if(Engine.getCurrentPlayer() == 1){
          System.out.println("You got a hit!");
          System.out.println();
          Engine.b2.updatePoint(shot, "X");
        }else{
          System.out.println("The enemy has hit your " + name + ".");
          System.out.println();
          Engine.b1.updatePoint(shot, "X");
        }
        return true;
      }
    }
    return false;
  }

  public String getState(int i){
    return states[i];
  }
}