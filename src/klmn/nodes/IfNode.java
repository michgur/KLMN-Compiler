package klmn.nodes;

import ast.AST;
import jvm.methods.Label;
import klmn.writing.MethodWriter;
import lang.Token;

public class IfNode extends StmtNode
{
    public IfNode(Token value, AST cond, AST body) { super(value, cond, body); }

    @Override
    public void write(MethodWriter writer) {
        Label end = new Label();
        writer.setCondEnd(end);
        writer.setInCond(true);
        ((ExpNode) getChild(0)).write(writer);
        writer.setInCond(false);
        writer.enterScope();
        ((StmtNode) getChild(1)).write(writer);
        writer.exitScope();
        writer.assign(end);
    }
}
