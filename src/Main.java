import ast.AST;
import javafx.util.Pair;
import lex.TokenStream;
import parsing.*;
import lex.Token;
import parsing.slr.Parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    // Parsing Is The Only General Phase Of Compilation Since It's Virtually Impossible To Implement Specifically.

    // Some TODOs:
    //      Create New Exceptions For Each Phase Of Compiling
    //      Improve The Symbol/ Terminal System, And Its Interaction With Tokens & Token Types
    //      Merge Symbol/ Token Systems To Avoid Repetition. Use This As A Chance To Generalize Tokenizing.
    //      Merge AST With Symbol Productions.
    //      These 'Merges' Don't Necessarily Mean Put Everything In A Single Class, But
    //          The Classes Have To Be Bound Somehow For Generalizing & Avoiding Repetition.
    //      After That Shite Is Done, Go Over The Code In Parser & Grammar, And Clean It.
    //          Add Some Documentation In Complicated Parts.
    public static void main(String[] args) {
        Terminal.END_OF_INPUT.isTerminal(); // it's stupid, will be removed when I refine the Symbol System
        String code = "((1 - 2) * 3) / 4 + 5 * 6";
        TokenStream t = new TokenStream(code);

        Symbol E = new Symbol("EXPR"), T = new Symbol("T"), F = new Symbol("F");

        E.addProduction(T)//.addProduction(E, Token.Type.OPEN_BRACKET.t, E, Token.Type.CLOSE_BRACKET.t)
        .addProduction(E, Token.Type.PLUS.t, T).addProduction(E, Token.Type.DASH.t, T);
        T.addProduction(T, Token.Type.ASTERISK.t, F).addProduction(T, Token.Type.SLASH.t, F).addProduction(F);
        F.addProduction(Token.Type.OPEN_PAREN.t, E, Token.Type.CLOSE_PAREN.t).addProduction(Token.Type.NUMBER.t);

        Grammar g = new Grammar(E);
        System.out.println(g);
        System.out.println();

        ParseTree parseTree = new Parser(g).parse(t);
        System.out.println(parseTree);

        productions.put(new Pair<>(E, new Symbol[] { E, Token.Type.PLUS.t, T }),
                tree -> new AST(tree.getChild(1).getValue(), generateAST(tree.getChild(0)), generateAST(tree.getChild(2))));
        productions.put(new Pair<>(E, new Symbol[] { E, Token.Type.DASH.t, T }),
                tree -> new AST(tree.getChild(1).getValue(), generateAST(tree.getChild(0)), generateAST(tree.getChild(2))));
        productions.put(new Pair<>(E, new Symbol[] { T }), tree -> generateAST(tree.getChild(0)));
        productions.put(new Pair<>(T, new Symbol[] { T, Token.Type.ASTERISK.t, F }),
                tree -> new AST(tree.getChild(1).getValue(), generateAST(tree.getChild(0)), generateAST(tree.getChild(2))));
        productions.put(new Pair<>(T, new Symbol[] { T, Token.Type.SLASH.t, F }),
                tree -> new AST(tree.getChild(1).getValue(), generateAST(tree.getChild(0)), generateAST(tree.getChild(2))));
        productions.put(new Pair<>(T, new Symbol[] { F }), tree -> generateAST(tree.getChild(0)));
        productions.put(new Pair<>(F, new Symbol[] { Token.Type.NUMBER.t }), tree -> generateAST(tree.getChild(0)));
        productions.put(new Pair<>(F, new Symbol[] { Token.Type.OPEN_PAREN.t, E, Token.Type.CLOSE_PAREN.t }),
                tree -> generateAST(tree.getChild(1)));

        System.out.println(generateAST(parseTree));
    }

    private static Map<Pair<Symbol, Symbol[]>, Production> productions = new HashMap<>();
    private static AST generateAST(ParseTree tree) {
        if (tree.getSymbol().isTerminal()) return new AST(tree.getValue());

        Symbol[] p = new Symbol[tree.getChildren().size()];
        for (int i = 0; i < p.length; i++) p[i] = tree.getChildren().get(i).getSymbol();
        for (Pair<Symbol, Symbol[]> production : productions.keySet())
            if (production.getKey() == tree.getSymbol() && Arrays.equals(p, production.getValue()))
                return productions.get(production).generate(tree);
        System.out.println("Could Not Find Production: " + tree.getSymbol() + " -> " + Arrays.toString(p));
        return null;
    }
    public interface Production { AST generate(ParseTree tree); }
}
