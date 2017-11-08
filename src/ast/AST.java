package ast;

import lang.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/14/2017.
 */
public class AST
{
    private Token value;
    private List<AST> children = new ArrayList<>(); // todo: make this an arraylist

    public AST(Token value, AST... children) { this(value, Arrays.asList(children)); }
    public AST(Token value, List<AST> children) {
        this.value = value;
        this.children.addAll(children);
    }

    public Token getValue() { return value; }
    public List<AST> getChildren() { return children; }

    public AST getChild(int i) { return children.get(i); }
    public int addChild(AST child) {
        children.add(child);
        return children.size();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("| AST\n|\n");
        toString(s, "", true);
        return s.toString().substring(0, s.lastIndexOf("\n", s.lastIndexOf("\n") - 1));
    }
    private void toString(StringBuilder s, String prefix, boolean last) {
        if (value == null) s.append(prefix).append((last) ? "\\-[]\n" : "|-[]\n");
        else s.append(prefix).append((last) ? "\\-[" : "|-[").append(value.getValue()).append("]\n");
        prefix += (last) ? "  " : "| ";
        if (children.size() == 0) {
            if (last) s.append(prefix).append('\n');
            return;
        }
        s.append(prefix).append("|\n");
        for (int i = 0; i < children.size() - 1; i++) children.get(i).toString(s, prefix, false);
        children.get(children.size() - 1).toString(s, prefix, true);
    }
}
