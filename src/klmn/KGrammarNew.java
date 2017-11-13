package klmn;

import ast.AST;
import ast.ASTFactory;
import jvm.Opcodes;
import jvm.methods.Frame;
import klmn.nodes.*;
import klmn.writing.MethodWriter;
import klmn.writing.SymbolTable;
import lang.*;

import static klmn.writing.TypeEnv.Type;

public class KGrammarNew implements Opcodes
{
    // TEMPORARY
    // MAIN TODOS:
    //  TODO: raise error for inaccessible code
    //  TODO: manage types properly, store var&func types in symbolTable and keep type conversions somewhere

    /*
    *   Module:
    *       Epsilon
    *       Module VarDecl
    *       Module FuncDecl
    *       Module ClassDecl
    *       // MAYBE actual code
    *
    *   VarDecl:
    *       Modifiers Type id;
    *       Modifiers Type id = Expression;
    *
    *   FuncDecl:
    *       Modifiers Type id ( Params ) Statement
    *
    *   Modifiers:
    *       final
    *       public
    *       private
    *       static
    *
    *   Type: // add arrays, tuples & lambdas
    *       id
    *
    *   Expression:
    *       ( Expression )
    *       StatementExpression
    *       // more annoying shit
    *
    *   StatementExpression:
    *       print Expression
    *       Assignment
    *       Increment / Decrement
    *       id ( Params )
    *
    *   StatementExpression+:
    *       StatementExpression
    *       VarDecl
    *
    *   Statement:
    *       StatementExpression+
    *       if ( Expression ) Statement
    *       if ( Expression ) Statement else Statement
    *       for ( StatementExpression+ ; Expression ; StatementExpression+ ) Statement
    *       { Block }
    *
    *   Block:
    *       Epsilon
    *       Statement ;
    *       Block Statement ;
    *
    * */

    public static Language lang = new Language();
    public static Grammar grammar;
    public static ASTFactory factory = new ASTFactory();
    public static String moduleName = "";
    static {
        Symbol M = new Symbol("Module"), VD = new Symbol("VarDecl"), FD = new Symbol("FuncDecl");
        Symbol MF = new Symbol("Modifiers"), T = new Symbol("Type"), E = new Symbol("Expression");
        Symbol SE = new Symbol("StatementExpression"),
                S = new Symbol("Statement"), B = new Symbol("Block"), A = new Symbol("Assignment");
        Symbol P = new Symbol("Params"), PD = new Symbol("ParamsDecl"), DI = new Symbol("Decrement/Increment");

        Terminal id = new Terminal("identifier"), assign = new Terminal("="), semicolon = new Terminal(";"),
         openRound = new Terminal("("), closeRound = new Terminal(")"), comma = new Terminal(","),
         openCurly = new Terminal("{"), closeCurly = new Terminal("}"),
         kwIf = new Terminal("if"), kwElse = new Terminal("else"), kwFor = new Terminal("for"),
         kwPrint = new Terminal("print"), kwFinal = new Terminal("final"), kwStatic = new Terminal("static"),
         kwPublic = new Terminal("public"), kwPrivate = new Terminal("private"), kwReturn = new Terminal("return");

        M.addProduction();
        M.addProduction(M, VD, semicolon);
        M.addProduction(M, FD);

        VD.addProduction(MF, T, id);
        VD.addProduction(MF, T, A);
        VD.addProduction(T, id);
        VD.addProduction(T, A);

        FD.addProduction(MF, T, id, openRound, PD, closeRound, openCurly, B, closeCurly);
        FD.addProduction(T, id, openRound, PD, closeRound, openCurly, B, closeCurly);

        Symbol PD0 = new Symbol("ParamsDecl0");
        PD0.addProduction(T, id);
        PD0.addProduction(PD0, comma, T, id);
        PD.addProduction();
        PD.addProduction(PD0);

        Symbol MF0 = new Symbol("Modifiers0");
        MF0.addProduction(kwFinal);
        MF0.addProduction(kwPublic);
        MF0.addProduction(kwPrivate);
        MF0.addProduction(kwStatic);
        MF.addProduction(MF0);
        MF.addProduction(MF, MF0);

        T.addProduction(id);

        A.addProduction(id, assign, E);

        SE.addProduction(A);
        SE.addProduction(id, openRound, P, closeRound); // func call, add proper id symbol
        SE.addProduction(id, openRound, closeRound); // func call, add proper id symbol
        SE.addProduction(DI);

        S.addProduction(SE, semicolon);
        S.addProduction(VD, semicolon);
        S.addProduction(kwPrint, E, semicolon);
        S.addProduction(kwIf, openRound, E, closeRound, S);
        S.addProduction(kwIf, openRound, E, closeRound, S, kwElse, S);
        Symbol FI = new Symbol("ForInit");
        FI.addProduction(SE);
        FI.addProduction(VD);
        S.addProduction(kwFor, openRound, FI, semicolon, E, semicolon, SE, closeRound, S);
        S.addProduction(openCurly, B, closeCurly);
        S.addProduction(kwReturn, semicolon);
        S.addProduction(kwReturn, E, semicolon);

        B.addProduction();
        B.addProduction(S);
        B.addProduction(B, S);

        P.addProduction(E);
        P.addProduction(P, comma, E);

        //<editor-fold desc="Expressions">
        Symbol T0 = new Symbol("Primary"), T1 = new Symbol("Prefixed"),
                T2 = new Symbol("Mul/Div"), T3 = new Symbol("Add/Sub"), T4 = new Symbol("Cmp0"),
                T5 = new Symbol("Cmp1"), T6 = new Symbol("Logical Ops");
        Terminal plus = new Terminal("+"), minus = new Terminal("-"), times = new Terminal("*"),
                divide = new Terminal("/"),
                numberL = new Terminal("num"), stringL = new Terminal("string"),
                eq = new Terminal("=="), ne = new Terminal("!="), lt = new Terminal("<"),
                gt = new Terminal(">"), le = new Terminal("<="), ge = new Terminal(">="),
                lAnd = new Terminal("&&"), lOr = new Terminal("||"),
                increment = new Terminal("++"), decrement = new Terminal("--"),
                kwTrue = new Terminal("true"), kwFalse = new Terminal("false");

        E.addProduction(E, lOr, T6);
        E.addProduction(T6); // E -> T6
        T6.addProduction(T6, lAnd, T5);
        T6.addProduction(T5); // T6 -> T5
        T5.addProduction(T5, eq, T4);
        T5.addProduction(T5, ne, T4);
        T5.addProduction(T4); // T5 -> T4
        T4.addProduction(T4, lt, T3);
        T4.addProduction(T4, gt, T3);
        T4.addProduction(T4, le, T3);
        T4.addProduction(T4, ge, T3);
        T4.addProduction(T3); // T4 -> T3
        T3.addProduction(T3, plus, T2);
        T3.addProduction(T3, minus, T2);
        T3.addProduction(T2); // T3 -> T
        T2.addProduction(T2, times, T1);
        T2.addProduction(T2, divide, T1);
        T2.addProduction(T1);
        T1.addProduction(DI);
        T1.addProduction(plus, T1);
        T1.addProduction(minus, T1);
        T1.addProduction(T0);
        DI.addProduction(T0, decrement);
        DI.addProduction(T0, increment);
        DI.addProduction(increment, T0);
        DI.addProduction(decrement, T0);
        T0.addProduction(openRound, E, closeRound);
        T0.addProduction(numberL);
        T0.addProduction(stringL);
        T0.addProduction(kwTrue);
        T0.addProduction(kwFalse);
        T0.addProduction(id);
        T0.addProduction(SE);
        //</editor-fold>

        grammar = new Grammar(M);

        //<editor-fold desc="Factory">
        factory.addProduction(M, new Symbol[] {}, c -> new ModuleNode(moduleName));
        factory.addProduction(M, new Symbol[] { M, VD, semicolon }, c -> {
            c[0].addChild(c[1]);
            return c[0];
        });
        factory.addProduction(M, new Symbol[] { M, FD }, c -> {
            c[0].addChild(c[1]);
            return c[0];
        });

        factory.addProduction(VD, new Symbol[] { MF, T, id }, c -> new VarNode(c[2].getValue(), c[0], c[1]));
        factory.addProduction(VD, new Symbol[] { MF, T, A }, c -> new VarNode(c[2].getChild(0).getValue(), c[0], c[1], c[2].getChild(1)));
        factory.addProduction(VD, new Symbol[] { T, id }, c ->
                new VarNode(c[1].getValue(), new AST(new Token("Modifiers")), c[0]));
        factory.addProduction(VD, new Symbol[] { T, A }, c ->
                new VarNode(c[1].getChild(0).getValue(), new AST(new Token("Modifiers")), c[0], c[1].getChild(1)));

        factory.addProduction(FD, new Symbol[] { MF, T, id, openRound, PD, closeRound, openCurly, B, closeCurly },
                c -> new MethodNode(c[2].getValue(), c[0], c[1], c[4], c[7]));
        factory.addProduction(FD, new Symbol[] { T, id, openRound, PD, closeRound, openCurly, B, closeCurly },
                c -> new MethodNode(c[2].getValue(), new AST(new Token("Modifiers")), c[0], c[3], c[6]));

        factory.addProduction(PD0, new Symbol[] { T, id }, c -> new AST(new Token("Params"), new AST(c[1].getValue(), c[0])));
        factory.addProduction(PD0, new Symbol[] { PD0, comma, T, id}, c -> {
            c[0].addChild(new AST(c[3].getValue(), c[2]));
            return c[0];
        });
        factory.addProduction(PD, new Symbol[] {}, c -> new AST(new Token("Params")));
        factory.addProduction(PD, new Symbol[] { PD0 }, c -> c[0]);

        factory.addProduction(MF0, new Symbol[] { kwFinal }, c -> c[0]);
        factory.addProduction(MF0, new Symbol[] { kwPublic }, c -> c[0]);
        factory.addProduction(MF0, new Symbol[] { kwPrivate }, c -> c[0]);
        factory.addProduction(MF0, new Symbol[] { kwStatic }, c -> c[0]);
        factory.addProduction(MF, new Symbol[] { MF0 }, c -> new AST(new Token("Modifiers"), c[0]));
        factory.addProduction(MF, new Symbol[] { MF, MF0 }, c -> {
            c[0].addChild(c[1]);
            return c[0];
        });

        factory.addProduction(T, new Symbol[] { id }, c -> new TypeNode(c[0].getValue())); // todo: create TypeNode

        factory.addProduction(A, new Symbol[] { id, assign, E }, c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
            @Override protected Type typeCheck(MethodWriter writer) {
                if (getExpChild(1).getType(writer) != getExpChild(0).getType(writer)) throw new TypeException();
                return getExpChild(1).getType(writer);
            }
            @Override public void write(MethodWriter writer) {
                getExpChild(1).write(writer);
                String name = getChild(0).getValue().getValue();
                writer.popToLocal(name);
                writer.pushLocal(name);
            }
        });

        factory.addProduction(SE, new Symbol[] { A }, c -> new StmtExpNode(c[0].getValue(), c[0]) {
            @Override protected Type typeCheck(MethodWriter writer) { return writer.typeOf(getChild(0).getChild(0).getValue().getValue()); }
            @Override public void writeExp(MethodWriter writer) { ((ExpNode) getChild(0)).write(writer); }
            @Override public void writeStmt(MethodWriter writer) {
                ((ExpNode) getChild(0)).write(writer);
                writer.pop();
            }
        });
        factory.addProduction(SE, new Symbol[] { id, openRound, P, closeRound }, c -> new StmtExpNode(new Token("()"), c[0], c[2]) {
            @Override protected Type typeCheck(MethodWriter writer) { return writer.typeOf(getChild(0).getValue().getValue()); }
            @Override public void writeExp(MethodWriter writer) {
                String[] params = new String[getChild(1).getChildren().size()];
                for (int i = 0; i < params.length; i++) {
                    ExpNode exp = (ExpNode) getChild(1).getChild(i);
                    params[i] = exp.getType(writer).getDescriptor();
                    exp.write(writer);
                }
                String name = getChild(0).getValue().getValue();
                writer.callStatic(writer.getParentName(), name, writer.typeOf(name).getDescriptor(), params);
            }
            @Override public void writeStmt(MethodWriter writer) {
                String[] params = new String[getChild(1).getChildren().size()];
                for (int i = 0; i < params.length; i++) {
                    ExpNode exp = (ExpNode) getChild(1).getChild(i);
                    params[i] = exp.getType(writer).getDescriptor();
                    exp.write(writer);
                }
                String name = getChild(0).getValue().getValue(), type = writer.typeOf(name).getDescriptor();
                writer.callStatic(writer.getParentName(), name, type, params);
                if (!type.equals("V")) writer.pop();
            }
        }); // todo: validate parameter types & amount
        factory.addProduction(SE, new Symbol[] { id, openRound, closeRound }, c -> new StmtExpNode(new Token("()"), c[0]) {
            @Override protected Type typeCheck(MethodWriter writer) { return writer.typeOf(getChild(0).getValue().getValue()); }
            @Override public void writeExp(MethodWriter writer) {
                String name = getChild(0).getValue().getValue();
                writer.callStatic(writer.getParentName(), name, writer.typeOf(name).getDescriptor());
            }
            @Override public void writeStmt(MethodWriter writer) {
                String name = getChild(0).getValue().getValue(), type = writer.typeOf(name).getDescriptor();
                writer.callStatic(writer.getParentName(), name, type);
                if (!type.equals("V")) writer.pop();
            }
        });
        factory.addProduction(SE, new Symbol[] { DI }, c -> new StmtExpNode(c[0].getValue(), c[0].getChild(0)) {
            @Override public void writeExp(MethodWriter writer) { ((ExpNode) c[0]).write(writer); }
            @Override public void writeStmt(MethodWriter writer) {
                ((ExpNode) c[0]).write(writer);
                writer.pop();
            }
            @Override protected Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
        });

        factory.addProduction(S, new Symbol[] { SE, semicolon }, c -> new StmtNode(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(MethodWriter writer) { ((StmtExpNode) c[0]).writeStmt(writer); }
        });
        factory.addProduction(S, new Symbol[] { VD, semicolon }, c -> c[0]);
        factory.addProduction(S, new Symbol[] { kwPrint, E, semicolon }, c -> new StmtNode(c[0].getValue(), c[1]) {
            @Override public void write(MethodWriter writer) {
                writer.pushStaticField("java/lang/System", "out", "Ljava/io/PrintStream;");
                ((ExpNode) getChild(0)).write(writer);
                writer.call("java/io/PrintStream", "println", "V",
                        ((ExpNode) getChild(0)).getType(writer).getDescriptor());
            }
        });
        factory.addProduction(S, new Symbol[] { kwIf, openRound, E, closeRound, S }, c -> new IfNode(c[0].getValue(), c[2], c[4]));
        // todo: S.addProduction(kwIf, openRound, E, closeRound, S, kwElse, S);
        factory.addProduction(FI, new Symbol[] { SE }, c -> new StmtNode(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(MethodWriter writer) { ((StmtExpNode) c[0]).writeStmt(writer); }
        });
        factory.addProduction(FI, new Symbol[] { VD }, c -> c[0]);
        factory.addProduction(S, new Symbol[] { kwFor, openRound, FI, semicolon, E, semicolon, SE, closeRound, S },
                c -> new ForNode(c[0].getValue(), c[2], c[4], c[6], c[8]));
        factory.addProduction(S, new Symbol[] { openCurly, B, closeCurly }, c -> new StmtNode(c[1].getValue(), c[1].getChildren()) {
            @Override public void write(MethodWriter writer) {
                writer.enterScope();
                ((StmtNode) c[1]).write(writer);
                if (writer.hasReturned()) {
                    writer.exitScope();
                    writer.getSymbolTable().ret();
                }
                else writer.exitScope();
            }
        });
        factory.addProduction(S, new Symbol[] { kwReturn, semicolon }, c -> new StmtNode(c[0].getValue()) {
            @Override public void write(MethodWriter writer) { writer.ret(); }
        });
        factory.addProduction(S, new Symbol[] { kwReturn, E, semicolon }, c -> new StmtNode(c[0].getValue(), c[1]) {
            @Override public void write(MethodWriter writer) {
                ((ExpNode) getChild(0)).write(writer);
                writer.ret();
            }
        });

        factory.addProduction(B, new Symbol[] {}, c -> new StmtNode(new Token("Block")) {
            @Override public void write(MethodWriter writer) {}
        });
        factory.addProduction(B, new Symbol[] { S }, c -> new StmtNode(new Token("Block"), c[0]) {
            @Override public void write(MethodWriter writer) {
                for (AST c : getChildren()) {
                    if (writer.hasReturned()) throw new RuntimeException("unreachable code!");
                    ((StmtNode) c).write(writer);
                }
            }
        });
        factory.addProduction(B, new Symbol[] { B, S }, c -> {
            c[0].addChild(c[1]);
            return c[0];
        });

        factory.addProduction(P, new Symbol[] { E }, c -> new AST(new Token("Params"), c[0]));
        factory.addProduction(P, new Symbol[] { P, comma, E }, c -> {
            c[0].addChild(c[2]);
            return c[0];
        });
        
        //<editor-fold desc="Expressions">
        factory.addProduction(E, new Symbol[] { T6 }, c -> c[0]);
        factory.addProduction(E, new Symbol[] { E, lOr, T6 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type b = writer.getTypeEnv().getForName("boolean");
                        if (getExpChild(0).getType(writer) != b || getExpChild(1).getType(writer) != b) throw new TypeException();
                        return b;
                    }
                    @Override protected void writeCond(MethodWriter writer) {
                        Frame end = writer.getCondEnd(), body = new Frame();
                        writer.setCondEnd(body);
                        writer.setSkipFor(!writer.getSkipFor());
                        getExpChild(0).write(writer); // true -> skip to 'body', false -> resume
                        writer.setSkipFor(!writer.getSkipFor());
                        writer.setCondEnd(end);
                        getExpChild(1).write(writer); // true -> resume, false -> skip to 'end'
                        writer.assignFrame(body);
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        Frame cond2 = new Frame(), end = new Frame();
                        getExpChild(0).write(writer);
                        writer.useJmpOperator(IFEQ, cond2); // if false(=0), check other cond
                        writer.pushInt(1); // if true(=0), push true and end
                        writer.useJmpOperator(GOTO, end);
                        writer.assignFrame(cond2); // here we get the final result
                        getExpChild(1).write(writer);
                        writer.assignFrame(end);
                    }
                });
        factory.addProduction(T6, new Symbol[] { T5 }, c -> c[0]);
        factory.addProduction(T6, new Symbol[] { T6, lAnd, T5 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type b = writer.getTypeEnv().getForName("boolean");
                        if (getExpChild(0).getType(writer) != b || getExpChild(1).getType(writer) != b) throw new TypeException();
                        return b;
                    }
                    @Override protected void writeCond(MethodWriter writer) {
                        if (writer.getSkipFor()) {
                            writer.setSkipFor(false);
                            getExpChild(0).write(writer); // true -> skip to END, false -> resume
                            writer.setSkipFor(true);
                            getExpChild(1).write(writer); // true -> skip to END, false -> resume
                            return;
                        }
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        Frame cond2 = new Frame(), end = new Frame();
                        getExpChild(0).write(writer);
                        writer.useJmpOperator(IFNE, cond2); // if true(=1), check other cond
                        writer.pushInt(0); // if false(=0), push false and end
                        writer.useJmpOperator(GOTO, end);
                        writer.assignFrame(cond2); // here we get the final result
                        getExpChild(1).write(writer);
                        writer.assignFrame(end);
                    }
                });
        factory.addProduction(T5, new Symbol[] { T4 }, c -> c[0]);
        factory.addProduction(T5, new Symbol[] { T5, eq, T4 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFEQ, writer.getCondEnd());
                        else writer.useJmpOperator(IFNE, writer.getCondEnd());
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
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
        factory.addProduction(T5, new Symbol[] { T5, ne, T4 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FCMPG);
                        if (!writer.getSkipFor()) writer.useJmpOperator(IFEQ, writer.getCondEnd());
                        else writer.useJmpOperator(IFNE, writer.getCondEnd());
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FCMPG);
                    }
                });
        factory.addProduction(T4, new Symbol[] { T4, lt, T3 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFLT, writer.getCondEnd());
                        else writer.useJmpOperator(IFGE, writer.getCondEnd());
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        getExpChild(1).write(writer);
                        getExpChild(0).write(writer);
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
        factory.addProduction(T4, new Symbol[] { T4, gt, T3 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFGT, writer.getCondEnd());
                        else writer.useJmpOperator(IFLE, writer.getCondEnd());
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
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
        factory.addProduction(T4, new Symbol[] { T4, le, T3 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFLE, writer.getCondEnd());
                        else writer.useJmpOperator(IFGT, writer.getCondEnd());
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
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
        factory.addProduction(T4, new Symbol[] { T4, ge, T3 },
                c -> new BoolExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override protected void writeCond(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FCMPG);
                        if (writer.getSkipFor()) writer.useJmpOperator(IFGE, writer.getCondEnd());
                        else writer.useJmpOperator(IFLT, writer.getCondEnd());
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
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
        factory.addProduction(T4, new Symbol[] { T3 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T2 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T3, plus, T2 },
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override public void write(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FADD);
                    }
                });
        factory.addProduction(T3, new Symbol[] { T3, minus, T2 },
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override public void write(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FSUB);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, times, T1 },
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override public void write(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(IMUL);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T2, divide, T1 },
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer) {
                        Type t = getExpChild(0).getType(writer); // todo: something better
                        if (getExpChild(1).getType(writer) != t) throw new TypeException();
                        return t;
                    }
                    @Override public void write(MethodWriter writer) {
                        getExpChild(0).write(writer);
                        getExpChild(1).write(writer);
                        writer.useOperator(FDIV);
                    }
                });
        factory.addProduction(T2, new Symbol[] { T1 }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T0 }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { plus, T1 }, c -> c[1]);
        factory.addProduction(T1, new Symbol[] { minus, T1 }, c -> new ExpNode(c[0].getValue(), c[1]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void write(MethodWriter writer) {
                getExpChild(0).write(writer);
                writer.useOperator(FNEG);
            }
        });
        factory.addProduction(T1, new Symbol[] { DI }, c -> c[0]);
        factory.addProduction(DI, new Symbol[] { increment, T0 }, c -> new ExpNode(c[0].getValue(), c[1]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void write(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToLocal(name);
                writer.pushLocal(name);
            }
        });
        factory.addProduction(DI, new Symbol[] { decrement, T0 }, c -> new ExpNode(c[0].getValue(), c[1]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void write(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToLocal(name);
                writer.pushLocal(name);
            }
        });
        factory.addProduction(DI, new Symbol[] { T0, increment }, c -> new ExpNode(c[1].getValue(), c[0]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void write(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushLocal(name);
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToLocal(name);
            }
        });
        factory.addProduction(DI, new Symbol[] { T0, decrement }, c -> new ExpNode(c[1].getValue(), c[0]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void write(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushLocal(name);
                writer.pushLocal(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToLocal(name);
            }
        });
        factory.addProduction(T0, new Symbol[] { openRound, E, closeRound }, c -> c[1]);
        factory.addProduction(T0, new Symbol[] { numberL }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override public Type typeCheck(MethodWriter writer) {
                if (getValue().getValue().contains(".") || getValue().getValue().endsWith("T0")
                        || getValue().getValue().endsWith("f")) return writer.getTypeEnv().getForName("float");
                return writer.getTypeEnv().getForName("int");
            }
            @Override public void write(MethodWriter writer) {
                if (getType(writer) == writer.getTypeEnv().getForName("float"))
                    writer.pushFloat(Float.valueOf(getValue().getValue()));
                else writer.pushInt(Integer.valueOf(getValue().getValue()));
            }
        });
        factory.addProduction(T0, new Symbol[] { stringL }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override public Type typeCheck(MethodWriter writer) { return writer.getTypeEnv().getForName("string"); }
            @Override public void write(MethodWriter writer) { writer.pushString(getValue().getValue().substring(2)); }
        });
        factory.addProduction(T0, new Symbol[] { id }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override public Type typeCheck(MethodWriter writer) { return writer.typeOf(getValue().getValue()); }
            @Override public void write(MethodWriter writer) { writer.pushLocal(getValue().getValue()); }
        });
        factory.addProduction(T0, new Symbol[] { kwFalse },
                c -> new BoolExpNode(c[0].getValue(), c[0].getChildren()) {
                    @Override public Type typeCheck(MethodWriter writer) { return writer.getTypeEnv().getForName("boolean"); }
                    @Override protected void writeCond(MethodWriter writer)
                    { if (!writer.getSkipFor()) writer.useJmpOperator(GOTO, writer.getCondEnd()); }
                    @Override protected void writeExp(MethodWriter writer) { writer.pushInt(0); }
                });
        factory.addProduction(T0, new Symbol[] { kwTrue },
                c -> new BoolExpNode(c[0].getValue(), c[0].getChildren()) {
                    @Override public Type typeCheck(MethodWriter writer) { return writer.getTypeEnv().getForName("boolean"); }
                    @Override protected void writeCond(MethodWriter writer)
                    { if (writer.getSkipFor()) writer.useJmpOperator(GOTO, writer.getCondEnd()); }
                    @Override protected void writeExp(MethodWriter writer) { writer.pushInt(1); }
                });
        factory.addProduction(T0, new Symbol[] { SE }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override protected Type typeCheck(MethodWriter writer) { return ((StmtExpNode) c[0]).getType(writer); }
            @Override public void write(MethodWriter writer) { ((StmtExpNode) c[0]).writeExp(writer); }
        });
        //</editor-fold>
        //</editor-fold>

        //<editor-fold desc="tokenizing">
        lang.addTerminal(assign, '=').addTerminal(plus, '+').addTerminal(semicolon, ';')
                .addTerminal(minus, '-').addTerminal(times, '*').addTerminal(lAnd, "&&").addTerminal(lOr, "||")
                .addTerminal(divide, '/').addTerminal(openRound, '(').addTerminal(closeRound, ')')
                .addTerminal(increment, "++").addTerminal(decrement, "--")
                .addTerminal(kwPrint, "print").addTerminal(kwIf, "if").addTerminal(comma, ",")
                .addTerminal(openCurly, '{').addTerminal(closeCurly, '}').addTerminal(kwFor, "for")
                .addTerminal(kwTrue, "true").addTerminal(kwFalse, "false")
                .addTerminal(eq, "==").addTerminal(ne, "!=").addTerminal(lt, '<')
                .addTerminal(gt, '>').addTerminal(le, "<=").addTerminal(ge, ">=")
                .addTerminal(kwElse, "else").addTerminal(kwFinal, "final").addTerminal(kwStatic, "static")
                .addTerminal(kwPrivate, "private").addTerminal(kwPublic, "public").addTerminal(kwReturn, "return")
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
                        else {
                            if (c == 'f') value.append(c);
                            break;
                        }
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
                .addTerminal(id, (src, i) -> {
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
        //</editor-fold>
    }
}
