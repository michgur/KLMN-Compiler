package lex;

import parsing.Terminal;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/3/2017.
 */
public class Token
{
    public enum Type {
        IDENTIFIER("id"),
        NUMBER("num"),
        STRING("str"),
        OPEN_PAREN("("),
        CLOSE_PAREN(")"),
        OPEN_BRACKET("["),
        CLOSE_BRACKET("]"),
        OPEN_CURLY("{"),
        CLOSE_CURLY("}"),
        PLUS("+"),
        DASH("-"),
        ASTERISK("*"),
        SLASH("/"),
        EQUALS("="),
        COMMA(","),
        SEMICOLON(";"),
        END("$"),
        CLASS("class"); // remove

        // temporary
        public Terminal t;
        Type(String name) {
            t = new Terminal(name, this);
            if (name.equals("$")) t = Terminal.END_OF_INPUT;
        }
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
