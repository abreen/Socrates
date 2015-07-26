package io.breen.socrates;

import java.io.FileInputStream;
import java.util.Map;

import org.yaml.snakeyaml.*;

public class Socrates {

    public static UserInput userInput;

    public static void main(String[] args) throws Exception {
        userInput = new CommandLineUserInput();

        Yaml yaml = new Yaml(new SocratesConstructor());

        java.io.File f = new java.io.File("/Users/abreen/ps0a.yml");
        Map map = (Map)yaml.load(new FileInputStream(f));
        //Criteria c = new Criteria(map);
        System.out.println(map);
    }
}