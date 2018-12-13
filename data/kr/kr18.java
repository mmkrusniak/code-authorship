//Karl Ramberg

import javax.swing.*;
import java.awt.*;

// the builder class for the applications window.
public class Window extends JFrame {

    // TODO change dynamically based on OS.
    private final String LOOKANDFEEL = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

    // panel to the right with controls.
    private Sidebar sidebar;
    private Map map;
    private Container container;

    public Window(String title, int mapWidth, int mapHeight, Sidebar sidebar, Map map) {
        this.sidebar = sidebar;
        this.map = map;

        // window settings
        setTitle(title);
        setLookAndFeel();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(mapWidth + 200, mapHeight); // 200 extra for the sidebar
        setLocationRelativeTo(null);
        setResizable(false);

        // add sidebar aligned to the left and Map to the right.
        container = getContentPane();
        container.add(sidebar, BorderLayout.EAST);
        container.add(map, BorderLayout.WEST);
    }

    public void setLookAndFeel() {
        // your standard java exception handling bullshit
        try {
            UIManager.setLookAndFeel(LOOKANDFEEL);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
