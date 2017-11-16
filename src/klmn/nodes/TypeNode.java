package klmn.nodes;

import ast.AST;
import klmn.writing.MethodWriter;
import klmn.writing.ModuleWriter;
import klmn.writing.TypeEnv;
import lang.Token;

public class TypeNode extends AST
{
    private int dim = 0;
    public TypeNode(Token value) { super(value); }
    public TypeEnv.Type get(ModuleWriter writer) { return writer.getTypeEnv().getForName(getValue().getValue(), dim); }
    public TypeEnv.Type get(MethodWriter writer) { return writer.getTypeEnv().getForName(getValue().getValue(), dim); }
    public TypeNode arrayOfThis() {
        TypeNode t = new TypeNode(getValue());
        t.dim = dim + 1;
        return t;
    }
}
