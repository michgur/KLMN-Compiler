package automata;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/5/2017.
 */
public class NFA<I>
{
    List<Map<I, Set<Integer>>> transitions = new ArrayList<>();
    List<Set<Integer>> epsilonTransitions = new ArrayList<>();
    Set<Integer> accept = new HashSet<>();

    // possible todo: add values to states (Something Like NFA<S, I>. Each State Represents a Value Of Type S)
    public NFA(int size) {
        for (int i = 0; i < size; i++) {
            transitions.add(new HashMap<>());
            epsilonTransitions.add(new HashSet<>());
        }
    }

    // todo: simpler & more intuitive interface for creating NFAs, maybe using fake state pattern
    public int addState() {
        transitions.add(new HashMap<>());
        epsilonTransitions.add(new HashSet<>());
        return transitions.size() - 1;
    }
    public void addTransition(int from, int to) { epsilonTransitions.get(from).add(to); }
    public void addTransition(int from, I input, int to) {
        transitions.get(from).putIfAbsent(input, new HashSet<>());
        transitions.get(from).get(input).add(to);
    }
    public void acceptOn(int state) { accept.add(state); }

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

    public DFA<I> toDFA() { return new Converter<>(this).convert(); }

    Set<Integer> epsilonClosure(int state) {
        Set<Integer> res = new HashSet<>();
        res.add(state);
        for (int i : epsilonTransitions.get(state))
            if (!res.contains(i)) res.addAll(epsilonClosure(i));
        return res;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("NFA {\n");
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        boolean useABC = transitions.size() < 27;

        for (int i = 0; i < transitions.size(); i++) {
            s.append('\t');
            if (accept.contains(i)) s.append("\033[0;4m");
            s.append((useABC) ? abc.charAt(i) : "" + i);
            if (accept.contains(i)) s.append("\033[0;24m");
            s.append('[');
            if (transitions.get(i).size() == 0 && epsilonTransitions.get(i).isEmpty()) break;
            for (I key : transitions.get(i).keySet()) {
                if (transitions.get(i).get(key).size() == 0) break;
                s.append(key).append("-> {");
                for (int symbol : transitions.get(i).get(key)) {
                    if (accept.contains(symbol)) s.append("\033[0;4m");
                    s.append((useABC) ? abc.charAt(symbol) : "" + symbol);
                    if (accept.contains(symbol)) s.append("\033[0;24m");
                    s.append(", ");
                }
                s.delete(s.length() - 2, s.length()).append("}, ");
            }
            if (!epsilonTransitions.get(i).isEmpty()) {
                s.append("ε-> {");
                for (int symbol : epsilonTransitions.get(i)) {
                    if (accept.contains(symbol)) s.append("\033[0;4m");
                    s.append((useABC) ? abc.charAt(symbol) : "" + symbol);
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
