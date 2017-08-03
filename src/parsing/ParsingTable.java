package parsing;

import lex.Token;
import lex.TokenStream;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/3/2017.
 */
public class ParsingTable
{
    private Grammar grammar;
    private Grammar.Symbol[][][] table;
    private List<Grammar.Symbol> terminals = new ArrayList<>(), nonTerminals = new ArrayList<>();
    public ParsingTable(Grammar g) {
        grammar = g;
        for (Grammar.Symbol s : g.getSymbols()) {
            if (s instanceof Grammar.Terminal) terminals.add(s);
            else nonTerminals.add(s);
        }
        terminals.add(Grammar.END);
        table = new Grammar.Symbol[nonTerminals.size()][terminals.size()][];

        for (Grammar.Symbol s : g.getProductions().keySet())
            for (Grammar.Symbol[] p : g.getProductions().get(s)) {
                Set<Grammar.Symbol> set = new HashSet<>();
                set.addAll(g.firstSet(p));
                if (set.contains(Grammar.EPSILON)) set.addAll(g.followSet(s));
                for (Grammar.Symbol symbol : set)
                    if (symbol instanceof Grammar.Terminal || symbol == Grammar.END) set(s, symbol, p);
            }
    }

    public Grammar.Symbol[] get(Grammar.Symbol s, Token t) {
        for (int i = 0; i < terminals.size(); i++)
            if (((Grammar.Terminal) terminals.get(i)).getValue() == t.getType()) return table[nonTerminals.indexOf(s)][i];
        throw new RuntimeException();
    }
    public Grammar.Symbol[] get(Grammar.Symbol s, Grammar.Symbol t) { return table[nonTerminals.indexOf(s)][terminals.indexOf(t)]; }
    public void set(Grammar.Symbol s, Grammar.Symbol t, Grammar.Symbol[] v) { table[nonTerminals.indexOf(s)][terminals.indexOf(t)] = v; }

    public void parse(TokenStream tokens) {
        Stack<Grammar.Symbol> stack = new Stack<>();
        stack.push(Grammar.END);
        stack.push(grammar.getStart());

        while (!stack.empty()) {
            if (stack.peek() == Grammar.EPSILON) stack.pop();
            else if (stack.peek() instanceof Grammar.Terminal) {
                if (((Grammar.Terminal) stack.peek()).getValue() == tokens.peek().getType()) {
                    stack.pop();
                    tokens.next();
                }
            }
            else {
                Grammar.Symbol[] p = get(stack.pop(), tokens.peek());
                for (int i = p.length - 1; i >= 0; i--) stack.push(p[i]);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append('\t');
        for (Grammar.Symbol symbol : terminals) s.append(symbol).append('\t');
        for (int i = 0; i < table.length; i++) {
            s.append('\n').append(nonTerminals.get(i)).append('\t');
            for (int j = 0; j < table[i].length; j++) {
                if (table[i][j] == null) s.append("----");
                else for (Grammar.Symbol symbol : table[i][j]) s.append(symbol);
                s.append('\t');
            }
        }
        return s.toString();
    }
}
