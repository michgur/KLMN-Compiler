package klmn.nodes;

import ast.AST;
import klmn.writing.MethodWriter;
import klmn.writing.ModuleWriter;
import klmn.writing.types.Type;
import lang.Token;

public class TypeNode extends AST
{
    private int dim = 0;
    public TypeNode(Token value) { super(value); }
    public Type get(ModuleWriter writer) { return writer.getTypeEnv().getForName(getValue().getValue(), dim); }
    public Type get(MethodWriter writer) { return writer.getTypeEnv().getForName(getValue().getValue(), dim); }
    public TypeNode arrayOfThis() {
        TypeNode t = new TypeNode(getValue());
        if (dim >= 15) throw new RuntimeException("maximum array dimensions: 15");
        t.dim = dim + 1;
        return t;
    }

    public static class TupleTypeNode extends TypeNode
    {
        public TupleTypeNode(AST types) {
            super(new Token("Tuple Type"));
            for (AST t : types.getChildren()) addChild(t);
        }

        @Override
        public Type get(ModuleWriter writer) {
            Type[] params = new Type[getChildren().size()];
            for (int i = 0; i < params.length; i++)
                params[i] = ((TypeNode) getChildren().get(i)).get(writer);
            return new Type.Tuple(params);
        }
        @Override
        public Type get(MethodWriter writer) {
            Type[] params = new Type[getChildren().size()];
            for (int i = 0; i < params.length; i++)
                params[i] = ((TypeNode) getChildren().get(i)).get(writer);
            return new Type.Tuple(params);
        }
    }

    public static class FuncTypeNode extends TypeNode
    {
        public FuncTypeNode(TypeNode ret, AST params) {
            super(new Token("Function Type"));
            addChild(ret);
            for (AST t : params.getChildren()) addChild(t);
        }

        @Override
        public Type get(ModuleWriter writer) {
            Type[] params = new Type[getChildren().size() - 1];
            for (int i = 1; i < params.length; i++)
                params[i - 1] = ((TypeNode) getChildren().get(i)).get(writer);
            return new Type.Function(((TypeNode)getChild(0)).get(writer), params);
        }
        @Override
        public Type get(MethodWriter writer) {
            Type[] params = new Type[getChildren().size() - 1];
            for (int i = 0; i < params.length; i++)
                params[i] = ((TypeNode) getChildren().get(i + 1)).get(writer);
            return new Type.Function(((TypeNode)getChild(0)).get(writer), params);
        }
    }
}
