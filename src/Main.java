import ast.AST;
import ast.ASTFactory;
import lang.*;
import parsing.Parser;
import test.KVM;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception {
        // this class should be deleted FOR NOW, tokenizing should be implemented manually
        Language KLMN = new Language();

        String code = new String(Files.readAllBytes(Paths.get(args[0])));
        System.out.println(code);
        TokenStream t = KLMN.tokenize(code);

        Symbol B = new Symbol("BLCK"), S = new Symbol("STMT");
        Symbol E = new Symbol("EXPR"), T = new Symbol("T"), F = new Symbol("F");
        Terminal plus = new Terminal("+"), minus = new Terminal("-"), times = new Terminal("*"),
                divide = new Terminal("/"), open = new Terminal("("), close = new Terminal(")"),
                number = new Terminal("num"), semicolon = new Terminal(";");
        Terminal assign = new Terminal("="), var = new Terminal("var"), print = new Terminal("print"),
        identifier = new Terminal("ID");

        KLMN.addTerminal(assign, '=').addTerminal(plus, '+').addTerminal(semicolon, ';')
        .addTerminal(minus, '-').addTerminal(times, '*')
        .addTerminal(divide, '/').addTerminal(open, '(').addTerminal(close, ')').addTerminal(var, "var").addTerminal(print, "print")
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
        })
        .addTerminal(identifier, (src, i) -> {
            if (!(Character.isLetter(src.charAt(i)) || src.charAt(i) == '_')) return null;
            StringBuilder value = new StringBuilder().append(src.charAt(i));
            while (++i < src.length()) {
                char c = src.charAt(i);
                if (Character.isLetterOrDigit(c) || c == '_') value.append(c);
                else break;
            }
            return value.toString();
        });
        KLMN.ignore((src, i) -> { // ignore spaces
            char c = src.charAt(i);
            if (c != ' ' && c != '\n' && c != '\t' && c != '\r') return null;
            StringBuilder value = new StringBuilder().append(src.charAt(i));
            while (++i < src.length()) {
                c = src.charAt(i);
                if (c == ' ' || c == '\n' || c == '\t' || c == '\r') value.append(c);
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

        B.addProduction(S);
        B.addProduction(B, S);
        S.addProduction(var, identifier, assign, E, semicolon);
        S.addProduction(print, E, semicolon);
        E.addProduction(T);
        E.addProduction(E, plus, T);
        E.addProduction(E, minus, T);
        T.addProduction(T, times, F);
        T.addProduction(T, divide, F);
        T.addProduction(F);
        F.addProduction(open, E, close);
        F.addProduction(number);
        F.addProduction(identifier);

        ASTFactory factory = new ASTFactory();
        factory.addProduction(B, new Symbol[] { S }, c -> new AST(new Token(new Terminal("Code"), "Code"), c) {
            @Override public String generateCode() { return getChildren()[0].generateCode(); }
        });
        factory.addProduction(B, new Symbol[] { B, S }, c -> {
            AST[] children = new AST[c[0].getChildren().length + 1];
            System.arraycopy(c[0].getChildren(), 0, children, 0, c[0].getChildren().length);
            children[children.length - 1] = c[1];
            return new AST(null, children) {
                @Override public String generateCode() {
                    StringBuilder s = new StringBuilder();
                    for (AST ast : getChildren()) s.append(ast.generateCode());
                    return s.toString();
                }
            };
        });
        factory.addProduction(S, new Symbol[] { var, identifier, assign, E, semicolon }, c ->
        new AST(c[2].getValue(), c[1], c[3]) {
            @Override public String generateCode() {
                return getChildren()[1].generateCode() + "pop #" + getChildren()[0].getValue().getValue() + '\n';
            }
        });
        factory.addProduction(S, new Symbol[] { print, E, semicolon }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public String generateCode() {
                return getChildren()[0].generateCode() + "pop #0r\nprint #0r\n";
            }
        });
        factory.addProduction(E, new Symbol[] { T }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { E, plus, T },
        c -> new AST(c[1].getValue(), c[0], c[2]) {
            @Override public String generateCode() {
                return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                "pop #0r\npop #0l\nadd #0l #0r\npush #0l\n";
            }
        });
        factory.addProduction(E, new Symbol[] { E, minus, T },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #0r\npop #0l\nsub #0l #0r\npush #0l\n";
                    }
                });
        factory.addProduction(T, new Symbol[] { T, times, F },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #0r\npop #0l\nmul #0l #0r\npush #0l\n";
                    }
                });
        factory.addProduction(T, new Symbol[] { T, divide, F },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #0r\npop #0l\ndiv #0l #0r\npush #0l\n";
                    }
                });
        factory.addProduction(T, new Symbol[] { F }, c -> c[0]);
        factory.addProduction(F, new Symbol[] { open, E, close }, c -> c[1]);
        factory.addProduction(F, new Symbol[] { number }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return "push " + getValue().getValue() + '\n'; }
        });
        factory.addProduction(F, new Symbol[] { identifier }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return "push #" + getValue().getValue() + '\n'; }
        });

        Grammar g = new Grammar(B);
//        System.out.println(g);
//        System.out.println();

//        t.forEachRemaining(System.out::println);
//        System.out.println(new Parser(g).parse(t, factory));
        KVM.run(new Parser(g).parse(t, factory).generateCode());
    }
}
