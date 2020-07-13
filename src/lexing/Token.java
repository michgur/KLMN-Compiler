package lexing;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/3/2017.
 */
public class Token
{
    private Terminal type;
    private String value;

    public Token(Terminal type, String value) {
        this.type = type;
        this.value = value;
    }

    public Terminal getType() { return type; }
    public String getValue() { return value; }

    @Override
    public boolean equals(Object other) {
        return other instanceof Token && ((Token) other).type == type && ((Token) other).value.equals(value);
    }

    @Override
    public String toString() { return "Token: " + type + "(\"" + value + "\")"; }
}
