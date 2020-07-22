package parsing;

import parsing.automata.DFA;
import lexing.Grammar;
import lexing.Symbol;
import lexing.Terminal;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/9/2017.
 */
class Action // SLR(1) Action
{
    public enum Type { SHIFT, REDUCE, ACCEPT, ERROR }

    Type type;
    Item item = null;
    int state = 0;

    private Action(Type type) { this.type = type; }
    private Action(Type type, Item item) { this(type); this.item = item; }
    private Action(Type type, int state) { this(type); this.state = state; }

    // in case of shift/ reduce conflicts, we choose to shift
    public static Map<Map.Entry<Integer, Terminal>, Action> generateActionMap(Grammar grammar, DFA<Item, Symbol> dfa) {
        Map<Map.Entry<Integer, Terminal>, Action> action = new HashMap<>();
        for (Terminal t : grammar.terminals()) {
            for (int i = 0; i < dfa.size(); i++) {
                Map.Entry<Integer, Terminal> pair = new AbstractMap.SimpleEntry<>(i, t);
                for (Item item : dfa.getState(i)) {
                    if (!item.canReduce() && t == item.value[item.index]) {
                        action.put(pair, new Action(Type.SHIFT, dfa.getTransition(i, t)));
                        break;
                    }
                    else if (item.key == grammar.getStartSymbol() && item.canReduce())
                        action.put(pair, new Action(Type.ACCEPT));
                    else if (item.canReduce() && grammar.followSet(item.key).contains(t)) {
                        action.put(pair, new Action(Type.REDUCE, item));
                    }
                }
                if (action.get(pair) == null) action.put(pair, new Action(Type.ERROR));
            }
        }
        return action;
    }
}
