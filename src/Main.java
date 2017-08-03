import parsing.Grammar;
import lex.Token;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    public static void main(String[] args) {
//        String code = "12 * (34 + 2.2)";
//        TokenStream t = new TokenStream(code);
//        t.forEachRemaining(token -> System.out.println(token.getType() + " " + token.getValue()));
        Grammar g = new Grammar();
        Grammar.Symbol E = g.addSymbol("E"), T = g.addSymbol("T"), X = g.addSymbol("X"), Y = g.addSymbol("Y");
        Grammar.Terminal i = g.addTerminal("int", Token.Type.NUMBER), op = g.addTerminal("(", Token.Type.PUNCTUATION, "("),
                cp = g.addTerminal(")", Token.Type.PUNCTUATION, ")"), p = g.addTerminal("+", Token.Type.OPERATOR, "+"),
                m = g.addTerminal("*", Token.Type.OPERATOR, "*");
        g.setStart(E);
        g.addProduction(E, T, X);
        g.addProduction(T, op, E, cp);
        g.addProduction(T, i, Y);
        g.addProduction(X, p, E);
        g.addProduction(X, Grammar.EPSILON);
        g.addProduction(Y, m, T);
        g.addProduction(Y, Grammar.EPSILON);

        System.out.println(g.followSet(i));
    }
}
