/**
 * PEDIGREE CHART CREATOR by Karl Ramberg
 * 12/4/15
 * This class draws and organizes a pedigree chart.
 */

import java.awt.*;
import javax.swing.JFrame;

public class Chart extends Canvas{

    public static final int WIDTH = 500;
    public static final int HEIGHT = 500;
    public Person mom;
    public Person dad;
    public Person[] kids = new Person[5];
    public int numOfKids;

    public Chart(String family, Person mom, Person dad, Person[] kids, int numOfKids){

        this.dad = dad;
        this.mom = mom;
        this.kids = kids;
        this.numOfKids = numOfKids;

        JFrame chart = new JFrame("The " + family + " Family");
        chart.setSize(WIDTH, HEIGHT);
        chart.setResizable(false);
        chart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chart.setBackground(Color.WHITE);
        chart.add(this);
        chart.setVisible(true);

    }

    public void paint(Graphics g){

        g.setColor(Color.BLACK);

        if(mom.affected){

            g.fillOval(WIDTH/2 - 30, HEIGHT/2 - 100, 30, 30);

        }else {

            g.drawOval(WIDTH/2 - 30, HEIGHT/2 - 100, 30, 30);

        }

        if(dad.affected){

            g.fillRect(WIDTH/2 + 30, HEIGHT/2 - 100, 30, 30);

        }else {

            g.drawRect(WIDTH/2 + 30, HEIGHT/2 - 100, 30, 30);

        }
        
        g.drawLine(WIDTH/2 , HEIGHT/2 - 85, WIDTH/2 + 30, HEIGHT/2 - 85);
        g.drawLine(WIDTH/2 + 15, HEIGHT/2 - 85, WIDTH/2 + 15, 270);

        int x = 1;
        switch(numOfKids){
            case 1: x = WIDTH/2;
                    break;
            case 2: x = WIDTH/2 - 30;
                    break;
            case 3: x = WIDTH/2 - 60;
                    break;
            case 4: x = WIDTH/2 - 90;
                    break;
            case 5: x = WIDTH/2 - 120;
                    break;
        }

        for(int i = 0; i < 5; i++){

            if(kids[i].print){
                if(kids[i].gender == 1){
                    if(kids[i].affected){
                        g.fillOval(x, 300, 30, 30);
                        g.drawLine(x+15, 300, x+15, 270);
                        if(i < numOfKids - 1)
                            g.drawLine(x+15, 270, x+75, 270);
                        x += 60;
                    }else{
                        g.drawOval(x, 300, 30, 30);
                        g.drawLine(x+15, 300, x+15, 270);
                        if(i < numOfKids - 1)
                            g.drawLine(x+15, 270, x+75, 270);
                        x += 60;
                    }
                }

                if(kids[i].gender == 2){
                    if(kids[i].affected){
                        g.fillRect(x, 300, 30, 30);
                        g.drawLine(x+15, 300, x+15, 270);
                        if(i < numOfKids - 1)
                            g.drawLine(x+15, 270, x+75, 270);
                        x += 60;
                    }else{
                        g.drawRect(x, 300, 30, 30);
                        g.drawLine(x+15, 300, x+15, 270);
                        if(i < numOfKids - 1)
                            g.drawLine(x+15, 270, x+75, 270);
                        x += 60;
                    }
                }                
            }
        }
    }

}