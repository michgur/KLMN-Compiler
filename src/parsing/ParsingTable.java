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
        int[] columns = new int[terminals.size()];
        for (int col = 0; col < columns.length; col++) {
            columns[col] = 3;
            for (int row = 0, l = 0; row < nonTerminals.size(); row++, l = 0) {
                if (table[row][col] == null) continue;

                for (Symbol c : table[row][col]) l += c.toString().length();
                if (l > columns[col]) columns[col] = l;
            }
        }

        int firstCol = 1;
        for (Symbol s : nonTerminals) if (s.toString().length() > firstCol) firstCol = s.toString().length();

        StringBuilder s = new StringBuilder().append(String.join("", Collections.nCopies(firstCol, " "))).append('|');
        for (int i = 0; i < terminals.size(); i++)
            s.append(String.format("%1$" + columns[i] + "s", terminals.get(i))).append('|');

        for (int i = 0; i < table.length; i++) {
            s.append('\n').append(String.format("%1$" + firstCol + "s", nonTerminals.get(i))).append('|');
            for (int j = 0; j < table[i].length; j++) {
                if (table[i][j] == null) s.append(String.join("", Collections.nCopies(columns[j], " ")));
                else {
                    StringBuilder p = new StringBuilder();
                    for (Symbol symbol : table[i][j]) p.append(symbol);
                    s.append(String.format("%1$" + columns[j] + "s", p.toString()));
                }
                s.append("|");
            }
        }
        return s.toString();
    }
}
