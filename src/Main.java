import automata.DFA;
import automata.NFA;
import lex.TokenStream;
import parsing.*;
import lex.Token;

import java.util.ArrayList;
import java.util.List;

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

        Symbol E = new Symbol("E"), T = new Symbol("T");
        Terminal num = new Terminal("int", Token.Type.NUMBER), open = new Terminal("(", Token.Type.OPEN_PAREN),
                close = new Terminal(")", Token.Type.CLOSE_PAREN), plus = new Terminal("+", Token.Type.PLUS),
                mul = new Terminal("*", Token.Type.TIMES);

        E.addProduction(T, plus, E);
        E.addProduction(T);
        T.addProduction(num, mul, T);
        T.addProduction(num);
        T.addProduction(open, E, close);

        Grammar g = new Grammar(E);
//        System.out.println(g + "\n");

        NFA<Character> nfa = new NFA<>(5);
        nfa.addTransition(0, 1);
        nfa.addTransition(0, 2);
        nfa.addTransition(1, 'a', 1);
        nfa.addTransition(1, 4);
        nfa.addTransition(2, 'a', 3);
        nfa.addTransition(2, 4);
        nfa.addTransition(3, 'b', 2);
        nfa.addTransition(4, 'b', 4);
        nfa.acceptOn(4);

        /* SBDS 2K18: Battle Of The Pretty Prints */
        System.out.println(nfa);
        System.out.println(nfa.toDFA());

        String string = "ababababbbbbbbbb";
        List<Character> input = new ArrayList<>();
        for (int i = 0; i < string.length(); i++) input.add(string.charAt(i));
        System.out.println("NFA: " + nfa.test(input.iterator()));
        System.out.println("DFA: " + nfa.toDFA().test(input.iterator()));
    }
}
