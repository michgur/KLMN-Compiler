package klmn;

import codegen.CodeGenerator;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import lexing.Symbol;
import codegen.SymbolTable;
import parsing.AST;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CodeGenInit implements Opcodes, KLMNSymbols
{
    public static CodeGenerator initialize_codeGenerator() {
        CodeGenerator generator = new CodeGenerator();
        SymbolTable st = new SymbolTable();
        st.enterScope();

        final Label[] condEnd = { new Label() }; // the end of current condition
        final boolean[] insideCond = { false }, // whether current AST node is inside a condition
                skipFor = { false }; // for conditions, the boolean they should skip to condEnd for

        generator.addGenerator(BLOCK, (MethodVisitor mv, AST[] ast) -> {
             generator.apply(mv, ast[0]);
             if (ast.length == 2) generator.apply(mv, ast[1]);
        });
        generator.addGenerator(STATEMENT_FULL, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 2) generator.apply(mv, ast[0]);
            else if (ast[0].getValue() == KW_FOR) {
                Label loop = new Label(), end = new Label();
                st.enterScope();
                generator.apply(mv, ast[2]);
                mv.visitLabel(loop);
                applyFrame(mv, 0, null);
                condEnd[0] = end;
                insideCond[0] = true;
                generator.apply(mv, ast[3]);
                insideCond[0] = false;
                st.enterScope();
                generator.apply(mv, ast[ast.length == 8 ? 7 : 8]);
                removeLocals(st.exitScope());
                generator.apply(mv, ast[5]);
                mv.visitJumpInsn(GOTO, loop);
                removeLocals(st.exitScope());
                mv.visitLabel(end);
                applyFrame(mv, 0, null);
            }
            else if (ast[0].getValue() == KW_WHILE) {
                Label loop = new Label(), end = new Label();
                st.enterScope();
                mv.visitLabel(loop);
                applyFrame(mv, 0, null);
                condEnd[0] = end;
                insideCond[0] = true;
                generator.apply(mv, ast[2]);
                insideCond[0] = false;
                generator.apply(mv, ast[ast.length == 5 ? 4 : 5]);
                mv.visitJumpInsn(GOTO, loop);
                removeLocals(st.exitScope());
                mv.visitLabel(end);
                applyFrame(mv, 0, null);
            }
            else if (ast[0].getValue() == KW_IF) {
                Label end = new Label();
                condEnd[0] = end;
                insideCond[0] = true;
                generator.apply(mv, ast[2]);
                insideCond[0] = false;
                st.enterScope();
                applyFrame(mv, 0, null);
                generator.apply(mv, ast[ast.length == 5 ? 4 : 5]);
                removeLocals(st.exitScope());
                mv.visitLabel(end);
                applyFrame(mv, 0, null);
            }
        });
        generator.addGenerator(STATEMENT, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 1) {
                generator.apply(mv, ast[0]);
                if (ast[0].getValue() == VALUE_UNARY_OP) mv.visitInsn(POP);
            } else {
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                generator.apply(mv, ast[1]);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);
            }
        });
        generator.addGenerator(ASSIGNMENT, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 4) {
                String name = ast[1].getText();
                if (st.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                st.addSymbol(name);
                generator.apply(mv, ast[3]);
                mv.visitVarInsn(FSTORE, st.findSymbol(name));
                addLocal(FLOAT);
            } else if (ast.length == 6) {
                mv.visitVarInsn(ALOAD, st.findSymbol(ast[0].getText()));
                generator.apply(mv, ast[2]);
                mv.visitInsn(F2I);
                generator.apply(mv, ast[5]);
                mv.visitInsn(FASTORE);
            } else {
                generator.apply(mv, ast[2]);
                mv.visitVarInsn(FSTORE, st.findSymbol(ast[0].getText()));
            }
        });
        generator.addGenerator(ARRAY_INIT, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 5) {
                String name = ast[4].getText();
                if (st.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                st.addSymbol(name);
                generator.apply(mv, ast[2]);
                mv.visitInsn(F2I);
                mv.visitIntInsn(NEWARRAY, T_FLOAT);
                mv.visitVarInsn(ASTORE, st.findSymbol(name));
                addLocal("[F");
            } else {
                String name = ast[3].getText();
                if (st.checkScope(name)) throw new RuntimeException("variable " + name + " already defined!");
                st.addSymbol(name);

                AST value = ast[6];
                Stack<AST> values = new Stack<>();
                while (value.getChildren().length > 1) {
                    values.push(value.getChildren()[2]);
                    value = value.getChildren()[0];
                } values.push(value.getChildren()[0]);
                mv.visitLdcInsn(values.size());
                mv.visitIntInsn(NEWARRAY, T_FLOAT);
                for (int i = 0, size = values.size(); i < size; i++) {
                    mv.visitInsn(DUP);
                    mv.visitLdcInsn(i);
                    generator.apply(mv, values.pop());
                    mv.visitInsn(FASTORE);
                }
                mv.visitVarInsn(ASTORE, st.findSymbol(name));
                addLocal("[F");
            }
        });
        generator.addGenerator(EXPRESSION, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 1) generator.apply(mv, ast[0]);
            else {
                if (insideCond[0]) {
                    Label end = condEnd[0], body = new Label();
                    condEnd[0] = body;
                    skipFor[0] = !skipFor[0];
                    generator.apply(mv, ast[0]); // true -> skip to 'body', false -> resume
                    skipFor[0] = !skipFor[0];
                    condEnd[0] = end;
                    applyFrame(mv, 0, null);
                    generator.apply(mv, ast[2]); // true -> resume, false -> skip to 'end'
                    mv.visitLabel(body);
                    applyFrame(mv, 0, null);
                    return;
                }
                Label cond2 = new Label(), end = new Label();
                generator.apply(mv, ast[0]);
                mv.visitJumpInsn(IFEQ, cond2); // if false(=0), check other cond
                mv.visitInsn(ICONST_1); // if true(=0), push true and end
                mv.visitJumpInsn(GOTO, end);
                mv.visitLabel(cond2); // here we get the final result
                applyFrame(mv, 0, null);
                generator.apply(mv, ast[2]);
                mv.visitLabel(end);
                applyFrame(mv, 1, new Object[] { INTEGER });
            }
        });
        generator.addGenerator(VALUE_LOGICAL_OP, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 1) generator.apply(mv, ast[0]);
            else {
                if (insideCond[0]) {
                    if (skipFor[0]) {
                        skipFor[0] = false;
                        generator.apply(mv, ast[0]); // true -> skip to END, false -> resume
                        skipFor[0] = true;
                        applyFrame(mv, 0, null);
                        generator.apply(mv, ast[2]); // true -> skip to END, false -> resume
                        return;
                    }
                    generator.apply(mv, ast[0]);
                    applyFrame(mv, 0, null);
                    generator.apply(mv, ast[2]);
                    return;
                }
                Label cond2 = new Label(), end = new Label();
                generator.apply(mv, ast[0]);
                mv.visitJumpInsn(IFNE, cond2); // if true(=1), check other cond
                mv.visitInsn(ICONST_0); // if false(=0), push false and end
                mv.visitJumpInsn(GOTO, end);
                mv.visitLabel(cond2); // here we get the final result
                applyFrame(mv, 0, null);
                generator.apply(mv, ast[2]);
                mv.visitLabel(end);
                applyFrame(mv, 1, new Object[] { INTEGER });
            }
        });
        generator.addGenerator(VALUE_EQUALS_OP, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 1) generator.apply(mv, ast[0]);
            else {
                if (insideCond[0]) {
                    generator.apply(mv, ast[0]);
                    generator.apply(mv, ast[2]);
                    mv.visitInsn(FCMPG);
                    if (skipFor[0] == (ast[1].getValue() == EQUALS)) mv.visitJumpInsn(IFEQ, condEnd[0]);
                    //          skipfor     !skipfor
                    //equals    true        false
                    //nequals   false       true
                    else mv.visitJumpInsn(IFNE, condEnd[0]);
                    return;
                }
                generator.apply(mv, ast[0]);
                generator.apply(mv, ast[2]);
                mv.visitInsn(FCMPG);
                if (ast[1].getValue() == EQUALS) {
                    mv.visitInsn(DUP);
                    mv.visitInsn(IXOR);
                }
            }
        });
        generator.addGenerator(VALUE_COMP_OP, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 1) generator.apply(mv, ast[0]);
            else {
                // fixme only does less-than
                if (insideCond[0]) {
                    generator.apply(mv, ast[0]);
                    generator.apply(mv, ast[2]);
                    mv.visitInsn(FCMPG);
                    if (ast[1].getValue() == LT) {
                        if (skipFor[0]) mv.visitJumpInsn(IFLT, condEnd[0]);
                        else mv.visitJumpInsn(IFGE, condEnd[0]);
                    }
                    else if (ast[1].getValue() == GT) {
                        if (skipFor[0]) mv.visitJumpInsn(IFGT, condEnd[0]);
                        else mv.visitJumpInsn(IFLE, condEnd[0]);
                    }
                    else if (ast[1].getValue() == LTEQUALS) {
                        if (skipFor[0]) mv.visitJumpInsn(IFLE, condEnd[0]);
                        else mv.visitJumpInsn(IFGT, condEnd[0]);
                    }
                    else if (ast[1].getValue() == GTEQUALS) {
                        if (skipFor[0]) mv.visitJumpInsn(IFGE, condEnd[0]);
                        else mv.visitJumpInsn(IFLT, condEnd[0]);
                    }
                    return;
                }
                generator.apply(mv, ast[0]);
                generator.apply(mv, ast[2]);
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
        generator.addGenerator(VALUE_TIMES_OP, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 1) generator.apply(mv, ast[0]);
            else {
                generator.apply(mv, ast[0]);
                generator.apply(mv, ast[2]);

                Symbol op = ast[1].getValue();
                if (op == TIMES) mv.visitInsn(FMUL);
                else if (op == DIVIDE) mv.visitInsn(FDIV);
                else if (op == MODULO) mv.visitInsn(FREM);
            }
        });
        generator.addGenerator(VALUE_PLUS_OP, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 1) generator.apply(mv, ast[0]);
            else {
                generator.apply(mv, ast[0]);
                generator.apply(mv, ast[2]);

                Symbol op = ast[1].getValue();
                if (op == PLUS) mv.visitInsn(FADD);
                else if (op == MINUS) mv.visitInsn(FSUB);
            }
        });
        generator.addGenerator(VALUE_SIGNED, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 1) generator.apply(mv, ast[0]);
            else {
                generator.apply(mv, ast[1]);
                if (ast[0].getValue() == MINUS) mv.visitInsn(FNEG);
            }
        });
        generator.addGenerator(VALUE_UNARY_OP, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 4) {
                mv.visitVarInsn(ALOAD, st.findSymbol(ast[2].getText()));
                mv.visitInsn(ARRAYLENGTH);
                mv.visitInsn(I2F);
            } else if (ast[0].getValue() == VALUE) {
                if (ast[0].getChildren()[0].getValue() != IDENTIFIER)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(ast[0].getChildren()[0].getText());
                mv.visitVarInsn(FLOAD, i);
                mv.visitInsn(DUP);
                mv.visitInsn(FCONST_1);
                mv.visitInsn(ast[1].getValue() == INCREMENT ? FADD: FSUB);
                mv.visitVarInsn(FSTORE, i);
            } else {
                if (ast[1].getChildren()[0].getValue() != IDENTIFIER)
                    throw new RuntimeException("variable expected!");
                int i = st.findSymbol(ast[1].getChildren()[0].getText());
                mv.visitVarInsn(FLOAD, i);
                mv.visitInsn(FCONST_1);
                mv.visitInsn(ast[0].getValue() == INCREMENT ? FADD: FSUB);
                mv.visitVarInsn(FSTORE, i);
                mv.visitVarInsn(FLOAD, i);
            }
        });
        generator.addGenerator(VALUE, (MethodVisitor mv, AST[] ast) -> {
            if (ast.length == 3) generator.apply(mv, ast[1]);
            else if (ast.length == 4) {
                mv.visitVarInsn(ALOAD, st.findSymbol(ast[0].getText()));
                generator.apply(mv, ast[2]);
                mv.visitInsn(F2I);
                mv.visitInsn(FALOAD);
            }
            else if (ast[0].getValue() == NUMBER) mv.visitLdcInsn(Float.valueOf(ast[0].getText()));
            else if (ast[0].getValue() == IDENTIFIER) mv.visitVarInsn(FLOAD, st.findSymbol(ast[0].getText()));
            else if (ast[0].getValue() == KW_FALSE) {
                if (insideCond[0]) {
                    if (!skipFor[0]) mv.visitJumpInsn(GOTO, condEnd[0]);
                    return;
                }
                mv.visitInsn(ICONST_0);
            } else if (ast[0].getValue() == KW_TRUE) {
                if (insideCond[0]) {
                    if (skipFor[0]) mv.visitJumpInsn(GOTO, condEnd[0]);
                    return;
                }
                mv.visitInsn(ICONST_1);
            }
        });

        return generator;
    }

    private static int arrIndex = 0;
    public static int getMaxLocals() { return maxLocals; }

    // TODO: understand what this shit VVV is all about
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
