package klmn;

import codegen.Type;
import jvm.JVMType;
import parsing.AST;

public interface KLMNTypes
{
    Type INT = new Type("int", JVMType.INTEGER);
    Type FLOAT = new Type("float", JVMType.FLOAT);
    Type STRING = new Type("string", JVMType.refType("java/lang/String"));
    Type VOID = new Type("void", JVMType.VOID);
    Type BOOL = new Type("bool", JVMType.BOOLEAN);
    Type FLOAT_ARRAY = new Type.Array(FLOAT);

    static Type getType(AST ast) {
        if (ast.getChildren().length != 1)
            return new Type.Array(getType(ast.getChildren()[0]));
        return switch (ast.getChildren()[0].getText()) {
            case "int" -> INT;
            case "float" -> FLOAT;
            case "string" -> STRING;
            case "void" -> VOID;
            case "bool" -> BOOL;
            default -> throw new RuntimeException(ast.getChildren()[0].getText() + " is not type");
        };
    }
}
