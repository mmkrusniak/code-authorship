package com.github.compling;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

public class App {
    public static void main(String[] args) {
        try {
            String dataPath = "data";
            File dir = new File(dataPath);
            File[] files = dir.listFiles();
            if (files != null) {
                for(File subdir: files) for(File file: subdir.listFiles()) {
                    ArrayList<String> resultsArray = new ArrayList<String>();
                    resultsArray.add(toJSON(JavaParser.parse(file), 0));
                    Path outFile = Paths.get("out" + File.separator + subdir.getName() + File.separator + file.getName().replace(".java", ".json"));
                    Files.write(outFile, resultsArray, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toJSON(Node root, int depth) {
        java.util.List<Node> children = root.getChildNodes();

        String name = root.getClass().getSimpleName();
        String result = repeat("  ", depth) + "{\"name\": \"" + name + "\"";

        for(int i = 0; i < children.size(); i++) {
            if(i==0) result += ", \"children\": [\n";
            result += toJSON(children.get(i), depth+1);
            if(i!=children.size()-1) result +=", \n";
            else result += "\n" + repeat("  ", depth) + "]";
        }
        return result + "}";
    }

    public static String repeat(String s, int t) {
        return new String(new char[t]).replace("\0", s);
    }
}