package lexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/15/2017.
 *
 * This Class Manages A Grammar & Its Tokens
 */
public class Language
{
    // data for tokenizing code
    Map<Character, Terminal> operators = new HashMap<>();
    Map<String, Terminal> keywords = new HashMap<>();
    Set<Tokenizer> ignored = new HashSet<>();
    Map<Tokenizer, Terminal> others = new HashMap<>();

    public interface Tokenizer { String read(String src, int index); }

    public Language addTerminal(Terminal terminal, Character operator) {
        operators.put(operator, terminal);
        return this;
    }
    public Language addTerminal(Terminal terminal, String keyword) {
        keywords.put(keyword, terminal);
        return this;
    }
    public Language addTerminal(Terminal terminal, Tokenizer tokenizer) {
        others.put(tokenizer, terminal);
        return this;
    }
    public Language ignore(Tokenizer tokenizer) {
        ignored.add(tokenizer);
        return this;
    }

    public TokenStream tokenize(String src) { return new TokenStream(this, src); }
}
