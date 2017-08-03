package parsing;

import lex.Token;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/3/2017.
 */
public class Grammar
{
    private Set<Symbol> symbols = new HashSet<>();
    private HashMap<Symbol, Set<Symbol[]>> productions = new HashMap<>();

    private Symbol start = null;

    public Symbol addSymbol(String name) {
        Symbol s = new Symbol(name);
        symbols.add(s);
        return s;
    }
    public Terminal addTerminal(String name, Token.Type type) {
        Terminal t = new Terminal(name, type);
        symbols.add(t);
        return t;
    }
    public void setStart(Symbol s) { start = s; }
    public Symbol getStart() { return start; }

    public Set<Symbol> getSymbols() { return symbols; }
    public HashMap<Symbol, Set<Symbol[]>> getProductions() { return productions; }

    // ---Do NOT call these functions before fully defining the grammar--- (fixme)
    private HashMap<Symbol, Set<Symbol>> firstSets = new HashMap<>();
    public Set<Symbol> firstSet(Symbol s) {
        if (firstSets.putIfAbsent(s, new HashSet<>()) != null) return firstSets.get(s);

        Set<Symbol> first = firstSets.get(s);
        if (s instanceof Terminal || s == EPSILON) { first.add(s); return first; }
        for (Symbol[] p : productions.get(s)) {
            if (p[0] == EPSILON) first.add(EPSILON);
            else {
                boolean eps = true;
                for (Symbol c : p)
                    if (!firstSet(c).contains(EPSILON)) {
                        eps = false;
                        break;
                    }
                if (eps) first.add(EPSILON);
            }
            Set<Symbol> set = null;
            for (int i = 0; i < p.length; i++) {
                if (i == 0 || set.contains(EPSILON)) first.addAll(set = firstSet(p[i]));
                else break;
            }
        }
        return first;
    }
    public Set<Symbol> firstSet(Symbol[] s) {
        Set<Symbol> first = new HashSet<>();
        for (int i = 0; i < s.length; i++) {
            Set<Symbol> t = firstSet(s[i]);
            first.addAll(t);
            if (!t.contains(EPSILON)) break;
        }
        return first;
    }

    private HashMap<Symbol, Set<Symbol>> followSets = new HashMap<>();
    public Set<Symbol> followSet(Symbol s) {
        if (followSets.putIfAbsent(s, new HashSet<>()) != null) return followSets.get(s);

        Set<Symbol> follow = followSets.get(s);
        if (s == start) follow.add(END);
        for (Symbol symbol : productions.keySet())
            for (Symbol[] p : productions.get(symbol)) {
                int i = Arrays.asList(p).indexOf(s);
                if (i == -1) continue;

                while (++i < p.length) {
                    Set<Symbol> first = firstSet(p[i]);
                    follow.addAll(first);
                    if (!first.contains(EPSILON)) break;
                }
                if (i == p.length) follow.addAll(followSet(symbol));
            }

        follow.remove(EPSILON);
        return follow;
    }
    // -------------------------------------------------------------------

    public void addProduction(Symbol s, Symbol... production) {
        productions.putIfAbsent(s, new HashSet<>());
        productions.get(s).add(production);
    }

    public static final Symbol EPSILON = new Symbol("ε"), END = new Terminal("$", Token.Type.END);

    public static class Symbol {
        private String name;
        Symbol(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
    public static class Terminal extends Symbol {
        private Token.Type value;
        private Terminal(String name, Token.Type value) {
            super(name);
            this.value = value;
        }

        public Token.Type getValue() { return value; }
    }
}
