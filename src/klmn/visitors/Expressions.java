package klmn.visitors;

import codegen.CodeGenerator;
import jvm.JVMType;
import jvm.Opcodes;
import klmn.KLMNSymbols;
import lexing.Symbol;
import parsing.AST;

public class Expressions implements KLMNSymbols
{
    public static void length(CodeGenerator generator, AST... ast) {
//        KW_LENGTH ROUND_OPEN IDENTIFIER ROUND_CLOSE
        generator.push(ast[2].getText());
        generator.useOperator(Opcodes.ARRAYLENGTH);
        generator.convertTop(JVMType.INTEGER, JVMType.FLOAT);
    }

    private static void unaryOpPre(byte opcode, CodeGenerator generator, AST... ast) {
        if (ast[1].getChildren()[0].getValue() != IDENTIFIER)
            throw new RuntimeException("variable expected!");
        String name = ast[1].getChildren()[0].getText();
        generator.push(name);
        generator.pushFloat(1);
        generator.useOperator(opcode);
        generator.popTo(name);
        generator.push(name);
    }
    private static void unaryOpPost(byte opcode, CodeGenerator generator, AST... ast) {
        if (ast[0].getChildren()[0].getValue() != IDENTIFIER)
            throw new RuntimeException("variable expected!");
        String name = ast[0].getChildren()[0].getText();
        generator.push(name);
        generator.dup();
        generator.pushFloat(1);
        generator.useOperator(opcode);
        generator.popTo(name);
    }
    public static void preIncrement(CodeGenerator generator, AST... ast) { unaryOpPre(Opcodes.FADD, generator, ast); }
    public static void preDecrement(CodeGenerator generator, AST... ast) { unaryOpPre(Opcodes.FSUB, generator, ast); }
    public static void postIncrement(CodeGenerator generator, AST... ast) { unaryOpPost(Opcodes.FADD, generator, ast); }
    public static void postDecrement(CodeGenerator generator, AST... ast) { unaryOpPost(Opcodes.FSUB, generator, ast); }

    public static void negate(CodeGenerator generator, AST... ast) {
        generator.apply(ast[1]);
        generator.useOperator(Opcodes.FNEG);
    }

    private static void binaryOp(byte opcode, CodeGenerator generator, AST... ast) {
        generator.apply(ast[0]);
        generator.apply(ast[2]);
        generator.useOperator(opcode);
    }
    public static void add(CodeGenerator generator, AST... ast) { binaryOp(Opcodes.FADD, generator, ast); }
    public static void sub(CodeGenerator generator, AST... ast) { binaryOp(Opcodes.FSUB, generator, ast); }
    public static void mul(CodeGenerator generator, AST... ast) { binaryOp(Opcodes.FMUL, generator, ast); }
    public static void div(CodeGenerator generator, AST... ast) { binaryOp(Opcodes.FDIV, generator, ast); }
    public static void rem(CodeGenerator generator, AST... ast) { binaryOp(Opcodes.FREM, generator, ast); }

    public static void print(CodeGenerator generator, AST... ast) {
        generator.pushField("java/lang/System", "out", JVMType.refType("java.io.PrintStream"), true);
        generator.apply(ast[1]);
        generator.call("java/io/PrintStream", "println", JVMType.VOID, JVMType.FLOAT);
    }
}
