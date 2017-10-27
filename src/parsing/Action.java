package parsing;

import automata.DFA;
import lang.Grammar;
import lang.Symbol;
import lang.Terminal;
import util.Pair;

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
    static Map<Pair<Integer, Terminal>, Action> generateActionMap(Grammar grammar, DFA<Item, Symbol> dfa) {
        Map<Pair<Integer, Terminal>, Action> action = new HashMap<>();
        for (Terminal t : grammar.terminals()) {
            for (int i = 0; i < dfa.size(); i++) {
                Pair<Integer, Terminal> pair = Pair.of(i, t);
                for (Item item : dfa.getState(i)) {
                    if (!item.canReduce() && t == item.value[item.index]) {
                        action.put(pair, new Action(Type.SHIFT, dfa.getTransition(i, t)));
                        break;
                    }
                    else if (item.key == grammar.getStartSymbol() && item.canReduce())
                        action.put(pair, new Action(Type.ACCEPT));
                    else if (item.canReduce() && grammar.followSet(item.key).contains(t))
                        action.put(pair, new Action(Type.REDUCE, item));
                }
                if (action.get(pair) == null) action.put(pair, new Action(Type.ERROR));
            }
        }
        return action;
    }
}
