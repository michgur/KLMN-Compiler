package ast.nodes;

import ast.AST;
import lang.Token;
import test.MethodWriter;

public abstract class BoolExpNode extends ExpNode {
    public BoolExpNode(Token value, AST... children) { super(value, children); }

    @Override
    public void write(MethodWriter writer) {
        if (writer.isInCond()) writeCond(writer);
        else writeExp(writer);
    }

    protected abstract void writeCond(MethodWriter writer);
    protected abstract void writeExp(MethodWriter writer);
}
