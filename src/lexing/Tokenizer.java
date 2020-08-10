package lexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/15/2017.
 *
 * This Class Manages A Grammar & Its Tokens
 */
public class Tokenizer
{
    // data for tokenizing code
    Map<Character, Terminal> operators = new HashMap<>();
    Map<String, Terminal> keywords = new HashMap<>();
    Set<Reader> ignored = new HashSet<>();
    Map<Reader, Terminal> others = new HashMap<>();

    public interface Reader extends BiFunction<String, Integer, String> {}

    public Tokenizer addTerminal(Terminal terminal, Character operator) {
        operators.put(operator, terminal);
        return this;
    }
    public Tokenizer addTerminal(Terminal terminal, String keyword) {
        keywords.put(keyword, terminal);
        return this;
    }
    public Tokenizer addTerminal(Terminal terminal, Reader reader) {
        others.put(reader, terminal);
        return this;
    }
    public Tokenizer ignore(Reader reader) {
        ignored.add(reader);
        return this;
    }

    public TokenStream tokenize(String src) { return new TokenStream(this, src); }
}
