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
    // Parsing Is The Only General Phrase Of Compilation Since It's Virtually Impossible To Implement Specifically.

    // Some TODOs:
    //      Create New Exceptions For Each Phase Of Compiling
    //      Improve The Symbol/ Terminal System, And Its Interaction With Tokens & Token Types
    //      Merge Symbol/ Token System To Avoid Repetition. Use This As A Chance To Generalize Tokenizing.
    //      Merge AST With Symbol Productions.
    //      These 'Merges' Don't Necessarily Mean Put Everything In A Single Class, But
    //          The Classes Have To Be Bound Somehow For Generalizing & Avoiding Repetition.
    //      After That Shite Is Done, Go Over The Code In Parser & Grammar, And Clean It.
    //          Add Some Documentation In Complicated Parts.
    public static void main(String[] args) {
        String code = "1 + 2 * 4";
        TokenStream t = new TokenStream(code);

        Symbol E = new Symbol("EXPR"), T = new Symbol("T"), F = new Symbol("F");

        E.addProduction(T).addProduction(E, Token.Type.OPEN_BRACKET.t, E, Token.Type.CLOSE_BRACKET.t)
        .addProduction(E, Token.Type.PLUS.t, T).addProduction(E, Token.Type.DASH.t, T);
        T.addProduction(T, Token.Type.ASTERISK.t, F).addProduction(T, Token.Type.SLASH.t, F).addProduction(F);
        F.addProduction(Token.Type.OPEN_PAREN.t, E, Token.Type.CLOSE_PAREN.t).addProduction(Token.Type.NUMBER.t);

        Grammar g = new Grammar(E);
        System.out.println(g);
        System.out.println();

        System.out.println(new Parser(g).parse(t));
    }
}
