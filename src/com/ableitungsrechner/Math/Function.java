package com.ableitungsrechner.Math;

import java.util.*;

public class Function {
    private String function;
    private boolean variable = false;
    //private final String ONLY_WORKS_PARTIALLY = "(?<=[+/*^()])|(?=[+/*^()-])|(?<=-(?<!((?:[*/(^+-])|(?:[sin(]|[cos(]|[tan(]) \\()-))";
    private final double Genauigkeit = 10; // Genauigkeit = auf wie viele Nachkommastelle gerundet werden soll / to how many decimal places he should round to
    public Function(String function) {
        this.function = function;
        if(function.contains("x")) variable = true;
    }
    public String getDerivative() {
        List<String> args = getArguments();
        return getDerivative(args);
    }
    public String getResult() {
        List<String> args = getArguments();

        return variable ? getDerivative(args) : Double.toString(ClauseResult(args));
    }
    private List<String> getArguments() {
        function = function.replaceAll("-","+-"); // 2-3 => 2+-3 || 2*-3 => 2*+-3
        function = function.replaceFirst("^[+]",""); // -3+3 => +-3+3 => -3+3
        String[] split = function.split("((?<=[+/*^()]|-sin|sin|-cos|cos|-tan|tan)|(?=[+/*^()]|-sin|sin|-cos|cos|-tan|tan))");
        List<String> args = new ArrayList<>(Arrays.asList(split));
        for(int i=0; i<args.size(); i++) {
            if(i>0 && args.get(i-1).matches("[*/(]|\\^") && args.get(i).equals("+")) { // 3*+-2 => 3*-2 || 3^+-2 => 3^-2
                args.remove(i);
            }
        }
        System.out.println("Splitten der Funktion: "+args);
        return args;
    }
    public String getDerivative(List<String> args) {
        if(!variable) return "0";
        List<String> derivativeList = new ArrayList<>();
        HashMap<Integer,Double> powerofeachX = new HashMap<>();
        findEachpowerofX(args,powerofeachX);
        args = ClauseResultX(args,powerofeachX);
        args.remove("(");
        args.remove(")");
        findEachpowerofX(args,powerofeachX);

        for(Map.Entry<Integer,Double> map : powerofeachX.entrySet()) {
            String x = args.get(map.getKey()); // position = map.getKey() => args.get(position) => z.B 2x
            double vorfaktor = map.getValue()*parse(findVorfaktor(x)); // (potenz = map.getValue() , (x = 2x => findVaktor(x) => 2)) => 2*2 => 4.0
            vorfaktor = (double)Math.round(vorfaktor * Math.pow(10,Genauigkeit)) / Math.pow(10,Genauigkeit);
            if(map.getValue()!=0 && !derivativeList.isEmpty()) derivativeList.add("+"); // [] => []; [4.0x^2] => [4.0x^2+]
            if(map.getValue()!=1 && map.getValue()!=0) { // potenz = map.Value() => if(potenz != 1) => [c*x^(potenz-1)] else [c]
                derivativeList.add(vorfaktor== 0 ? String.valueOf(0d) : vorfaktor+"x"); // [4.0x]
                double potenz = map.getValue() - 1; // oldpotenz = map.getValue() => potenz = oldpotenz - 1
                potenz = (double)Math.round(potenz * Math.pow(10,Genauigkeit)) / Math.pow(10,Genauigkeit);
                if(potenz != 1) {
                    derivativeList.add("^"); //[4.0x] => [4.0x,^]
                    derivativeList.add(String.valueOf(potenz)); //[4.0x,^] => [4.0x,^,potenz]
                }
            }else if(map.getValue()==1) derivativeList.add(String.valueOf(vorfaktor));

        }
        if(args.isEmpty()) return "Nicht's angegeben";
        StringBuilder fxbuild = new StringBuilder(); // derivativelist as a term
        for (String s : args) {
            fxbuild.append(s);
        }
        String fx = "f(x) = "+fxbuild.toString();
        if(derivativeList.isEmpty()) return fx+" 0";
        StringBuilder derivativebuild = new StringBuilder(); // derivativelist as a term
        for (String s : derivativeList) {
            derivativebuild.append(s);
        }
        String derivative = " f'(x) = "+derivativebuild.toString();
        return fx+derivative;
    }
    public double ClauseResult(List<String> args) {
        int index = 0;
        List<Integer> firstclausepos = new LinkedList<>();
        do{

            if(args.get(index).equals(")")) {

                int firstclauseposition = firstclausepos.get(firstclausepos.size()-1);
                String result = OperationResult(args,firstclauseposition+1,index).get(0);
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
        return args.size() == 1 ? parse(args.get(0)) : parse(OperationResult(args,0,args.size()).get(0));
    }
    public List<String> ClauseResultX(List<String> args,HashMap<Integer,Double> powerofeachX) {
        int index = 0;
        List<Integer> firstclausepos = new LinkedList<>();
        do{

            if(args.get(index).equals(")")) {
                int firstclauseposition = firstclausepos.get(firstclausepos.size()-1);

                firstclausepos.remove(firstclausepos.get(firstclausepos.size()-1));

                if(!firstclausepos.isEmpty() && index+1 < args.size() && firstclauseposition == firstclausepos.get(firstclausepos.size()-1)+1 && args.get(index+1).equals(")")) {
                    args.remove(firstclauseposition);
                    args.remove(index);
                    index = 0;
                    continue;
                }
                ArrayList<String> result = OperationResultX(args,firstclauseposition,index,powerofeachX);

                if (index > firstclauseposition) {
                    args.subList(firstclauseposition, index).clear();
                }

                for (int i = result.size() - 1; i >= 0; i--) {

                    if (i == result.size() - 1 && result.size() > 1) {
                        args.add(firstclauseposition + 1, ")");
                    }
                    args.add(firstclauseposition + 1, result.get(i));

                }
                if (result.size() > 1) {
                    args.set(firstclauseposition, "(");
                } else args.remove(firstclauseposition);


                System.out.println("Klammerauflösung: "+args);
                if(firstclausepos.isEmpty()) break;
            }
            if(index < args.size() &&args.get(index).equals("(") && !firstclausepos.contains(index)) {
                firstclausepos.add(index);
            }
            index++;
        }while(index < args.size());
        args = OperationResultX(args,0,args.size(),powerofeachX);
        return args;
    }
    private List<String> OperationResult(List<String> list, int pos1, int pos2) {
        //cloning because you can't edit sublists
        ArrayList<String> args = new ArrayList<>(list.subList(pos1,pos2));

        Trigonometry(args);
        System.out.println("Nach Trigonometrie: "+args);


        PowerResult(args);
        System.out.println("Nach Potenzrechnung: "+args);

        ProductResult(args,null);
        System.out.println("Nach Punkt vor Strich: "+args);


        AdditionResult(args,null);
        System.out.println("Nach +/-: "+args);


        return args;
    }
    private ArrayList<String> OperationResultX(List<String> list, int pos1, int pos2, HashMap<Integer,Double> powerofeachX) {
        //clonen, da man eine subListe nicht verändern kann
        ArrayList<String> args = new ArrayList<>(list.subList(pos1,pos2));
        Trigonometry(args);

        findEachpowerofX(args,powerofeachX);
        System.out.println("Nach Trigonometrie: "+args);
        PowerResult(args);

        findEachpowerofX(args,powerofeachX);

        System.out.println("Nach Potenzrechnung: "+args);
        ProductResult(args,powerofeachX);

        findEachpowerofX(args,powerofeachX);

        System.out.println("Nach Punkt vor Strich: "+args);
        AdditionResult(args,powerofeachX);

        System.out.println("Nach +/-: "+args);
        findEachpowerofX(args,powerofeachX);


        return args;
    }
    public void Trigonometry(ArrayList<String> args) {
        int index = 0;
        if(variable) {

        }else {
            do {
                double tempresult;
                switch (args.get(index)) {
                    case "-sin", "sin" -> {
                        tempresult = (args.get(index).contains("-") ? -1 : 1) * Math.sin(parse(args.get(index + 1)));
                        args.remove(index);
                        args.set(index, Double.toString(tempresult));
                        System.out.println("sin: " + args);
                    }
                    case "-cos", "cos" -> {
                        tempresult = (args.get(index).contains("-") ? -1 : 1) * Math.cos(parse(args.get(index + 1)));
                        args.remove(index);
                        args.set(index, Double.toString(tempresult));
                        System.out.println("cos: " + args);
                    }
                    case "-tan", "tan" -> {
                        tempresult = (args.get(index).contains("-") ? -1 : 1) * Math.tan(parse(args.get(index + 1)));
                        args.remove(index);
                        args.set(index, Double.toString(tempresult));
                        System.out.println("tan: " + args);
                    }
                }
                index++;
            } while (index != args.size());
        }
    }
    private void PowerResult(ArrayList<String> args) {
        int index = 0;
        if(variable) {

        }else {
            do {
                if (args.get(index).equals("^")) {
                    double tempresult = Math.pow(parse(args.get(index - 1)), parse(args.get(index + 1)));
                    args.remove(index);
                    args.remove(index);
                    args.set(index - 1, Double.toString(tempresult));
                    index = 0;
                    System.out.println("Potenzrechnung: " + args);
                }
                if (args.size() <= index) break;
                index++;
            } while (index != args.size());
        }
    }

    private void ProductResult (ArrayList<String> args, HashMap<Integer,Double> powerofeachX) {
        int index = 0;
        if(variable) {
            do {
                if(args.get(index).equals("*") || args.get(index).equals("/")) {
                    boolean mal = args.get(index).equals("*");
                    boolean potenz, potenz2;
                    potenz = index - 2 >= 0 && args.get(index - 2).equals("^"); // checks if the first factor has an exponent bigger than 1
                    potenz2 = index + 2 <= args.size()-1 && args.get(index + 2).equals("^"); // checks if the second factor has an exponent bigger than 1
                    List<String> list = new ArrayList<>();
                    List<String> list2 = new ArrayList<>();
                    int counter = 2, firstclausepos = index-(potenz ? 3 : 1) , secondclausepos = index+(potenz2 ? 3 : 1);
                    if(index > 0 && args.get(index-1).equals(")")) {
                        int amountoffirstclauses = 0;
                        while (!(amountoffirstclauses == 0 && args.get(index - counter).equals("("))) {
                            if(args.get(index-counter).equals("(")) amountoffirstclauses++;
                            else if(args.get(index-counter).equals(")")) amountoffirstclauses--;
                            list.add(args.get(index - counter));
                            counter++;
                        }
                        firstclausepos = index-counter;
                        Collections.reverse(list);
                        counter = 2;
                    }else {
                        if(potenz) {
                            list.add(args.get(index-3));
                            list.add(args.get(index-2));
                            list.add(args.get(index-1));
                        }else list.add(args.get(index-1));
                    }

                    if(index < args.size()-1 && args.get(index+1).equals("(")) {
                        int amountoffirstclauses = 0;
                        while(!(amountoffirstclauses == 0 && args.get(index+counter).equals(")"))) {
                            if(args.get(index+counter).equals("(")) amountoffirstclauses++;
                            else if(args.get(index+counter).equals(")")) amountoffirstclauses--;
                            list2.add(args.get(index + counter));
                            counter++;
                        }
                        secondclausepos = index+counter;
                    }else {
                        list2.add(args.get(index+1));
                        if(potenz2) {
                            list2.add(args.get(index+2));
                            list2.add(args.get(index+3));
                        }
                    }
                    list = OperationResultX(list,0,list.size(),findEachpowerofX(list,new HashMap<>())); //if list still has some multiplication to do => it will get solved

                    list2 = OperationResultX(list2,0,list2.size(),findEachpowerofX(list2,new HashMap<>())); // look two lines above and instead of list its list2

                    List<String> result = productResult(mal,list,list2);
                    args.add(firstclausepos,"(");
                    for(int i= firstclausepos; i<=secondclausepos; i++) {
                        args.remove(firstclausepos+1);
                    }
                    args.add(firstclausepos+1,")");
                    for(int i=result.size()-1;i>=0; i--) {
                        args.add(firstclausepos+1,result.get(i));
                    }

                    System.out.println("* or / :" + args);
                    findEachpowerofX(args,powerofeachX);
                    index = 0;
                }
            }while(++index != args.size());
        }else {
            do {
                switch (args.get(index)) {
                    case "*" -> {
                        double tempresult = parse(args.get(index - 1)) * parse(args.get(index + 1));
                        args.remove(index);
                        args.remove(index);
                        args.set(index - 1, Double.toString(tempresult));
                        index = 0;
                        System.out.println("*: " + args);
                    }
                    case "/" -> {
                        double tempresult = parse(args.get(index - 1)) / parse(args.get(index + 1));
                        args.remove(index);
                        args.remove(index);
                        args.set(index - 1, Double.toString(tempresult));
                        index = 0;
                        System.out.println("/: " + args);
                    }
                }
                index++;
            } while (index != args.size());
        }
    }

    private void AdditionResult(ArrayList<String> args, HashMap<Integer,Double> powerofeachX) {
        int index=0;
        if(variable) {

            args.remove("(");
            args.remove(")");
            findEachpowerofX(args,powerofeachX);
            //because there are no other operators except for +, the associative law of addition applies and we can remove every clause
            do {
                if (args.get(index).equals("+")) {
                    double summand1,potenz;
                    boolean potenz1;
                    if (index - 2 >= 0 && args.get(index - 2).equals("^")) {
                        summand1 = parse(findVorfaktor(args.get(index - 3)));
                        potenz = powerofeachX.get(index - 3);
                        potenz1 = false;
                    }else {
                        summand1 = parse(findVorfaktor(args.get(index - 1)));
                        potenz = powerofeachX.get(index - 1);
                        potenz1 = true;
                    }
                    int space = 0;
                    do {
                        try {
                            if(args.get(index+space).contains("x") || (index+space > 0 && !args.get(index+space-1).equals("^") && !args.get(index+space).matches("[+-/()*^]"))) { //Filterung
                                double summand2 = parse(findVorfaktor(args.get(index + space)));
                                if (potenz == powerofeachX.get(index + space)) {
                                    double tempresult = summand1 + summand2;
                                    args.set(index - (potenz1 ? 1 : 3), (tempresult == 0) ? String.valueOf((0d)) : ((potenz == 0) ? String.valueOf(tempresult) : tempresult + "x")); //falls tempresult = 0 => [3x-3x] => [0]; [3x] => tempresult+"x"  (tempresult = 3+3 = 6] => [6x]
                                    do{
                                        args.remove(index+space);
                                    }while(!(index+space == args.size()|| args.get(index+space).equals("+")));
                                    args.remove(index+space-1);

                                    System.out.println("+/-: " + args);
                                    findEachpowerofX(args, powerofeachX);
                                    index = 0;
                                    break;
                                }
                            }
                        } catch (NumberFormatException | NullPointerException ignored) {
                        }
                        space++;
                    } while (index + space < args.size());
                }
            }while(++index!=args.size());
        }else {
            do {
                if (args.get(index).equals("+")) {
                    try {
                        double tempresult = parse(args.get(index - 1)) + parse(args.get(index + 1));
                        args.remove(index);
                        args.remove(index);
                        args.set(index - 1, Double.toString(tempresult));
                        index = 0;
                        System.out.println("+: " + args);
                    } catch (Exception e) {
                        args.remove(index);
                    }
                } else if (args.get(index).equals("-")) {
                    double tempresult = parse(args.get(index - 1)) - parse(args.get(index + 1));
                    args.remove(index);
                    args.remove(index);
                    args.set(index - 1, Double.toString(tempresult));
                    index = 0;
                    System.out.println("-: " + args);
                }
                index++;
            } while (index != args.size());
        }
    }

    private double parse(String s) { return Double.parseDouble(s); }

    public String findVorfaktor(String s) {
        s = s.replace("x",""); // 2x => 2
        if(s.equals("")) s = "1"; // x => nothing => 1 (because 1x is the same as x)
        return s;
    }
    private HashMap<Integer, Double> findEachpowerofX(List<String> args, HashMap<Integer,Double> powerofeachX) {
        if(!variable) return powerofeachX;
        powerofeachX.clear();
        int index = 0;
        do{
            if(args.get(index).contains("x")) {
                if (index < args.size() - 2 && args.get(index + 1).equals("^")) { // searches after the exponent of x
                    if(args.get(index+2).equals("(")) { // if x^(..)
                        List<String> klammerloesung = new ArrayList<>();
                        int counter = 2;
                        while (!args.get(index + counter + 1).equals(")")) { //extracts the content inside the clauses [4x^(3+1)] => [3+1]
                            counter++;
                            klammerloesung.add(args.get(index + counter));
                        }
                        String loesung = OperationResultX(klammerloesung,0,klammerloesung.size(),new HashMap<>()).get(0); // 2+3 => 5 => loesung = 5
                        powerofeachX.put(index,parse(loesung));

                    }else { //if x^c while c is just a number
                        powerofeachX.put(index, parse(args.get(index + 2))); // c will just get inputed into the hashmap

                    }
                } else {
                    powerofeachX.put(index, 1d);
                }
            }else if(!args.get(index).matches("[-+()/^]")) {
                powerofeachX.put(index,0d);
            }
        }while(++index != args.size());
        return powerofeachX;
    }
    public List<String> productResult(boolean mal,List<String> list, List<String> list2) {
        HashMap<Integer,Double> powerofeachXoflist = new HashMap<>();
        findEachpowerofX(list,powerofeachXoflist);
        HashMap<Integer,Double> powerofeachXoflist2 = new HashMap<>();
        findEachpowerofX(list2,powerofeachXoflist2);
        List<String> res = new ArrayList<>();
        for(int i=0; i<list.size(); i++) {
            double faktor1, potenz1;
            if(list.get(i).contains("x") || (i > 0 && !list.get(i-1).equals("^") && !list.get(i).matches("[+-/*()^]")) || i==0) { // [2x] gets accepted but from [2x^2] the [2] won't get accepted because it is follows a ^; the i==0 is there because the first
                faktor1 = parse(findVorfaktor(list.get(i)));
                potenz1 = powerofeachXoflist.get(i);
            }else continue;
            for(int j=0; j<list2.size(); j++) {
                double faktor2, potenz2;
                if(list2.get(j).contains("x") || (j > 0 && !list2.get(j-1).equals("^") && !list2.get(j).matches("[+-/*()^]")) || j==0) {
                    faktor2 = parse(findVorfaktor(list2.get(j)));
                    potenz2 = powerofeachXoflist2.get(j);
                    double potenz = potenz1+(mal ? potenz2 : -1*potenz2); // the -1*potenz2 => only works for numbers not variables
                    double tempresult = faktor1 * (mal ? faktor2 : 1/faktor2); //the 1/faktor2 => only works for numbers not variables
                    if(tempresult != 0 && !res.isEmpty()) res.add("+");

                    if(potenz == 1) {
                        res.add(tempresult+"x");
                    }else {
                        if(potenz != 0) {
                            res.add(tempresult+"x");
                            res.add("^");
                            res.add(String.valueOf(potenz));
                        }else res.add(String.valueOf(tempresult));
                    }
                }
            }
        }
        return res;
    }
}
