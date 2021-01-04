# Ableitungsrechner/Derivative Calculator/VDex

A derivative calculator with the aim to show a step by step solution of how to get the derivative of a function. (The step by step solution is still work in progress)

This is a solo project of mine therefore I am the one designing, writing and coding this project and because I am neither a graphic designer nor a software engineer, don't be too harsh on me.

## Long Term Goals
* A fast and friendly to use derivative calculator 
* The addition of a plotter and a synchronized UI
* A detailed documentation of the inner workings of the calculator (mostly via code comments)
* Impressing my old physics teacher and my shunning and presumptuous friend

## Short Term Goals
* Adding trigonomical functions and the use of non-integer exponents for clauses
* Improving GUI
* Simplyfication of fractions such as (x+1)/(x+1) to be 1
* Adding Step-by-Step

### Known Bugs
* Dividing by 0 is "possible" (x)/(2x-2x) => (x)/(0)
* Fractions in which the numerator/dividend or the denominator/divisor is a fraction itself
* Non-integer powers for clauses e.g. (x+1)^0.5 and negative powers for fractions ((x+1)/(x-1))^-3 because this will create a new fractions with a divisor/denominator as a fraction
