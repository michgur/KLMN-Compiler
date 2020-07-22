package klmn.visitors;

import codegen.CodeGenerator;
import codegen.SymbolTable;
import jvm.JVMType;
import jvm.Opcodes;
import klmn.KLMNSymbols;
import klmn.KLMNTypes;
import parsing.AST;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MethodVisitor implements CodeGenerator.Visitor
{
    private String name;
    private List<Pair<JVMType, String>> params;

    @Override
    public void apply(CodeGenerator generator, AST... ast) {
        generator.editMethod(name);
        generator.getSymbolTable().enterScope(SymbolTable.Context.BLOCK);
        for (Pair<JVMType, String> p : params)
            generator.getSymbolTable().addSymbol(p.getValue(), p.getKey());
        generator.apply(ast[0]);
        generator.ret();
    }

    public void declare(CodeGenerator generator, AST... ast) {
        AST p = ast[3];

        params = new ArrayList<>();
        if (p.getChildren().length > 0) {
        p.getChildren()[0].leftmostTraverse(KLMNSymbols.PARAMS_DECL_NON_ZERO, param -> {
//            PARAMS_DECL_NON_ZERO -> TYPE IDENTIFIER
//            PARAMS_DECL_NON_ZERO -> PARAMS_DECL_NON_ZERO COMMA TYPE IDENTIFIER
            int index = param.getChildren().length - 2;
            params.add(Pair.of(KLMNTypes.getType(param.getChildren()[index]).getJvmType(),
                    param.getChildren()[index + 1].getText()));
        });
        }

        JVMType type = KLMNTypes.getType(ast[0]).getJvmType();
        JVMType[] paramTypes = new JVMType[params.size()];
        for (int i = 0; i < paramTypes.length; i++) paramTypes[i] = params.get(i).getKey();

        name = ast[1].getText();
        generator.addMethod(ast[1].getText(), Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, type, paramTypes);

        Modules.methods.put(this, ast[6]);
    }
}
