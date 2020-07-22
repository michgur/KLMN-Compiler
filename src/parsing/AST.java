package parsing;

import lexing.Symbol;
import lexing.Token;

import java.util.function.Consumer;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/14/2017.
 */
public abstract class AST
{
    private Symbol value;
    private Symbol[] production;
    private String text;
    private AST[] children;

    AST(Symbol value, Symbol[] production, AST... children) {
        this.value = value;
        this.children = children;
        this.production = production;
    }
    AST(Token token) {
        this.value = token.getType();
        this.text = token.getValue();
        this.children = new AST[0];
        this.production = new Symbol[0];
    }

    public Symbol getValue() { return value; }
    public Symbol[] getProduction() { return production; }
    public AST[] getChildren() { return children; }

    public boolean isTerminal() { return text != null; }
    public String getText() { return text; }

    /* Traverse the leftmost branch, accept when value is symbol */
    public void leftmostTraverse(Symbol symbol, Consumer<AST> consumer) {
        if (children[0].getValue() == symbol) children[0].leftmostTraverse(symbol, consumer);
        consumer.accept(this);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("| AST\n|\n");
        toString(s, "", true);
        return s.toString().substring(0, s.lastIndexOf("\n", s.lastIndexOf("\n") - 1));
    }
    private void toString(StringBuilder s, String prefix, boolean last) {
        s.append(prefix).append((last) ? "\\-[" : "|-[").append(isTerminal() ? text : (value != null ? value.toString() : "")).append("]\n");
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
