package automata;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/5/2017.
 */
public class NFA<I>
{
    // [state, input] -> state set
    List<Map<I, Set<Integer>>> nfa = new ArrayList<>();

    public NFA(int size) { for (int i = 0; i < size; i++) nfa.add(new HashMap<>()); }

    // todo: simpler & more intuitive interface for creating NFAs, maybe using fake state pattern
    public void addTransition(int from, int to) { addTransition(from, null, to); }
    public void addTransition(int from, I input, int to) {
        nfa.get(from).putIfAbsent(input, new HashSet<>());
        nfa.get(from).get(input).add(to);
    }

    public boolean test(Iterator<I> input) {
        Set<Integer> states = new HashSet<>();
        states.add(0);
        while (!states.isEmpty() && input.hasNext()) {
            I value = input.next();
            Set<Integer> next = new HashSet<>();
            for (int state : states) {
                Set<Integer> epsilon = new HashSet<>();

                epsilon.add(state);
                while (!epsilon.isEmpty()) {
                    Set<Integer> nextE = new HashSet<>();
                    for (int stateE : epsilon) {
                        if (nfa.get(stateE).get(value) != null) next.addAll(nfa.get(stateE).get(value));
                        if (nfa.get(stateE).get(null) != null) nextE.addAll(nfa.get(stateE).get(null));
                    }
                    epsilon = nextE;
                }
            }
            states = next;
        }
        // some paths of the test might have consumed all of the input
        // but didn't reach the final state, so states.size() might be bigger than 1.
        // we only care about the final state
        boolean accept = false;
        for (int i : states) accept |= epsilonClosure(i).contains(nfa.size() - 1);
        return !input.hasNext() && accept;
    }

    public DFA<I> toDFA() { return new Converter<>(this).convert(); }

    Set<Integer> epsilonClosure(int state) {
        Set<Integer> res = new HashSet<>();
        res.add(state);
        if (nfa.get(state).get(null) != null)
            for (int i : nfa.get(state).get(null)) if (!res.contains(i)) res.addAll(epsilonClosure(i));

        return res;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("NFA {\n");
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        boolean useABC = nfa.size() < 27;

        for (int i = 0; i < nfa.size(); i++) {
            if (nfa.get(i).size() == 0) break;
            s.append('\t').append((useABC) ? abc.charAt(i) : "" + i).append('[');
            for (I key : nfa.get(i).keySet()) {
                if (nfa.get(i).get(key).size() == 0) break;
                s.append((key != null) ? key : "ε").append("-> {");
                for (int symbol : nfa.get(i).get(key)) s.append((useABC) ? abc.charAt(symbol) : "" + symbol).append(", ");
                s.delete(s.length() - 2, s.length()).append("}, ");
            }
            s.delete(s.length() - 2, s.length()).append("]\n");
        }

        return s.append('}').toString();
    }
}
