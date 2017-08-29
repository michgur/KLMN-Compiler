import ast.AST;
import ast.ASTFactory;
import lang.*;
import parsing.Parser;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    /**
     * Parsing Is The Only Fully General Phase Of Compilation,
     * Since It's Virtually Impossible To Implement Specifically.
     *
     * Some TODOs:
     * DE-MERGE AST With Symbol Productions.
     * These 'Merges' Don't Necessarily Mean Put Everything In A Single Class, But
     *      The Classes Have To Be Bound Somehow For Generalizing & Avoiding Repetition.
     * After That Shite Is Done, Go Over The Code In Parser & Grammar, And Clean It.
     * Add Some Documentation In Complicated Parts.
    */
    public static void main(String[] args) {
        // this class should be deleted FOR NOW, tokenizing should be implemented manually
        Language KLMN = new Language();

        String code = "1 + (2 - 3) * 4 / (5 + 6)";
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
                char c = src.charAt(i);
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
            char c = src.charAt(i);
            if (c != ' ' && c != '\n' && c != '\t') return null;
            StringBuilder value = new StringBuilder().append(src.charAt(i));
            while (++i < src.length()) {
                c = src.charAt(i);
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

        // move the lambdas to the parser (Map Pair<Symbol, Symbol[]> to them)
        // then again, not sure if this is a good solution either. the productions
        // determine the AST node type, which is important for parsing, BUT
        // ALSO for semantic analysis & code generation.
        // I'll have to define some sort of an AST factory that the parser can use.
        // stuff to consider: avoiding repetition, putting shit where it belongs
        E.addProduction(T);
        E.addProduction(E, plus, T);
        E.addProduction(E, minus, T);
        T.addProduction(T, times, F);
        T.addProduction(T, divide, F);
        T.addProduction(F);
        F.addProduction(open, E, close);
        F.addProduction(number);

        new ASTFactory(c -> c[0], T);
        new ASTFactory(c -> new AST(c[1].getValue(), c[0], c[2]), E, plus, T);
        new ASTFactory(c -> new AST(c[1].getValue(), c[0], c[2]), E, minus, T);
        new ASTFactory(c -> new AST(c[1].getValue(), c[0], c[2]), T, times, F);
        new ASTFactory(c -> new AST(c[1].getValue(), c[0], c[2]), T, divide, F);
        new ASTFactory(c -> c[0], F);
        new ASTFactory(c -> c[1], open, E, close);
        new ASTFactory(c -> c[0], number);

        Grammar g = new Grammar(E);
        System.out.println(g);
        System.out.println();

        System.out.println(new Parser(g).parse(t));
    }
}
