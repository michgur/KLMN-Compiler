import automata.DFA;
import automata.NFA;
import lex.TokenStream;
import parsing.*;
import lex.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    // todo: create new exceptions for each phase of compiling
    public static void main(String[] args) {
        String code = "3 * (1 + 2)";
        TokenStream t = new TokenStream(code);

        Symbol E = new Symbol("E"), T = new Symbol("T"), X = new Symbol("X"), Y = new Symbol("Y");
        Terminal num = new Terminal("int", Token.Type.NUMBER), open = new Terminal("(", Token.Type.OPEN_PAREN),
                close = new Terminal(")", Token.Type.CLOSE_PAREN), plus = new Terminal("+", Token.Type.PLUS),
                mul = new Terminal("*", Token.Type.TIMES);

        E.addProduction(T, X);
        T.addProduction(open, E, close);
        T.addProduction(num, Y);
        X.addProduction(plus, E);
        X.addProduction(Symbol.EPSILON);
        Y.addProduction(mul, T);
        Y.addProduction(Symbol.EPSILON);

//        Grammar g = new Grammar(E);
//        System.out.println(g + "\n");
//        ParsingTable table = new ParsingTable(g);
//        System.out.println(table + "\n");
//        System.out.println(table.parse(t));

        NFA<Integer> nfa = new NFA<>(10);
        nfa.addTransition(0, 1);
        nfa.addTransition(0, 7);
        nfa.addTransition(1, 2);
        nfa.addTransition(1, 3);
        nfa.addTransition(2, 1, 4);
        nfa.addTransition(3, 0, 5);
        nfa.addTransition(4, 6);
        nfa.addTransition(5, 6);
        nfa.addTransition(6, 0);
        nfa.addTransition(6, 7);
        nfa.addTransition(7, 8);
        nfa.addTransition(8, 1, 9);

//        DFA<Integer> dfa = new DFA<>(3);
//        dfa.addTransition(0, 1, 1);
//        dfa.addTransition(0, 0, 2);
//        dfa.addTransition(1, 1, 1);
//        dfa.addTransition(1, 0, 2);
//        dfa.addTransition(2, 1, 1);
//        dfa.addTransition(2, 0, 2);
//        dfa.acceptOn(1);
//        System.out.println(dfa);
//
        List<Integer> input = new ArrayList<>();
        for (int i = 0; i < 1000; i++) input.add((int) Math.round(Math.random()));
        input.add(1);
        System.out.println(nfa.test(input.iterator()));
        System.out.println(nfa.toDFA().test(input.iterator()));
    }
}
