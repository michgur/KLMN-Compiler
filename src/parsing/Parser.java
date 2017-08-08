package parsing;

import lex.TokenStream;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/8/2017.
 */
public class Parser // SLR(1) Parser
{
    private Grammar grammar;

    public Parser(Grammar grammar) {
        this.grammar = grammar;
        // generate NFA
    }

    public ParseTree parse(TokenStream input) {
        return null;
    }
}
