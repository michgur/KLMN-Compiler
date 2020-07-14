package klmn.nodes;

import ast.AST;
import klmn.writing.MethodWriter;
import lang.Token;

import java.util.List;

public abstract class BoolExpNode extends ExpNode
{
    public BoolExpNode(Token value, List<AST> children) { super(value, children); }
    public BoolExpNode(Token value, AST... children) { super(value, children); }

    @Override
    public void write(MethodWriter writer) {
        if (writer.isInCond()) writeCond(writer);
        else writeExp(writer);
    }

    protected abstract void writeCond(MethodWriter writer);
    protected abstract void writeExp(MethodWriter writer);
}
