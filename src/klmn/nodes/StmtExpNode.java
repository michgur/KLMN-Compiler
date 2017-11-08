package klmn.nodes;

import ast.AST;
import klmn.writing.MethodWriter;
import lang.Token;

import java.util.List;

public abstract class StmtExpNode extends ExpNode {
    public StmtExpNode(Token value, List<AST> children) { super(value, children); }
    public StmtExpNode(Token value, AST... children) { super(value, children); }

    @Override
    public void write(MethodWriter writer) { }

    public abstract void writeExp(MethodWriter writer);
    public abstract void writeStmt(MethodWriter writer);
}
