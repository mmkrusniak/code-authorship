//Karl Ramberg

// the main starter class
public class Main {

    private int mapWidth = 800;
    private int mapHeight = 600;

    private String title = "Ymir";

    private Window window;
    private Sidebar sidebar;
    private Map map;

    public static void main(String[] args){
        new Main();
    }

    public Main(){
        map = new Map(mapWidth, mapHeight);
        sidebar = new Sidebar(mapHeight, map);
        window = new Window(title, mapWidth, mapHeight, sidebar, map);
        window.setVisible(true);
    }
}