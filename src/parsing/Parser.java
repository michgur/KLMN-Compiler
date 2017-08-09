package parsing;

import automata.DFA;
import automata.NFA;
import javafx.util.Pair;
import lex.TokenStream;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/8/2017.
 */
public class Parser // SLR(1) Parser
{
    private Grammar grammar;
    private DFA<Item, Symbol> dfa;

    public Parser(Grammar grammar) {
        this.grammar = grammar;

        // items to NFA state indices
        Map<Item, Integer> items = new HashMap<>();
        NFA<Item, Symbol> nfa = new NFA<>();
        Symbol[] start = null;
        for (Symbol[] s : grammar.getStartSymbol().getProductions()) start = s;
        generateState(nfa, items, new Item(grammar.getStartSymbol(), start, 0));

        dfa = nfa.toDFA();
    }

    public ParseTree parse(TokenStream input) {
        Stack<ParseTree> stack = new Stack<>();
        stack.push(new ParseTree(grammar.getSymbol(input.next())));

        while (stack.get(0).getSymbol() != grammar.getStartSymbol()) {
            dfa.test(new Iterator<Symbol>() {
                private Iterator<ParseTree> iterator = stack.iterator();
                @Override public boolean hasNext() { return iterator.hasNext(); }
                @Override public Symbol next() { return iterator.next().getSymbol(); }
            });
            Set<Item> state = dfa.getState(dfa.getTerminatedState());
            Symbol next = grammar.getSymbol(input.peek());

            for (Item item : state) {
                if (item.canReduce() && grammar.followSet(item.symbol).contains(next)) {
                    ParseTree[] children = new ParseTree[item.index];
                    for (int i = 0; i < item.index; i++) children[item.index - 1 - i] = stack.pop();
                    stack.push(ParseTree.reduce(children, item.symbol));
                    break;
                } // reduce
                if (!item.canReduce() && next == item.production[item.index]) {
                    stack.push(new ParseTree(grammar.getSymbol(input.next())));
                    break;
                } // shift
            }
        } return stack.get(0);
    }

    private int generateState(NFA<Item, Symbol> nfa, Map<Item, Integer> items, Item item) {
        if (items.get(item) != null) return items.get(item);
        items.put(item, nfa.addState(item));
        int index = items.get(item);
        nfa.acceptOn(index);
        if (item.canReduce()) return index;

        Symbol symbol = item.production[item.index];
        if (item.index < item.production.length)
            nfa.addTransition(index, symbol, generateState(nfa, items, item.next()));
        if (!symbol.isTerminal())
            for (Symbol[] rule : symbol.getProductions())
                nfa.addTransition(index, generateState(nfa, items, new Item(symbol, rule, 0)));

        return index;
    }

    private static class Item
    {
        private Symbol symbol;
        private Symbol[] production;
        private int index;

        public Item(Symbol symbol, Symbol[] production, Integer index) {
            this.symbol = symbol;
            this.production = production;
            this.index = index;
        }
        public Item next() { return new Item(symbol, production, index + 1); } // does not check for index out of bound

        public boolean canReduce() { return index == production.length; }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append(symbol).append("-> ");
            for (int i = 0; i < production.length; i++) {
                if (i == index) s.append('.');
                s.append(production[i]);
            } if (canReduce()) s.append('.');
            return s.toString();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Item &&
                    ((Item) o).symbol == symbol &&
                    ((Item) o).index == index &&
                    Arrays.equals(production, ((Item) o).production);
        }
        @Override
        public int hashCode() {
            int result = symbol != null ? symbol.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(production);
            result = 31 * result + index;
            return result;
        }
    }
}
