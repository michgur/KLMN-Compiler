package klmn.visitors;

import codegen.CodeGenerator;
import codegen.SymbolTable;
import codegen.Type;
import jvm.Opcodes;
import klmn.KLMNTypes;
import parsing.AST;

public class Variables
{
    public static void declareVar(CodeGenerator generator, AST[] ast) {
        String name = ast[1].getText();
        if (generator.getSymbolTable().isDefinedLocally(name))
            throw new RuntimeException("variable " + name + " already defined!");
        Type type = KLMNTypes.getType(ast[0]);

        if (generator.getSymbolTable().getContext() == SymbolTable.Context.CLASS)
            generator.addField(name, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, type.getJvmType());
        else generator.getSymbolTable().addSymbol(name, type.getJvmType());
        if (ast.length > 2) {
            generator.apply(ast[3]);
            generator.popTo(name);
        }
    }

    public static void assign(CodeGenerator generator, AST[] ast) {
        generator.apply(ast[2]);
        generator.popTo(ast[0].getText());
    }
}
