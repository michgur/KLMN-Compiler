package parsing;

import lexing.*;
import parsing.automata.DFA;
import parsing.automata.NFA;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/8/2017.
 */
public class Parser // SLR(1) Parser
{
    private Grammar grammar;
    private DFA<Item, Symbol> dfa;
    private Map<Map.Entry<Integer, Terminal>, Action> action;

    public Parser(Grammar grammar) {
        this.grammar = grammar;

        // items to NFA state indices
        Map<Item, Integer> items = new HashMap<>();
        NFA<Item, Symbol> nfa = new NFA<>();
        Symbol[] start = null;
        for (Symbol[] s : grammar.getStartSymbol().getProductions()) start = s;
        generateState(nfa, items, new Item(grammar.getStartSymbol(), start, 0));

        dfa = nfa.toDFA();
        action = Action.generateActionMap(grammar, dfa);
    }

    public AST parse(TokenStream input) {
        Stack<Map.Entry<AST, Integer>> stack = new Stack<>();
        stack.push(new AbstractMap.SimpleEntry<>(null, 0));

        while (true) {
            Action a = action.get(new AbstractMap.SimpleEntry<>(stack.peek().getValue(), grammar.getTerminal(input.peek())));
            switch (a.type) {
                case SHIFT:
                    stack.push(new AbstractMap.SimpleEntry<>(new AST(input.peek()) {
                    }, a.state));
                    input.next();
                    break;
                case REDUCE:
                    AST[] p = new AST[a.item.index];
                    for (int i = p.length - 1; i >= 0; i--) p[i] = stack.pop().getKey();
                    stack.push(new AbstractMap.SimpleEntry<>(new AST(a.item.key, a.item.value, p) {
                    }, dfa.getTransition(stack.peek().getValue(), a.item.key)));
                    break;
                case ACCEPT: return stack.get(1).getKey();
                case ERROR: throw new ParsingException(input.peek());
            }
        }
    }

    private int generateState(NFA<Item, Symbol> nfa, Map<Item, Integer> items, Item item) {
        if (items.get(item) != null) return items.get(item);
        items.put(item, nfa.addState(item));
        int index = items.get(item);
        nfa.acceptOn(index);
        if (item.canReduce()) return index;

        Symbol symbol = item.value[item.index];
        if (item.index < item.value.length)
            nfa.addTransition(index, symbol, generateState(nfa, items, item.next()));
        if (!symbol.isTerminal())
            for (Symbol[] rule : symbol.getProductions())
                nfa.addTransition(index, generateState(nfa, items, new Item(symbol, rule, 0)));

        return index;
    }
}
