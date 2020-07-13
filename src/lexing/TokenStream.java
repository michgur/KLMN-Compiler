package lexing;

import java.util.*;
import java.util.function.Consumer;

/**
 * ಠ^ಠ.
 * Created by Michael on 8/2/2017.
 */
public class TokenStream implements Iterator<Token>
{
    private Language lang;

    private String code;
    private int index, length;
    private boolean end = false;

    private Token next = null;

    public TokenStream(Language lang, String code) {
        this.lang = lang;
        this.code = code;

        length = code.length();
        index = 0;
    }

    public Token peek() {
        if (next == null) next = readNext();
        return next;
    }

    @Override
    public Token next() {
        Token n = (next != null) ? next : readNext();
        next = null;
        return n;
    }

    @Override
    public void forEachRemaining(Consumer<? super Token> action) { while (hasNext()) action.accept(next()); }

    @Override
    public boolean hasNext() { return !end; }

    private Token readNext() {
        if (index >= length) {
            end = true;
            return new Token(Terminal.END_OF_INPUT, "EOF");
        }
        char c = code.charAt(index);

        for (Language.Tokenizer t : lang.ignored) {
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
        for (Language.Tokenizer t : lang.others.keySet()) {
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
