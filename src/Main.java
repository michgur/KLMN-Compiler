import ast.AST;
import javafx.util.Pair;
import lang.*;
import parsing.*;
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
        Language KLMN = new Language();

        Terminal.END_OF_INPUT.isTerminal(); // it's stupid, will be removed when I refine the Symbol System
        String code = "((1 - 2) * 3) / 4 + 5 * 6";
        TokenStream t = KLMN.tokenize(code);

        Symbol E = new Symbol("EXPR"), T = new Symbol("T"), F = new Symbol("F");
        Terminal plus = new Terminal("+"), minus = new Terminal("-"), times = new Terminal("*"),
                divide = new Terminal("/"), open = new Terminal("("), close = new Terminal(")"),
                number = new Terminal("num");

        KLMN.addTerminal(plus, '+').addTerminal(minus, '-').addTerminal(times, '*')
        .addTerminal(divide, '/').addTerminal(open, '(').addTerminal(close, ')')
        .addTerminal(number, (src, i) -> {
            if (!Character.isDigit(src.charAt(i))) return null;
            StringBuilder value = new StringBuilder().append(src.charAt(i));
            boolean dot = false;
            while (++i < src.length()) {
                char c = code.charAt(i);
                if (c == '.' && !dot) {
                    dot = true;
                    value.append('.');
                }
                else if (Character.isDigit(c)) value.append(c);
                else break;
            }
            return value.toString();
        });
        KLMN.ignore((src, i) -> { // ignore spaces
            char c = code.charAt(i);
            if (c != ' ' && c != '\n' && c != '\t') return null;
            StringBuilder value = new StringBuilder().append(src.charAt(i));
            while (++i < src.length()) {
                c = code.charAt(i);
                if (c == ' ' || c == '\n' || c == '\t') value.append(c);
                else break;
            }
            return value.toString();
        });
        KLMN.ignore((src, i) -> { // ignore comments
            if (src.charAt(i) != '#') return null;
            int end = src.indexOf('\n', i);
            if (end == -1) return src.substring(i);
            else return src.substring(i, end + 1);
        });

        // the lambdas have destroyed the neat use of addProduction. find a fix
        // everything will become twice as messy when we get to Code generation
        E.addProduction(tree -> tree.getChild(0).generateAST(), T);
        E.addProduction(tree -> new AST(tree.getChild(1).getValue(), tree.getChild(0).generateAST(), tree.getChild(2).generateAST()), E, plus, T);
        E.addProduction(tree -> new AST(tree.getChild(1).getValue(), tree.getChild(0).generateAST(), tree.getChild(2).generateAST()), E, minus, T);
        T.addProduction(tree -> new AST(tree.getChild(1).getValue(), tree.getChild(0).generateAST(), tree.getChild(2).generateAST()), T, times, F);
        T.addProduction(tree -> new AST(tree.getChild(1).getValue(), tree.getChild(0).generateAST(), tree.getChild(2).generateAST()), T, divide, F);
        T.addProduction(tree -> tree.getChild(0).generateAST(), F);
        F.addProduction(tree -> tree.getChild(1).generateAST(), open, E, close);
        F.addProduction(tree -> tree.getChild(0).generateAST(), number);

        Grammar g = new Grammar(E);
        System.out.println(g);
        System.out.println();

        ParseTree parseTree = new Parser(g).parse(t);
        System.out.println(parseTree);

        System.out.println(parseTree.generateAST());
    }
}
