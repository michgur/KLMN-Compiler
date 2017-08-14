import lex.TokenStream;
import parsing.*;
import lex.Token;
import parsing.slr.Parser;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    // todo: create new exceptions for each phase of compiling
    public static void main(String[] args) {
        String code = "int a#poo\n = 1 + 2;int b = 3 * (1 + 2 - (5 * 6 / (7-2)) * 2) + 2;" +
                "class poo {" +
                "   void poopoo {" +
                "       int a = 2; # some clever remark\n" +
                "   }" +
                "}";
        TokenStream t = new TokenStream(code);

        Symbol S = new Symbol("STMT"), B = new Symbol("BLOCK"),
                E = new Symbol("EXPR"), T = new Symbol("T"), F = new Symbol("F");

        B.addProduction();
        B.addProduction(Terminal.END_OF_INPUT); // required for empty input. not ideal, but fixing it will be a pain.
        B.addProduction(S);
        B.addProduction(B, S);
        // type name = EXPR ;
        S.addProduction(Token.Type.IDENTIFIER.t, Token.Type.IDENTIFIER.t, Token.Type.EQUALS.t, E, Token.Type.SEMICOLON.t);
        E.addProduction(T);
        E.addProduction(E, Token.Type.PLUS.t, T);
        E.addProduction(E, Token.Type.DASH.t, T);
        T.addProduction(T, Token.Type.ASTERISK.t, F);
        T.addProduction(T, Token.Type.SLASH.t, F);
        T.addProduction(F);
        F.addProduction(Token.Type.OPEN_PAREN.t, E, Token.Type.CLOSE_PAREN.t);
        F.addProduction(Token.Type.NUMBER.t);
        // function definition
        // return_type name { BLOCK }
        S.addProduction(Token.Type.IDENTIFIER.t, Token.Type.IDENTIFIER.t, Token.Type.OPEN_CURLY.t, B, Token.Type.CLOSE_CURLY.t);
        // class definition
        // class name { BLOCK }
        S.addProduction(Token.Type.CLASS.t, Token.Type.IDENTIFIER.t, Token.Type.OPEN_CURLY.t, B, Token.Type.CLOSE_CURLY.t);

        Grammar g = new Grammar(B);
        System.out.println(g);
        System.out.println();

        System.out.println(new Parser(g).parse(t));
    }
}
