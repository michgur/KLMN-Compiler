package automata;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/7/2017.
 */
public class DFA<I>
{
    List<Map<I, Integer>> dfa = new ArrayList<>();
    Set<Integer> accept = new HashSet<>();

    DFA() {}
    public DFA(int size) { for (int i = 0; i < size; i++) dfa.add(new HashMap<>()); }

    int addState() {
        dfa.add(new HashMap<>());
        return dfa.size() - 1;
    }

    public void addTransition(int from, @NotNull I input, int to) { dfa.get(from).put(input, to); }
    public void acceptOn(int state) { accept.add(state); }

    public boolean test(Iterator<I> input) {
        Integer state = 0, next;
        while (input.hasNext() && (next = dfa.get(state).get(input.next())) != null) { state = next; }
        return !input.hasNext() && accept.contains(state);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append("DFA {\n");
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        boolean useABC = dfa.size() < 27;

        for (int i = 0; i < dfa.size(); i++) {
            s.append('\t');
            if (accept.contains(i)) s.append("\033[0;4m");
            s.append((useABC) ? abc.charAt(i) : "" + i);
            if (accept.contains(i)) s.append("\033[0;24m");
            if (dfa.get(i).size() == 0) { s.append("\n"); continue; }
            s.append('[');
            for (I key : dfa.get(i).keySet()) {
                Integer t = dfa.get(i).get(key);
                if (t == null) break;
                s.append(key).append("-> ");
                if (accept.contains(t)) s.append("\033[0;4m");
                s.append((useABC) ? abc.charAt(t) : "" + t).append(", ");
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
}
