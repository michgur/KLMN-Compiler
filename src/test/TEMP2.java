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
    public static void t(ConstPool constPool, Code code, String source) throws Exception {
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

        // TODO: merge Code and these vars to some writer class
        // TODO:    (perhaps keep the lower-level shit at Code class, and add writing funcs to the writer)
        // TODO:    it should manage scopes and vars automatically, & help to avoid lots of repetition
        SymbolTable st = new SymbolTable();
        st.enterScope();
        ASTFactory factory = new ASTFactory();
        ///////conditions
        final Frame[] condEnd = { new Frame() }; // the end of current condition
        final boolean[] insideCond = { false }, // whether current AST node is inside a condition
                skipFor = { false }; // for conditions, the boolean they should skip to condEnd for
        /////////////////
        //<editor-fold desc="Factory">
        factory.addProduction(B, new Symbol[] { S1 }, c -> new AST(new Token(null, "Block"), c[0]) {
            @Override public void write(Code code) {
                st.enterScope();
                getChildren()[0].write(code);
                code.chop(st.exitScope());
            }
        });
        factory.addProduction(S1, new Symbol[] { S, semicolon }, c -> c[0]);
        factory.addProduction(B, new Symbol[] { B, S1 }, c -> {
            AST[] children = new AST[c[0].getChildren().length + 1];
            System.arraycopy(c[0].getChildren(), 0, children, 0, c[0].getChildren().length);
            children[children.length - 1] = c[1];
            return new AST(new Token(null, "Block"), children) {
                @Override public void write(Code code) {
                    st.enterScope();
                    for (AST ast : getChildren()) ast.write(code);
                    code.chop(st.exitScope());
                }
            };
        });
        factory.addProduction(S, new Symbol[] { A }, c -> c[0]);
        factory.addProduction(A, new Symbol[] { var, identifier, assign, E }, c ->
                new AST(c[2].getValue(), c[1], c[3]) {
                    @Override public void write(Code code) {
                        String name = getChildren()[0].getValue().getValue();
                        if (st.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                        st.addSymbol(name);
                        getChildren()[1].write(code);
                        code.popToLocal(st.findSymbol(name));
                    }
                });
        factory.addProduction(A, new Symbol[] { identifier, assign, E }, c ->
                new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        getChildren()[1].write(code);// + "pop #" + st.findSymbol(getChildren()[0].getValue().getValue()) + '\n';
                        code.popToLocal(st.findSymbol(getChildren()[0].getValue().getValue()));
                    }
                });
        factory.addProduction(S, new Symbol[] { print, E }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void write(Code code) {
                code.pushField(GETSTATIC, constPool.addFieldref("java/lang/System", "out", 
                        "Ljava/io/PrintStream;"), "Ljava/io/PrintStream;");
                getChildren()[0].write(code);
                code.invoke(INVOKEVIRTUAL, constPool.addMethodref("java/io/PrintStream", 
                        "println", "(F)V"), "V", 1);
            }
        });
        factory.addProduction(E, new Symbol[] { T4 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { E, lOr, T4 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        if (insideCond[0]) {
                            Frame end = condEnd[0], body = new Frame();
                            condEnd[0] = body;
                            skipFor[0] = !skipFor[0];
                            getChildren()[0].write(code); // true -> skip to 'body', false -> resume
                            skipFor[0] = !skipFor[0];
                            condEnd[0] = end;
                            getChildren()[1].write(code); // true -> resume, false -> skip to 'end'
                            code.assignFrame(body);
                            return;
                        }
                        Frame cond2 = new Frame(), end = new Frame();
                        getChildren()[0].write(code);
                        code.unaryJmpOperator(IFEQ, cond2); // if false(=0), check other cond
                        code.pushInt(1); // if true(=0), push true and end
                        code.jmpOperator(GOTO, end);
                        code.assignFrame(cond2); // here we get the final result
                        getChildren()[1].write(code);
                        code.assignFrame(end);
                    }
                });
        factory.addProduction(T4, new Symbol[] { T3 }, c -> c[0]);
        factory.addProduction(T4, new Symbol[] { T4, lAnd, T3 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                @Override public void write(Code code) {
                        if (insideCond[0]) {
                            if (skipFor[0]) {
                                skipFor[0] = false;
                                getChildren()[0].write(code); // true -> skip to END, false -> resume
                                skipFor[0] = true;
                                getChildren()[1].write(code); // true -> skip to END, false -> resume
                                return;
                            }
                            getChildren()[0].write(code);
                            getChildren()[1].write(code);
                            return;
                        }
                        Frame cond2 = new Frame(), end = new Frame();
                        getChildren()[0].write(code);
                        code.unaryJmpOperator(IFNE, cond2); // if true(=1), check other cond
                        code.pushInt(0); // if false(=0), push false and end
                        code.jmpOperator(GOTO, end);
                        code.assignFrame(cond2); // here we get the final result
                        getChildren()[1].write(code);
                        code.assignFrame(end);
                    }
                });
        factory.addProduction(T3, new Symbol[] { T2 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T3, equals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        if (insideCond[0]) {
                            getChildren()[0].write(code);
                            getChildren()[1].write(code);
                            code.binaryOperator(FCMPG);
                            if (skipFor[0]) code.unaryJmpOperator(IFEQ, condEnd[0]);
                            else code.unaryJmpOperator(IFNE, condEnd[0]);
                            return;
                        }
                        getChildren()[0].write(code);
                        getChildren()[1].write(code);
                        code.binaryOperator(FCMPG);
                        Frame t = new Frame(), f = new Frame();
                        code.unaryJmpOperator(IFNE, f);
                        code.pushInt(1);
                        code.jmpOperator(GOTO, t);
                        code.assignFrame(f);
                        code.pushInt(0);
                        code.assignFrame(t);
                    }
                });
        factory.addProduction(T3, new Symbol[] { T3, nEquals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        if (insideCond[0]) {
                            getChildren()[0].write(code);
                            getChildren()[1].write(code);
                            code.binaryOperator(FCMPG);
                            if (!skipFor[0]) code.unaryJmpOperator(IFEQ, condEnd[0]);
                            else code.unaryJmpOperator(IFNE, condEnd[0]);
                            return;
                        }
                        getChildren()[0].write(code);
                        getChildren()[1].write(code);
                        code.binaryOperator(FCMPG);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, less, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        if (insideCond[0]) {
                            getChildren()[0].write(code);
                            getChildren()[1].write(code);
                            code.binaryOperator(FCMPG);
                            if (skipFor[0]) code.unaryJmpOperator(IFLT, condEnd[0]);
                            else code.unaryJmpOperator(IFGE, condEnd[0]);
                            return;
                        }
                        getChildren()[1].write(code);
                        getChildren()[0].write(code);
                        code.binaryOperator(FCMPG);
                        code.pushInt(1);
                        Frame t = new Frame(), f = new Frame();
                        code.binaryJmpOperator(IF_ICMPNE, f);
                        code.pushInt(1);
                        code.jmpOperator(GOTO, t);
                        code.assignFrame(f);
                        code.pushInt(0);
                        code.assignFrame(t);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, greater, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        if (insideCond[0]) {
                            getChildren()[0].write(code);
                            getChildren()[1].write(code);
                            code.binaryOperator(FCMPG);
                            if (skipFor[0]) code.unaryJmpOperator(IFGT, condEnd[0]);
                            else code.unaryJmpOperator(IFLE, condEnd[0]);
                            return;
                        }
                        getChildren()[0].write(code);
                        getChildren()[1].write(code);
                        code.binaryOperator(FCMPG);
                        code.pushInt(1);
                        Frame t = new Frame(), f = new Frame();
                        code.binaryJmpOperator(IF_ICMPNE, f);
                        code.pushInt(1);
                        code.jmpOperator(GOTO, t);
                        code.assignFrame(f);
                        code.pushInt(0);
                        code.assignFrame(t);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, lessEquals, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        if (insideCond[0]) {
                            getChildren()[0].write(code);
                            getChildren()[1].write(code);
                            code.binaryOperator(FCMPG);
                            if (skipFor[0]) code.unaryJmpOperator(IFLE, condEnd[0]);
                            else code.unaryJmpOperator(IFGT, condEnd[0]);
                            return;
                        }
                        getChildren()[0].write(code);
                        getChildren()[1].write(code);
                        code.binaryOperator(FCMPG);
                        code.pushInt(1);
                        Frame t = new Frame(), f = new Frame();
                        code.binaryJmpOperator(IF_ICMPEQ, f);
                        code.pushInt(1);
                        code.jmpOperator(GOTO, t);
                        code.assignFrame(f);
                        code.pushInt(0);
                        code.assignFrame(t);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, greaterEquals, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        if (insideCond[0]) {
                            getChildren()[0].write(code);
                            getChildren()[1].write(code);
                            code.binaryOperator(FCMPG);
                            if (skipFor[0]) code.unaryJmpOperator(IFGE, condEnd[0]);
                            else code.unaryJmpOperator(IFLT, condEnd[0]);
                            return;
                        }
                        getChildren()[1].write(code);
                        getChildren()[0].write(code);
                        code.binaryOperator(FCMPG);
                        code.pushInt(1);
                        Frame t = new Frame(), f = new Frame();
                        code.binaryJmpOperator(IF_ICMPEQ, f);
                        code.pushInt(1);
                        code.jmpOperator(GOTO, t);
                        code.assignFrame(f);
                        code.pushInt(0);
                        code.assignFrame(t);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T1 }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T1, plus, T },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        getChildren()[0].write(code);
                        getChildren()[1].write(code);
                        code.binaryOperator(FADD);
                    }
                });
        factory.addProduction(T1, new Symbol[] { T1, minus, T },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        getChildren()[0].write(code);
                        getChildren()[1].write(code);
                        code.binaryOperator(FSUB);
                    }
                });
        factory.addProduction(T, new Symbol[] { T, times, F1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        getChildren()[0].write(code);
                        getChildren()[1].write(code);
                        code.binaryOperator(FMUL);
                    }
                });
        factory.addProduction(T, new Symbol[] { T, divide, F1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(Code code) {
                        getChildren()[0].write(code);
                        getChildren()[1].write(code);
                        code.binaryOperator(FDIV);
                    }
                });
        factory.addProduction(T, new Symbol[] { F1 }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { F }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { plus, F1 }, c -> c[1]);
        factory.addProduction(F1, new Symbol[] { minus, F1 }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void write(Code code) {
                getChildren()[0].write(code);
                code.unaryOperator(FNEG);
            }
        });
        factory.addProduction(F1, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(S, new Symbol[] { F2 }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(Code code) {
                c[0].write(code);
                code.pop();
            }
        });
        factory.addProduction(F2, new Symbol[] { increment, F }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void write(Code code) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                code.pushLocal(i);
                code.pushFloat(1);
                code.binaryOperator(FADD);
                code.popToLocal(i);
                code.pushLocal(i);
            }
        });
        factory.addProduction(F2, new Symbol[] { decrement, F }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void write(Code code) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                code.pushLocal(i);
                code.pushFloat(1);
                code.binaryOperator(FSUB);
                code.popToLocal(i);
                code.pushLocal(i);
            }
        });
        factory.addProduction(F2, new Symbol[] { F, increment }, c -> new AST(c[1].getValue(), c[0]) {
            @Override public void write(Code code) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                code.pushLocal(i);
                code.pushLocal(i);
                code.pushFloat(1);
                code.binaryOperator(FADD);
                code.popToLocal(i);
            }
        });
        factory.addProduction(F2, new Symbol[] { F, decrement }, c -> new AST(c[1].getValue(), c[0]) {
            @Override public void write(Code code) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                code.pushLocal(i);
                code.pushLocal(i);
                code.pushFloat(1);
                code.binaryOperator(FSUB);
                code.popToLocal(i);
            }
        });
        factory.addProduction(F, new Symbol[] { open, E, close }, c -> c[1]);
        factory.addProduction(F, new Symbol[] { number }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(Code code) {
                code.pushFloat(Float.valueOf(getValue().getValue()));
            }
        });
        factory.addProduction(F, new Symbol[] { identifier }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(Code code) {
                code.pushLocal(st.findSymbol(getValue().getValue()));
            }
        });
        factory.addProduction(F, new Symbol[] { kwFalse }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(Code code) {
                if (insideCond[0]) {
                    if (!skipFor[0]) code.jmpOperator(GOTO, condEnd[0]);
                    return;
                }
                code.pushInt(0);
            }
        });
        factory.addProduction(F, new Symbol[] { kwTrue }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(Code code) {
                if (insideCond[0]) {
                    if (skipFor[0]) code.jmpOperator(GOTO, condEnd[0]);
                    return;
                }
                code.pushInt(1);
            }
        });
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, S1 }, c ->
        new AST(c[0].getValue(), c[2], c[4]) {
            @Override public void write(Code code) {
                Frame end = new Frame();
                condEnd[0] = end;
                insideCond[0] = true;
                getChildren()[0].write(code);
                insideCond[0] = false;
                st.enterScope();
                getChildren()[1].write(code);
                code.chop(st.exitScope());
                code.assignFrame(end);
            }
        });
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, openCurly, B, closeCurly }, c ->
                new AST(c[0].getValue(), c[2], c[5]) {
                    @Override public void write(Code code) {
                        Frame end = new Frame();
                        condEnd[0] = end;
                        insideCond[0] = true;
                        getChildren()[0].write(code);
                        insideCond[0] = false;
                        st.enterScope();
                        getChildren()[1].write(code);
                        code.chop(st.exitScope());
                        code.assignFrame(end);
                    }
                }
        );
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, openCurly, B, closeCurly }, c ->
                new AST(c[0].getValue(), c[2], c[3], c[5], c[8]) {
                    @Override public void write(Code code) {
                        Frame loop = new Frame(), end = new Frame();
                        st.enterScope();
                        getChildren()[0].write(code);
                        code.assignFrame(loop);
                        condEnd[0] = end;
                        insideCond[0] = true;
                        getChildren()[1].write(code);
                        insideCond[0] = false;
                        st.enterScope();
                        getChildren()[3].write(code);
                        code.chop(st.exitScope());
                        getChildren()[2].write(code);
                        code.jmpOperator(GOTO, loop);
                        code.chop(st.exitScope());
                        code.assignFrame(end);
                    }
                });
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, S1 }, c ->
                new AST(c[0].getValue(), c[2], c[3], c[5], c[7]) {
                    @Override public void write(Code code) {
                        Frame loop = new Frame(), end = new Frame();
                        st.enterScope();
                        getChildren()[0].write(code);
                        code.assignFrame(loop);
                        condEnd[0] = end;
                        insideCond[0] = true;
                        getChildren()[1].write(code);
                        insideCond[0] = false;
                        st.enterScope();
                        getChildren()[3].write(code);
                        code.chop(st.exitScope());
                        getChildren()[2].write(code);
                        code.jmpOperator(GOTO, loop);
                        code.chop(st.exitScope());
                        code.assignFrame(end);
                    }
                });
        //</editor-fold>

        Grammar g = new Grammar(B);
        new Parser(g).parse(t, factory).write(code);
    }
}
