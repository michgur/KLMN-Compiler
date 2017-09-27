package test;

import ast.AST;
import ast.ASTFactory;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
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
    public static int t(MethodVisitor mv, String code) throws Exception {
        Language KLMN = new Language();
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
        ASTFactory factory = new ASTFactory();
        factory.addProduction(B, new Symbol[] { S1 }, c -> new AST(new Token(null, "Block"), c[0]) {
            @Override public void apply(MethodVisitor mv) {
                st.enterScope();
                getChildren()[0].apply(mv);
                removeLocals(st.exitScope());
            }
        });
        factory.addProduction(S1, new Symbol[] { S, semicolon }, c -> c[0]);
        factory.addProduction(B, new Symbol[] { B, S1 }, c -> {
            AST[] children = new AST[c[0].getChildren().length + 1];
            System.arraycopy(c[0].getChildren(), 0, children, 0, c[0].getChildren().length);
            children[children.length - 1] = c[1];
            return new AST(new Token(null, "Block"), children) {
                @Override public void apply(MethodVisitor mv) {
                    st.enterScope();
                    for (AST ast : getChildren()) ast.apply(mv);
                    removeLocals(st.exitScope());
                }
            };
        });
        factory.addProduction(S, new Symbol[] { A }, c -> c[0]);
        factory.addProduction(A, new Symbol[] { var, identifier, assign, E }, c ->
                new AST(c[2].getValue(), c[1], c[3]) {
                    @Override public void apply(MethodVisitor mv) {
                        String name = getChildren()[0].getValue().getValue();
                        if (st.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                        st.addSymbol(name);
                        getChildren()[1].apply(mv);
                        mv.visitVarInsn(FSTORE, st.findSymbol(name));
                        addLocal(FLOAT);
                    }
                });
        factory.addProduction(A, new Symbol[] { identifier, assign, E }, c ->
                new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[1].apply(mv);// + "pop #" + st.findSymbol(getChildren()[0].getValue().getValue()) + '\n';
                        mv.visitVarInsn(FSTORE, st.findSymbol(getChildren()[0].getValue().getValue()));
                    }
                });
        factory.addProduction(S, new Symbol[] { print, E }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void apply(MethodVisitor mv) {
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                getChildren()[0].apply(mv);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);
            }
        });
        factory.addProduction(E, new Symbol[] { T4 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { E, lOr, T4 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        Label cond2 = new Label(), end = new Label();
                        getChildren()[0].apply(mv);
                        mv.visitJumpInsn(IFEQ, cond2); // if false(=0), check other cond
                        mv.visitInsn(ICONST_1); // if true(=0), push true and end
                        mv.visitJumpInsn(GOTO, end);
                        mv.visitLabel(cond2); // here we get the final result
                        applyFrame(mv, 0, null);
                        getChildren()[1].apply(mv);
                        mv.visitLabel(end);
                        applyFrame(mv, 1, new Object[] { INTEGER });
                    }
                });
        factory.addProduction(T4, new Symbol[] { T3 }, c -> c[0]);
        factory.addProduction(T4, new Symbol[] { T4, lAnd, T3 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                @Override public void apply(MethodVisitor mv) {
                        Label cond2 = new Label(), end = new Label();
                        getChildren()[0].apply(mv);
                        mv.visitJumpInsn(IFNE, cond2); // if true(=1), check other cond
                        mv.visitInsn(ICONST_0); // if false(=0), push false and end
                        mv.visitJumpInsn(GOTO, end);
                        mv.visitLabel(cond2); // here we get the final result
                        applyFrame(mv, 0, null);
                        getChildren()[1].apply(mv);
                        mv.visitLabel(end);
                        applyFrame(mv, 1, new Object[] { INTEGER });
                    }
                });

        Label skipIf;
        factory.addProduction(T3, new Symbol[] { T2 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T3, equals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        getChildren()[1].apply(mv);
                        mv.visitInsn(FCMPG);
                        Label t = new Label(), f = new Label();
                        mv.visitJumpInsn(IFNE, f);
                        mv.visitInsn(ICONST_1);
                        mv.visitJumpInsn(GOTO, t);
                        mv.visitLabel(f);
                        applyFrame(mv, 0, null);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(t);
                        applyFrame(mv, 1, new Object[] { INTEGER });
                    }
                });
        factory.addProduction(T3, new Symbol[] { T3, nEquals, T2 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        getChildren()[1].apply(mv);
                        mv.visitInsn(FCMPG);
                    }
                });
        // todo: you can be smarter about these expressions if you know for sure that they are inside IF
        // (by utilizing the if_icmp<> instructions directly
        // i'll have to separate if statements from boolean expressions in the future
        // hierarchy: AND + OR come before EQ, NE, LT, LE, GT & GE
        // the IF requires one label by default. every OR adds another label, but ANDs don't
        //      the other operators operate on numbers, so they can be treated just-
        //      like arithmetic operators - who don't need JMPs and shit.
        //      but NVM dat, since the operator determines the type of JMP. I don't-
        //      wanna change the entire tree structure just for that shite.
        //
        // SO:
        // IF:
        //      COND
        //      FALSE- SKIP
        //      BODY
        //      SKIP
        //
        // AND:
        //      COND1
        //      FALSE- SKIP
        //      COND2
        //      FALSE- SKIP
        //
        // OR:
        //      COND1
        //      TRUE- SKIP2
        //      COND2
        //      SKIP2
        factory.addProduction(T2, new Symbol[] { T2, less, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[1].apply(mv);
                        getChildren()[0].apply(mv);
                        mv.visitInsn(FCMPG);
                        mv.visitInsn(ICONST_1);
                        Label t = new Label(), f = new Label();
                        mv.visitJumpInsn(IF_ICMPNE, f);
                        mv.visitInsn(ICONST_1);
                        mv.visitJumpInsn(GOTO, t);
                        mv.visitLabel(f);
                        applyFrame(mv, 0, null);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(t);
                        applyFrame(mv, 1, new Object[] { INTEGER });
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, greater, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        getChildren()[1].apply(mv);
                        mv.visitInsn(FCMPG);
                        mv.visitInsn(ICONST_1);
                        Label t = new Label(), f = new Label();
                        mv.visitJumpInsn(IF_ICMPNE, f);
                        mv.visitInsn(ICONST_1);
                        mv.visitJumpInsn(GOTO, t);
                        mv.visitLabel(f);
                        applyFrame(mv, 0, null);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(t);
                        applyFrame(mv, 1, new Object[] { INTEGER });
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, lessEquals, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        getChildren()[1].apply(mv);
                        mv.visitInsn(FCMPG);
                        mv.visitInsn(ICONST_1);
                        Label t = new Label(), f = new Label();
                        mv.visitJumpInsn(IF_ICMPEQ, f);
                        mv.visitInsn(ICONST_1);
                        mv.visitJumpInsn(GOTO, t);
                        mv.visitLabel(f);
                        applyFrame(mv, 0, null);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(t);
                        applyFrame(mv, 1, new Object[] { INTEGER });
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, greaterEquals, T1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[1].apply(mv);
                        getChildren()[0].apply(mv);
                        mv.visitInsn(FCMPG);
                        mv.visitInsn(ICONST_1);
                        Label t = new Label(), f = new Label();
                        mv.visitJumpInsn(IF_ICMPEQ, f);
                        mv.visitInsn(ICONST_1);
                        mv.visitJumpInsn(GOTO, t);
                        mv.visitLabel(f);
                        applyFrame(mv, 0, null);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(t);
                        applyFrame(mv, 1, new Object[] { INTEGER });
                    }
                });
        factory.addProduction(T2, new Symbol[] { T1 }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T1, plus, T },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        getChildren()[1].apply(mv);
                        mv.visitInsn(FADD);
                    }
                });
        factory.addProduction(T1, new Symbol[] { T1, minus, T },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        getChildren()[1].apply(mv);
                        mv.visitInsn(FSUB);
                    }
                });
        factory.addProduction(T, new Symbol[] { T, times, F1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        getChildren()[1].apply(mv);
                        mv.visitInsn(FMUL);
                    }
                });
        factory.addProduction(T, new Symbol[] { T, divide, F1 },
                c -> new AST(c[1].getValue(), c[0], c[2]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        getChildren()[1].apply(mv);
                        mv.visitInsn(FDIV);
                    }
                });
        factory.addProduction(T, new Symbol[] { F1 }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { F }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { plus, F1 }, c -> c[1]);
        factory.addProduction(F1, new Symbol[] { minus, F1 }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void apply(MethodVisitor mv) {
                getChildren()[0].apply(mv);
                mv.visitInsn(FNEG);
            }
        });
        factory.addProduction(F1, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(S, new Symbol[] { F2 }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void apply(MethodVisitor mv) {
                c[0].apply(mv);
                mv.visitInsn(POP);
            }
        });
        factory.addProduction(F2, new Symbol[] { increment, F }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void apply(MethodVisitor mv) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                mv.visitVarInsn(FLOAD, i);
                mv.visitInsn(FCONST_1);
                mv.visitInsn(FADD);
                mv.visitVarInsn(FSTORE, i);
                mv.visitVarInsn(FLOAD, i);
            }
        });
        factory.addProduction(F2, new Symbol[] { decrement, F }, c -> new AST(c[0].getValue(), c[1]) {
            @Override public void apply(MethodVisitor mv) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                mv.visitVarInsn(FLOAD, i);
                mv.visitInsn(FCONST_1);
                mv.visitInsn(FSUB);
                mv.visitVarInsn(FSTORE, i);
                mv.visitVarInsn(FLOAD, i);
            }
        });
        factory.addProduction(F2, new Symbol[] { F, increment }, c -> new AST(c[1].getValue(), c[0]) {
            @Override public void apply(MethodVisitor mv) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                mv.visitVarInsn(FLOAD, i);
                mv.visitVarInsn(FLOAD, i);
                mv.visitInsn(FCONST_1);
                mv.visitInsn(FADD);
                mv.visitVarInsn(FSTORE, i);
            }
        });
        factory.addProduction(F2, new Symbol[] { F, decrement }, c -> new AST(c[1].getValue(), c[0]) {
            @Override public void apply(MethodVisitor mv) {
                if (getChildren()[0].getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(getChildren()[0].getValue().getValue());
                mv.visitVarInsn(FLOAD, i);
                mv.visitVarInsn(FLOAD, i);
                mv.visitInsn(FCONST_1);
                mv.visitInsn(FSUB);
                mv.visitVarInsn(FSTORE, i);
            }
        });
        factory.addProduction(F, new Symbol[] { open, E, close }, c -> c[1]);
        factory.addProduction(F, new Symbol[] { number }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void apply(MethodVisitor mv) {
                mv.visitLdcInsn(Float.valueOf(getValue().getValue()));
            }
        });
        factory.addProduction(F, new Symbol[] { identifier }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void apply(MethodVisitor mv) {
                mv.visitVarInsn(FLOAD, st.findSymbol(getValue().getValue()));
            }
        });
        factory.addProduction(F, new Symbol[] { kwFalse }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void apply(MethodVisitor mv) { mv.visitInsn(ICONST_0); }
        });
        factory.addProduction(F, new Symbol[] { kwTrue }, c -> new AST(c[0].getValue(), c[0].getChildren()) {
            @Override public void apply(MethodVisitor mv) { mv.visitInsn(ICONST_1); }
        });
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, S1 }, c ->
        new AST(c[0].getValue(), c[2], c[4]) {
            @Override public void apply(MethodVisitor mv) {
                getChildren()[0].apply(mv);
                Label end = new Label();
                mv.visitJumpInsn(IFEQ, end);
                st.enterScope();
                getChildren()[1].apply(mv);
                removeLocals(st.exitScope());
                mv.visitLabel(end);
                applyFrame(mv, 0, null);
            }
        });
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, openCurly, B, closeCurly }, c ->
                new AST(c[0].getValue(), c[2], c[5]) {
                    @Override public void apply(MethodVisitor mv) {
                        getChildren()[0].apply(mv);
                        Label end = new Label();
                        mv.visitJumpInsn(IFEQ, end);
                        st.enterScope();
                        getChildren()[1].apply(mv);
                        removeLocals(st.exitScope());
                        mv.visitLabel(end);
                        applyFrame(mv, 0, null);
                    }
                }
        );
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, openCurly, B, closeCurly }, c ->
                new AST(c[0].getValue(), c[2], c[3], c[5], c[8]) {
                    @Override public void apply(MethodVisitor mv) {
                        Label loop = new Label(), end = new Label();
                        st.enterScope();
                        getChildren()[0].apply(mv);
                        mv.visitLabel(loop);
                        applyFrame(mv, 0, null);
                        getChildren()[1].apply(mv);
                        mv.visitJumpInsn(IFEQ, end);
                        st.enterScope();
                        getChildren()[3].apply(mv);
                        removeLocals(st.exitScope());
                        getChildren()[2].apply(mv);
                        mv.visitJumpInsn(GOTO, loop);
                        removeLocals(st.exitScope());
                        mv.visitLabel(end);
                        applyFrame(mv, 0, null);
                    }
                });
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, S1 }, c ->
                new AST(c[0].getValue(), c[2], c[3], c[5], c[7]) {
                    @Override public void apply(MethodVisitor mv) {
                        Label loop = new Label(), end = new Label();
                        st.enterScope();
                        getChildren()[0].apply(mv);
                        mv.visitLabel(loop);
                        applyFrame(mv, 0, null);
                        getChildren()[1].apply(mv);
                        mv.visitJumpInsn(IFEQ, end);
                        st.enterScope();
                        getChildren()[3].apply(mv);
                        removeLocals(st.exitScope());
                        getChildren()[2].apply(mv);
                        mv.visitJumpInsn(GOTO, loop);
                        removeLocals(st.exitScope());
                        mv.visitLabel(end);
                        applyFrame(mv, 0, null);
                    }
                });

        Grammar g = new Grammar(B);
        new Parser(g).parse(t, factory).apply(mv);
        return maxLocals;
    }

    private static int maxLocals = 1;
    private static int localSize = 1;
    private static List<Object> locals = new ArrayList<>();
    static { locals.add("[Ljava/lang/String;"); }

    private static void addLocal(Object type) {
        locals.add(type);
        if (locals.size() > maxLocals) maxLocals = locals.size();
    }
    private static void removeLocal() { locals.remove(locals.size() - 1); }
    private static void removeLocals(int size) { for (int i = 0; i < size; i++) removeLocal(); }

    private static void applyFrame(MethodVisitor mv, int stackSize, Object[] stack) {  // currently only manages locals
        int newSize = locals.size(); // currently doesn't take care of longs/doubles
        if (newSize == localSize && stackSize == 0) mv.visitFrame(F_SAME, 0, null, 0, null);
        else if (newSize == localSize && stackSize == 1)
            try { mv.visitFrame(F_SAME1, 0, null, 1, stack); }
            catch (IllegalStateException e) {} // could be called twice in a row, whatever
        else if (stackSize != 0) mv.visitFrame(F_FULL, newSize, locals.toArray(), stackSize, stack);
        else if (newSize > localSize) mv.visitFrame(F_APPEND, newSize - localSize,
                locals.subList(localSize, newSize).toArray(), 0, null);
        else mv.visitFrame(F_CHOP, localSize - newSize, null, 0, null);
        localSize = newSize;
    }
}
