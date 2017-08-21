package ast;

import lang.Token;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/14/2017.
 */
public class AST
{
    private Token value;
    private AST[] children;

    public AST(Token value, AST... children) {
        this.value = value;
        this.children = children;
    }

    public Token getValue() { return value; }
    public AST[] getChildren() { return children; }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("| AST\n|\n");
        toString(s, "", true);
        return s.toString().substring(0, s.lastIndexOf("\n", s.lastIndexOf("\n") - 1));
    }
    private void toString(StringBuilder s, String prefix, boolean last) {
        s.append(prefix).append((last) ? "\\-[" : "|-[").append(value.getValue()).append("]\n");
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
