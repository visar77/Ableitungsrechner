package com.ableitungsrechner.Math;

import java.util.*;

public class Function {
    private String function;
    private final String ONLY_WORKS_PARTIALLY = "(?<=[+/*^()])|(?=[+/*^()-])|(?<=-(?<!((?:[*/(^+-])|(?:[sin(]|[cos(]|[tan(]) \\()-))";
    public Function(String function) {
        this.function = function;
    }

    public String getDerivative() { return null; }
    private List<String> getArguments() {
        function = function.replaceAll("-","+-1*");
        String[] split = function.split("((?<=[+/*^()]|sin|cos|tan)|(?=[+/*^()]|sin|cos|tan))");

        List<String> args = new ArrayList<>(Arrays.asList(split));
        System.out.println("Splitten der Funktion: "+args);
        return args;

    }
    public double getResult() {
        List<String> args = getArguments();

        return ClauseResult(args);
    }
    public void Trigonometry(ArrayList<String> args) {
        int index = 0;
        do{
            switch (args.get(index)) {
                case "sin" -> {
                    double tempresult = Math.sin(parse(args.get(index + 1)));
                    args.remove(index);
                    args.set(index, Double.toString(tempresult));
                    System.out.println("sin: " + args);
                }
                case "cos" -> {
                    double tempresult = Math.cos(parse(args.get(index + 1)));
                    args.remove(index);
                    args.set(index, Double.toString(tempresult));
                    System.out.println("cos: " + args);
                }
                case "tan" -> {
                    double tempresult = Math.tan(parse(args.get(index + 1)));
                    args.remove(index);
                    args.set(index, Double.toString(tempresult));
                    System.out.println("tan: " + args);
                }
            }
            index++;
        }while(index != args.size());
    }
    public double ClauseResult(List<String> args) {
        int index = 0;
        List<Integer> firstclausepos = new LinkedList<>();
        do{

            if(args.get(index).equals(")")) {

                int firstclauseposition = firstclausepos.get(firstclausepos.size()-1);
                String result = Double.toString(OperationResult(args,firstclauseposition+1,index));
                firstclausepos.clear();
                if (index > firstclauseposition) {
                    args.subList(firstclauseposition, index).clear();
                }
                args.set(firstclauseposition,result);
                System.out.println("Klammerauflösung: "+args);
                index = 0;

            }
            if(args.get(index).equals("(")) {
                firstclausepos.add(index);
            }
            index++;
        }while(index != args.size());
        return args.size() == 1 ? parse(args.get(0)) : OperationResult(args,0,args.size());
    }
    private void PowerResult(ArrayList<String> args) {
        int index = 0;
        do{
            if (args.get(index).equals("^")) {
                double tempresult = Math.pow(parse(args.get(index - 1)),parse(args.get(index + 1)));
                args.remove(index);
                args.remove(index);
                args.set(index - 1, Double.toString(tempresult));
                index = 0;
                System.out.println("Potenzrechnung: "+args);
            }
            if(args.size() <= index) break;
            index++;
        }while(index != args.size());
    }
    private void AdditionResult(ArrayList<String> args) {
        int index=0;
        do {
            if(args.get(index).equals("+")) {
                double tempresult = parse(args.get(index - 1)) + parse(args.get(index + 1));
                args.remove(index);
                args.remove(index);
                args.set(index - 1, Double.toString(tempresult));
                index = 0;
                System.out.println("+: " + args);
            }else if(args.get(index).equals("-")) {
                double tempresult = parse(args.get(index-1)) - parse(args.get(index + 1));
                args.remove(index);
                args.remove(index);
                args.set(index-1, Double.toString(tempresult));
                index = 0;
                System.out.println("-: " + args);
            }
            index++;
        }while(index != args.size());
    }
    private void ProductResult (ArrayList<String> args) {
        int index = 0;
        do {
            switch (args.get(index)) {
                case "*" -> {
                    double tempresult = parse(args.get(index-1)) * parse(args.get(index+1));
                    args.remove(index);
                    args.remove(index);
                    args.set(index-1,Double.toString(tempresult));
                    index = 0;
                    System.out.println("*: "+args);
                }
                case "/" -> {
                    double tempresult = parse(args.get(index-1)) / parse(args.get(index+1));
                    args.remove(index);
                    args.remove(index);
                    args.set(index-1,Double.toString(tempresult));
                    index = 0;
                    System.out.println("/: "+args);
                }
            }
            index++;
        } while (index != args.size());
    }
    private double OperationResult(List<String> list, int pos1, int pos2) {
        //clonen, da man eine subListe nicht verändern kann
        ArrayList<String> args = new ArrayList<>(list.subList(pos1,pos2));
        //System.out.println("Vor Trigonometrie: "+args);
        Trigonometry(args);
        System.out.println("Nach Trigonometrie: "+args);
        //System.out.println("Vor Potenzrechnung: "+args);
        PowerResult(args);
        System.out.println("Nach Potenzrechnung: "+args);
        //System.out.println("Vor Punkt vor Strich: "+args);
        ProductResult(args);
        System.out.println("Nach Punkt vor Strich: "+args);
        //System.out.println("Vor +/-: "+args);
        AdditionResult(args);
        System.out.println("Nach +/-: "+args);

        return parse(args.get(0));
    }

    private double parse(String s) { return Double.parseDouble(s); }
}
