package klmn.nodes;

import ast.AST;
import lang.Token;

import java.util.List;

public abstract class StmtNode extends AST implements MethodNode.BodyNode {
    // todo: currently blocks (multiple statements) can also be StmtNodes. add a way to differentiate them
    public StmtNode(Token value, AST... children) { super(value, children); }
    public StmtNode(Token value, List<AST> children) { super(value, children); }
}
