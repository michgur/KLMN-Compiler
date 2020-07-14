package lang;

import java.util.*;
import java.util.function.Consumer;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/15/2017.
 */
public class Tokenizer
{
    // data for tokenizing code
    Map<Character, Terminal> operators = new HashMap<>();
    Map<String, Terminal> keywords = new HashMap<>();
    Set<T> ignored = new HashSet<>();
    Map<T, Terminal> others = new HashMap<>();

    public interface T { String read(String src, int index); }

    public Tokenizer addTerminal(Terminal terminal, Character operator) {
        operators.put(operator, terminal);
        return this;
    }
    public Tokenizer addTerminal(Terminal terminal, String keyword) {
        keywords.put(keyword, terminal);
        return this;
    }
    public Tokenizer addTerminal(Terminal terminal, T tokenizer) {
        others.put(tokenizer, terminal);
        return this;
    }
    public Tokenizer ignore(T tokenizer) {
        ignored.add(tokenizer);
        return this;
    }

    public TokenStream tokenize(String src) { return new TokenStream(this, src); }

    public static class TokenStream implements Iterator<Token>
    {
        private Tokenizer lang;

        private String code;
        private int index, length;
        private boolean end = false;

        private Token next = null;

        public TokenStream(Tokenizer lang, String code) {
            this.lang = lang;
            this.code = code;
            length = code.length();
            index = 0;
        }

        public Token peek() {
            if (next == null) next = readNext();
            return next;
        }
        @Override public Token next() {
            Token n = (next != null) ? next : readNext();
            next = null;
            return n;
        }

        @Override public void forEachRemaining(Consumer<? super Token> action) { while (hasNext()) action.accept(next()); }
        @Override public boolean hasNext() { return !end; }
        private Token readNext() {
            if (index >= length) {
                end = true;
                return new Token(Terminal.END_OF_INPUT, "EOF");
            }
            char c = code.charAt(index);

            for (Tokenizer.T t : lang.ignored) {
                String ignore = t.read(code, index);
                if (ignore == null) continue;
                index += ignore.length();
                return next();
            }
            for (String kw : lang.keywords.keySet())
                if (code.startsWith(kw, index)) {
                    index += kw.length();
                    return new Token(lang.keywords.get(kw), kw);
                }
            for (Tokenizer.T t : lang.others.keySet()) {
                String value = t.read(code, index);
                if (value == null) continue;
                index += value.length();
                return new Token(lang.others.get(t), value);
            }

            index++;
            for (char op : lang.operators.keySet()) if (c == op) return new Token(lang.operators.get(c), c + "");
            throw new ReadingException(c, index - 1);
        }
    }
}
