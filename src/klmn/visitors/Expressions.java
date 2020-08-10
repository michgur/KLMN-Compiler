package klmn.visitors;

import codegen.CodeGenerator;
import jvm.JVMType;
import jvm.Opcodes;
import klmn.KLMNSymbols;
import parsing.AST;

public class Expressions implements KLMNSymbols
{
    public static JVMType length(CodeGenerator generator, AST... ast) {
//        KW_LENGTH ROUND_OPEN IDENTIFIER ROUND_CLOSE
        generator.push(ast[2].getText());
        generator.useOperator(Opcodes.ARRAYLENGTH);
        return JVMType.INTEGER;
    }

    private static JVMType unaryOpPre(byte opcode, CodeGenerator generator, AST... ast) {
        if (ast[1].getChildren()[0].getValue() != IDENTIFIER)
            throw new RuntimeException("variable expected!");
        String name = ast[1].getChildren()[0].getText();
        JVMType type = generator.getSymbolTable().typeOf(name);
        generator.push(name);
        if (type == JVMType.FLOAT) generator.pushFloat(1);
        else generator.pushInt(1);
        generator.useOperator(getOpcode(opcode, type));
        generator.popTo(name);
        generator.push(name);
        return type;
    }
    private static JVMType unaryOpPost(byte opcode, CodeGenerator generator, AST... ast) {
        if (ast[0].getChildren()[0].getValue() != IDENTIFIER)
            throw new RuntimeException("variable expected!");
        String name = ast[0].getChildren()[0].getText();
        JVMType type = generator.getSymbolTable().typeOf(name);
        generator.push(name);
        generator.dup();
        if (type == JVMType.FLOAT) generator.pushFloat(1);
        else generator.pushInt(1);
        generator.useOperator(getOpcode(opcode, type));
        generator.popTo(name);
        return type;
    }
    public static JVMType preIncrement(CodeGenerator generator, AST... ast) { return unaryOpPre(Opcodes.IADD, generator, ast); }
    public static JVMType preDecrement(CodeGenerator generator, AST... ast) { return unaryOpPre(Opcodes.ISUB, generator, ast); }
    public static JVMType postIncrement(CodeGenerator generator, AST... ast) { return unaryOpPost(Opcodes.IADD, generator, ast); }
    public static JVMType postDecrement(CodeGenerator generator, AST... ast) { return unaryOpPost(Opcodes.ISUB, generator, ast); }

    public static JVMType negate(CodeGenerator generator, AST... ast) {
        JVMType type = generator.applyTypeChecked(ast[1]);
        generator.useOperator(getOpcode(Opcodes.INEG, type));
        return type;
    }

    private static byte getOpcode(byte opcode, JVMType type) {
        return (byte) (opcode + switch (type.getDescriptor()) {
            case "I" -> 0;
            case "J" -> 1;
            case "F" -> 2;
            case "D" -> 3;
            default -> throw new RuntimeException();
        });
    }
    private static JVMType binaryOp(byte opcode, CodeGenerator generator, AST... ast) {
        JVMType left = generator.applyTypeChecked(ast[0]);
        JVMType right = generator.applyTypeChecked(ast[2]);
        if (right != left) generator.convertTop(right, left);

        generator.useOperator(getOpcode(opcode, left));
        return left;
    }
    public static JVMType add(CodeGenerator generator, AST... ast) { return binaryOp(Opcodes.IADD, generator, ast); }
    public static JVMType sub(CodeGenerator generator, AST... ast) { return binaryOp(Opcodes.ISUB, generator, ast); }
    public static JVMType mul(CodeGenerator generator, AST... ast) { return binaryOp(Opcodes.IMUL, generator, ast); }
    public static JVMType div(CodeGenerator generator, AST... ast) { return binaryOp(Opcodes.IDIV, generator, ast); }
    public static JVMType rem(CodeGenerator generator, AST... ast) { return binaryOp(Opcodes.IREM, generator, ast); }

    public static void print(CodeGenerator generator, AST... ast) {
        generator.pushField("java/lang/System", "out", JVMType.refType("java.io.PrintStream"), true);
        JVMType type = generator.applyTypeChecked(ast[1]);
        generator.call("java/io/PrintStream", "println", JVMType.VOID,
                type.isPrimitive() ? type : JVMType.refType("java/lang/Object"));
    }
}
