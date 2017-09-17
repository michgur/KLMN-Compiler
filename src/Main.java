import ast.AST;
import ast.ASTFactory;
import lang.*;
import parsing.Parser;
import test.KVM;
import test.SymbolTable;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class Main
{
    // TODO:::
    // TODO:::  OK, I've successfully demonstrated that code generation is possible.
    // TODO:::  Now, I have to focus on learning how to actually manage the semantics,
    // TODO:::  do shit like variables, memory management, functions, type checking,
    // TODO:::  scopes, and equally importantly a proper platform for generated code
    // TODO:::  (the VM is hilarious but not really useful / efficient in any way).
    // TODO:::

    /* 💩💩💩 AMAZING 💩💩💩 */
    public static void main(String[] args) throws Exception {
        // this class should be deleted FOR NOW, tokenizing should be implemented manually
        Language KLMN = new Language();

        String code = new String(Files.readAllBytes(Paths.get(args[0])));
        TokenStream t = KLMN.tokenize(code);

        Symbol B = new Symbol("BLCK"), S = new Symbol("STMT");
        Symbol E = new Symbol("EXPR"), T = new Symbol("T"), F = new Symbol("F"), T1 = new Symbol("T1"), T2 = new Symbol("T2");
        Terminal plus = new Terminal("+"), minus = new Terminal("-"), times = new Terminal("*"),
                divide = new Terminal("/"), open = new Terminal("("), close = new Terminal(")"),
                number = new Terminal("num"), semicolon = new Terminal(";"),
                equals = new Terminal("=="), nEquals = new Terminal("!="), less = new Terminal("<"),
                greater = new Terminal(">"), lessEquals = new Terminal("<="), greaterEquals = new Terminal(">="),
                openCurly = new Terminal("{"), closeCurly = new Terminal("}");
        Terminal assign = new Terminal("="), var = new Terminal("var"), print = new Terminal("print"),
        identifier = new Terminal("ID"), kwIf = new Terminal("if");

        KLMN.addTerminal(assign, '=').addTerminal(plus, '+').addTerminal(semicolon, ';')
        .addTerminal(minus, '-').addTerminal(times, '*')
        .addTerminal(divide, '/').addTerminal(open, '(').addTerminal(close, ')')
        .addTerminal(var, "var").addTerminal(print, "print").addTerminal(kwIf, "if")
        .addTerminal(openCurly, '{').addTerminal(closeCurly, '}')
        .addTerminal(equals, "==").addTerminal(nEquals, "!=").addTerminal(less, '<')
        .addTerminal(greater, '>').addTerminal(lessEquals, "<=").addTerminal(greaterEquals, ">=")
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
        S.addProduction(kwIf, open, E, close, openCurly, B, closeCurly);
        S.addProduction(var, identifier, assign, E, semicolon);
        S.addProduction(print, E, semicolon);
        S.addProduction(kwIf, open, E, close, S);
        E.addProduction(T2);
        E.addProduction(E, equals, T2);
        E.addProduction(E, nEquals, T2);
        T2.addProduction(T1);
        T2.addProduction(T2, less, T1);
        T2.addProduction(T2, greater, T1);
        T2.addProduction(T2, lessEquals, T1);
        T2.addProduction(T2, greaterEquals, T1);
        T1.addProduction(T1, plus, T);
        T1.addProduction(T1, minus, T);
        T1.addProduction(T);
        T.addProduction(T, times, F);
        T.addProduction(T, divide, F);
        T.addProduction(F);
        F.addProduction(open, E, close);
        F.addProduction(number);
        F.addProduction(identifier);

        SymbolTable st = new SymbolTable();
        st.enterScope();
        st.addSymbol("lll"); // index 0, temporary (registers will not be vars)
        st.addSymbol("rrr"); // index 1, temporary (registers will not be vars)
        final int[] skip = { 0 };
        ASTFactory factory = new ASTFactory();
        factory.addProduction(B, new Symbol[] { S }, c -> new AST(new Token(null, "Block"), c) {
            @Override public String generateCode() {
                st.enterScope();
                String code = getChildren()[0].generateCode();
                st.exitScope();
                return code;
            }
        });
        factory.addProduction(B, new Symbol[] { B, S }, c -> {
            AST[] children = new AST[c[0].getChildren().length + 1];
            System.arraycopy(c[0].getChildren(), 0, children, 0, c[0].getChildren().length);
            children[children.length - 1] = c[1];
            return new AST(new Token(null, "Block"), children) {
                @Override public String generateCode() {
                    st.enterScope();
                    StringBuilder s = new StringBuilder();
                    for (AST ast : getChildren()) s.append(ast.generateCode());
                    st.exitScope();
                    return s.toString();
                }
            };
        });
        factory.addProduction(S, new Symbol[] { kwIf, open, E, close, openCurly, B, closeCurly }, c ->
                new AST(c[0].getValue(), c[2], c[5]) {
                    @Override public String generateCode() {
                        st.enterScope();
                        String body = getChildren()[1].generateCode();
                        st.exitScope();
                        return getChildren()[0].generateCode() +
                            "pop #0\nje 0 #0 skip" + skip[0] + '\n' + body + ":skip" + skip[0]++ + '\n';
                    }
                }
        );
        factory.addProduction(S, new Symbol[] { var, identifier, assign, E, semicolon }, c ->
        new AST(c[2].getValue(), c[1], c[3]) {
            @Override public String generateCode() {
                String name = getChildren()[0].getValue().getValue();
                if (st.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                st.addSymbol(name);
                return getChildren()[1].generateCode() + "pop #" + st.findSymbol(name) + '\n';
            }
        });
        factory.addProduction(S, new Symbol[] { print, E, semicolon }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public String generateCode() {
                return getChildren()[0].generateCode() + "pop #1\nprint #1\n";
            }
        });
        factory.addProduction(E, new Symbol[] { T2 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { E, equals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\neq #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(E, new Symbol[] { E, nEquals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\nneq #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, less, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\nlt #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, greater, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\ngt #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, lessEquals, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\nleq #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, greaterEquals, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\ngeq #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T2, new Symbol[] { T1 }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T1, plus, T },
        c -> new AST(c[1].getValue(), c[0], c[2]) {
            @Override public String generateCode() {
                return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                "pop #1\npop #0\nadd #0 #1\npush #0\n";
            }
        });
        factory.addProduction(T1, new Symbol[] { T1, minus, T },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\nsub #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T, new Symbol[] { T, times, F },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\nmul #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T, new Symbol[] { T, divide, F },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\ndiv #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T, new Symbol[] { F }, c -> c[0]);
        factory.addProduction(F, new Symbol[] { open, E, close }, c -> c[1]);
        factory.addProduction(F, new Symbol[] { number }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return "push " + getValue().getValue() + '\n'; }
        });
        factory.addProduction(F, new Symbol[] { identifier }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return "push #" + st.findSymbol(getValue().getValue()) + '\n'; }
        });
        factory.addProduction(S, new Symbol[] { kwIf, open, E, close, S }, c ->
            new AST(c[0].getValue(), c[2], c[4]) {
                @Override public String generateCode() {
                    return getChildren()[0].generateCode() +
                            "pop #0\nje 0 #0 skip" + skip[0] + '\n' + getChildren()[1].generateCode() + ":skip" + skip[0]++ + '\n';
                }
            }
        );

        Grammar g = new Grammar(B);
//        System.out.println(g);
//        System.out.println();

//        t.forEachRemaining(System.out::println);
//        System.out.println(new Parser(g).parse(t, factory));
        KVM.run(new Parser(g).parse(t, factory).generateCode());
    }
}
