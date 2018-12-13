//Karl Ramberg

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