package lang;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/4/2017.
 */
public class Terminal extends Symbol
{
    public static final Terminal END_OF_INPUT = new Terminal("$");

    public Terminal(String name) { super(name); }

    @Override
    public boolean isTerminal() { return true; }
}
