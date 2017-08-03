import lex.TokenStream;
import parsing.Grammar;
import lex.Token;
import parsing.ParsingTable;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    public static void main(String[] args) {
        String code = "(1 + 2) * 3";
        TokenStream t = new TokenStream(code);

        Grammar g = new Grammar();
        Grammar.Symbol E = g.addSymbol("E"), T = g.addSymbol("T"), X = g.addSymbol("X"), Y = g.addSymbol("Y");
        Grammar.Terminal i = g.addTerminal("int", Token.Type.NUMBER), op = g.addTerminal("(", Token.Type.OPEN_PAREN),
                cp = g.addTerminal(")", Token.Type.CLOSE_PAREN), p = g.addTerminal("+", Token.Type.PLUS),
                m = g.addTerminal("*", Token.Type.TIMES);
        g.setStart(E);
        g.addProduction(E, T, X);
        g.addProduction(T, op, E, cp);
        g.addProduction(T, i, Y);
        g.addProduction(X, p, E);
        g.addProduction(X, Grammar.EPSILON);
        g.addProduction(Y, m, T);
        g.addProduction(Y, Grammar.EPSILON);

        System.out.println(g.firstSet(X));
        System.out.println(g.firstSet(E));
        System.out.println(g.firstSet(T));
        System.out.println(g.firstSet(Y));
        System.out.println(g.firstSet(op));
        System.out.println(g.firstSet(cp));
        System.out.println(g.firstSet(p));
        System.out.println(g.firstSet(m));
        System.out.println(g.firstSet(i));
        System.out.println(g.followSet(X));
        System.out.println(g.followSet(E));
        System.out.println(g.followSet(T));
        System.out.println(g.followSet(Y));
        System.out.println(g.followSet(op));
        System.out.println(g.followSet(cp));
        System.out.println(g.followSet(p));
        System.out.println(g.followSet(m));
        System.out.println(g.followSet(i));

        ParsingTable table = new ParsingTable(g);
        table.parse(t);
//        t.forEachRemaining(System.out::println);
    }
}
