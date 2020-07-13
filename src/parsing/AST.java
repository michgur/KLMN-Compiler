package parsing;

import lexing.Symbol;
import lexing.Token;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/14/2017.
 */
public abstract class AST
{
    private Symbol value;
    private String text;
    private AST[] children;

    public AST(Symbol value, AST... children) {
        this.value = value;
        this.children = children;
    }
    public AST(Token token) {
        this.value = token.getType();
        this.text = token.getValue();
        this.children = new AST[0];
    }

    public Symbol getValue() { return value; }
    public AST[] getChildren() { return children; }

    public boolean isLeaf() { return children.length == 0; }
    public String getText() { return text; }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("| AST\n|\n");
        toString(s, "", true);
        return s.toString().substring(0, s.lastIndexOf("\n", s.lastIndexOf("\n") - 1));
    }
    private void toString(StringBuilder s, String prefix, boolean last) {
        s.append(prefix).append((last) ? "\\-[" : "|-[").append(isLeaf() ? text : (value != null ? value.toString() : "")).append("]\n");
        prefix += (last) ? "  " : "| ";
        if (children.length == 0) {
            if (last) s.append(prefix).append('\n');
            return;
        }
        s.append(prefix).append("|\n");
        for (int i = 0; i < children.length - 1; i++) children[i].toString(s, prefix, false);
        children[children.length - 1].toString(s, prefix, true);
    }
}
