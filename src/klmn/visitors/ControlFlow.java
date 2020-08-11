package klmn.visitors;

import codegen.CodeGenerator;
import codegen.SymbolTable;
import jvm.JVMType;
import jvm.Opcodes;
import jvm.methods.Label;
import parsing.AST;

import java.util.HashMap;
import java.util.Map;

public class ControlFlow
{
    public static Label target = null, conditionEnd = null;
    public static boolean invert = true;
    private final static Map<Byte, Byte> opposites = new HashMap<>();
    static {
        opposites.put(Opcodes.IFEQ, Opcodes.IFNE);
        opposites.put(Opcodes.IFNE, Opcodes.IFEQ);
        opposites.put(Opcodes.IFLT, Opcodes.IFGE);
        opposites.put(Opcodes.IFGT, Opcodes.IFLE);
        opposites.put(Opcodes.IFLE, Opcodes.IFGT);
        opposites.put(Opcodes.IFGE, Opcodes.IFLT);
    }

    private static void condition(byte opcode, CodeGenerator generator, AST... ast) {
        JVMType left = generator.applyTypeChecked(ast[0]);
        JVMType right = generator.applyTypeChecked(ast[2]);
        if (right != left) generator.convertTop(right, left);

        if (left == JVMType.FLOAT) {
            generator.useOperator(Opcodes.FCMPG);
            generator.useJmpOperator(invert ? opposites.get(opcode) : opcode, target);
        } else generator.useJmpOperator((byte) ((invert ? opposites.get(opcode) : opcode) + 6), target);
    }
    public static void lessThan(CodeGenerator generator, AST... ast) { condition(Opcodes.IFLT, generator, ast); }
    public static void greaterThan(CodeGenerator generator, AST... ast) { condition(Opcodes.IFGT, generator, ast); }
    public static void lessThanEq(CodeGenerator generator, AST... ast) { condition(Opcodes.IFLE, generator, ast); }
    public static void greaterThanEq(CodeGenerator generator, AST... ast) { condition(Opcodes.IFGE, generator, ast); }
    public static void equals(CodeGenerator generator, AST... ast) { condition(Opcodes.IFEQ, generator, ast); }
    public static void notEquals(CodeGenerator generator, AST... ast) { condition(Opcodes.IFNE, generator, ast); }

    public static void trueCondition(CodeGenerator generator, AST... ast)
    { if (!invert) generator.useJmpOperator(Opcodes.GOTO, target); }
    public static void falseCondition(CodeGenerator generator, AST... ast)
    { if (invert) generator.useJmpOperator(Opcodes.GOTO, target); }

    public static void and(CodeGenerator generator, AST... ast) {
        // if any of the branches are FALSE, skip to target.
        generator.apply(ast[0]);
        generator.apply(ast[2]);
    }

    public static void or(CodeGenerator generator, AST... ast) {
        // for or conditions you can skip the condition if one of the branches is true,
        // so every or condition we create a conditionEnd Label and set it as target.
        // we want to jump to it when the condition is TRUE, so we invert the jumping opcodes
        boolean newCondition = conditionEnd == null;
        if (newCondition) conditionEnd = new Label();
        // the rightmost branch of the or condition is different, and jumps to the original
        // target if the result is FALSE (thus evaluating the entire condition to FALSE)
        Label statementEnd = target;
        target = conditionEnd;
        boolean rightmost = invert; // will only be true for the rightmost branch
        invert = false;
        generator.apply(ast[0]);
        invert = rightmost;
        if (rightmost) target = statementEnd;
        generator.apply(ast[2]);
        if (newCondition) {
            generator.assign(conditionEnd);
            conditionEnd = null;
        }
    }

    public static void forStatement(CodeGenerator generator, AST[] ast) {
        Label loop = new Label(), end = new Label();
        target = end;
        generator.getSymbolTable().enterScope(SymbolTable.Context.BLOCK);
        generator.apply(ast[2]);
        generator.assign(loop);
        generator.apply(ast[3]);
        generator.apply(ast[ast.length == 8 ? 7 : 8]);
        generator.apply(ast[5]);
        generator.useJmpOperator(Opcodes.GOTO, loop);
        generator.assign(end);
        generator.getSymbolTable().exitScope();
    }

    public static void ifStatement(CodeGenerator generator, AST[] ast) {
        Label end = new Label();
        target = end;
        generator.getSymbolTable().enterScope(SymbolTable.Context.BLOCK);
        generator.apply(ast[2]);
        generator.apply(ast[ast.length == 5 ? 4 : 5]);
        generator.assign(end);
        generator.getSymbolTable().exitScope();
    }

    public static void whileStatement(CodeGenerator generator, AST[] ast) {
        Label loop = new Label(), end = new Label();
        target = end;
        generator.getSymbolTable().enterScope(SymbolTable.Context.BLOCK);
        generator.assign(loop);
        generator.apply(ast[2]);
        generator.apply(ast[ast.length == 5 ? 4 : 5]);
        generator.useJmpOperator(Opcodes.GOTO, loop);
        generator.assign(end);
        generator.getSymbolTable().exitScope();
    }
}
