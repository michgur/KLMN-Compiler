package parsing;

import ast.AST;
import lang.Production;
import lang.Symbol;
import lang.Terminal;
import lang.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/6/2017.
 *
 * I'm going to skip this, and generate ASTs
 */
public class ParseTree
{
    private ParseTree parent;
    private List<ParseTree> children = new ArrayList<>();
    private Symbol symbol;
    private Token token;

    public ParseTree(Terminal symbol, Token token) { this(symbol, token, null); }
    public ParseTree(Terminal symbol, Token token, ParseTree parent) {
        this(symbol, parent);
        this.token = token;
    }
    public ParseTree(Symbol symbol) { this(symbol, null); }
    public ParseTree(Symbol symbol, ParseTree parent) {
        this.symbol = symbol;
        this.parent = parent;
    }

    public static ParseTree reduce(ParseTree[] rule, Symbol reduce) {
        ParseTree res = new ParseTree(reduce);
        for (ParseTree t : rule) {
            t.parent = res;
            res.children.add(t);
        }
        return res;
    }

    public boolean removeRedundant() {
        for (ParseTree child : new ArrayList<>(children)) child.removeRedundant();
        if (children.size() > 0 || parent == null || symbol.isTerminal()) return false;
        parent.remove(this);
        return true;
    }

    public void remove(ParseTree child) {
        child.parent = null;
        children.remove(child);
    }

    public ParseTree get(Symbol symbol) {
        if (symbol == this.symbol) return this;
        for (ParseTree p : children) if (p.symbol == symbol) return p;
        return null;
    }

    public ParseTree getParent() { return parent; }
    public List<ParseTree> getChildren() { return children; }
    public ParseTree getChild(int i) { return children.get(i); }

    public Symbol getSymbol() { return symbol; }
    public Token getValue() { return token; }

    public Production getProduction() {
        if (children.size() == 0) return null;
        Symbol[] s = new Symbol[children.size()];
        for (int i = 0; i < s.length; i++) s[i] = children.get(i).getSymbol();
        for (Production p : symbol.getProductions())
            if (Arrays.equals(p.getValue(), s)) return p;
        return null;
    }
    public AST generateAST() {
        Production p = getProduction();
        if (p == null) return new AST(getValue());
        return getProduction().generateAST(this);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("| ParseTree\n");
        toString(s, "", true);
        return s.toString();
    }
    private void toString(StringBuilder s, String prefix, boolean last) {
        String t = symbol.toString();
        if (token != null && !token.getValue().equals(t)) t += " \"" + token.getValue() + '"';
        if (children.size() == 0) {
            s.append(prefix).append((last) ? "\\-" : "|-").append('[').append(t).append("]\n");
            return;
        }
        s.append(prefix).append((last) ? "\\-" : "|-").append(t).append('\n');
        prefix += (last) ? "  " : "| ";
        for (int i = 0; i < children.size() - 1; i++) children.get(i).toString(s, prefix, false);
        children.get(children.size() - 1).toString(s, prefix, true);
    }
}
