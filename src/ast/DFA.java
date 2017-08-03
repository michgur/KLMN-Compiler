package ast;

import lex.TokenStream;

import java.util.Set;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/3/2017.
 */
public class DFA
{
    private Set<DFA> states;
    private Test test;

    public DFA(Test test, Set<DFA> states) {
        this.test = test;
        this.states = states;
    }

    public DFA next(TokenStream t) {
        for (DFA dfa : states)
            if (dfa.test(t)) {
                t.next();
                return dfa;
            }
        throw new RuntimeException("Error: Invalid Syntax Blah Blah");
    }

    private boolean test(TokenStream t) { return test.test(t); }

    public interface Test { boolean test(TokenStream t); }
}
