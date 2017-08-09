package parsing;

import javafx.util.Pair;
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
    private Map<Pair<Symbol, Terminal>, Symbol[]> map = new HashMap<>();

    public ParsingTable(Grammar grammar) {
        this.grammar = grammar;
        for (Symbol s : grammar.symbols())
            for (Symbol[] p : s.getProductions()) {
                Set<Symbol> set = new HashSet<>();
                set.addAll(grammar.firstSet(p));
                if (set.contains(EPSILON)) set.addAll(grammar.followSet(s));
                for (Symbol symbol : set)
                    if (symbol.isTerminal()) map.put(new Pair<>(s, (Terminal) symbol), p);
            }
    }

    public ParseTree parse(TokenStream tokens) {
        ParseTree tree = new ParseTree(grammar.getStartSymbol()), end = new ParseTree(END_OF_INPUT);

        Stack<ParseTree> stack = new Stack<>();
        stack.push(end);
        stack.push(tree);

        while (!stack.empty()) {
            if (stack.peek().getSymbol() == EPSILON) {
                ParseTree p = stack.pop();
                p.getParent().remove(p);
            }
            else if (stack.peek().getSymbol().isTerminal()) {
                if (stack.peek().getSymbol().matches(tokens.peek())) {
                    stack.pop();
                    tokens.next();
                }  // else { parse error }
            }
            else {
                Symbol[] production = null;
                for (Pair<Symbol, Terminal> p : map.keySet())
                    if (p.getKey() == stack.peek().getSymbol() && p.getValue().matches(tokens.peek())) production = map.get(p);
                ParseTree p = stack.pop();
                p.expand(production);
                if (production == null) throw new RuntimeException(); // parse error
                for (int i = production.length - 1; i >= 0; i--) stack.push(p.getChildren().get(i));
            }
        }
        return tree;
    }

    // do not expand, for this method is dark & full of terrors (prints a nice table tho)
    @Override
    public String toString() {
        Set<Symbol> nonTerminals = new HashSet<>();
        Set<Terminal> terminals = new HashSet<>();
        for (Pair<Symbol, Terminal> p : map.keySet()) {
            nonTerminals.add(p.getKey());
            terminals.add(p.getValue());
        }
        Symbol[] nt = new Symbol[nonTerminals.size()];
        Terminal[] t = new Terminal[terminals.size()];
        terminals.toArray(t);
        nonTerminals.toArray(nt);

        int[] columns = new int[terminals.size()];
        for (int col = 0; col < columns.length; col++) {
            columns[col] = 3;
            for (int row = 0, l = 0; row < nonTerminals.size(); row++, l = 0) {
                if (map.get(new Pair<>(nt[row], t[col])) == null) continue;

                for (Symbol c : map.get(new Pair<>(nt[row], t[col]))) l += c.toString().length();
                if (l > columns[col]) columns[col] = l;
            }
        }

        int firstCol = 1;
        for (Symbol s : nonTerminals) if (s.toString().length() > firstCol) firstCol = s.toString().length();

        StringBuilder s = new StringBuilder().append(String.join("", Collections.nCopies(firstCol, " "))).append('|');
        for (int i = 0; i < terminals.size(); i++)
            s.append(String.format("%1$" + columns[i] + "s", t[i])).append('|');

        for (int i = 0; i < nt.length; i++) {
            s.append('\n').append(String.format("%1$" + firstCol + "s", nt[i])).append('|');
            for (int j = 0; j < t.length; j++) {
                if (map.get(new Pair<>(nt[i], t[j])) == null) s.append(String.join("", Collections.nCopies(columns[j], " ")));
                else {
                    StringBuilder p = new StringBuilder();
                    for (Symbol symbol : map.get(new Pair<>(nt[i], t[j]))) p.append(symbol);
                    s.append(String.format("%1$" + columns[j] + "s", p.toString()));
                }
                s.append("|");
            }
        }
        return s.toString();
    }
}
