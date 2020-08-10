package klmn;

import jvm.JVMType;
import parsing.AST;

public interface KLMNTypes
{
    static JVMType getType(AST ast) {
        if (ast.getChildren().length > 1)
            return JVMType.arrayType(getType(ast.getChildren()[0]), 1);
        else if (ast.getChildren().length == 1) ast = ast.getChildren()[0];
        return switch (ast.getText()) {
            case "int" -> JVMType.INTEGER;
            case "float" -> JVMType.FLOAT;
            case "string" -> JVMType.refType("java/lang/String");
            case "void" -> JVMType.VOID;
            case "bool" -> JVMType.BOOLEAN;
            default -> throw new RuntimeException(ast.getText() + " is not type");
        };
    }
}
