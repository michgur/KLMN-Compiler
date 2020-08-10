package klmn.visitors;

import codegen.CodeGenerator;
import codegen.SymbolTable;
import jvm.JVMType;
import jvm.Opcodes;
import parsing.AST;

import java.util.HashMap;
import java.util.Map;

public class Modules
{
    final static Map<MethodVisitor, AST> methods = new HashMap<>();

    public static void apply(CodeGenerator generator, AST... ast) {
        generator.getSymbolTable().enterScope(SymbolTable.Context.CLASS);
        generator.getSymbolTable().enterScope(SymbolTable.Context.BLOCK);
        generator.addMethod("<clinit>", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, JVMType.VOID);
        generator.editMethod("<clinit>");
        generator.apply(ast[0]);
        generator.ret();

        for (MethodVisitor method : methods.keySet()) method.apply(generator, methods.get(method));
        methods.clear();
    }
}
