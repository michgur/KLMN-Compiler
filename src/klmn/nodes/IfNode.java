package klmn.nodes;

import ast.AST;
import jvm.methods.Frame;
import klmn.writing.MethodWriter;
import lang.Token;

public class IfNode extends StmtNode
{
    public IfNode(Token value, AST cond, AST body) { super(value, cond, body); }

    @Override
    public void write(MethodWriter writer) {
        Frame end = new Frame();
        writer.setCondEnd(end);
        writer.setInCond(true);
        ((ExpNode) getChild(0)).write(writer);
        writer.setInCond(false);
        writer.enterScope();
        ((StmtNode) getChild(1)).write(writer);
        writer.exitScope();
        writer.assignFrame(end);
    }
}
