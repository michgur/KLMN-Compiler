package klmn.visitors;

import codegen.CodeGenerator;
import codegen.SymbolTable;
import jvm.JVMType;
import jvm.Opcodes;
import klmn.KLMNTypes;
import parsing.AST;

public class Variables
{
    public static void declareVar(CodeGenerator generator, AST[] ast) {
        String name = ast[1].getText();
        if (generator.getSymbolTable().isDefinedLocally(name))
            throw new RuntimeException("variable " + name + " already defined!");
        JVMType type = KLMNTypes.getType(ast[0]);

        if (generator.getSymbolTable().getContext() == SymbolTable.Context.CLASS)
            generator.addField(name, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, type);
        else generator.getSymbolTable().addSymbol(name, type);
        if (ast.length > 2) {
            JVMType left = generator.getSymbolTable().typeOf(name);
            JVMType right = generator.applyTypeChecked(ast[3]);
            if (right != left) generator.convertTop(right, left);
            generator.popTo(name);
        }
    }

    public static void assign(CodeGenerator generator, AST[] ast) {
        JVMType left = generator.getSymbolTable().typeOf(ast[0].getText());
        JVMType right = generator.applyTypeChecked(ast[2]);
        if (right != left) generator.convertTop(right, left);
        generator.popTo(ast[0].getText());
    }
}
