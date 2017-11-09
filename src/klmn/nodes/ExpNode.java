package klmn.nodes;

import ast.AST;
import klmn.writing.MethodWriter;
import klmn.writing.TypeEnv;
import lang.Token;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ExpNode extends AST implements MethodNode.BodyNode {
    private static Set<ExpNode> expNodes = new HashSet<>(); // todo: less shitty way

    private TypeEnv.Type type;
    public ExpNode(Token value, List<AST> children) {
        super(value, children);
        expNodes.add(this);
    }
    public ExpNode(Token value, AST... children) {
        super(value, children);
        expNodes.add(this);
    }

    public static void typeCheckAll(MethodWriter writer) { for (ExpNode n : expNodes) n.getType(writer); }

    protected abstract TypeEnv.Type typeCheck(MethodWriter writer);
    public TypeEnv.Type getType(MethodWriter writer) { return (type != null) ? type : (type = typeCheck(writer)); }

    public ExpNode getExpChild(int i) { return (ExpNode) getChild(i); }
}
