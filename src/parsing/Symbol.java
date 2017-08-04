package parsing;

import java.util.HashSet;
import java.util.Set;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/4/2017.
 */
public class Symbol
{
    public static final Symbol EPSILON = new Symbol("ε");

    private Set<Symbol[]> productions = new HashSet<>();
    private String name; // only needed for debugging purposes
    boolean used = false;

    public Symbol(String name) { this.name = name; }

    public void addProduction(Symbol... p) {
        if (used) throw new IllegalStateException("Cannot Modify Symbol " + name + " After Constructing Grammar!");
        productions.add(p);
    }
    Set<Symbol[]> getProductions() { return productions; }

    public boolean isTerminal() { return false; }

    @Override
    public String toString() { return name; }
}
