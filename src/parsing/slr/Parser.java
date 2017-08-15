package parsing.slr;

import automata.DFA;
import automata.NFA;
import javafx.util.Pair;
import lang.*;
import parsing.ParseTree;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/8/2017.
 */
public class Parser // SLR(1) Parser
{
    private Grammar grammar;
    private DFA<Item, Symbol> dfa;
    private Map<Pair<Integer, Terminal>, Action> action;

    public Parser(Grammar grammar) {
        this.grammar = grammar;

        // items to NFA state indices
        Map<Item, Integer> items = new HashMap<>();
        NFA<Item, Symbol> nfa = new NFA<>();
        Production start = null;
        for (Production s : grammar.getStartSymbol().getProductions()) start = s;
        generateState(nfa, items, new Item(start, 0));

        dfa = nfa.toDFA();
        action = Action.generateActionMap(grammar, dfa);
    }

    public DFA<Item, Symbol> getDFA() { return dfa; }

    public ParseTree parse(TokenStream input) {
        Stack<Pair<ParseTree, Integer>> stack = new Stack<>();
        stack.push(new Pair<>(null, 0));

        boolean parse = true;
        while (parse) {
            Action a = action.get(new Pair<>(stack.peek().getValue(), grammar.getTerminal(input.peek())));
            switch (a.type) {
                case ACCEPT:
                    parse = false;
                    break;
                case SHIFT:
                    stack.push(new Pair<>(new ParseTree(grammar.getTerminal(input.peek()), input.next()), a.state));
                    break;
                case REDUCE:
                    ParseTree[] children = new ParseTree[a.item.index];
                    for (int i = 0; i < children.length; i++) children[children.length - 1 - i] = stack.pop().getKey();
                    stack.push(new Pair<>(ParseTree.reduce(children, a.item.production.getKey()),
                            dfa.getTransition(stack.peek().getValue(), a.item.production.getKey())));
                    break;
                case ERROR:
                default:
                    throw new RuntimeException("Parsing Error! Unexpected " + input.peek());
            }
        }
        stack.get(1).getKey().removeRedundant();
        return stack.get(1).getKey();
    }

    private int generateState(NFA<Item, Symbol> nfa, Map<Item, Integer> items, Item item) {
        if (items.get(item) != null) return items.get(item);
        items.put(item, nfa.addState(item));
        int index = items.get(item);
        nfa.acceptOn(index);
        if (item.canReduce()) return index;

        Symbol symbol = item.production.getValue()[item.index];
        if (item.index < item.production.getValue().length)
            nfa.addTransition(index, symbol, generateState(nfa, items, item.next()));
        if (!symbol.isTerminal())
            for (Production rule : symbol.getProductions())
                nfa.addTransition(index, generateState(nfa, items, new Item(rule, 0)));

        return index;
    }
}
