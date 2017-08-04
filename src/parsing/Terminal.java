package parsing;

import lex.Token;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/4/2017.
 */
public class Terminal extends Symbol
{
    public static final Terminal END_OF_INPUT = new Terminal("$", Token.Type.END);

    private Token.Type type;

    public Terminal(String name, Token.Type type) {
        super(name);
        this.type = type;
    }

    public Token.Type getType() { return type; }

    @Override
    public boolean isTerminal() { return true; }
}
