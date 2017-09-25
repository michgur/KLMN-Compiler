package automata;

import java.util.*;

/**
 * ಠ^ಠ.
 *
 * A Nondeterministic Finite State Machine
 * S - State Value Type
 * I - Input Type
 *
 * Created by Michael on 8/5/2017.
 */
public class NFA<S, I>
{
    // todo: better interface (consider using fake state pattern)

    List<Map<I, Set<Integer>>> transitions = new ArrayList<>();
    List<Set<Integer>> epsilonTransitions = new ArrayList<>();
    Set<Integer> accept = new HashSet<>();

    List<S> values = new ArrayList<>();

    public NFA() {}

    public int addState(S value) {
        transitions.add(new HashMap<>());
        epsilonTransitions.add(new HashSet<>());
        values.add(value);
        return transitions.size() - 1;
    }
    public void addTransition(int from, int to) { epsilonTransitions.get(from).add(to); }
    public void addTransition(int from, I input, int to) {
        transitions.get(from).putIfAbsent(input, new HashSet<>());
        transitions.get(from).get(input).add(to);
    }
    public void acceptOn(int state) { accept.add(state); }

    public S getState(int index) { return values.get(index); }

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
                        if (transitions.get(stateE).get(value) != null) next.addAll(transitions.get(stateE).get(value));
                        nextE.addAll(epsilonTransitions.get(stateE));
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
        for (int i : states) accept |= !Collections.disjoint(epsilonClosure(i), this.accept);
        return !input.hasNext() && accept;
    }

    public DFA<S, I> toDFA() { return new NFA2DFA<>(this).convert(); }

    public Set<Integer> epsilonClosure(int state) {
        Set<Integer> e = new HashSet<>();
        epsilonClosure(state, e);
        return e;
    }
    private void epsilonClosure(int state, Set<Integer> set) {
        set.add(state);
        for (int i : epsilonTransitions.get(state))
            if (!set.contains(i)) epsilonClosure(i, set);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("NFA {\n");
        int digits = (int) Math.log10(transitions.size()) + 1;
        for (int i = 0; i < transitions.size(); i++) {
            s.append('\t').append(String.format("%1$" + digits + "s:  ", i));
            if (accept.contains(i)) s.append("\033[0;4m");
            s.append(getState(i));
            if (accept.contains(i)) s.append("\033[0;24m");
            s.append('[');
            if (transitions.get(i).size() == 0 && epsilonTransitions.get(i).isEmpty()) {
                s.deleteCharAt(s.length() - 1);
                if (accept.contains(i)) s.append(" accept");
                s.append("\n");
                continue;
            }
            for (I key : transitions.get(i).keySet()) {
                if (transitions.get(i).get(key).size() == 0) break;
                s.append(key).append("-> {");
                for (int symbol : transitions.get(i).get(key)) {
                    if (accept.contains(symbol)) s.append("\033[0;4m");
                    s.append(symbol);
                    if (accept.contains(symbol)) s.append("\033[0;24m");
                    s.append(", ");
                }
                s.delete(s.length() - 2, s.length()).append("}, ");
            }
            if (!epsilonTransitions.get(i).isEmpty()) {
                s.append("ε-> {");
                for (int symbol : epsilonTransitions.get(i)) {
                    if (accept.contains(symbol)) s.append("\033[0;4m");
                    s.append(symbol);
                    if (accept.contains(symbol)) s.append("\033[0;24m");
                    s.append(", ");
                }
                s.delete(s.length() - 2, s.length()).append("}, ");
            }
            s.delete(s.length() - 2, s.length()).append(']');
            if (accept.contains(i)) s.append(" accept");
            s.append('\n');
        }

        return s.append('}').toString();
    }
}
