package klmn.nodes;

import ast.AST;
import jvm.methods.Frame;
import klmn.writing.MethodWriter;
import lang.Token;

import static jvm.Opcodes.GOTO;

public class ForNode extends StmtNode
{
    public ForNode(Token value, AST init, AST cond, AST step, AST body) { super(value, init, cond, step, body); }

    @Override
    public void write(MethodWriter writer) {
        Frame loop = new Frame(), end = new Frame();
        writer.enterScope();
        ((StmtNode) getChild(0)).write(writer);
        writer.assignFrame(loop);
        writer.setCondEnd(end);
        writer.setInCond(true);
        ((ExpNode) getChild(1)).write(writer);
        writer.setInCond(false);
        writer.enterScope();
        ((StmtNode) getChild(3)).write(writer);
        writer.exitScope();
        ((StmtExpNode) getChild(2)).writeStmt(writer);
        writer.useJmpOperator(GOTO, loop);
        writer.exitScope();
        writer.assignFrame(end);
    }
}
