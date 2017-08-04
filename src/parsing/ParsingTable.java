package parsing;

import lex.Token;
import lex.TokenStream;

import java.util.*;

import static parsing.Terminal.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/3/2017.
 */
public class ParsingTable // LL(1) Parsing Table
{
    private Grammar grammar;
    private Symbol[][][] table;
    private List<Symbol> terminals = new ArrayList<>(), nonTerminals = new ArrayList<>();
    public ParsingTable(Grammar g) {
        grammar = g;
        for (Symbol s : g.getSymbols()) {
            if (s.isTerminal()) terminals.add(s);
            else nonTerminals.add(s);
        }
        terminals.add(END_OF_INPUT);
        table = new Symbol[nonTerminals.size()][terminals.size()][];

        for (Symbol s : g.getSymbols())
            for (Symbol[] p : s.getProductions()) {
                Set<Symbol> set = new HashSet<>();
                set.addAll(g.firstSet(p));
                if (set.contains(EPSILON)) set.addAll(g.followSet(s));
                for (Symbol symbol : set)
                    if (symbol.isTerminal() || symbol == END_OF_INPUT) set(s, symbol, p);
            }
    }

    public Symbol[] get(Symbol s, Token t) {
        for (int i = 0; i < terminals.size(); i++)
            if (((Terminal) terminals.get(i)).getType() == t.getType()) return table[nonTerminals.indexOf(s)][i];
        throw new RuntimeException();
    }
    public Symbol[] get(Symbol s, Symbol t) { return table[nonTerminals.indexOf(s)][terminals.indexOf(t)]; }
    public void set(Symbol s, Symbol t, Symbol[] v) { table[nonTerminals.indexOf(s)][terminals.indexOf(t)] = v; }

    public void parse(TokenStream tokens) {
        Stack<Symbol> stack = new Stack<>();
        stack.push(END_OF_INPUT);
        stack.push(grammar.getStartSymbol());

        while (!stack.empty()) {
            if (stack.peek() == EPSILON) stack.pop();
            else if (stack.peek().isTerminal()) {
                if (((Terminal) stack.peek()).getType() == tokens.peek().getType()) {
                    stack.pop();
                    tokens.next();
                }
            }
            else {
                Symbol[] p = get(stack.pop(), tokens.peek());
                for (int i = p.length - 1; i >= 0; i--) stack.push(p[i]);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append('\t');
        for (Symbol symbol : terminals) s.append(symbol).append('\t');
        for (int i = 0; i < table.length; i++) {
            s.append('\n').append(nonTerminals.get(i)).append('\t');
            for (int j = 0; j < table[i].length; j++) {
                if (table[i][j] == null) s.append("----");
                else for (Symbol symbol : table[i][j]) s.append(symbol);
                s.append('\t');
            }
        }
        return s.toString();
    }
}
