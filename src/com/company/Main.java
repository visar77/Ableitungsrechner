package com.company;

import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    public static void main(String[] args) {

        while(true) {
            System.out.println("Gib die Gleichung an:");
            Graph g = new Graph(in.next());
            System.out.println("Ergebnis: "+g.getResult());
        }
    }
}
