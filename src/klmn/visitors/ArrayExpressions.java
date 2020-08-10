package klmn.visitors;

import codegen.CodeGenerator;
import codegen.SymbolTable;
import jvm.JVMType;
import jvm.Opcodes;
import klmn.KLMNSymbols;
import klmn.KLMNTypes;
import parsing.AST;

import java.util.ArrayList;
import java.util.List;

public class ArrayExpressions
{
    public static void assign(CodeGenerator generator, AST[] ast) {
        generator.push(ast[0].getText());
        JVMType indexType = generator.applyTypeChecked(ast[2]);
        if (indexType == JVMType.FLOAT) generator.convertTop(JVMType.FLOAT, JVMType.INTEGER);
        else if (indexType != JVMType.INTEGER)
            throw new RuntimeException("error when accessing array " + ast[0].getText() + "- integer expected ");
        JVMType arrayType = generator.getSymbolTable().typeOf(ast[0].getText()).getBaseType();
        JVMType valueType = generator.applyTypeChecked(ast[5]);
        if (valueType != arrayType) generator.convertTop(valueType, arrayType);
        generator.popToArray(ast[0].getText());
    }

    public static JVMType access(CodeGenerator generator, AST[] ast) {
        generator.push(ast[0].getText());
        JVMType type = generator.applyTypeChecked(ast[2]);
        if (type == JVMType.FLOAT) generator.convertTop(JVMType.FLOAT, JVMType.INTEGER);
        else if (type != JVMType.INTEGER)
            throw new RuntimeException("error when accessing array " + ast[0].getText() + "- integer expected ");
        generator.pushFromArray(ast[0].getText());
        return generator.getSymbolTable().typeOf(ast[0].getText()).getBaseType();
    }

    public static void init(CodeGenerator generator, AST[] ast) {
        String name = ast[4].getText();
        JVMType type = JVMType.arrayType(KLMNTypes.getType(ast[0]));
        if (generator.getSymbolTable().isDefinedLocally(name)) throw new RuntimeException("variable " + name + " already defined!");
        if (generator.getSymbolTable().getContext() != SymbolTable.Context.CLASS) {
            generator.getSymbolTable().addSymbol(name, type);
        } else { generator.addField(name, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, type); }
        generator.apply(ast[2]);
        generator.convertTop(JVMType.FLOAT, JVMType.INTEGER);
        generator.pushNewArray(type);
        generator.popTo(name);
    }
    public static void initWithValues(CodeGenerator generator, AST[] ast) {
        String name = ast[3].getText();
        JVMType type = JVMType.arrayType(KLMNTypes.getType(ast[0]));
        if (!type.isArrayType()) throw new RuntimeException("array type expected!");

        if (generator.getSymbolTable().isDefinedLocally(name)) throw new RuntimeException("variable " + name + " already defined!");
        if (generator.getSymbolTable().getContext() != SymbolTable.Context.CLASS) {
            generator.getSymbolTable().addSymbol(name, type);
        } else { generator.addField(name, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, type); }

        List<AST> values = new ArrayList<>();
        ast[6].leftmostTraverse(KLMNSymbols.ARRAY_VALUE, value -> {
//            ARRAY_VALUE -> EXPRESSION
//            ARRAY_VALUE -> ARRAY_VALUE COMMA EXPRESSION
            int index = value.getChildren().length - 1;
            values.add(value.getChildren()[index]);
        });
        generator.pushInt(values.size());
        generator.pushNewArray(type);
        JVMType arrayType = type.getBaseType();
        for (int i = 0, size = values.size(); i < size; i++) {
            generator.dup();
            generator.pushInt(i);
            JVMType valueType = generator.applyTypeChecked(values.get(i));
            if (valueType != arrayType) generator.convertTop(valueType, arrayType);
            generator.popToArray(name);
        }
        generator.popTo(name);
    }
}
