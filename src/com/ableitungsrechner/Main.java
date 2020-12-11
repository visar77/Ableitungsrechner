package com.ableitungsrechner;

import com.ableitungsrechner.Math.Function;

import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    public static void main(String[] args) {

        while(true) {
            System.out.println("Gib die Gleichung an:");
            Function g = new Function(in.next());
            System.out.println("Ergebnis: "+g.getDerivative());
        }
    }
}
