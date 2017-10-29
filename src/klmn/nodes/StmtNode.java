package klmn.nodes;

import ast.AST;
import lang.Token;
import klmn.MethodWriter;

public abstract class StmtNode extends AST {
    // todo: currently blocks (multiple statements) can also be StmtNodes. add a way to differentiate them
    public StmtNode(Token value, AST... children) { super(value, children); }
    public abstract void write(MethodWriter writer);
}
