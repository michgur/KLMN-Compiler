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
        StringBuilder s = new StringBuilder().append("| AST\n");
        toString(s, "", true);
        return s.toString();
    }
    private void toString(StringBuilder s, String prefix, boolean last) {
        s.append(prefix).append((last) ? "\\-[" : "|-[").append(value.getValue()).append("]\n");
        prefix += (last) ? "  " : "| ";
        if (children.length == 0) return;
        for (int i = 0; i < children.length - 1; i++) children[i].toString(s, prefix, false);
        children[children.length - 1].toString(s, prefix, true);
    }
}
