package lex;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/3/2017.
 */
public class Token
{
    public enum Type { IDENTIFIER, NUMBER, STRING, PUNCTUATION, OPERATOR, KEYWORD }

    private Type type;
    private String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() { return type; }
    public String getValue() { return value; }

    @Override
    public String toString() { return "Token: " + type + "(\"" + value + "\")"; }
}
