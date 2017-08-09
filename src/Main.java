import automata.DFA;
import automata.NFA;
import lex.TokenStream;
import parsing.*;
import lex.Token;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    // todo: create new exceptions for each phase of compiling
    public static void main(String[] args) {
        String code = "1 * (2 + 3 * 4)";
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
        System.out.println(g + "\n");

        System.out.println(new Parser(g).parse(t));
    }
}
