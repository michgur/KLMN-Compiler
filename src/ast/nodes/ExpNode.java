package ast.nodes;

import ast.AST;
import lang.Token;
import test.MethodWriter;

public abstract class ExpNode extends AST {
    public ExpNode(Token value, AST... children) { super(value, children); }
    public abstract void write(MethodWriter writer);

    public ExpNode getExpChild(int i) { return (ExpNode) getChild(i); }
    // todo: add type checking here
}
