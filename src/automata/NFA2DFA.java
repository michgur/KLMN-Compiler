package automata;

import java.util.*;

/**
 * ಠ^ಠ.
 *
 * A Helper Class To Convert NFAs to DFAs
 * S - State Value Type
 * I - Input Type
 *
 * Created by Michael on 8/8/2017.
 */
class NFA2DFA<S, I>
{
    private NFA<S, I> src;
    private DFA<S, I> res = new DFA<>();
    private Set<Integer> end;

    // NFA index to DFA index
    private Map<Set<Integer>, Integer> indices = new HashMap<>();
    // store epsilon closures of src
    private Map<Integer, Set<Integer>> epsClosure = new HashMap<>();

    NFA2DFA(NFA<S, I> src) {
        this.src = src;
        end = src.accept;
    }

    DFA<S, I> convert() {
        generateState(epsilonClosure(0));
        return res;
    }

    // generate DFA state from NFA states
    private void generateState(Set<Integer> state) {
        Set<S> value = new HashSet<>();
        state.forEach(i -> value.add(src.getState(i)));
        int index = res.addState(value);
        indices.put(state, index);
        if (!Collections.disjoint(state, end)) res.acceptOn(index);

        Map<I, Set<Integer>> nextStates = new HashMap<>();
        for (int s : state)
            for (I input : src.transitions.get(s).keySet()) {
                nextStates.putIfAbsent(input, new HashSet<>());
                nextStates.get(input).addAll(src.transitions.get(s).get(input));
            }
        for (I input : nextStates.keySet()) {
            nextStates.put(input, epsilonClosure(nextStates.get(input)));
            if (indices.get(nextStates.get(input)) == null) generateState(nextStates.get(input));
            res.addTransition(index, input, indices.get(nextStates.get(input)));
        }
    }

    private Set<Integer> epsilonClosure(int state) {
        if (epsClosure.putIfAbsent(state, new HashSet<>()) != null) return epsClosure.get(state);
        epsClosure.put(state, src.epsilonClosure(state));
        return epsClosure.get(state);
    }
    private Set<Integer> epsilonClosure(Set<Integer> states) {
        Set<Integer> e = new HashSet<>();
        for (int i : states) e.addAll(epsilonClosure(i));
        return e;
    }
}
