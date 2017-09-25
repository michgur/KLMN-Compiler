package test;

import ast.AST;
import ast.ASTFactory;
import lang.*;
import parsing.Parser;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/25/2017.
 */
public class TEMP
{
    private static void t(String[] args) throws Exception {
        Language KLMN = new Language();

        String code = new String(Files.readAllBytes(Paths.get(args[0])));
        TokenStream t = KLMN.tokenize(code);

        Symbol B = new Symbol("BLCK"), S = new Symbol("STMT"), A = new Symbol(":="), S1 = new Symbol("STMT1");
        Symbol E = new Symbol("EXPR"), F = new Symbol("F"), F1 = new Symbol("F1"), F2 = new Symbol("F2"),
                T = new Symbol("T"), T1 = new Symbol("T1"), T2 = new Symbol("T2"),
                T3 = new Symbol("T3"), T4 = new Symbol("T4");
        Terminal plus = new Terminal("+"), minus = new Terminal("-"), times = new Terminal("*"),
                divide = new Terminal("/"), open = new Terminal("("), close = new Terminal(")"),
                number = new Terminal("num"), semicolon = new Terminal(";"),
                equals = new Terminal("=="), nEquals = new Terminal("!="), less = new Terminal("<"),
                greater = new Terminal(">"), lessEquals = new Terminal("<="), greaterEquals = new Terminal(">="),
                openCurly = new Terminal("{"), closeCurly = new Terminal("}"), lAnd = new Terminal("&&"), lOr = new Terminal("||");
        Terminal assign = new Terminal("="), var = new Terminal("var"), print = new Terminal("print"),
                increment = new Terminal("++"), decrement = new Terminal("--"),
                identifier = new Terminal("ID"), kwIf = new Terminal("if"), kwFor = new Terminal("for"),
                kwTrue = new Terminal("true"), kwFalse = new Terminal("false");

        KLMN.addTerminal(assign, '=').addTerminal(plus, '+').addTerminal(semicolon, ';')
                .addTerminal(minus, '-').addTerminal(times, '*').addTerminal(lAnd, "&&").addTerminal(lOr, "||")
                .addTerminal(divide, '/').addTerminal(open, '(').addTerminal(close, ')')
                .addTerminal(increment, "++").addTerminal(decrement, "--")
                .addTerminal(var, "var").addTerminal(print, "print").addTerminal(kwIf, "if")
                .addTerminal(openCurly, '{').addTerminal(closeCurly, '}').addTerminal(kwFor, "for")
                .addTerminal(kwTrue, "true").addTerminal(kwFalse, "false")
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

        B.addProduction(S1);
        B.addProduction(B, S1);
        S1.addProduction(S, semicolon);
        S1.addProduction(kwFor, open, S1, E, semicolon, S, close, S1);
        S1.addProduction(kwFor, open, S1, E, semicolon, S, close, openCurly, B, closeCurly);
        S1.addProduction(kwIf, open, E, close, S1);
        S1.addProduction(kwIf, open, E, close, openCurly, B, closeCurly);
        S.addProduction(A);
        A.addProduction(var, identifier, assign, E);
        A.addProduction(identifier, assign, E);
        S.addProduction(print, E);
        E.addProduction(E, lOr, T4);
        E.addProduction(T4); // E -> T4
        T4.addProduction(T4, lAnd, T3);
        T4.addProduction(T3); // T4 -> T3
        T3.addProduction(T3, equals, T2);
        T3.addProduction(T3, nEquals, T2);
        T3.addProduction(T2); // T3 -> T2
        T2.addProduction(T2, less, T1);
        T2.addProduction(T2, greater, T1);
        T2.addProduction(T2, lessEquals, T1);
        T2.addProduction(T2, greaterEquals, T1);
        T2.addProduction(T1); // T2 -> T1
        T1.addProduction(T1, plus, T);
        T1.addProduction(T1, minus, T);
        T1.addProduction(T); // T1 -> T
        T.addProduction(T, times, F1);
        T.addProduction(T, divide, F1);
        T.addProduction(F1);
        F1.addProduction(F2);
        F1.addProduction(plus, F1);
        F1.addProduction(minus, F1);
        F1.addProduction(F);
        S.addProduction(F2);
        E.addProduction(F2);
        F2.addProduction(F, decrement);
        F2.addProduction(F, increment);
        F2.addProduction(increment, F);
        F2.addProduction(decrement, F);
        F.addProduction(open, E, close);
        F.addProduction(number);
        F.addProduction(kwTrue);
        F.addProduction(kwFalse);
        F.addProduction(identifier);

        SymbolTable st = new SymbolTable();
        st.enterScope();
        st.addSymbol("lll"); // index 0, temporary (registers will not be vars)
        st.addSymbol("rrr"); // index 1, temporary (registers will not be vars)
        final int[] skip = { 0 };
        ASTFactory factory = new ASTFactory();
        factory.addProduction(B, new Symbol[] { S1 }, c -> new AST(new Token(null, "Block"), c[0]) {
            @Override public String generateCode() {
                st.enterScope();
                String code = getChildren()[0].generateCode();
                st.exitScope();
                return code;
            }
        });
        factory.addProduction(S1, new Symbol[] { S, semicolon }, c -> c[0]);
        factory.addProduction(B, new Symbol[] { B, S1 }, c -> {
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
        factory.addProduction(S, new Symbol[] { A }, c -> c[0]);
        factory.addProduction(A, new Symbol[] { var, identifier, assign, E }, c ->
                new AST(c[2].getValue(), c[1], c[3]) {
                    @Override public String generateCode() {
                        String name = getChildren()[0].getValue().getValue();
                        if (st.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                        st.addSymbol(name);
                        return getChildren()[1].generateCode() + "pop #" + st.findSymbol(name) + '\n';
                    }
                });
        factory.addProduction(A, new Symbol[] { identifier, assign, E }, c ->
                new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[1].generateCode() + "pop #" + st.findSymbol(getChildren()[0].getValue().getValue()) + '\n';
                    }
                });
        factory.addProduction(S, new Symbol[] { print, E }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public String generateCode() {
                return getChildren()[0].generateCode() + "pop #1\nprint #1\n";
            }
        });
        factory.addProduction(E, new Symbol[] { T4 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { E, lOr, T4 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\nlor #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T4, new Symbol[] { T3 }, c -> c[0]);
        factory.addProduction(T4, new Symbol[] { T4, lAnd, T3 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\nland #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T3, new Symbol[] { T2 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T3, equals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\neq #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(E, new Symbol[] { T3, nEquals, T2 },
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
        factory.addProduction(T, new Symbol[] { T, times, F1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\nmul #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T, new Symbol[] { T, divide, F1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public String generateCode() {
                        return getChildren()[0].generateCode() + getChildren()[1].generateCode() +
                                "pop #1\npop #0\ndiv #0 #1\npush #0\n";
                    }
                });
        factory.addProduction(T, new Symbol[] { F1 }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { F }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { plus, F1 }, c -> c[1]);
        factory.addProduction(F1, new Symbol[] { minus, F1 }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public String generateCode() {
                return getChildren()[0].generateCode() + "pop #0\nmul #0 -1\npush #0\n";
            }
        });
        factory.addProduction(F1, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(S, new Symbol[] { F2 }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return c[0].generateCode() + "pop #0\n"; }
        });
        factory.addProduction(F2, new Symbol[] { increment, F }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public String generateCode() {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                return getChildren()[0].generateCode() + "add #" + i + " 1\npop #0\npush #" + i + '\n';
            }
        });
        factory.addProduction(F2, new Symbol[] { decrement, F }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public String generateCode() {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                return getChildren()[0].generateCode() + "sub #" + i + " 1\npop #0\npush #" + i + '\n';
            }
        });
        factory.addProduction(F2, new Symbol[] { F, increment }, c -> new AST(c[1].getValue(), c[0]) {
            @Override public String generateCode() {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                return getChildren()[0].generateCode() + "add #" + st.findSymbol(getChildren()[0].getValue().getValue()) + " 1\n";
            }
        });
        factory.addProduction(F2, new Symbol[] { F, decrement }, c -> new AST(c[1].getValue(), c[0]) {
            @Override public String generateCode() {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                return getChildren()[0].generateCode() + "sub #" + st.findSymbol(getChildren()[0].getValue().getValue()) + " 1\n";
            }
        });
        factory.addProduction(F, new Symbol[] { open, E, close }, c -> c[1]);
        factory.addProduction(F, new Symbol[] { number }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return "push " + getValue().getValue() + '\n'; }
        });
        factory.addProduction(F, new Symbol[] { identifier }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return "push #" + st.findSymbol(getValue().getValue()) + '\n'; }
        });
        factory.addProduction(F, new Symbol[] { kwFalse }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return "push 0\n"; }
        });
        factory.addProduction(F, new Symbol[] { kwTrue }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public String generateCode() { return "push 1\n"; }
        });
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, S1 }, c ->
                new AST(c[0].getValue(), c[2], c[4]) {
                    @Override public String generateCode() {
                        st.enterScope();
                        String body = getChildren()[1].generateCode();
                        st.exitScope();
                        return getChildren()[0].generateCode() +
                                "pop #0\nje 0 #0 skip" + skip[0] + '\n' + body + ":skip" + skip[0]++ + '\n';
                    }
                }
        );
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, openCurly, B, closeCurly }, c ->
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
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, openCurly, B, closeCurly }, c ->
                new AST(c[0].getValue(), c[2], c[3], c[5], c[8]) {
                    @Override public String generateCode() {
                        st.enterScope();
                        String init = getChildren()[0].generateCode();
                        String condition = getChildren()[1].generateCode();
                        String increment = getChildren()[2].generateCode();
                        st.enterScope();
                        String body = getChildren()[3].generateCode();
                        st.exitScope();
                        st.exitScope();
                        return init + ":skip" + skip[0]++ + '\n' + condition + "pop #0\nje 0 #0 skip" + skip[0]
                                + '\n' + body + increment + "jmp skip" + (skip[0] - 1) + "\n:skip" + skip[0]++ + '\n';
                    }
                });
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, S1 }, c ->
                new AST(c[0].getValue(), c[2], c[3], c[5], c[7]) {
                    @Override public String generateCode() {
                        st.enterScope();
                        String init = getChildren()[0].generateCode();
                        String condition = getChildren()[1].generateCode();
                        String increment = getChildren()[2].generateCode();
                        st.enterScope();
                        String body = getChildren()[3].generateCode();
                        st.exitScope();
                        st.exitScope();
                        return init + ":skip" + skip[0]++ + '\n' + condition + "pop #0\nje 0 #0 skip" + skip[0]
                                + '\n' + body + increment + "jmp skip" + (skip[0] - 1) + "\n:skip" + skip[0]++ + '\n';
                    }
                });

        Grammar g = new Grammar(B);
//        System.out.println(g);
//        System.out.println();

//        t.forEachRemaining(System.out::println);
//        System.out.println(new Parser(g).parse(t, factory));
        KVM.run(new Parser(g).parse(t, factory).generateCode());
    }
}
