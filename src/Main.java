import lex.TokenStream;
import parsing.*;
import lex.Token;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
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

        Grammar g = new Grammar(E);

        System.out.println(g);

        ParsingTable table = new ParsingTable(g);
        System.out.println(table);
        table.parse(t);
    }
}
