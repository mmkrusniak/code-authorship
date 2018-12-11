//KR

import java.util.Random;

// class for generating elevation values and others based on elevation
public class Generator {

    // elevation, political, climate, biome map values
    private double[][] elevation;
    private double[][] political;
    private double[][] climate;
    private double[][] biome;

    private Random r = new Random();

    private int width, height;

    public Generator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    // elevation is default
    public double[][] generateNewWorld() {
        elevation = new double[width][height];

        // TODO elevation gen
        int col = 0;
        for(int i = 0; i < elevation.length; i++){
            for(int j = 0; j < elevation[i].length; j++){
                elevation[i][j] = r.nextFloat()*(2.0)-1.0; // temp val
            }
        }

        // TODO pol derives from elev

        // TODO clim derives from elev

        // TODO bio derives from elev

        return elevation;
    }

    // very WIP perlin noise
    public double perlin(double x, double y) {
        int x0 = floor(x);
        int x1 = x0 + 1;
        int y0 = floor(y);
        int y1 = y0 + 1;

        double sx = x - (double)x0;
        double sy = y - (double)y0;

        double n0, n1, ix0, ix1;
        n0 = grad(x0, y0, x, y);
        n1 = grad(x1, y0, x, y);
        ix0 = lerp(n0, n1, sx);
        n0 = grad(x0, y1, x, y);
        n1 = grad(x1, y1, x, y);
        ix1 = lerp(n0, n1, sx);
        return lerp(ix0, ix1, sy);
    }

    public int floor(double num) {
        return (int) (num - (num%1.0));
    }

    public double lerp(double a0, double a1, double w) {
        return (1.0 - w)*a0 + w*a1;
    }

    public double grad(int ix, int iy, double x, double y) {
        return 1.0;
    }

    public double[][] switchType(int type) {
        switch(type){
            case 0: return elevation;
            case 1: return political;
            case 2: return climate;
            case 3: return biome;
            default: return elevation;
        }
    }
}

//KR

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

// KR

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

// class for raw integer conversion and map displaying
public class Map extends JLabel {

    public BufferedImage img;
    public BufferedImage elevImg;
    private Generator gen;
    private double[][] vals;

    public Map(int width, int height) {
        gen = new Generator(width, height);

        setPreferredSize(new Dimension(width, height));

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setText("No Map Loaded");
    }

    public void newMap(String name) throws IOException {
        // activate the generator!!
        vals = gen.generateNewWorld();

        // convert to vals to img
        //TODO
        valToImg(vals, 0);

        // display map
        setIcon(new ImageIcon(img));
        elevImg = img;
    }

    public void switchMap(int type) throws IOException {
        vals = gen.switchType(type);
        valToImg(vals, type);
        setIcon(new ImageIcon(img));
    }

    // TODO implement random name gen
    public String getRandomWorldName(){
        return "New World";
    }

    // converts integers to colors and puts them into image
    private void valToImg(double[][] vals, int type) throws IOException {
        ArrayList<String> palette;

        switch(type){
            case 1: palette = sever("res/text/politicalCol.txt");
                    break;
            case 2: palette = sever("res/text/climateCol.txt");
                    break;
            case 3: palette = sever("res/text/biomeCol.txt");
                    break;
            default: palette = sever("res/text/elevationCol.txt");
                    break;
        }

        int col;
        for(int i = 0; i < vals.length; i++) {
            for(int j = 0; j < vals[i].length; j++) {
                vals[i][j] = ((vals[i][j]+1)*16);
                col = hexToRGBA(palette.get((int)vals[i][j]));
                img.setRGB(i, j, col);
            }
        }
    }

    // #ffffff format to rbga integer
    private int hexToRGBA(String hex) {
        // get substring and parse an int from the hexidecimal, 0-255
        int r = Integer.parseInt(hex.substring(1,3),16);
        int g = Integer.parseInt(hex.substring(3,5),16);
        int b = Integer.parseInt(hex.substring(5,7),16);
        int a = 255;

        // combine into one integer using bit manipulation
        int c = 0;
        c += a<<24;
        c += r<<16;
        c += g<<8;
        c += b;

        return c;
    }

    // partitions a files lines to separate indices
    private ArrayList<String> sever(String path) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        String line;
        BufferedReader br = new BufferedReader(new FileReader(path));

        line = br.readLine();
        while(line != null){
            lines.add(line);
            line = br.readLine();
        }
        return lines;
    }
}

//KR

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

//A builder for the control panel (sidebar) in the main application window.
public class Sidebar extends JPanel {

    public Dimension d;

    public JLabel worldName;

    private Handler handler;
    private Map map;

    // buttons
    private JButton elevationButton;
    private JButton politicalButton;
    private JButton climateButton;
    private JButton biomeButton;
    private JButton newMapButton;
    private JButton saveButton;
    private JButton settingsButton;

    // icons
    private Image elevationIcon;
    private Image politicalIcon;
    private Image climateIcon;
    private Image biomeIcon;
    private Image newMapIcon;
    private Image saveIcon;
    private Image settingsIcon;

    public Sidebar(int mapHeight, Map map) {

        this.map = map;
        handler = new Handler();

        // panel fill the window's height and the last 200 pixels on the width.
        d = new Dimension(200, mapHeight);
        setPreferredSize(d);

        /* CONFIGURE GUI */

        // set world name
        worldName = new JLabel("World Name");
        worldName.setFont(new Font("Roboto", Font.BOLD, 24));

        elevationButton = new JButton("Elevation");
        politicalButton = new JButton("Poltical");
        climateButton = new JButton("Climate");
        biomeButton = new JButton("Biome");
        newMapButton = new JButton("");
        settingsButton = new JButton("");
        saveButton = new JButton("");

        // get resources for icons
        try {
            elevationIcon = ImageIO.read(getClass().getResource("icons/elevation.png"));
            politicalIcon = ImageIO.read(getClass().getResource("icons/political.png"));
            climateIcon = ImageIO.read(getClass().getResource("icons/climate.png"));
            biomeIcon = ImageIO.read(getClass().getResource("icons/biome.png"));
            newMapIcon = ImageIO.read(getClass().getResource("icons/newMap.png"));
            settingsIcon = ImageIO.read(getClass().getResource("icons/settings.png"));
            saveIcon = ImageIO.read(getClass().getResource("icons/save.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // set icons
        elevationButton.setIcon(new ImageIcon(elevationIcon));
        politicalButton.setIcon(new ImageIcon(politicalIcon));
        climateButton.setIcon(new ImageIcon(climateIcon));
        biomeButton.setIcon(new ImageIcon(biomeIcon));
        newMapButton.setIcon(new ImageIcon(newMapIcon));
        saveButton.setIcon(new ImageIcon(saveIcon));
        settingsButton.setIcon(new ImageIcon(settingsIcon));

        // nifty tooltips
        elevationButton.setToolTipText("Switch to Elevation View");
        politicalButton.setToolTipText("Switch to Political View");
        climateButton.setToolTipText("Switch to Climate View");
        biomeButton.setToolTipText("Switch to Biome View");
        newMapButton.setToolTipText("Generate New Map");
        saveButton.setToolTipText("Save World");
        settingsButton.setToolTipText("Change Settings");

        // consistent fonts, roboto masterrace
        elevationButton.setFont(new Font("Roboto", Font.PLAIN, 12));
        politicalButton.setFont(new Font("Roboto", Font.PLAIN, 12));
        climateButton.setFont(new Font("Roboto", Font.PLAIN, 12));
        biomeButton.setFont(new Font("Roboto", Font.PLAIN, 12));
        newMapButton.setFont(new Font("Roboto", Font.PLAIN, 12));
        saveButton.setFont(new Font("Roboto", Font.PLAIN, 12));
        settingsButton.setFont(new Font("Roboto", Font.PLAIN, 12));

        // add listeners
        elevationButton.addActionListener(handler);
        politicalButton.addActionListener(handler);
        climateButton.addActionListener(handler);
        biomeButton.addActionListener(handler);
        newMapButton.addActionListener(handler);
        saveButton.addActionListener(handler);
        settingsButton.addActionListener(handler);

        /* LAYOUT */

        // set layout type
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        // buttons snap to the center
        gc.anchor = GridBagConstraints.CENTER;

        // padding between components
        gc.weightx = 0.5;
        gc.weighty = 0.5;

        // cell size is 3 for wider buttons
        gc.gridwidth = 3;

        // world name
        gc.gridx = 0;
        gc.gridy = 0;
        add(worldName, gc);

        // elevation
        gc.ipadx = 10;
        gc.ipady = 5;
        gc.gridx = 0;
        gc.gridy = 1;
        add(elevationButton, gc);

        // political
        gc.ipadx = 19;
        gc.ipady = 5;
        gc.gridx = 0;
        gc.gridy = 2;
        add(politicalButton, gc);

        // climate
        gc.ipadx = 20;
        gc.ipady = 5;
        gc.gridx = 0;
        gc.gridy = 3;
        add(climateButton, gc);

        // biome
        gc.ipadx = 27;
        gc.ipady = 5;
        gc.gridx = 0;
        gc.gridy = 4;
        add(biomeButton, gc);

        // cell size is now 1 to accommodate 3 components in the last row
        gc.gridwidth = 1;

        // new map
        gc.ipadx = 0;
        gc.ipady = 0;
        gc.gridx = 0;
        gc.gridy = 5;
        add(newMapButton, gc);

        // save
        gc.ipadx = 0;
        gc.ipady = 0;
        gc.gridx = 1;
        gc.gridy = 5;
        add(saveButton, gc);

        // settings
        gc.ipadx = 0;
        gc.ipady = 0;
        gc.gridx = 2;
        gc.gridy = 5;
        add(settingsButton, gc);

    }

    private class Handler implements ActionListener {

        private Handler(){}

        @Override   // button functions
        public void actionPerformed(ActionEvent e) {

            // elevation button functions
            if(e.getSource() == elevationButton){
                try {
                    map.switchMap(0);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if(e.getSource() == politicalButton) { // political button functions
                try {
                    map.switchMap(1);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if(e.getSource() == climateButton) { // climate button functions
                try {
                    map.switchMap(2);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if(e.getSource() == biomeButton) { // biome button functions
                try {
                    map.switchMap(3);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if(e.getSource() == newMapButton) { // new map button, see Map.java
                String name  = "";
                if(name.equals("")) {
                    name = map.getRandomWorldName();
                }
                worldName.setText(name);
                try {
                    map.newMap(name);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if(e.getSource() == saveButton) {  // TODO save button functions
            }else if(e.getSource() == settingsButton){ // TODO settings button functions

            }
        }
    }
}

//KR

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