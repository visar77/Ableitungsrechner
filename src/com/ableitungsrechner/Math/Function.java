package com.ableitungsrechner.Math;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Function {
    private String function;
    private boolean variable = false;
    private final double ACCURACY = 10; // Genauigkeit = auf wie viele Nachkommastelle gerundet werden soll / to how many decimal places the code will round to
    public Function(String function) {
        this.function = function;
        if(function.contains("x")) variable = true;
    }
    public String getDerivative() throws DividingbyNullException{
        List<String> args = getArguments();
        return getDerivative(args);
    }
    public String  getResult() throws DividingbyNullException {
        List<String> args = getArguments();
        args = ClauseResultX(args);
        StringBuilder argsbuild = new StringBuilder();
        for(String s : args) {
            argsbuild.append(s);
        }
        return argsbuild.toString();
    }
    private List<String> getArguments() {
        function = function.replaceAll(" ","");
        function = function.replaceAll("-","+-"); // 2-3 => 2+-3 || 2*-3 => 2*+-3
        function = function.replaceFirst("^[+]",""); // -3+3 => +-3+3 => -3+3
        String[] split = function.split("((?<=[+/*^()]|-sin|sin|-cos|cos|-tan|tan)|(?=[+/*^()]|-sin|sin|-cos|cos|-tan|tan))");
        List<String> args = new ArrayList<>(Arrays.asList(split));
        checkandsolvemultiplesigns(args);
        for(int i=0; i<args.size(); i++) {
            if(i>0 && args.get(i-1).matches("[*/+(]|\\^") && args.get(i).equals("+")) { // 3*+-2 => 3*-2 || 3^+-2 => 3^-2
                args.remove(i);
                i = 0;
            }
            if(args.get(i).equals("-")) {
                args.remove(i);
                if(i > 0) {
                    if(args.get(i-1).equals("^")) {
                        args.remove(i);
                        args.add(i+1,")");
                        args.addAll(i, Arrays.asList("(","(","-1",")","*"));
                        i = -1;
                        continue;
                    }else {
                        try {
                            findVorfaktor(args.get(i - 1));
                            args.addAll(i, Arrays.asList("+", "(", "-1", ")", "*"));
                            i = -1;
                            continue;
                        } catch (NumberFormatException ignored) {
                        }
                    }

                }
                args.addAll(i, Arrays.asList("(", "-1", ")", "*"));
                i = 0;
            }
        }
        System.out.println("Splitten der Funktion: "+args);
        return args;
    }
    private void checkandsolvemultiplesigns(List<String> args) {

        for(int i=0; i<args.size(); i++) {

            if(args.get(i).equals("+")) {
                if(i < args.size()-1) {
                    if(args.get(i+1).equals("+")) {
                        args.remove(i+1);
                        i = 0;
                    }else if(args.get(i+1).equals("-")) {
                        args.remove(i+1);
                        args.set(i,"-");
                        i = 0;
                    }
                }
            }else if(args.get(i).equals("-")) {
                if(i < args.size() - 1) {
                    if (args.get(i + 1).equals("+")) {
                        args.remove(i + 1);
                        i = 0;
                    } else if (args.get(i + 1).equals("-")) {
                        args.remove(i + 1);
                        args.set(i, "+");
                        i = 0;
                    }
                }
            }
        }
    }
    public String getDerivative(List<String> args) throws DividingbyNullException {

        HashMap<Integer,Double> powerofeachX = new HashMap<>();
        args = ClauseResultX(args);
        findEachpowerofX(args,powerofeachX);
        StringBuilder fxbuild = new StringBuilder(); // args as a term
        for (String s : args) {
            fxbuild.append(s);
        }
        if(!variable) {
            System.out.println("f(x) = " + fxbuild.toString() + "  f'(x) = 0");
            return "f(x) = "+ fxbuild.toString()+"  f'(x) = 0";
        }
        List<String> derivativeList = new ArrayList<>(args);

        if(derivativeList.contains("/")) {
            derivativeList = QuotientRuleDerivative(derivativeList);
        }else derivativeList = getEachDerivative(derivativeList,powerofeachX);
        derivativeList = ClauseResultX(derivativeList);
        String fx = "f(x) = "+fxbuild.toString();
        if(derivativeList.isEmpty()) return fx+" f'(x) = 0";
        StringBuilder derivativebuild = new StringBuilder(); // derivativelist as a term
        for (String s : derivativeList) {
            derivativebuild.append(s);
        }
        String derivative = "  f'(x) = "+derivativebuild.toString();
        System.out.println(fx + derivative);
        return fx+derivative;
    }
    public List<String> QuotientRuleDerivative(List<String> derivative) throws DividingbyNullException {
        //To explain all of this in detail would be too much to handle (because it sure was for me)
        //But in summary:
        //The program finds the fraction and creates two lists : the dividend and the divisor
        //He then gets the derivative of both lists and multiplies the dividend with the derivative of the divisor (derivativeDividendPart2) and the divisor with the derivative of the dividend (derivativeDividendPart1)
        //After that he subtracts derivativeDividendPart2 to derivativeDividendPart1 and gets a the new derivative dividend => derivativeDividendComplete
        //After that he takes the square of the divisor (divisorSquared) and replaces the dividend with derivativeDividendComplete and the divisor with divisorSauared
        //Summary of the summary => if f(x) = v(x)/u(x) => then f'(x) = (v'(x)*u(x)-u'(x)*v(x))/((u(x))^2)
        int index = 0;
        do {
            if(derivative.get(index).equals("/")) {
                List<String> divisor = new ArrayList<>();
                int counter = derivative.get(index+1).equals("(") ? 2 : 1;
                while(!derivative.get(index+counter).equals(")")) {
                    divisor.add(derivative.get(index+counter));
                    derivative.remove(index+counter);
                }
                List<String> derivativeDivisor = getEachDerivative(divisor,findEachpowerofX(divisor,new HashMap<>()));
                List<String> dividend = new ArrayList<>();
                counter = 2;
                for(int i=index-counter; i>=0; i--) {
                    if(derivative.get(i).equals("(")) break;
                    dividend.add(derivative.get(i));
                    derivative.remove(i);
                }
                Collections.reverse(dividend);
                int dividendOpeningClause = index-counter-dividend.size();
                List<String> derivativeDividend = getEachDerivative(dividend,findEachpowerofX(dividend,new HashMap<>()));
                List<String> derivativeDividendPart2 = productResult(true,derivativeDividend,divisor);
                derivativeDividendPart2 = ClauseResultX(derivativeDividendPart2);
                List<String> derivativeDividendPart1 = productResult(true,derivativeDivisor,dividend);
                derivativeDividendPart1 = ClauseResultX(derivativeDividendPart1);
                List<String> divisorSquared = productResult(true,divisor,divisor);
                divisorSquared = ClauseResultX(divisorSquared);
                List<String> derivativeDividendComplete = new ArrayList<>(derivativeDividendPart2);
                derivativeDividendComplete.addAll(derivativeDividendComplete.size(), Arrays.asList("+","(","-1",")","*","(",")"));
                derivativeDividendComplete.addAll(derivativeDividendComplete.size()-1,derivativeDividendPart1);
                derivativeDividendComplete = ClauseResultX(derivativeDividendComplete);
                int divisorOpeningClause = dividendOpeningClause + derivativeDividendComplete.size() + counter + 1;
                derivative.addAll(dividendOpeningClause + 1,derivativeDividendComplete);
                derivative.addAll(divisorOpeningClause + 1, divisorSquared);
                break;
            }
        }while(++index < derivative.size());

        return derivative;
    }
    private List<String> getEachDerivative(List<String> args, HashMap<Integer,Double> powerofeachX) { //takes a bit from a list and takes the derivative
        List<String> derivative = new ArrayList<>();
        for(int i=0; i<args.size(); i++) {
            //[4x^3+2x^2] => [4x] => valid
            //[4x^3+2x^2] => [3] => not valid
            if(i == 0 || (!args.get(i-1).equals("^"))) {
                //this checks if the position is part of the HashMap, because if it is not that means that the String in this position is a string and so the program ignores it and moves on
                if(!powerofeachX.containsKey(i)) continue;
                double power = powerofeachX.get(i);
                if(!derivative.isEmpty()) {
                    derivative.add("+");
                }
                if(power == 0) {
                    derivative.add("0");
                    continue;
                }
                if (power != 1) {
                    double vorfaktor = power * parse(findVorfaktor(args.get(i)));
                    vorfaktor = Math.round(vorfaktor * Math.pow(10, ACCURACY)) / Math.pow(10, ACCURACY);
                    derivative.add(vorfaktor + "x");
                    if (powerofeachX.get(i) - 1 != 1) {
                        derivative.add("^");
                        derivative.add(String.valueOf(power - 1));
                    }
                } else {
                    if (args.get(i).contains("x")) {
                        derivative.add(findVorfaktor(args.get(i)));
                    }
                }
            }
        }
        return derivative;
    }
    public List<String> ClauseResultX(List<String> args) throws DividingbyNullException {
        int index = 0;
        args.add(0,"(");
        args.add(args.size() ,")");
        List<Integer> firstClausePosList = findEachOpeningClause(args,index);
        do{
            if(args.get(index).equals("(")) {
                firstClausePosList.add(index);
            }
            if(args.get(index).equals(")")) {

                //this checks if there are unnecessary clauses like ((2x+2))*(2x+1) => and deletes them => (2x+2)*(2x+1)
                if(firstClausePosList.size() > 1 && index < args.size()-1) {
                    int firstclause = firstClausePosList.get(firstClausePosList.size()-1); //gets position of firstclause => in the example above: 1
                    int duplicateclause = firstClausePosList.get(firstClausePosList.size()-2); //gets position of duplicateclause => in the example above: 0
                    if(firstclause-1 == duplicateclause && args.get(index+1).equals(")")) { //checks if there is a closing duplicate clause
                        args.remove(duplicateclause); //((2x+2))*(2x+1) => (2x+2))*(2x-1)
                        args.remove(index);// (2x+2))*(2x-1) => (2x+2)*(2x-1)
                        firstClausePosList.remove(firstClausePosList.size()-1); //deletes the unnecessary clause from the list
                        index--;
                        continue;
                    }
                }
                //takes the last clause, becuase => (((2x+x)-3)*3)+200 => the last "(" will be always closed by the first ")"
                int positionofFirstClause = firstClausePosList.get(firstClausePosList.size()-1);
                firstClausePosList.remove(firstClausePosList.size()-1);

                //Now a subList from the position of the opening clause + 1 to the index/position of the closing clause will be created to solve (2x+x) as much as possible
                //this will create a subList without the clauses => (((2x+x)-3)*3)+200 => 2x+x
                List<String> subList = new ArrayList<>(args.subList(positionofFirstClause+1,index));
                //the subList will get solved then

                OperationResultX(subList, findEachpowerofX(subList, new HashMap<>()));

                //checks if the clauses are needed, the criteria is as follows:
                //1.If the clauses don't come after a "^", because 3x^(3+2)*3 => 3x^5*3 => clauses not needed
                //2. If the clauses are followed by a "*","/" or a "^" the clauses are needed => (x+1)*(x-1) => clauses are needed OR (x+1)^2 => the clauses of (x+1) are needed
                //  OR If the clauses come after a "*" or a "/" the clauses are needed => you can take the example from above, but this time (x-1) is the clause being checked
                boolean clauseneeded = positionofFirstClause-1 >= 0 && !args.get(positionofFirstClause-1).equals("^") && ((args.get(positionofFirstClause-1).matches("[*/]"))||(index+1 < args.size() && args.get(index+1).matches("[*/^]")));
                //the removing process => (((2x+x)-3)*3)+200 =>  ((-3)*3)+200
                args.subList(positionofFirstClause, index+1).clear();
                //now the adding process => ((-3)*3)+200 => ((3x-3)*3)+200

                if(clauseneeded) {
                    args.add(positionofFirstClause ,")");
                    args.add(positionofFirstClause,"(");
                }
                index=positionofFirstClause+subList.size()+(clauseneeded ? 1  : -1);
                args.addAll(positionofFirstClause+(clauseneeded ? 1 : 0),subList);

                System.out.println("Nach Klammernauflösung: "+args);
            }
            index++;
        }while(index < args.size());

        return args;
    }
    private List<String> OperationResultX(List<String> args, HashMap<Integer,Double> powerofeachX) throws DividingbyNullException {
        //cloning because you can't edit sublists
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
    public void Trigonometry(List<String> args) {
        int index = 0;
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
        } while (index < args.size());
    }
    private void  PowerResult(List<String> args) {
        int index = 0;
        do {
            if (index > 0 && args.get(index).equals("^") && !args.get(index - 1).contains("x")) {
                if (!args.get(index-1).equals(")")) {
                    double tempresult = Math.pow(parse(args.get(index - 1)), parse(args.get(index + 1)));
                    args.remove(index);
                    args.remove(index);
                    args.set(index - 1, Double.toString(tempresult));
                    index = 0;
                }else {
                    double exponent = parse(args.get(index + 1));
                    //if the exponent is 0 then it will remove the whole term and substitute it with 1
                    if(exponent == 0) {
                        int counter = 2;
                        int amountoffirstclauses = 0;
                        while(!(amountoffirstclauses == 0 && args.get(index-counter).equals("("))) {
                            if (args.get(index - counter).equals("(")) amountoffirstclauses++;
                            else if (args.get(index - counter).equals(")")) amountoffirstclauses--;
                            counter++;
                        }
                        args.subList(index-counter ,index+2).clear();
                        args.add(index-counter ,"1");
                        index = -1;
                        continue;
                    }
                    if (exponent % 1 == 0 ) { // if the exponent is an integer and the c of c^exponent does not contain x
                        //first the exponent and the exponent operator need to get deleted
                        int counter = 2;
                        int amountoffirstclauses = 0;
                        int closingClause = index-counter + 1;
                        List<String> basisList = new ArrayList<>();
                        basisList.add(args.get(index-counter+1));
                        while(!(amountoffirstclauses == 0 && args.get(index-counter).equals("("))) {
                            if (args.get(index - counter).equals("(")) amountoffirstclauses++;
                            else if (args.get(index - counter).equals(")")) amountoffirstclauses--;
                            basisList.add(args.get(index - counter));
                            counter++;
                        }
                        basisList.add(args.get(index-counter));
                        int openingClause = index-counter;
                        Collections.reverse(basisList);
                        int space = 0;
                        if(exponent < 0) {
                            args.addAll(openingClause,Arrays.asList("(","1",")","/","("));
                            space=5;
                            args.add(index+space+2, ")");
                        }
                        args.remove(index+space);
                        args.remove(index+space);
                        if (args.get(index + space - 1).equals(")")) {
                            for (int i = 1; i < Math.abs(exponent);i++) { // repeats it exponent-1 times
                                //this whole following section adds (c)^3 => c*c*c; c can be anything: an integer, a clause everything
                                args.add(closingClause + space +1, "*");
                                args.addAll(closingClause + space + 2,basisList);
                                closingClause += basisList.size() + 1;
                            }
                        }
                        index = -1;
                    }
                }
            }
        }while(++index < args.size());
    }

    private void ProductResult (List<String> args, HashMap<Integer,Double> powerofeachX) throws DividingbyNullException {
        int index = 0;

            do {
                if(args.get(index).matches("[*/]")) {
                    boolean mal = args.get(index).equals("*");
                    List<String> list = new ArrayList<>();
                    int counter = 2;
                    int amountoffirstclauses = 0;
                    if(args.get(index+1).equals("(")) {
                        while (!(amountoffirstclauses == 0 && args.get(index + counter).equals(")"))) {
                            if (args.get(index + counter).equals("(")) amountoffirstclauses++;
                            else if (args.get(index + counter).equals(")")) amountoffirstclauses--;
                            list.add(args.get(index + counter));
                            counter++;
                        }
                    }else {
                        boolean exponent2 = index + 2 <= args.size() - 1 && args.get(index + 2).equals("^");
                        if(exponent2) {
                            list.add(args.get(index+1));
                            list.add(args.get(index+2));
                            list.add(args.get(index+3));
                        }else list.add(args.get(index+1));
                        counter = exponent2 ? 3 : 1;
                    }
                    System.out.println("list " + list);
                    int closingClause = index+counter;
                    List<String> list2 = new ArrayList<>();
                    counter = 2;
                    if(args.get(index-1).equals(")")) {
                        while (!(amountoffirstclauses == 0 && args.get(index - counter).equals("("))) {
                            if (args.get(index - counter).equals("(")) amountoffirstclauses++;
                            else if (args.get(index - counter).equals(")")) amountoffirstclauses--;
                            list2.add(args.get(index - counter));
                            counter++;
                        }
                        Collections.reverse(list2);
                    }else {
                        boolean exponent = index-2 > 0 && args.get(index-2).equals("^");
                        if(exponent) {
                            list2.add(args.get(index-3));
                            list2.add(args.get(index-2));
                            list2.add(args.get(index-1));
                        }else list2.add(args.get(index-1));
                        counter = exponent ? 3 : 1;
                    }
                    System.out.println("list2 " + list2);
                    int openingClause = index-counter;
                    if(list2.size() == 1 && parse(findVorfaktor(list2.get(0))) == 0d) {
                        args.subList(openingClause,closingClause+1).clear();
                        args.add(openingClause,"0");
                        index = -1;
                        continue;
                    }
                    if(!mal && list.size() == 1 && parse(findVorfaktor(list.get(0))) == 0d) {
                        throw new DividingbyNullException("You can't divide by null!");
                    }
                    if(mal || (list.size() == 1 || (list.size() == 3 && list.contains("^")))) {
                        List<String> result = productResult(mal, list2, list);
                        boolean clauseneeded =  (openingClause-1 > 0 && args.get(openingClause-1).matches("[*/^]")) || ( closingClause+1 < args.size() && args.get(closingClause+1).matches("[*/^]"));
                        args.subList(openingClause,closingClause+1).clear();
                        if(clauseneeded) {
                            args.addAll(openingClause,Arrays.asList("(",")"));
                        }
                        System.out.println("args " + args);
                        args.addAll(openingClause + (clauseneeded ? 1 : 0),result);
                        System.out.println("args " + args);
                    }else {
                        index = closingClause;
                    }

                }
            }while(++index < args.size());
    }

    private void AdditionResult(List<String> args, HashMap<Integer,Double> powerofeachX) throws DividingbyNullException {
       int index = 0;
       do {
           if(args.get(index).equals("(")) {
               int timesdone = 0;
               int setindex = index;
               for(int i=setindex+1; i<args.size(); i++) {
                   if(args.get(i).equals(")")) {
                       timesdone++;
                       if(timesdone == 2) {
                           index++;
                           break;
                       }
                   }
                   index++;
               }
               if(index == args.size()-1) break;
           }
           if(index > 0 && args.get(index).matches("[+-]")) {
               boolean solved = false;
               boolean addition = args.get(index).equals("+");
               if(!args.get(index-1).equals(")")) {
                   boolean exponent1 = index >= 3 && args.get(index - 2).equals("^"); // checks if the first summand has an exponent bigger than 1
                   double summand1 = parse(findVorfaktor(args.get(index - (exponent1 ? 3 : 1))));
                   double potenz1 = powerofeachX.get(index - (exponent1 ? 3 : 1));


                   //after he has the first summand, he needs to get the second one, but he needs to find a valid summand with the same exponent
                   //The Rules are as follows:
                   //1.Go through the whole list and find a valid summand
                   //2.Check if the found summand has the same exponent
                   //3.If it does have the same exponent (or in this case labeled "potenz") then add them both together add the result to the list and set solved to true so it moves on
                   //  If NOT then keep searching until you find a summand with the same exponent => solved stays false and he goes to the next part


                   int space = 1;
                   while (index + space < args.size()) {
                       //Finding a valid summand
                       if(args.get(index + space).equals("(")) {
                           int timesdone = 0;
                           for(int i=index+space+1; i<args.size(); i++) {
                               if(args.get(i).equals(")")) {
                                   timesdone++;
                                   if(timesdone == 2) {
                                       space++;
                                       break;
                                   }
                               }
                               space++;
                           }
                           if(index+space >= args.size()) break;
                       }
                       if (!args.get(index + space - 1).equals("^") && !args.get(index + space).matches("[+/()^-]")) { // as long as a term is not followed by a "^" and is not "+" or "(" or "/" or ") or "^" or "-", it passes as a potential summand

                           double summand2 = parse(findVorfaktor(args.get(index + space)));

                           double potenz2 = powerofeachX.get(index + space);


                           if (potenz2 != potenz1) {
                               space++;
                               continue;
                           }

                           double result = summand1 + (addition ? 1*summand2 : -1*summand2);
                           int exponentbump = (potenz1 != 0 && potenz1 != 1 ? 2 : 0); //if we want the sublist we need to take the exponent into account for exponent that are neither 0 or 1
                           //removing process

                           args.subList(index + space - 1, index + space + exponentbump + 1).clear();
                           //adding process
                           if (result == 0) {
                               if(exponent1) {
                                   args.remove(index - 1);
                                   args.remove(index - 2);
                               }
                               args.set(index - (exponent1 ? 3 : 1), String.valueOf(result));
                               solved = true;
                               break;
                           }
                           if (potenz1 == 0 || potenz1 == 1) {
                               args.set(index - 1, potenz1 == 0 ? String.valueOf(result) : result + "x");
                           } else {
                               args.set(index - 3, result + "x");
                               args.set(index - 2, "^");
                               args.set(index - 1, String.valueOf(potenz1));
                           }
                           solved = true;
                           break;

                       }
                       space++;
                   }
                   //This part is for fractions addition only
                   if (solved) {
                       index = 0;
                       findEachpowerofX(args, powerofeachX);
                       continue;
                   }
                   space = 0;
                   while(index + space < args.size()) {

                      if(args.get(index+space).equals("(")) {
                          List<String> dividend = new ArrayList<>();
                          int counter = 1;
                          int dividendOpeningClause = index+space + 1;
                          while(!args.get(index+space+counter).equals(")")) {
                              dividend.add(args.get(index+space+counter));
                              args.remove(index+space+counter);
                          }
                          counter+=3; //to skip the division sign "/" and the opening clause of the divisor "("
                          List<String> divisor = new ArrayList<>();
                          while(!args.get(index+space+counter).equals(")")) {
                              divisor.add(args.get(index+space+counter));
                              counter++;
                          }
                          int divisionClosingClause = index+counter;
                          List<String> summand1List = new ArrayList<>();
                          if(exponent1) {
                              summand1List.add(args.get(index-3));
                              summand1List.add(args.get(index-2));
                              summand1List.add(args.get(index-1));
                          }else summand1List.add(args.get(index-1));
                          List<String> summandTimesDivisor = productResult(true,summand1List,divisor);
                          List<String> additionDividend = new ArrayList<>(summandTimesDivisor);
                          if(addition) {
                              additionDividend.add("+");
                          }else {
                              additionDividend.addAll(additionDividend.size(), Arrays.asList("+","(","-1",")","*","(",")"));
                          }
                          additionDividend.addAll(additionDividend.size()-(addition ? 0 : 1),dividend);
                          additionDividend = ClauseResultX(additionDividend);
                          if(!(additionDividend.size()== 1 && parse(additionDividend.get(0)) == 0)) args.addAll(dividendOpeningClause,additionDividend);
                          else {
                              args.subList(index+1,divisionClosingClause + 2).clear();
                              args.add(dividendOpeningClause-1,"0");
                          }
                          args.remove(index);
                          if(exponent1) {
                              args.remove(index-3);
                              args.remove(index-3);
                              args.remove(index-3);
                          }else args.remove(index-1);
                          solved = true;
                          break;
                      }
                       space++;
                   }

                   if(solved) {
                       index = 0;
                       findEachpowerofX(args,powerofeachX);
                       continue;
                   }

               }else {
                   List<String> divisor = new ArrayList<>();
                   int counter = 2;
                   int divisorClosingClause = index-counter;
                   while(!args.get(index-counter).equals("(")) {
                       divisor.add(args.get(index-counter));
                       counter++;
                   }
                   int divisorOpeningClause = index-counter;
                   Collections.reverse(divisor);
                   counter+=3;
                   int dividendOpeningClause = index-counter;
                   List<String> dividend = new ArrayList<>();
                   while(!args.get(index-counter).equals("(")) {
                       dividend.add(args.get(index-counter));
                       counter++;
                   }

                   args.subList(index-counter + 1,dividendOpeningClause + 1).clear();
                   dividendOpeningClause = index - counter + 1;
                   Collections.reverse(dividend);

                   index -= dividend.size();
                   int space = 1;
                   while(index + space < args.size() ) {

                       if(args.get(index + space).equals("(")) {
                           List<String> dividend2 = new ArrayList<>();
                           int counter2 = 1;
                           int dividend2OpeningClause = index+space+counter2;
                           while(!args.get(index+space+counter2).equals(")")) {
                               dividend2.add(args.get(index+space+counter2));
                               counter2++;
                           }
                           counter2+=3;
                           List<String> divisor2 = new ArrayList<>();
                           while(!args.get(index+space+counter2).equals(")")) {
                               divisor2.add(args.get(index+space+counter2));
                               counter2++;
                           }
                           int divisor2ClosingClause = index+space+counter2;
                           List<String> additionDivisor;
                           List<String> dividend2TimesDivisor;
                           List<String> dividendTimesDivisor2;
                           if(!divisor2.equals(divisor)) {
                               additionDivisor = productResult(true, divisor, divisor2);
                               additionDivisor = ClauseResultX(additionDivisor);
                               dividendTimesDivisor2 = productResult(true,dividend,divisor2);
                               dividendTimesDivisor2 = ClauseResultX(dividendTimesDivisor2);
                               dividend2TimesDivisor = productResult(true,dividend2,divisor);
                               dividend2TimesDivisor = ClauseResultX(dividend2TimesDivisor);
                           }else {
                               additionDivisor = divisor;
                               dividendTimesDivisor2 = dividend;
                               dividend2TimesDivisor = dividend2;
                           }
                           List<String> additionDividend = new ArrayList<>(dividendTimesDivisor2);
                           if(addition) {
                               additionDividend.add("+");
                           }else {
                               additionDividend.addAll(Arrays.asList("+","(","-1",")","*","(",")"));
                           }
                           additionDividend.addAll(dividend2TimesDivisor);
                           additionDividend = ClauseResultX(additionDividend);
                           if(additionDividend.size() == 1 && parse(findVorfaktor(additionDividend.get(0))) == 0d) {
                               args.subList(dividendOpeningClause-1,divisor2ClosingClause+1).clear();
                               args.add(dividendOpeningClause - 1,"0");
                               solved = true;
                               break;
                           }
                           args.subList(dividend2OpeningClause - 1,divisor2ClosingClause+1).clear();
                           args.remove(index);
                           args.addAll(dividendOpeningClause,additionDividend);
                           args.subList(divisorOpeningClause + additionDividend.size(),divisorClosingClause+additionDividend.size()).clear();
                           args.addAll(divisorOpeningClause + additionDividend.size(),additionDivisor);
                           solved = true;
                           break;
                       }
                       if(!args.get(index + space - 1).equals("^") && !args.get(index + space).matches("[+/()^-]")) {

                           boolean exponent2 = index + space + 2 < args.size() && args.get(index+space+1).equals("^");
                           List<String> summand2List = new ArrayList<>();
                           if(exponent2) {
                               summand2List.add(args.get(index+space));
                               summand2List.add(args.get(index+space + 1));
                               summand2List.add(args.get(index+space + 2));
                           }else summand2List.add(args.get(index+space));

                           List<String> summand2TimesDivisor = productResult(true,summand2List,divisor);
                           List<String> additionDividend = new ArrayList<>(dividend);
                           if(addition) {
                               additionDividend.add("+");
                           }else {
                               additionDividend.addAll(Arrays.asList("+","(","-1",")","*","(",")"));
                           }
                           additionDividend.addAll(additionDividend.size() - (addition ? 0 : 1), summand2TimesDivisor);
                           additionDividend = ClauseResultX(additionDividend);
                           if(additionDividend.size() == 1 && parse(findVorfaktor(additionDividend.get(0))) == 0) {
                               args.subList(dividendOpeningClause - 1,divisorClosingClause+1).clear();
                               args.add(dividendOpeningClause - 1,"0");
                               solved = true;
                               break;
                           }
                           args.remove(index);
                           if(exponent2) {
                               args.remove(index);
                               args.remove(index);
                               args.remove(index);
                           }else args.remove(index);
                           args.addAll(dividendOpeningClause,additionDividend);
                           solved = true;
                           break;
                       }
                       space++;
                   }
               }
               if (solved) {
                   index = 0;
                   findEachpowerofX(args, powerofeachX);
                   continue;
               }
               findEachpowerofX(args, powerofeachX);// updates the powerofeachX Hashmap
           }
           index++;
       }while(index < args.size());
    }

    private double parse(String s) { return Double.parseDouble(s); }

    public String findVorfaktor(String s) {
        s = s.replace("x",""); // 2x => 2
        if(s.equals("")) s = "1";// x => nothing => 1 (because 1x is the same as x)
        else if(s.equals("-")) s = "-1"; // -x => - => -1 (because -1x is the same as -x)
        return s;
    }
    private HashMap<Integer, Double> findEachpowerofX(@NotNull List<String> args, HashMap<Integer,Double> powerofeachX) throws DividingbyNullException {
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
                        String loesung = OperationResultX(klammerloesung,new HashMap<>()).get(0); // 2+3 => 5 => loesung = 5
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
    public List<Integer> findEachOpeningClause(List<String> args, int index) {
        List<Integer> clauseList = new ArrayList<>();
        for(int i=0; i<index; i++) {
            if(args.get(i).equals("(")) {
                clauseList.add(i);
            }
        }
        return clauseList;
    }
    public List<String> productResult(boolean mal,List<String> list, List<String> list2) throws DividingbyNullException {
        HashMap<Integer,Double> powerofeachXoflist = new HashMap<>();
        findEachpowerofX(list,powerofeachXoflist);
        HashMap<Integer,Double> powerofeachXoflist2 = new HashMap<>();
        findEachpowerofX(list2,powerofeachXoflist2);
        List<String> res = new ArrayList<>();
        if(list.contains("/") || list2.contains("/")) {
                List<String> dividend = new ArrayList<>();
                List<String> divisor = new ArrayList<>();
                boolean switchFromDividendToDivisor = false;
                for(int index=(list.contains("/") ? 1 : 0); index<list.size(); index++) {
                    if(list.get(index).equals(")")) {
                        switchFromDividendToDivisor = true;
                        index += 2;
                        continue;
                    }
                    if(!switchFromDividendToDivisor) dividend.add(list.get(index));
                    else divisor.add(list.get(index));
                }
                if(divisor.isEmpty()) divisor.add("1");
                List<String> dividend2 = new ArrayList<>();
                List<String> divisor2 = new ArrayList<>();
                boolean switchFromDividendToDivisor2 = false;
                for(int index=(list2.contains("/") ? 1 : 0); index<list2.size(); index++) {
                    if(list2.get(index).equals(")")) {
                        switchFromDividendToDivisor2 = true;
                        index += 2;
                        continue;
                    }
                    if(!switchFromDividendToDivisor2) dividend2.add(list2.get(index));
                    else divisor2.add(list2.get(index));
                }
                if(divisor2.isEmpty()) divisor2.add("1");
                List<String> productofbothDividends = productResult(true,dividend,dividend2);
                productofbothDividends = ClauseResultX(productofbothDividends);
                if(productofbothDividends.size() == 1 && parse(findVorfaktor(productofbothDividends.get(0))) == 0d) {
                    res.add("0");
                    return res;
                }
                List<String> productofbothDivisors = productResult(true,divisor,divisor2);
                productofbothDivisors = ClauseResultX(productofbothDivisors);
                res.add("(");
                res.addAll(productofbothDividends);
                res.addAll(Arrays.asList(")","/","("));
                res.addAll(productofbothDivisors);
                res.add(")");
        }else {
            for (int i = 0; i < list.size(); i++) {
                double faktor1, potenz1;
                if (list.get(i).contains("x") || (i > 0 && !list.get(i - 1).equals("^") && !list.get(i).matches("[+-/*()^]")) || i == 0) { // [2x] gets accepted but from [2x^2] the [2] won't get accepted because it is follows a ^; the i==0 is there because the first
                    faktor1 = parse(findVorfaktor(list.get(i)));
                    if (faktor1 == 0) {
                        if(!res.isEmpty()) res.add("+");
                        res.add("0");
                        continue;
                    }
                    potenz1 = powerofeachXoflist.get(i);
                } else continue;
                for (int j = 0; j < list2.size(); j++) {
                    double faktor2, potenz2;
                    if (list2.get(j).contains("x") || (j > 0 && !list2.get(j - 1).equals("^") && !list2.get(j).matches("[+-/*()^]")) || j == 0) {
                        faktor2 = parse(findVorfaktor(list2.get(j)));
                        if (faktor2 == 0) {
                            if(!res.isEmpty()) res.add("+");
                            res.add("0");
                            continue;
                        }
                        potenz2 = powerofeachXoflist2.get(j);
                        double potenz = potenz1 + (mal ? potenz2 : -1 * potenz2); // the -1*potenz2 => only works for numbers not variables
                        double tempresult = faktor1 * (mal ? faktor2 : 1 / faktor2); //the 1/faktor2 => only works for numbers not variables
                        if (!res.isEmpty()) res.add("+");
                        if (potenz == 1) {
                            res.add(tempresult + "x");
                        } else {
                            if (potenz != 0) {
                                res.add(tempresult + "x");
                                res.add("^");
                                res.add(String.valueOf(potenz));
                            } else res.add(String.valueOf(tempresult));
                        }
                    }
                }
            }
        }
        return res;
    }
}
