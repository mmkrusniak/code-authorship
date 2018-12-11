package com.github.compling;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
        JavaParser parser = new JavaParser();
        System.out.println(toJSON(parser.parse("import java.util.List;class Test {  public static void main(String[] args) {    double n = 1.0;    double m = 2.0;    System.out.println(perlin(n, m));  }  public static double perlin(double x, double y) {        double x0 = 5*x;        double x1 = x0 + 1;        double y0 = 5*y;        double y1 = y0 + 1;        return 8.0;    }}")));
    }

    public static String toJSON(Node root) {
        java.util.List<Node> children = root.getChildNodes();

        String name = root.getClass().getSimpleName();

        String result = "{\"name\": \"" + name + "\"";
        
        for(int i = 0; i < children.size(); i++) {
            if(i==0) result += ", \"children\": [";
            result += toJSON(children.get(i));
            if(i!=children.size()-1) result +=", ";
            else result += "]";
        }
        return result + "}";
    }
}
