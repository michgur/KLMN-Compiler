package klmn;

import ast.AST;
import ast.ASTFactory;
import jvm.Opcodes;
import jvm.methods.Label;
import klmn.nodes.*;
import klmn.writing.MethodWriter;
import klmn.writing.TypeEnv;
import lang.*;
import parsing.Parser;

import static klmn.writing.TypeEnv.Type;

public class KLMN implements Opcodes, KGrammar
{
    // MAIN TODOS:
    //  TODO: NEDDED CLASSES (to make things less messy)
    //          -Type
    //          -Scope
    //          -Variable (local, module field)
    //          -Method
    //          -Rework TypeEnv to include operators
    //          -Basic classes for all primitives to implement default operators
    //          -Writer (that ModuleWriter/MethodWriter inherit)
    //          -Rework the current Writers

    public static ASTFactory factory = new ASTFactory();
    public static String moduleName = "";

    public static void compile(String name, String src) throws Exception {
        moduleName = name;
        ModuleNode module = ((ModuleNode) new Parser(KGrammar.GRAMMAR)
                .parse(KGrammar.TOKENIZER.tokenize(src), factory));
        module.run();
    }

    /* 🔥🔥🔥 ⛧ WELCOME TO HELL ⛧ 🔥🔥🔥 */
    static {
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

        factory.addProduction(T, new Symbol[] { id }, c -> new TypeNode(c[0].getValue()));
        factory.addProduction(T, new Symbol[] { T, openSquare, closeSquare }, c -> ((TypeNode) c[0]).arrayOfThis());

        factory.addProduction(A, new Symbol[] { id, assign, E }, c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
            @Override protected Type typeCheck(MethodWriter writer) { return writer.typeOf(getChild(0).getValue().getValue()); }
            @Override public void write(MethodWriter writer) {

                getExpChild(1).write(writer);
                String name = getChild(0).getValue().getValue();
                writer.popToVar(name);
                writer.pushVar(name);
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
        factory.addProduction(SE, new Symbol[] { DI }, c -> c[0]);

        factory.addProduction(S, new Symbol[] { SE, semicolon }, c -> new StmtNode(c[0].getValue(), c[0].getChildren()) {
            @Override public void write(MethodWriter writer) { ((StmtExpNode) c[0]).writeStmt(writer); }
        });
        factory.addProduction(S, new Symbol[] { VD, semicolon }, c -> c[0]);
        factory.addProduction(S, new Symbol[] { kwPrint, semicolon }, c -> new StmtNode(c[0].getValue()) {
            @Override public void write(MethodWriter writer) { writer.println(); }
        });
        factory.addProduction(S, new Symbol[] { kwPrint, P, semicolon }, c -> new StmtNode(c[0].getValue(), c[1]) {
            @Override public void write(MethodWriter writer) {
                for (int i = 0; i < getChild(0).getChildren().size() - 1; i++)
                    writer.print((ExpNode) getChild(0).getChildren().get(i));
                writer.println((ExpNode) getChild(0).getChildren().get(getChild(0).getChildren().size() - 1));
            }
        });
        factory.addProduction(S, new Symbol[] { kwPrint, P, comma, semicolon }, c -> new StmtNode(c[0].getValue(), c[1]) {
            @Override public void write(MethodWriter writer) {
                getChild(0).getChildren().forEach(p -> writer.print((ExpNode) p));
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
                writer.ret(((ExpNode) getChild(0)).getType(writer).getDescriptor());
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
                        Label end = writer.getCondEnd(), body = new Label();
                        writer.setCondEnd(body);
                        writer.setSkipFor(!writer.getSkipFor());
                        getExpChild(0).write(writer); // true -> skip to 'body', false -> resume
                        writer.setSkipFor(!writer.getSkipFor());
                        writer.setCondEnd(end);
                        getExpChild(1).write(writer); // true -> resume, false -> skip to 'end'
                        writer.assign(body);
                    }
                    @Override protected void writeExp(MethodWriter writer) {
                        Label cond2 = new Label(), end = new Label();
                        getExpChild(0).write(writer);
                        writer.useJmpOperator(IFEQ, cond2); // if false(=0), check other cond
                        writer.pushInt(1); // if true(=0), push true and end
                        writer.useJmpOperator(GOTO, end);
                        writer.assign(cond2); // here we get the final result
                        getExpChild(1).write(writer);
                        writer.assign(end);
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
                        Label cond2 = new Label(), end = new Label();
                        getExpChild(0).write(writer);
                        writer.useJmpOperator(IFNE, cond2); // if true(=1), check other cond
                        writer.pushInt(0); // if false(=0), push false and end
                        writer.useJmpOperator(GOTO, end);
                        writer.assign(cond2); // here we get the final result
                        getExpChild(1).write(writer);
                        writer.assign(end);
                    }
                });
        factory.addProduction(T5, new Symbol[] { T4 }, c -> c[0]);
        factory.addProduction(T5, new Symbol[] { T5, eq, T4 },
                c -> new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public void write(MethodWriter writer)
                    { writer.getTypeEnv().binaryBoolOp(writer, getValue().getType(), getExpChild(0), getExpChild(1)); }
                    @Override public Type typeCheck(MethodWriter writer) { return writer.getTypeEnv().getForDescriptor("Z"); }
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
                        Label t = new Label(), f = new Label();
                        writer.useJmpOperator(IF_ICMPNE, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assign(f);
                        writer.pushInt(0);
                        writer.assign(t);
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
                        Label t = new Label(), f = new Label();
                        writer.useJmpOperator(IF_ICMPNE, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assign(f);
                        writer.pushInt(0);
                        writer.assign(t);
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
                        Label t = new Label(), f = new Label();
                        writer.useJmpOperator(IF_ICMPEQ, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assign(f);
                        writer.pushInt(0);
                        writer.assign(t);
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
                        Label t = new Label(), f = new Label();
                        writer.useJmpOperator(IF_ICMPEQ, f);
                        writer.pushInt(1);
                        writer.useJmpOperator(GOTO, t);
                        writer.assign(f);
                        writer.pushInt(0);
                        writer.assign(t);
                    }
                });
        factory.addProduction(T4, new Symbol[] { T3 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T2 }, c -> c[0]);
        factory.addProduction(T3, new Symbol[] { T3, plus, T2 },
                c -> {
            if (c[0] instanceof NumberLiteral && c[2] instanceof NumberLiteral)
                return new NumberLiteral(((NumberLiteral) c[0]).getFloatValue() + ((NumberLiteral) c[2]).getFloatValue());
            return new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer)
                    { return writer.getTypeEnv().binaryOpType(getValue().getType(), getExpChild(0).getType(writer), getExpChild(1).getType(writer)); }
                    @Override public void write(MethodWriter writer)
                    { writer.getTypeEnv().binaryOp(writer, getValue().getType(), getExpChild(0), getExpChild(1)); }
                };});
        factory.addProduction(T3, new Symbol[] { T3, minus, T2 },
                c -> {
                    if (c[0] instanceof NumberLiteral && c[2] instanceof NumberLiteral)
                        return new NumberLiteral(((NumberLiteral) c[0]).getFloatValue() - ((NumberLiteral) c[2]).getFloatValue());
            return new ExpNode(c[1].getValue(), c[0], c[2]) {
                        @Override public Type typeCheck(MethodWriter writer)
                        { return writer.getTypeEnv().binaryOpType(getValue().getType(), getExpChild(0).getType(writer), getExpChild(1).getType(writer)); }
                        @Override public void write(MethodWriter writer)
                        { writer.getTypeEnv().binaryOp(writer, getValue().getType(), getExpChild(0), getExpChild(1)); }
                    };
                });
        factory.addProduction(T2, new Symbol[] { T2, times, T1 },
                c -> {
                    if (c[0] instanceof NumberLiteral && c[2] instanceof NumberLiteral)
                        return new NumberLiteral(((NumberLiteral) c[0]).getFloatValue() * ((NumberLiteral) c[2]).getFloatValue());
            return new ExpNode(c[1].getValue(), c[0], c[2]) {
                        @Override public Type typeCheck(MethodWriter writer)
                        { return writer.getTypeEnv().binaryOpType(getValue().getType(), getExpChild(0).getType(writer), getExpChild(1).getType(writer)); }
                        @Override public void write(MethodWriter writer)
                        { writer.getTypeEnv().binaryOp(writer, getValue().getType(), getExpChild(0), getExpChild(1)); }
                    }
                ;});
        factory.addProduction(T2, new Symbol[] { T2, divide, T1 },
                c -> {
                    if (c[0] instanceof NumberLiteral && c[2] instanceof NumberLiteral) {
                        float v = ((NumberLiteral) c[0]).getFloatValue() / ((NumberLiteral) c[2]).getFloatValue();
                        return new NumberLiteral(((NumberLiteral) c[0]).isInteger() && ((NumberLiteral) c[2]).isInteger() ? (int) v : v);
                    }
            return new ExpNode(c[1].getValue(), c[0], c[2]) {
                    @Override public Type typeCheck(MethodWriter writer)
                    { return writer.getTypeEnv().binaryOpType(getValue().getType(), getExpChild(0).getType(writer), getExpChild(1).getType(writer)); }
                    @Override public void write(MethodWriter writer)
                    { writer.getTypeEnv().binaryOp(writer, getValue().getType(), getExpChild(0), getExpChild(1)); }
                };});
        factory.addProduction(T2, new Symbol[] { T1 }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { T0 }, c -> c[0]);
        factory.addProduction(T1, new Symbol[] { plus, T1 }, c -> c[1]);
        factory.addProduction(T1, new Symbol[] { minus, T1 }, c -> {
            if (c[1] instanceof NumberLiteral) return new NumberLiteral(-((NumberLiteral) c[1]).getFloatValue());
            return new ExpNode(c[0].getValue(), c[1]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void write(MethodWriter writer) {
                getExpChild(0).write(writer);
                writer.useOperator(FNEG);
            }
        };});
        factory.addProduction(T1, new Symbol[] { DI }, c -> c[0]);
        factory.addProduction(DI, new Symbol[] { increment, T0 }, c -> new StmtExpNode(c[0].getValue(), c[1]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void writeExp(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushVar(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToVar(name);
                writer.pushVar(name);
            }
            @Override public void writeStmt(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushVar(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToVar(name);
            }
        });
        factory.addProduction(DI, new Symbol[] { decrement, T0 }, c -> new StmtExpNode(c[0].getValue(), c[1]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void writeExp(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushVar(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToVar(name);
                writer.pushVar(name);
            }
            @Override public void writeStmt(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushVar(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToVar(name);
            }
        });
        factory.addProduction(DI, new Symbol[] { T0, increment }, c -> new StmtExpNode(c[1].getValue(), c[0]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void writeExp(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushVar(name);
                writer.pushVar(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToVar(name);
            }
            @Override public void writeStmt(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushVar(name);
                writer.pushFloat(1);
                writer.useOperator(FADD);
                writer.popToVar(name);
            }
        });
        factory.addProduction(DI, new Symbol[] { T0, decrement }, c -> new StmtExpNode(c[1].getValue(), c[0]) {
            @Override public Type typeCheck(MethodWriter writer) {
                return getExpChild(0).getType(writer); // todo: something better
            }
            @Override public void writeExp(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushVar(name);
                writer.dup(writer.getTypeEnv().getForDescriptor("I"));
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToVar(name);
            }
            @Override public void writeStmt(MethodWriter writer) {
                if (getChild(0).getValue().getType() != id)
                    throw new RuntimeException("variable expected!");
                String name = getChild(0).getValue().getValue();
                writer.pushVar(name);
                writer.pushFloat(1);
                writer.useOperator(FSUB);
                writer.popToVar(name);
            }
        });
        factory.addProduction(T0, new Symbol[] { openRound, E, closeRound }, c -> c[1]);
        factory.addProduction(T0, new Symbol[] { numberL }, c -> new NumberLiteral(c[0].getValue().getValue()));
        factory.addProduction(T0, new Symbol[] { stringL }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override public Type typeCheck(MethodWriter writer) { return writer.getTypeEnv().getForName("<stringL>"); }
            @Override public void write(MethodWriter writer) { writer.pushString(getValue().getValue().substring(2)); }
        });
        factory.addProduction(T0, new Symbol[] { id }, c -> new ExpNode(c[0].getValue(), c[0].getChildren()) {
            @Override public Type typeCheck(MethodWriter writer) { return writer.typeOf(getValue().getValue()); }
            @Override public void write(MethodWriter writer) { writer.pushVar(getValue().getValue()); }
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
    }
}
