package klmn.visitors;

import codegen.CodeGenerator;
import codegen.SymbolTable;
import jvm.JVMType;
import jvm.Opcodes;
import klmn.KLMNTypes;
import parsing.AST;

import java.util.Stack;

public class ArrayExpressions
{
    public static void assign(CodeGenerator generator, AST[] ast) {
        generator.push(ast[0].getText());
        generator.apply(ast[2]);
        generator.convertTop(JVMType.FLOAT, JVMType.INTEGER);
        generator.apply(ast[5]);
        generator.popToArray(ast[0].getText());
    }

    public static void access(CodeGenerator generator, AST[] ast) {
        generator.push(ast[0].getText());
        generator.apply(ast[2]);
        generator.convertTop(JVMType.FLOAT, JVMType.INTEGER);
        generator.pushFromArray(ast[0].getText());
    }

    private static final JVMType FLOAT_ARRAY = JVMType.arrayType(JVMType.FLOAT, 1);
    public static void init(CodeGenerator generator, AST[] ast) {
        String name = ast[4].getText();
        if (generator.getSymbolTable().isDefinedLocally(name)) throw new RuntimeException("variable " + name + " already defined!");
        if (generator.getSymbolTable().getContext() != SymbolTable.Context.CLASS) {
            generator.getSymbolTable().addSymbol(name, FLOAT_ARRAY);
        } else { generator.addField(name, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FLOAT_ARRAY); }
        generator.apply(ast[2]);
        generator.convertTop(JVMType.FLOAT, JVMType.INTEGER);
        generator.pushNewArray(KLMNTypes.FLOAT_ARRAY.getJvmType());
        generator.popTo(name);
    }
    public static void initWithValues(CodeGenerator generator, AST[] ast) {
        String name = ast[3].getText();
        if (generator.getSymbolTable().isDefinedLocally(name)) throw new RuntimeException("variable " + name + " already defined!");
        if (generator.getSymbolTable().getContext() != SymbolTable.Context.CLASS) {
            generator.getSymbolTable().addSymbol(name, FLOAT_ARRAY);
        } else { generator.addField(name, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FLOAT_ARRAY); }

        AST value = ast[6];
        Stack<AST> values = new Stack<>();
        while (value.getChildren().length > 1) {
            values.push(value.getChildren()[2]);
            value = value.getChildren()[0];
        } values.push(value.getChildren()[0]);
        generator.pushInt(values.size());
        generator.pushNewArray(KLMNTypes.FLOAT_ARRAY.getJvmType());
        for (int i = 0, size = values.size(); i < size; i++) {
            generator.dup();
            generator.pushInt(i);
            generator.apply(values.pop());
            generator.popToArray(name);
        }
        generator.popTo(name);
    }
}
