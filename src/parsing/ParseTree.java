package parsing;

import java.util.ArrayList;
import java.util.List;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/6/2017.
 */
public class ParseTree
{
    private ParseTree parent;
    private List<ParseTree> children = new ArrayList<>();
    private Symbol symbol;

    public ParseTree(Symbol symbol) { this(symbol, null); }
    public ParseTree(Symbol symbol, ParseTree parent) {
        this.symbol = symbol;
        this.parent = parent;
    }

    public void expand(Symbol[] rule) {
        if (!symbol.getProductions().contains(rule)) return;
        for (int i = 0; i < rule.length; i++) children.add(new ParseTree(rule[i], this));
    }

    public void remove(ParseTree child) {
        child.parent = null;
        children.remove(child);

        if (children.size() == 0 && parent != null) parent.remove(this);
    }

    public ParseTree get(Symbol symbol) {
        if (symbol == this.symbol) return this;
        for (ParseTree p : children) if (p.symbol == symbol) return p;
        return null;
    }

    public ParseTree getParent() { return parent; }
    public List<ParseTree> getChildren() { return children; }

    public Symbol getSymbol() { return symbol; }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("| ParseTree\n");
        toString(s, "", true);
        return s.toString();
    }
    private void toString(StringBuilder s, String prefix, boolean last) {
        if (children.size() == 0) {
            s.append(prefix).append((last) ? "\\-" : "|-").append('[').append(symbol).append("]\n");
            return;
        }
        s.append(prefix).append((last) ? "\\-" : "|-").append(symbol).append('\n');
        prefix += (last) ? "  " : "| ";
        for (int i = 0; i < children.size() - 1; i++) children.get(i).toString(s, prefix, false);
        children.get(children.size() - 1).toString(s, prefix, true);
    }
}
