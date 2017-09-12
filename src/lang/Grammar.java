package lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static lang.Terminal.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/4/2017.
 */
public class Grammar
{
    private Set<Symbol> symbols = new HashSet<>();
    private Symbol start;

    private Map<Symbol, Set<Symbol>> firstSets = new HashMap<>(), followSets = new HashMap<>();

    public Grammar(Symbol start) {
        this.start = new Symbol("S'");
        this.start.addProduction(start);
        add(this.start);

        symbols.add(END_OF_INPUT);
        symbols.add(this.start);
        symbols.forEach(this::generateFirstSet);
        generateFollowSets();

        symbols.remove(EPSILON);
    }

    public Set<Symbol> symbols() { return symbols; }
    public Symbol getStartSymbol() { return start; }

    public Set<Terminal> terminals() {
        Set<Terminal> terminals = new HashSet<>();
        for (Symbol s : symbols) if (s.isTerminal()) terminals.add((Terminal) s);
        return terminals;
    }

    public Set<Symbol> firstSet(Symbol symbol) { return firstSets.get(symbol); }
    public Set<Symbol> followSet(Symbol symbol) { return followSets.get(symbol); }

    public Terminal getTerminal(Token t) {
        for (Symbol s : symbols) if (s.isTerminal() && s == t.getType()) return (Terminal) s;
        return null;
    }

    private void add(Symbol symbol) {
        symbol.modifiable = false;
        for (Symbol[] rule : symbol.getProductions())
            for (Symbol child : rule) if (symbols.add(child)) add(child);
    }

    private Set<Symbol> generateFirstSet(Symbol symbol) {
        if (firstSets.putIfAbsent(symbol, new HashSet<>()) != null) return firstSets.get(symbol);

        if (symbol.isTerminal() || symbol == EPSILON) {
            firstSets.get(symbol).add(symbol);
            return firstSets.get(symbol);
        }
        for (Symbol[] rule : symbol.getProductions()) {
            int i = 0;
            for (; i < rule.length; i++)
                if (!generateFirstSet(rule[i]).contains(EPSILON)) break;
            if (i == rule.length) firstSets.get(symbol).add(EPSILON);
            else firstSets.get(symbol).addAll(firstSet(rule[i]));
        }
        return firstSets.get(symbol);
    }
    private void generateFollowSets() {
        symbols.forEach(s -> followSets.put(s, new HashSet<>()));
        followSets.get(start).add(END_OF_INPUT);

        boolean change = true;
        while (change) {
            change = false;
            for (Symbol symbol : symbols)
                for (Symbol[] rule : symbol.getProductions()) {
                    boolean end = true; // whether the current symbol in iteration can appear at the end of the production
                    Set<Symbol> first = new HashSet<>(); // first set of all symbols that come after each symbol in iteration
                    for (int i = rule.length - 1; i >= 0; i--) {
                        if (rule[i] == EPSILON) continue;
                        if (end && followSets.get(rule[i]).addAll(followSets.get(symbol))) change = true;

                        if (followSets.get(rule[i]).addAll(first)) change = true;
                        if (!firstSet(rule[i]).contains(EPSILON)) {
                            end = false;
                            first.clear();
                        }
                        first.addAll(firstSet(rule[i]));
                    }
                }
        }
        followSets.values().forEach(s -> s.remove(EPSILON));
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Symbol symbol : symbols) {
            if (symbol.isTerminal() || symbol == start) continue;

            s.append(symbol).append(" -> ");
            for (Symbol[] rule : symbol.getProductions()) {
                for (Symbol child : rule) s.append(child);
                s.append(" | ");
            }
            s.delete(s.length() - 3, s.length()).append('\n');
        }
        return s.deleteCharAt(s.length() - 1).toString();
    }
}
