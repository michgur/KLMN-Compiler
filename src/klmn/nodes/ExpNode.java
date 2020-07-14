package klmn.nodes;

import ast.AST;
import klmn.writing.MethodWriter;
import klmn.writing.types.Type;
import klmn.writing.types.TypeEnv;
import lang.Token;

import java.util.List;

public abstract class ExpNode extends AST implements MethodNode.BodyNode {
    private Type type;
    public ExpNode(Token value, List<AST> children) { super(value, children); }
    public ExpNode(Token value, AST... children) { super(value, children); }

    protected abstract Type typeCheck(MethodWriter writer);
    public Type getType(MethodWriter writer) { return (type != null) ? type : (type = typeCheck(writer)); }

    public ExpNode getExpChild(int i) { return (ExpNode) getChild(i); }
}
