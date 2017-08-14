package automata;

import java.util.*;

/**
 * ಠ^ಠ.
 *
 * * A Deterministic Finite State Machine
 * S - State Value Type
 * I - Input Type
 *
 * Created by Michael on 8/7/2017.
 */
public class DFA<S, I>
{
    private List<Map<I, Integer>> transitions = new ArrayList<>();
    private Set<Integer> accept = new HashSet<>();
    private int terminated;

    private List<Set<S>> values = new ArrayList();

    public DFA() {}

    public int addState(Set<S> value) {
        transitions.add(new HashMap<>());
        values.add(value);
        return transitions.size() - 1;
    }
    public void addTransition(int from, I input, int to) { transitions.get(from).put(input, to); }
    public void acceptOn(int state) { accept.add(state); }

    public boolean test(Iterator<I> input) {
        Integer state = 0, next;
        while (input.hasNext() && (next = transitions.get(state).get(input.next())) != null) { state = next; }
        terminated = state;
        return !input.hasNext() && accept.contains(state);
    }

    public int getAcceptingState() { return terminated; }

    public Set<S> getState(int index) { return values.get(index); }

    public int getTransition(int from, I input) { return transitions.get(from).get(input); }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("DFA {\n");
        int digits = (int) Math.log10(transitions.size()) + 1;
        for (int i = 0; i < transitions.size(); i++) {
            s.append('\t').append(String.format("%1$" + digits + "s:  ", i));
            if (accept.contains(i)) s.append("\033[0;4m");
            s.append(getStateString(i));
            if (accept.contains(i)) s.append("\033[0;24m");
            if (transitions.get(i).size() == 0) {
                if (accept.contains(i)) s.append(" accept");
                s.append('\n');
                continue;
            }
            s.append('[');
            for (I key : transitions.get(i).keySet()) {
                Integer t = transitions.get(i).get(key);
                if (t == null) break;
                s.append(key).append("-> ");
                if (accept.contains(t)) s.append("\033[0;4m");
                s.append(t).append(", ");
                s.delete(s.length() - 2, s.length());
                if (accept.contains(t)) s.append("\033[0;24m");
                s.append(", ");
            }
            s.delete(s.length() - 2, s.length()).append(']');
            if (accept.contains(i)) s.append(" accept");
            s.append('\n');
        }

        return s.append('}').toString();
    }

    private String getStateString(int index) {
        String s = getState(index).toString();
        return '{' + s.substring(1, s.length() - 1) + '}';
    }

    public int size() { return transitions.size(); }
}
