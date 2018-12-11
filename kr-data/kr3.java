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

import java.util.Scanner;
import java.util.Random;

public class PedigreeCreator{

    public static Scanner scan = new Scanner(System.in);
    public static Trait trait = new Trait(1);
    public static Person mom;
    public static Person dad;
    public static Person[] kids = new Person[5];
    public static Chart chart;
    public static Random r = new Random();
    public static int numKids = 1;

    public static void main(String[]args){

        String familyName;
        System.out.println("}]} WELCOME TO THE PEDIGREE CHART CREATOR {[{");
        System.out.println();
        System.out.println("You will be asked the genotypes of two parents");
        System.out.println(" and from there the program will generate children.");
        System.out.println();
        System.out.println("For multiple choice questions please input the number of your answer.");
        System.out.println("------------------------------------------------------------------------------");

        System.out.println("What is your family's name?");
        familyName = scan.nextLine();

        System.out.println("How many kids shoudld the program generate? (1-5)");
        numKids = scan.nextInt();

        System.out.println();
        System.out.println("What type of trait are you tracking?");
        System.out.println("1: Autosomal Recessive");
        System.out.println("2: Autosomal Dominant");
        System.out.println("3: X-Linked Recessive");
        trait.type = scan.nextInt();

        if(trait.type == 1){

            askAutoRes();

        }else if(trait.type == 2){

            askAutoDom();

        }else{

            askXRes();

        }

        chart = new Chart(familyName, mom, dad, kids, numKids);

    }

    public static void askAutoRes(){
        boolean momAffected;
        int momAllele1;
        int momAllele2;
        boolean dadAffected;
        int dadAllele1;
        int dadAllele2;
        int temp;

        System.out.println();
        System.out.println("What is the genotype of the mother?");
        System.out.println("1: AA");
        System.out.println("2: Aa");
        System.out.println("3: aa");
        temp = scan.nextInt();

        if(temp == 1){

            momAffected = false;
            momAllele1 = 1;
            momAllele2 = 1;

        }else if(temp == 2){

            momAffected = false;
            momAllele1 = 1;
            momAllele2 = 2;

        }else{

            momAffected = true;
            momAllele1 = 2;
            momAllele2 = 2;

        }

        mom = new Person(1, momAffected, true, momAllele1, momAllele2);

        System.out.println();
        System.out.println("What is the genotype of the father?");
        System.out.println("1: AA");
        System.out.println("2: Aa");
        System.out.println("3: aa");
        temp = scan.nextInt();

        if(temp == 1){

            dadAffected = false;
            dadAllele1 = 1;
            dadAllele2 = 1;

        }else if(temp == 2){

            dadAffected = false;
            dadAllele1 = 1;
            dadAllele2 = 2;

        }else{

            dadAffected = true;
            dadAllele1 = 2;
            dadAllele2 = 2;

        }

        dad = new Person(2, dadAffected, true, dadAllele1, dadAllele2);

        for(int i = 0; i < 5; i++)
            kids[i] = new Person(r.nextInt(2) + 1, false, false, 1, 1);

        for(int i = 0; i < numKids; i++){

            kids[i].print = true;

            temp = r.nextInt(2) + 1;

            if(temp == 1)
                kids[i].allele1 = momAllele1;
            else
                kids[i].allele1 = momAllele2;

            temp = r.nextInt(2) + 1;

            if(temp == 1)
                kids[i].allele2 = dadAllele1;
            else
                kids[i].allele2 = dadAllele2;

            if(kids[i].allele1 == 1 || kids[i].allele2 == 1)
                kids[i].affected = false;
            else
                kids[i].affected = true;

        }

    }

    public static void askAutoDom(){

        boolean momAffected;
        int momAllele1;
        int momAllele2;
        boolean dadAffected;
        int dadAllele1;
        int dadAllele2;
        int temp;

        System.out.println();
        System.out.println("What is the genotype of the mother?");
        System.out.println("1: AA");
        System.out.println("2: Aa");
        System.out.println("3: aa");
        temp = scan.nextInt();

        if(temp == 1){

            momAffected = true;
            momAllele1 = 1;
            momAllele2 = 1;

        }else if(temp == 2){

            momAffected = true;
            momAllele1 = 1;
            momAllele2 = 2;

        }else{

            momAffected = false;
            momAllele1 = 2;
            momAllele2 = 2;

        }

        mom = new Person(1, momAffected,true, momAllele1, momAllele2);

        System.out.println();
        System.out.println("What is the genotype of the father?");
        System.out.println("1: AA");
        System.out.println("2: Aa");
        System.out.println("3: aa");
        temp = scan.nextInt();

        if(temp == 1){

            dadAffected = true;
            dadAllele1 = 1;
            dadAllele2 = 1;

        }else if(temp == 2){

            dadAffected = true;
            dadAllele1 = 1;
            dadAllele2 = 2;

        }else{

            dadAffected = false;
            dadAllele1 = 2;
            dadAllele2 = 2;

        }

        dad = new Person(2, dadAffected,true, dadAllele1, dadAllele2);

        for(int i = 0; i < 5; i++)
            kids[i] = new Person(r.nextInt(2) + 1, false, false, 1, 1);

        for(int i = 0; i < numKids; i++){

            kids[i].print = true;

            temp = r.nextInt(2) + 1;

            if(temp == 1)
                kids[i].allele1 = momAllele1;
            else
                kids[i].allele1 = momAllele2;

            temp = r.nextInt(2) + 1;

            if(temp == 1)
                kids[i].allele2 = dadAllele1;
            else
                kids[i].allele2 = dadAllele2;

            if(kids[i].allele1 == 1 || kids[i].allele2 == 1)
                kids[i].affected = true;
            else
                kids[i].affected = false;

        }

    }

    public static void askXRes(){

        boolean momAffected;
        int momAllele1;
        int momAllele2 = 3;
        boolean dadAffected = false;
        int dadAllele1 = 1;
        int dadAllele2 = 1;
        int temp;

        System.out.println();
        System.out.println("What is the genotype of the mother?");
        System.out.println("1: X^A X^A");
        System.out.println("2: X^A X^a");
        System.out.println("3: X^a X^a");
        temp = scan.nextInt();

        if(temp == 1){

            momAffected = false;
            momAllele1 = 1;
            momAllele2 = 1;

        }else if(temp == 2){

            momAffected = false;
            momAllele1 = 1;
            momAllele2 = 2;

        }else{

            momAffected = true;
            momAllele1 = 2;
            momAllele2 = 2;

        }

        mom = new Person(1, momAffected, true, momAllele1, momAllele2);

        System.out.println();
        System.out.println("What is the genotype of the father?");
        System.out.println("1: X^A Y");
        System.out.println("2: X^a Y");
        temp = scan.nextInt();

        if(temp == 1){

            dadAffected = false;
            dadAllele1 = 1;

        }else if(temp == 2){

            dadAffected = true;
            dadAllele1 = 2;

        }

        dad = new Person(2, dadAffected, true, dadAllele1, dadAllele2);

        for(int i = 0; i < 5; i++)
            kids[i] = new Person(r.nextInt(2) + 1, false, false, 1, 1);

        for(int i = 0; i < numKids; i++){

            kids[i].print = true;

            temp = r.nextInt(2) + 1;

            if(temp == 1)
                kids[i].allele1 = momAllele1;
            else
                kids[i].allele1 = momAllele2;

            temp = r.nextInt(2) + 1;

            if(temp == 1)
                kids[i].allele2 = dadAllele1;
            else
                kids[i].allele2 = dadAllele2;

            if(kids[i].gender == 1){
                if(kids[i].allele1 == 2 && kids[i].allele2 == 2)
                    kids[i].affected = true;
                else
                    kids[i].affected = false;
            }

            if(kids[i].gender == 2){
                kids[i].allele2 = 3;
                if(kids[i].allele1 == 2 && kids[i].allele2 == 3)
                    kids[i].affected = true;
                else
                    kids[i].affected = false;
            }

        }

    }

}


public class Person{
    
    public boolean affected;
    public boolean married;
    public int gender; //1 is female and 2 is male
    public int allele1; //1 is dom, 2 is res, 3 is Y
    public int allele2;
    public boolean print;
    
    public Person(int gender, boolean affected, boolean print, int allele1, int allele2){
        
        this.gender = gender;
        this.affected = affected;
        this.allele1 = allele1;
        this.allele2 = allele2;
        this.print = print;
        
    }
    
}

public class Trait{
    
    public int type;
    
    public Trait(int type){
        this.type = type;
    }

}