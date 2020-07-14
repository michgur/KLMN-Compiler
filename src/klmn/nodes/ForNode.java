package klmn.nodes;

import ast.AST;
import jvm.methods.Label;
import klmn.writing.MethodWriter;
import klmn.writing.SymbolTable;
import lang.Token;

import static jvm.Opcodes.GOTO;

public class ForNode extends StmtNode
{
    public ForNode(Token value, AST init, AST cond, AST step, AST body) { super(value, init, cond, step, body); }

    @Override
    public void write(MethodWriter writer) {
        Label loop = new Label(), end = new Label();
        writer.enterScope(SymbolTable.ScopeType.BLOCK);
        ((StmtNode) getChild(0)).write(writer);
        writer.assign(loop);
        writer.setCondEnd(end);
        writer.setInCond(true);
        ((ExpNode) getChild(1)).write(writer);
        writer.setInCond(false);
        writer.enterScope();
        ((StmtNode) getChild(3)).write(writer);
        if (!writer.hasReturned())  {
            writer.exitScope();
            ((StmtExpNode) getChild(2)).writeStmt(writer);
            writer.useJmpOperator(GOTO, loop);
        } else writer.exitScope();
        writer.exitScope();
        writer.assign(end);
    }
}
