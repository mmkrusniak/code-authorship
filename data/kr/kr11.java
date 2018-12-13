/**
 * PEDIGREE CHART CREATOR by Karl Ramberg
 * 4 December 2015
 * 
 * This class stores information for each individual person on the chart.
 */

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