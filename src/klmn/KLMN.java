package klmn;

import ast.AST;
import ast.ASTFactory;
import jvm.Opcodes;
import jvm.methods.Frame;
import klmn.nodes.*;
import lang.*;
import parsing.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * ಠ^ಠ.
 * Created by Michael on 9/27/2017.
 */
public class KLMN implements Opcodes
{
    private static Language lang = new Language();
    private static Grammar grammar;
    private static ASTFactory factory = new ASTFactory();

    private static String name = "";

    // NEXT TODO (NOW!): proper TypeEnv
    // BIG TODO (but for later): instead of having everything in the AST, have some module builder that gets it as a parameter
    public static void compile(String name, String src) throws Exception {
        KLMN.name = name;
        KGrammarNew.moduleName = name;
        ModuleNode module = ((ModuleNode) new Parser(KGrammarNew.grammar).parse(KGrammarNew.lang.tokenize(src), KGrammarNew.factory));
        module.run();
    }

    /* 🔥🔥🔥 ⛧ WELCOME TO HELL ⛧ 🔥🔥🔥
    static {
        Symbol M = new Symbol("MODULE"), MB = new Symbol("MBody");
        Symbol B = new Symbol("BLCK"), S = new Symbol("STMT"), A = new Symbol(":="), S1 = new Symbol("STMT1");
        Symbol E = new Symbol("EXPR"), F = new Symbol("F"), F1 = new Symbol("F1"), F2 = new Symbol("F2"),
                T = new Symbol("T"), T1 = new Symbol("T1"), T2 = new Symbol("T2"),
                T3 = new Symbol("T3"), T4 = new Symbol("T4");
        Terminal plus = new Terminal("+"), minus = new Terminal("-"), times = new Terminal("*"),
                divide = new Terminal("/"), open = new Terminal("("), close = new Terminal(")"),
                numberL = new Terminal("num"), semicolon = new Terminal(";"), stringL = new Terminal("string"),
                equals = new Terminal("=="), nEquals = new Terminal("!="), less = new Terminal("<"),
                greater = new Terminal(">"), lessEquals = new Terminal("<="), greaterEquals = new Terminal(">="),
                openCurly = new Terminal("{"), closeCurly = new Terminal("}"), lAnd = new Terminal("&&"), lOr = new Terminal("||");
        Terminal assign = new Terminal("="), var = new Terminal("var"), kwPrint = new Terminal("print"),
                increment = new Terminal("++"), decrement = new Terminal("--"),
                identifier = new Terminal("ID"), kwIf = new Terminal("if"), kwFor = new Terminal("for"),
                kwTrue = new Terminal("true"), kwFalse = new Terminal("false");

        lang.addTerminal(assign, '=').addTerminal(plus, '+').addTerminal(semicolon, ';')
                .addTerminal(minus, '-').addTerminal(times, '*').addTerminal(lAnd, "&&").addTerminal(lOr, "||")
                .addTerminal(divide, '/').addTerminal(open, '(').addTerminal(close, ')')
                .addTerminal(increment, "++").addTerminal(decrement, "--")
                .addTerminal(var, "var").addTerminal(kwPrint, "print").addTerminal(kwIf, "if")
                .addTerminal(openCurly, '{').addTerminal(closeCurly, '}').addTerminal(kwFor, "for")
                .addTerminal(kwTrue, "true").addTerminal(kwFalse, "false")
                .addTerminal(equals, "==").addTerminal(nEquals, "!=").addTerminal(less, '<')
                .addTerminal(greater, '>').addTerminal(lessEquals, "<=").addTerminal(greaterEquals, ">=")
                .addTerminal(numberL, (src, i) -> {
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
                .addTerminal(stringL, (src, i) -> {
                    if (src.charAt(i) != '"') return null;
                    StringBuilder value = new StringBuilder();
                    while (++i < src.length()) {
                        char c = src.charAt(i);
                        if (c == '"') break;
                        value.append(c);
                    }
                    return "  " + value.toString();
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
        lang.ignore((src, i) -> { // ignore spaces
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
        lang.ignore((src, i) -> { // ignore comments
            if (src.charAt(i) != '#') return null;
            int end = src.indexOf('\n', i);
            if (end == -1) return src.substring(i);
            else return src.substring(i, end + 1);
        });

        // TODO: some reorganizing for more accurate rules
        // TODO: support Epsilon productions
        M.addProduction(Symbol.EPSILON);
        M.addProduction(B); // todo: add here vars, funcs & classes
        B.addProduction(S1);
        B.addProduction(B, S1);
        S1.addProduction(S, semicolon);
        S1.addProduction(kwFor, open, S1, E, semicolon, S, close, S1);
        S1.addProduction(kwFor, open, S1, E, semicolon, S, close, openCurly, B, closeCurly);
        S1.addProduction(kwIf, open, E, close, S1);
        S1.addProduction(kwIf, open, E, close, openCurly, B, closeCurly);
        S.addProduction(A);
        A.addProduction(identifier, identifier, assign, E);
        A.addProduction(identifier, assign, E);
        S.addProduction(kwPrint, E);
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
        F.addProduction(numberL);
        F.addProduction(stringL);
        F.addProduction(kwTrue);
        F.addProduction(kwFalse);
        F.addProduction(identifier);

        grammar = new Grammar(M);

        factory.addProduction(M, new Symbol[] { B }, c -> new ModuleNode(name, c[0]));
        factory.addProduction(B, new Symbol[] { S1 }, c -> new StmtNode(new Token(null, "Block"), c[0]) {
            @Override public void write() {
                writer.enterScope();
                ((StmtNode) getChild(0)).write();
                writer.exitScope();
            }
        });
        factory.addProduction(S1, new Symbol[] { S, semicolon }, c -> c[0]);
        factory.addProduction(B, new Symbol[] { B, S1 }, c -> {
            List<AST> children = new ArrayList<>();
            children.addAll(c[0].getChildren());
            children.add(c[1]);
            return new StmtNode(new Token(null, "Block"), children) {
                @Override public void write() {
                    writer.enterScope();
                    for (AST ast : getChildren()) ((StmtNode) ast).write();
                    writer.exitScope();
                }
            };
        });
        factory.addProduction(S, new Symbol[] { A }, c -> {
            if (c[0] instanceof StmtNode) return c[0];
            return new StmtNode(c[0].getValue(), c[0].getChildren()) {
                @Override public void write() {
                    ((ExpNode) c[0]).write();
                    writer.pop();
                }
            };
        });
        factory.addProduction(A, new Symbol[] { identifier, identifier, assign, E }, c ->
                new StmtNode(c[2].getValue(), c[0], c[1], c[3]) {
                    @Override public void write() {
                        String name = getChild(1).getValue().getValue(), type =  getChild(0).getValue().getValue();
                        if (writer.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                        writer.addSymbol(name, type);
                        System.out.println(name + " " + type);
                        ((ExpNode) getChild(2)).write();
                        writer.popToLocal(name);

                        if (writer.getTypeID(type) != ((ExpNode) getChild(2)).getType()) throw new TypeException();
                    }
                });
        factory.addProduction(A, new Symbol[] { identifier, assign, E }, c ->
                new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public void write() {
                        getExpChild(1).write();
                        String name = getChild(0).getValue().getValue();
                        writer.popToLocal(name);
                        writer.pushLocal(name);
                    }
                    @Override public int typeCheck() {
                        if (getExpChild(1).getType() != getExpChild(0).getType()) throw new TypeException();
                        return getExpChild(1).getType();
                    }
                });
        factory.addProduction(S, new Symbol[] { kwPrint, E }, c -> new StmtNode(c[0].getValue(), c[1]) {
            @Override public void write() {
                writer.pushStaticField("java/lang/System", "out", "Ljava/io/PrintStream;");
                ((ExpNode) getChild(0)).write();
                writer.call("java/io/PrintStream", "println", "V",
                        writer.getTypeEnv().jvmType(((ExpNode) getChild(0)).getType()));
            }
        });
        factory.addProduction(E, new Symbol[] { T4 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { E, lOr, T4 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int b = writer.getTypeID("boolean");
                        if (getExpChild(0).getType() != b || getExpChild(1).getType() != b) throw new TypeException();
                        return b;
                    }
                    @Override protected void writeCond() {
                        Frame end = writer.getCondEnd(), body = new Frame();
                        writer.setCondEnd(body);
                        writer.setSkipFor(!writer.getSkipFor());
                        getExpChild(0).write(); // true -> skip to 'body', false -> resume
                        writer.setSkipFor(!writer.getSkipFor());
                        writer.setCondEnd(end);
                        getExpChild(1).write(); // true -> resume, false -> skip to 'end'
                        writer.assignFrame(body);
                    }
                    @Override protected void writeExp() {
                        Frame cond2 = new Frame(), end = new Frame();
                        getExpChild(0).write();
                        writer.useJmpOperator(IFEQ, cond2); // if false(=0), check other cond
                        writer.pushInt(1); // if true(=0), push true and end
                        writer.useJmpOperator(GOTO, end);
                        writer.assignFrame(cond2); // here we get the final result
                        getExpChild(1).write();
                        writer.assignFrame(end);
                    }
                });
        factory.addProduction(T4, new Symbol[] { T3 }, c -> c[0]);
        factory.addProduction(T4, new Symbol[] { T4, lAnd, T3 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int b = writer.getTypeID("boolean");
                        if (getExpChild(0).getType() != b || getExpChild(1).getType() != b) throw new TypeException();
                        return b;
                    }
                    @Override protected void writeCond() {
                        if (writer.getSkipFor()) {
                            writer.setSkipFor(false);
                            getExpChild(0).write(); // true -> skip to END, false -> resume
                            writer.setSkipFor(true);
                            getExpChild(1).write(); // true -> skip to END, false -> resume
                            return;
                        }
                        getExpChild(0).write();
                        getExpChild(1).write();
                    }
                    @Override protected void writeExp() {
                        Frame cond2 = new Frame(), end = new Frame();
                        getExpChild(0).write();
                        writer.useJmpOperator(IFNE, cond2); // if true(=1), check other cond
                        writer.pushInt(0); // if false(=0), push false and end
                        writer.useJmpOperator(GOTO, end);
                        writer.assignFrame(cond2); // here we get the final result
                        getExpChild(1).write();
                        writer.assignFrame(end);
                    }
                });
        factory.addProduction(T3, new Symbol[] { T2 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T3, equals, T2 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFEQ, writer.getCondEnd());
                        else writer.useJmpOperator(IFNE, writer.getCondEnd());
                    }
                    @Override protected void writeExp() {
                        getExpChild(0).write();
                        getExpChild(1).write();
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
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FCMPG);
                        if (!writer.getSkipFor()) writer.useJmpOperator(IFEQ, writer.getCondEnd());
                        else writer.useJmpOperator(IFNE, writer.getCondEnd());
                    }
                    @Override protected void writeExp() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FCMPG);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, less, T1 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFLT, writer.getCondEnd());
                        else writer.useJmpOperator(IFGE, writer.getCondEnd());
                    }
                    @Override protected void writeExp() {
                        getExpChild(1).write();
                        getExpChild(0).write();
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
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFGT, writer.getCondEnd());
                        else writer.useJmpOperator(IFLE, writer.getCondEnd());
                    }
                    @Override protected void writeExp() {
                        getExpChild(0).write();
                        getExpChild(1).write();
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
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFLE, writer.getCondEnd());
                        else writer.useJmpOperator(IFGT, writer.getCondEnd());
                    }
                    @Override protected void writeExp() {
                        getExpChild(0).write();
                        getExpChild(1).write();
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
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFGE, writer.getCondEnd());
                        else writer.useJmpOperator(IFLT, writer.getCondEnd());
                    }
                    @Override protected void writeExp() {
                        getExpChild(0).write();
                        getExpChild(1).write();
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
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override public void write() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FADD);
                    }
                });
        factory.addProduction(T1, new Symbol[] { T1, minus, T },
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override public void write() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FSUB);
                    }
                });
        factory.addProduction(T, new Symbol[] { T, times, F1 },
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override public void write() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FMUL);
                    }
                });
        factory.addProduction(T, new Symbol[] { T, divide, F1 },
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public int typeCheck() {
                        int t = getExpChild(0).getType(); // todo: something better
                        if (getExpChild(1).getType() != t) throw new TypeException();
                        return t;
                    }
                    @Override public void write() {
                        getExpChild(0).write();
                        getExpChild(1).write();
                        writer.useOperator(FDIV);
                    }
                });
        factory.addProduction(T, new Symbol[] { F1 }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { F }, c -> c[0]);
        factory.addProduction(F1, new Symbol[] { plus, F1 }, c -> c[1]);
        factory.addProduction(F1, new Symbol[] { minus, F1 }, c -> new ExpNode(c[0].getValue(), c[1]) {
            @Override public int typeCheck() {
                return getExpChild(0).getType(); // todo: something better
            }
            @Override public void write() {
                getExpChild(0).write();
                writer.useOperator(FNEG);
            }
        });
        factory.addProduction(F1, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { F2 }, c -> c[0]);
        factory.addProduction(S, new Symbol[] { F2 }, c -> new StmtNode(c[0].getValue(), c[0].getChildren()) {
            @Override public void write() {
                ((ExpNode) c[0]).write();
                writer.pop();
            }
        });
        factory.addProduction(F2, new Symbol[] { increment, F }, c -> new ExpNode(c[0].getValue(), c[1]) {
            @Override public int typeCheck() {
                return getExpChild(0).getType(); // todo: something better
            }
            @Override public void write() {
                if (getChild(0).getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToLocal(name);
                writer.pushLocal(name);
            }
        });
        factory.addProduction(F2, new Symbol[] { decrement, F }, c -> new ExpNode(c[0].getValue(), c[1]) {
            @Override public int typeCheck() {
                return getExpChild(0).getType(); // todo: something better
            }
            @Override public void write() {
                if (getChild(0).getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToLocal(name);
                writer.pushLocal(name);
            }
        });
        factory.addProduction(F2, new Symbol[] { F, increment }, c -> new ExpNode(c[1].getValue(), c[0]) {
            @Override public int typeCheck() {
                return getExpChild(0).getType(); // todo: something better
            }
            @Override public void write() {
                if (getChild(0).getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushLocal(name);
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToLocal(name);
            }
        });
        factory.addProduction(F2, new Symbol[] { F, decrement }, c -> new ExpNode(c[1].getValue(), c[0]) {
            @Override public int typeCheck() {
                return getExpChild(0).getType(); // todo: something better
            }
            @Override public void write() {
                if (getChild(0).getValue().getType() != identifier)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushLocal(name);
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToLocal(name);
            }
        });
        factory.addProduction(F, new Symbol[] { open, E, close }, c -> c[1]);
        factory.addProduction(F, new Symbol[] { numberL }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override public int typeCheck() {
                if (getValue().getValue().contains(".") || getValue().getValue().endsWith("F")
                        || getValue().getValue().endsWith("f")) return writer.getTypeID("float");
                return writer.getTypeID("int");
            }
            @Override public void write() {
                if (getType() == writer.getTypeID("float"))
                    writer.pushFloat(Float.valueOf(getValue().getValue()));
                else writer.pushInt(Integer.valueOf(getValue().getValue()));
            }
        });
        factory.addProduction(F, new Symbol[] { stringL }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override public int typeCheck() { return writer.getTypeID("string"); }
            @Override public void write() { writer.pushString(getValue().getValue().substring(2)); }
        });
        factory.addProduction(F, new Symbol[] { identifier }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override public int typeCheck() { return writer.typeOf(getValue().getValue()); }
            @Override public void write() {
                writer.pushLocal(getValue().getValue());
            }
        });
        factory.addProduction(F, new Symbol[] { kwFalse },
                c -> new BoolExpNode(c[0].getValue(), c[0].getChildren()) {
                    @Override public int typeCheck() { return writer.getTypeID("boolean"); }
                    @Override protected void writeCond()
                    { if (!writer.getSkipFor()) writer.useJmpOperator(GOTO, writer.getCondEnd()); }
                    @Override protected void writeExp() { writer.pushInt(0); }
                });
        factory.addProduction(F, new Symbol[] { kwTrue },
                c -> new BoolExpNode(c[0].getValue(), c[0].getChildren()) {
                    @Override public int typeCheck() { return writer.getTypeID("boolean"); }
                    @Override protected void writeCond()
                    { if (writer.getSkipFor()) writer.useJmpOperator(GOTO, writer.getCondEnd()); }
                    @Override protected void writeExp() { writer.pushInt(1); }
                });
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, S1 },
                c -> new IfNode(c[0].getValue(), c[2], c[4]));
        factory.addProduction(S1, new Symbol[] { kwIf, open, E, close, openCurly, B, closeCurly },
                c -> new IfNode(c[0].getValue(), c[2], c[5]));
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, openCurly, B, closeCurly },
                c -> new ForNode(c[0].getValue(), c[2], c[3], c[5], c[8]));
        factory.addProduction(S1, new Symbol[] { kwFor, open, S1, E, semicolon, S, close, S1 },
                c -> new ForNode(c[0].getValue(), c[2], c[3], c[5], c[7]));
    }*/
}
