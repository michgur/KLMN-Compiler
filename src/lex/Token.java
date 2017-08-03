package lex;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/3/2017.
 */
public class Token
{
    public enum Type {
        IDENTIFIER,
        NUMBER,
        STRING,
        OPEN_PAREN,
        CLOSE_PAREN,
        PLUS,
        TIMES,
        END,
        KEYWORD
    }

    private Type type;
    private String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() { return type; }
    public String getValue() { return value; }

    @Override
    public boolean equals(Object other) {
        return other instanceof Token && ((Token) other).type == type && ((Token) other).value.equals(value);
    }

    @Override
    public String toString() { return "Token: " + type + "(\"" + value + "\")"; }
}
