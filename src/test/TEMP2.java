package test;

import ast.AST;
import ast.ASTFactory;
import jvm.Opcodes;
import jvm.classes.ConstPool;
import jvm.methods.Code;
import jvm.methods.Frame;
import lang.*;
import parsing.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/27/2017.
 */
public class TEMP2 implements Opcodes
{
    public static void t(ConstPool constPool, MethodWriter writer, String source) throws Exception {
        Language KLMN = new Language();
        TokenStream t = KLMN.tokenize(source);

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

        // TODO: specialized AST node classes (Expressions, Conditions, Loops,
        // TODO:    and Eventually Methods (with MethodInfos & Codes) & Classes (with ClassFiles))

        ASTFactory factory = new ASTFactory();
        //<editor-fold desc="Factory">
        factory.addProduction(B, new Symbol[] { S1 }, c -> new AST(new Token(null, "Block"), c[0]) {
            @Override public void write(MethodWriter writer) {
                writer.enterScope();
                getChildren()[0].write(writer);
                writer.exitScope();
            }
        });
        factory.addProduction(S1, new Symbol[] { S, semicolon }, c -> c[0]);
        factory.addProduction(B, new Symbol[] { B, S1 }, c -> {
            AST[] children = new AST[c[0].getChildren().length + 1];
            System.arraycopy(c[0].getChildren(), 0, children, 0, c[0].getChildren().length);
            children[children.length - 1] = c[1];
            return new AST(new Token(null, "Block"), children) {
                @Override public void write(MethodWriter writer) {
                    writer.enterScope();
                    for (AST ast : getChildren()) ast.write(writer);
                    writer.exitScope();
                }
            };
        });
        factory.addProduction(S, new Symbol[] { A }, c -> c[0]);
        factory.addProduction(A, new Symbol[] { var, identifier, assign, E }, c ->
                new AST(c[2].getValue(), c[1], c[3]) {
                    @Override public void write(MethodWriter writer) {
                        String name = getChildren()[0].getValue().getValue();
                        if (writer.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                        writer.addSymbol(name);
                        getChildren()[1].write(writer);
                        writer.popToLocal(name);
                    }
                });
        factory.addProduction(A, new Symbol[] { identifier, assign, E }, c ->
                new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        getChildren()[1].write(writer);// + "pop #" + writer.findSymbol(getChildren()[0].getValue().getValue()) + '\n';
                        writer.popToLocal(getChildren()[0].getValue().getValue());
                    }
                });
        factory.addProduction(S, new Symbol[] { print, E }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void write(MethodWriter writer) {
                writer.pushStaticField("java/lang/System", "out", "Ljava/io/PrintStream;");
                getChildren()[0].write(writer);
                writer.call("java/io/PrintStream", "println", "V", "F");
            }
        });
        factory.addProduction(E, new Symbol[] { T4 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { E, lOr, T4 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        if (writer.isInCond()) {
                            Frame end = writer.getCondEnd(), body = new Frame();
                            writer.setCondEnd(body);
                            writer.setSkipFor(!writer.getSkipFor());
                            getChildren()[0].write(writer); // true -> skip to 'body', false -> resume
                            writer.setSkipFor(!writer.getSkipFor());
                            writer.setCondEnd(end);
                            getChildren()[1].write(writer); // true -> resume, false -> skip to 'end'
                            writer.assignFrame(body);
                            return;
                        }
                        Frame cond2 = new Frame(), end = new Frame();
                        getChildren()[0].write(writer);
                        writer.useJmpOperator(IFEQ, cond2); // if false(=0), check other cond
                        writer.pushInt(1); // if true(=0), push true and end
                        writer.useJmpOperator(GOTO, end);
                        writer.assignFrame(cond2); // here we get the final result
                        getChildren()[1].write(writer);
                        writer.assignFrame(end);
                    }
                });
        factory.addProduction(T4, new Symbol[] { T3 }, c -> c[0]);
        factory.addProduction(T4, new Symbol[] { T4, lAnd, T3 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                @Override public void write(MethodWriter writer) {
                        if (writer.isInCond()) {
                            if (writer.getSkipFor()) {
                                writer.setSkipFor(false);
                                getChildren()[0].write(writer); // true -> skip to END, false -> resume
                                writer.setSkipFor(true);
                                getChildren()[1].write(writer); // true -> skip to END, false -> resume
                                return;
                            }
                            getChildren()[0].write(writer);
                            getChildren()[1].write(writer);
                            return;
                        }
                        Frame cond2 = new Frame(), end = new Frame();
                        getChildren()[0].write(writer);
                        writer.useJmpOperator(IFNE, cond2); // if true(=1), check other cond
                        writer.pushInt(0); // if false(=0), push false and end
                        writer.useJmpOperator(GOTO, end);
                        writer.assignFrame(cond2); // here we get the final result
                        getChildren()[1].write(writer);
                        writer.assignFrame(end);
                    }
                });
        factory.addProduction(T3, new Symbol[] { T2 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T3, equals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        if (writer.isInCond()) {
                            getChildren()[0].write(writer);
                            getChildren()[1].write(writer);
                            writer.useOperator(FCMPG);
                            if (writer.getSkipFor()) writer.useJmpOperator(IFEQ, writer.getCondEnd());
                            else writer.useJmpOperator(IFNE, writer.getCondEnd());
                            return;
                        }
                        getChildren()[0].write(writer);
                        getChildren()[1].write(writer);
                        writer.useOperator(FCMPG);
                        Frame t = new Frame(), f = new Frame();
                        writer.useJmpOperator(IFNE, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assignFrame(f);
                        writer.pushInt(0);
                        writer.assignFrame(t);
                    }
                });
        factory.addProduction(T3, new Symbol[] { T3, nEquals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        if (writer.isInCond()) {
                            getChildren()[0].write(writer);
                            getChildren()[1].write(writer);
                            writer.useOperator(FCMPG);
                            if (!writer.getSkipFor()) writer.useJmpOperator(IFEQ, writer.getCondEnd());
                            else writer.useJmpOperator(IFNE, writer.getCondEnd());
                            return;
                        }
                        getChildren()[0].write(writer);
                        getChildren()[1].write(writer);
                        writer.useOperator(FCMPG);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, less, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        if (writer.isInCond()) {
                            getChildren()[0].write(writer);
                            getChildren()[1].write(writer);
                            writer.useOperator(FCMPG);
                            if (writer.getSkipFor()) writer.useJmpOperator(IFLT, writer.getCondEnd());
                            else writer.useJmpOperator(IFGE, writer.getCondEnd());
                            return;
                        }
                        getChildren()[1].write(writer);
                        getChildren()[0].write(writer);
                        writer.useOperator(FCMPG);
                        writer.pushInt(1);
                        Frame t = new Frame(), f = new Frame();
                        writer.useJmpOperator(IF_ICMPNE, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assignFrame(f);
                        writer.pushInt(0);
                        writer.assignFrame(t);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, greater, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        if (writer.isInCond()) {
                            getChildren()[0].write(writer);
                            getChildren()[1].write(writer);
                            writer.useOperator(FCMPG);
                            if (writer.getSkipFor()) writer.useJmpOperator(IFGT, writer.getCondEnd());
                            else writer.useJmpOperator(IFLE, writer.getCondEnd());
                            return;
                        }
                        getChildren()[0].write(writer);
                        getChildren()[1].write(writer);
                        writer.useOperator(FCMPG);
                        writer.pushInt(1);
                        Frame t = new Frame(), f = new Frame();
                        writer.useJmpOperator(IF_ICMPNE, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assignFrame(f);
                        writer.pushInt(0);
                        writer.assignFrame(t);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, lessEquals, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        if (writer.isInCond()) {
                            getChildren()[0].write(writer);
                            getChildren()[1].write(writer);
                            writer.useOperator(FCMPG);
                            if (writer.getSkipFor()) writer.useJmpOperator(IFLE, writer.getCondEnd());
                            else writer.useJmpOperator(IFGT, writer.getCondEnd());
                            return;
                        }
                        getChildren()[0].write(writer);
                        getChildren()[1].write(writer);
                        writer.useOperator(FCMPG);
                        writer.pushInt(1);
                        Frame t = new Frame(), f = new Frame();
                        writer.useJmpOperator(IF_ICMPEQ, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assignFrame(f);
                        writer.pushInt(0);
                        writer.assignFrame(t);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, greaterEquals, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        if (writer.isInCond()) {
                            getChildren()[0].write(writer);
                            getChildren()[1].write(writer);
                            writer.useOperator(FCMPG);
                            if (writer.getSkipFor()) writer.useJmpOperator(IFGE, writer.getCondEnd());
                            else writer.useJmpOperator(IFLT, writer.getCondEnd());
                            return;
                        }
                        getChildren()[1].write(writer);
                        getChildren()[0].write(writer);
                        writer.useOperator(FCMPG);
                        writer.pushInt(1);
                        Frame t = new Frame(), f = new Frame();
                        writer.useJmpOperator(IF_ICMPEQ, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assignFrame(f);
                        writer.pushInt(0);
                        writer.assignFrame(t);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T1 }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T1, plus, T },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        getChildren()[0].write(writer);
                        getChildren()[1].write(writer);
                        writer.useOperator(FADD);
                    }
                });
        factory.addProduction(T1, new Symbol[] { T1, minus, T },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        getChildren()[0].write(writer);
                        getChildren()[1].write(writer);
                        writer.useOperator(FSUB);
                    }
                });
        factory.addProduction(T, new Symbol[] { T, times, F1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        getChildren()[0].write(writer);
                        getChildren()[1].write(writer);
                        writer.useOperator(FMUL);
                    }
                });
        factory.addProduction(T, new Symbol[] { T, divide, F1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer) {
                        getChildren()[0].write(writer);
                        getChildren()[1].write(writer);
                        writer.useOperator(FDIV);
                    }
                });
        factory.addProduction(T, new Symbol[] { F1 }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { F }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { plus, F1 }, c -> c[1]);
        factory.addProduction(F1, new Symbol[] { minus, F1 }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void write(MethodWriter writer) {
                getChildren()[0].write(writer);
                writer.useOperator(FNEG);
            }
        });
        factory.addProduction(F1, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(S, new Symbol[] { F2 }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(MethodWriter writer) {
                c[0].write(writer);
                writer.pop();
            }
        });
        factory.addProduction(F2, new Symbol[] { increment, F }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void write(MethodWriter writer) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                String name = getChildren()[0].getValue().getValue();
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToLocal(name);
                writer.pushLocal(name);
            }
        });
        factory.addProduction(F2, new Symbol[] { decrement, F }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void write(MethodWriter writer) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                String name = getChildren()[0].getValue().getValue();
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToLocal(name);
                writer.pushLocal(name);
            }
        });
        factory.addProduction(F2, new Symbol[] { F, increment }, c -> new AST(c[1].getValue(), c[0]) {
            @Override public void write(MethodWriter writer) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                String name = getChildren()[0].getValue().getValue();
                writer.pushLocal(name);
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToLocal(name);
            }
        });
        factory.addProduction(F2, new Symbol[] { F, decrement }, c -> new AST(c[1].getValue(), c[0]) {
            @Override public void write(MethodWriter writer) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                String name = getChildren()[0].getValue().getValue();
                writer.pushLocal(name);
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToLocal(name);
            }
        });
        factory.addProduction(F, new Symbol[] { open, E, close }, c -> c[1]);
        factory.addProduction(F, new Symbol[] { number }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(MethodWriter writer) {
                writer.pushFloat(Float.valueOf(getValue().getValue()));
            }
        });
        factory.addProduction(F, new Symbol[] { identifier }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(MethodWriter writer) {
                writer.pushLocal(getValue().getValue());
            }
        });
        factory.addProduction(F, new Symbol[] { kwFalse }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(MethodWriter writer) {
                if (writer.isInCond()) {
                    if (!writer.getSkipFor()) writer.useJmpOperator(GOTO, writer.getCondEnd());
                    return;
                }
                writer.pushInt(0);
            }
        });
        factory.addProduction(F, new Symbol[] { kwTrue }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(MethodWriter writer) {
                if (writer.isInCond()) {
                    if (writer.getSkipFor()) writer.useJmpOperator(GOTO, writer.getCondEnd());
                    return;
                }
                writer.pushInt(1);
            }
        });
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, S1 }, c ->
        new AST(c[0].getValue(), c[2], c[4]) {
            @Override public void write(MethodWriter writer) {
                Frame end = new Frame();
                writer.setCondEnd(end);
                writer.setInCond(true);
                getChildren()[0].write(writer);
                writer.setInCond(false);
                writer.enterScope();
                getChildren()[1].write(writer);
                writer.exitScope();
                writer.assignFrame(end);
            }
        });
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, openCurly, B, closeCurly }, c ->
                new AST(c[0].getValue(), c[2], c[5]) {
                    @Override public void write(MethodWriter writer) {
                        Frame end = new Frame();
                        writer.setCondEnd(end);
                        writer.setInCond(true);
                        getChildren()[0].write(writer);
                        writer.setInCond(false);
                        writer.enterScope();
                        getChildren()[1].write(writer);
                        writer.exitScope();
                        writer.assignFrame(end);
                    }
                }
        );
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, openCurly, B, closeCurly }, c ->
                new AST(c[0].getValue(), c[2], c[3], c[5], c[8]) {
                    @Override public void write(MethodWriter writer) {
                        Frame loop = new Frame(), end = new Frame();
                        writer.enterScope();
                        getChildren()[0].write(writer);
                        writer.assignFrame(loop);
                        writer.setCondEnd(end);
                        writer.setInCond(true);
                        getChildren()[1].write(writer);
                        writer.setInCond(false);
                        writer.enterScope();
                        getChildren()[3].write(writer);
                        writer.exitScope();
                        getChildren()[2].write(writer);
                        writer.useJmpOperator(GOTO, loop);
                        writer.exitScope();
                        writer.assignFrame(end);
                    }
                });
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, S1 }, c ->
                new AST(c[0].getValue(), c[2], c[3], c[5], c[7]) {
                    @Override public void write(MethodWriter writer) {
                        Frame loop = new Frame(), end = new Frame();
                        writer.enterScope();
                        getChildren()[0].write(writer);
                        writer.assignFrame(loop);
                        writer.setCondEnd(end);
                        writer.setInCond(true);
                        getChildren()[1].write(writer);
                        writer.setInCond(false);
                        writer.enterScope();
                        getChildren()[3].write(writer);
                        writer.exitScope();
                        getChildren()[2].write(writer);
                        writer.useJmpOperator(GOTO, loop);
                        writer.exitScope();
                        writer.assignFrame(end);
                    }
                });
        //</editor-fold>

        Grammar g = new Grammar(B);
        new Parser(g).parse(t, factory).write(writer);
    }
}
