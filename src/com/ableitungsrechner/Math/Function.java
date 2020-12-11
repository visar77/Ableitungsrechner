package com.ableitungsrechner.Math;

import java.util.*;

public class Function {
    private final String function;
    private boolean variable = false;
    //private final String ONLY_WORKS_PARTIALLY = "(?<=[+/*^()])|(?=[+/*^()-])|(?<=-(?<!((?:[*/(^+-])|(?:[sin(]|[cos(]|[tan(]) \\()-))";
    public Function(String function) {
        this.function = function;
        if(function.contains("x")) variable = true;
    }
    public String getDerivative() {
        List<String> args = getArguments();
        return getDerivative(args);
    }
    public String getDerivative(List<String> args) {

        List<String> derivativeList = new ArrayList<>();
        HashMap<Integer,Double> powerofeachX = new HashMap<>();
        findEachpowerofX(args,powerofeachX);

        ArrayList<String> newargs = new ArrayList<>(args);
        args = AdditionResult(newargs,powerofeachX);
        findEachpowerofX(args,powerofeachX);


        for(Map.Entry<Integer,Double> map : powerofeachX.entrySet()) {
            String x = args.get(map.getKey()); // position = map.getKey() => args.get(position) => z.B 2x
            double vorfaktor = map.getValue()*parse(findVorfaktor(x)); // (potenz = map.getValue() , (x = 2x => findVaktor(x) => 2)) => 2*2 => 4.0
            if(map.getValue()!=0 && !derivativeList.isEmpty()) derivativeList.add("+"); // [] => []; [4.0x^2] => [4.0x^2+]
            if(map.getValue()!=1 && map.getValue()!=0) { // potenz = map.Value() => if(potenz != 1) => [c*x^(potenz-1)] else [c]
                derivativeList.add(vorfaktor== 0 ? String.valueOf(0d) : vorfaktor+"x"); // [4.0x]
                double potenz = map.getValue() - 1; // oldpotenz = map.getValue() => potenz = oldpotenz - 1
                if(potenz != 1) {
                    derivativeList.add("^"); //[4.0x] => [4.0x,^]
                    derivativeList.add(String.valueOf(potenz)); //[4.0x,^] => [4.0x,^,potenz]
                }
            }else if(map.getValue()==1) derivativeList.add(String.valueOf(vorfaktor));

        }
        if(derivativeList.isEmpty()) return "0";
        StringBuilder derivative = new StringBuilder();
        for (String s : derivativeList) {
            derivative.append(s);
        }

        return derivative.toString();

    }
    private List<String> getArguments() {
        //function = function.replaceAll("-(?! )","+-");
        String[] split = function.split("((?<=[+/*^()]|-sin|sin|-cos|cos|-tan|tan)|(?=[+/*^()]|-sin|sin|-cos|cos|-tan|tan))");
        List<String> args = new ArrayList<>(Arrays.asList(split));
        System.out.println("Splitten der Funktion: "+args);
        return args;
    }
    public String getResult() {
        List<String> args = getArguments();

        return variable ? getDerivative(args) : Double.toString(ClauseResult(args));
    }
    public void Trigonometry(ArrayList<String> args) {
        int index = 0;
        do{
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
    /*public List<String> ClauseResultX(List<String> args) {
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
        
        return args;
    }

     */
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
    private ArrayList<String> AdditionResult(ArrayList<String> args, HashMap<Integer,Double> powerofeachX) {
        int index=0;
        if(variable) {
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
                                if(args.get(index+space).contains("x")) {
                                    double summand2 = parse(findVorfaktor(args.get(index + space)));

                                    if (potenz == powerofeachX.get(index + space)) {
                                        double tempresult = summand1 + summand2;
                                        args.set(index - (potenz1 ? 1 : 3), (tempresult == 0) ? String.valueOf((0d)) : ((potenz == 0) ? String.valueOf(tempresult) : tempresult + "x")); //[3x] => tempresult+"x"  (tempresult = 3+3 = 6] => [6x]
                                        do{
                                            args.remove(index+space);
                                        }while(!(index+space == args.size()|| args.get(index+space).equals("+")));
                                        args.remove(index);
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
        return args;
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
        AdditionResult(args,null);
        System.out.println("Nach +/-: "+args);

        return parse(args.get(0));
    }

    private double parse(String s) { return Double.parseDouble(s); }
    public String findVorfaktor(String s) {
        s = s.replace("x","");
        return s;
    }
    private void findEachpowerofX(List<String> args,HashMap<Integer,Double> powerofeachX) {
        powerofeachX.clear();
        int index = 0;
        do{
            if(args.get(index).contains("x")) {
                if (index < args.size() - 2 && args.get(index + 1).equals("^")) {
                    powerofeachX.put(index,parse(args.get(index + 2)));
                } else {
                    powerofeachX.put(index, 1d);
                }
            }else if(!args.get(index).matches("[-+/^]")) {
                powerofeachX.put(index,0d);
            }
        }while(++index != args.size());
    }
}
